package ru.justice.justisetelegrambot.messageHandler.commands.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;
import ru.justice.justisetelegrambot.messageHandler.commands.Command;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import java.util.List;

/**
 * Сбрасывает выполнение команд.
 */
@Component
@RequiredArgsConstructor
public class Stop implements Command {

    public static final String STOP_MESSAGE = "/stop";

    private final CommandInProgressRepository repository;
    @Override
    public boolean isCalled(Update update) {
        return update.getMessage().hasText() && update.getMessage().getText().startsWith(STOP_MESSAGE);
    }

    @Override
    public List<Object> handle(Update update, ChatEntity chat) {
        CommandInProgress commandForThisChat = repository.findByChat(chat);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        if (commandForThisChat != null) {
            repository.delete(commandForThisChat);
            sendMessage.setText("Команда остановлена");
        } else {
            sendMessage.setText("Нет активных команд");
        }
        return List.of(sendMessage);
    }

    @Override
    public String description() {
        return "/stop Сбросить выполнение команды";
    }
}
