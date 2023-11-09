package ru.justice.justisetelegrambot.messageHandler.commands.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;
import ru.justice.justisetelegrambot.entities.Reminder.Reminder;
import ru.justice.justisetelegrambot.entities.Reminder.ReminderService;
import ru.justice.justisetelegrambot.messageHandler.commands.CommandWithStatus;
import ru.justice.justisetelegrambot.repositories.ChatRepository;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import java.util.List;
import java.util.Optional;

/**
 * Команда для создания напоминаний.
 */
@Component
@RequiredArgsConstructor
public class CreateReminder implements CommandWithStatus {
    private final CommandInProgressRepository commandInProgressRepository;
    private final ReminderService reminderService;
    private final ChatRepository chatRepository;
    @Override
    public boolean isCalled(Update update) {
        return update.getMessage().hasText() && update.getMessage().getText().startsWith("/createReminder");
    }

    @Override
    public List<Object> handle(Update update, ChatEntity chat) {
        createAndSaveCommandInProgress(chat, Status.Title.toString(), commandInProgressRepository);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        sendMessage.setText("Введите краткое название напоминания");
        return List.of(sendMessage);
    }

    @Override
    public String description() {
        return "/createReminder Создать новое напоминание";
    }

    @Override
    public List<Object> handleCommandWithStatus(Update update, CommandInProgress commandInProgress) {
        Status status = Status.valueOf(Status.class, commandInProgress.getStatus());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(commandInProgress.getChat().getId());
        switch (status) {
            case Title:
                String title = update.getMessage().getText();
                Optional<Reminder> optionalReminder = reminderService.getReminderByName(title);
                if (optionalReminder.isEmpty()) {
                    commandInProgress.setStatus(Status.Chat.toString());
                    commandInProgress.getAttributes().put(status.toString(), update.getMessage().getText());
                    sendMessage.setText("Введите название чата или имя пользователя для напоминания");
                    commandInProgressRepository.save(commandInProgress);
                } else {
                    sendMessage.setText("Напоминание с таким названием уже существует. Попробуйте снова");
                }
                break;
            case Chat:
                Optional<ChatEntity> chatOpt = chatRepository.findByTitle(update.getMessage().getText());
                if (chatOpt.isPresent()) {
                    sendMessage.setText("Введите текст напоминания");
                    commandInProgress.setStatus(Status.Text.toString());
                    commandInProgress.getAttributes().put(status.toString(), chatOpt.get().getId().toString());
                    commandInProgressRepository.save(commandInProgress);
                } else {
                    sendMessage.setText("Чат не найден. Попробуйте снова");
                }
                break;
            case Text:
                sendMessage.setText("Введите расписание в формате крон (пример * * * * * *)");
                commandInProgress.setStatus(Status.Cron.toString());
                commandInProgress.getAttributes().put(status.toString(), update.getMessage().getText());

                // Сохраняем форматирование текста в формате json
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String format = mapper.writeValueAsString(update.getMessage().getEntities());
                    commandInProgress.getAttributes().put(Status.Format.toString(), format);
                } catch (JsonProcessingException e) {
                    //do nothing
                }

                commandInProgressRepository.save(commandInProgress);
                break;
            case Cron:
                String cron = update.getMessage().getText();
                if (CronExpression.isValidExpression(cron)) {
                    sendMessage.setText("Напоминание успешно сохранено");
                    reminderService.createNewReminder(
                            commandInProgress.getAttributes().get(Status.Title.toString()),
                            update.getMessage().getText(),
                            commandInProgress.getAttributes().get(Status.Text.toString()),
                            chatRepository.findById(Long.valueOf(commandInProgress.getAttributes().get(Status.Chat.toString()))).get(),
                            commandInProgress.getAttributes().get(Status.Format.toString()));
                    commandInProgressRepository.delete(commandInProgress);
                } else {
                    sendMessage.setText("Крон невалидный. Попробуйте снова");
                }
        }

        return List.of(sendMessage);
    }

    private enum Status {
        Title,
        Chat,
        Text,
        Format,
        Cron
    }
}
