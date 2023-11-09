package ru.justice.justisetelegrambot.messageHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;
import ru.justice.justisetelegrambot.messageHandler.commands.Command;
import ru.justice.justisetelegrambot.messageHandler.commands.CommandWithStatus;
import ru.justice.justisetelegrambot.repositories.ChatRepository;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.justice.justisetelegrambot.messageHandler.commands.impls.Stop.STOP_MESSAGE;

/**
 * Обработчик сообщений
 */
@Component
public class MessageHandler {
    private final Map<String, Command> commands;

    private final ChatRepository chatRepository;

    private final CommandInProgressRepository commandInProgressRepository;

    private final String helpText;

    public  MessageHandler(Map<String, Command> commands,
                           ChatRepository chatRepository,
                           CommandInProgressRepository commandInProgressRepository) {
        this.commands = commands;
        this.chatRepository = chatRepository;
        helpText = commands.values().stream()
                .map(Command::description)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        this.commandInProgressRepository = commandInProgressRepository;
    }

    @PostConstruct
    private void initTask(){
        commandInProgressRepository.deleteAll();
    }

    /**
     * Обработка сообщения.
     * @param update сообщение от юзера
     * @return лист с ответами, которые отправит бот.
     */
    @Transactional
    public List<Object> handle(Update update) {
        //Получаем чат
        ChatEntity chat = getChat(update);

        if (chat == null) {
            return List.of();
        }

        //Если есть активная команда для этого чата, то отправляем сразу туда
        CommandInProgress commandInProgresses = commandInProgressRepository.findByChat(chat);
        if (commandInProgresses != null && !isStop(update)) {
             Command command = commands.values().stream()
                     .filter(c -> c.getClass().equals(commandInProgresses.getCommandClass()))
                     .findFirst()
                     .orElse(null);
             if (command instanceof CommandWithStatus) {
                 return ((CommandWithStatus) command).handleCommandWithStatus(update, commandInProgresses);
             }
        }

        //Проверяем что не /help
        if (isHelp(update)) {
            return buildHelpAnswer(update);
        }
        return commands.values().stream()
                .filter(command -> command.isCalled(update))
                .map(command -> command.handle(update, chat))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


    /**
     * Проверяет не является ли введенное сообщение командой /help.
     * @param update update
     * @return true - является, false - не является
     */
    private boolean isHelp(Update update) {
        return update.getMessage().hasText() && update.getMessage().getText().startsWith("/help");
    }

    /**
     * Проверяет не является ли введенное сообщение командой /stop.
     * @param update update
     * @return true - является, false - не является
     */
    private boolean isStop(Update update) {
        return update.getMessage() != null
                && update.getMessage().hasText()
                && update.getMessage().getText().startsWith(STOP_MESSAGE);
    }

    /**
     * Возвращает ответ на команду /help.
     * @param update update
     * @return ответ на команду /help
     */
    private List<Object> buildHelpAnswer(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(helpText);
        return List.of(sendMessage);
    }

    /**
     * Получает чат из бд или создает новый
     * @param update сообщение пользователя
     * @return
     */
    private ChatEntity getChat(Update update) {
        Long chatId;
        if (update.getMessage() != null) {
            chatId = update.getMessage().getChatId();
        } else if (update.getCallbackQuery() != null) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            return null;
        }

        Optional<ChatEntity> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            ChatEntity newChatEntity = new ChatEntity();
            newChatEntity.setId(chatId);
            if (update.getMessage().getChat().isGroupChat()) {
                newChatEntity.setGroupChat(true);
                newChatEntity.setTitle(update.getMessage().getChat().getTitle());
            } else {
                newChatEntity.setTitle(update.getMessage().getChat().getUserName());
            }
            return chatRepository.save(newChatEntity);
        } else {
            return chatOpt.get();
        }
    }
}
