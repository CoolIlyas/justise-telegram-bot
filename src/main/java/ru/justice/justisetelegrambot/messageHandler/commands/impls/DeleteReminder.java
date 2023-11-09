package ru.justice.justisetelegrambot.messageHandler.commands.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;
import ru.justice.justisetelegrambot.entities.Reminder.Reminder;
import ru.justice.justisetelegrambot.entities.Reminder.ReminderService;
import ru.justice.justisetelegrambot.messageHandler.commands.CommandWithStatus;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeleteReminder implements CommandWithStatus {
    private final CommandInProgressRepository commandInProgressRepository;
    private final ReminderService reminderService;

    @Override
    public boolean isCalled(Update update) {
        return update.getMessage().hasText() && update.getMessage().getText().startsWith("/deleteReminder");
    }

    @Override
    public List<Object> handle(Update update, ChatEntity chat) {
        createAndSaveCommandInProgress(chat, null, commandInProgressRepository);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        sendMessage.setText("Введите название напоминания, которое необходимо удалить");
        return List.of(sendMessage);
    }

    @Override
    public String description() {
        return "/deleteReminder Удалить напоминание";
    }

    @Override
    public List<Object> handleCommandWithStatus(Update update, CommandInProgress commandInProgress) {
        Optional<Reminder> reminder = reminderService.getReminderByName(update.getMessage().getText());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(commandInProgress.getChat().getId());
        if(reminder.isPresent()) {
            reminderService.deleteReminder(reminder.get());
            sendMessage.setText("Напоминание удалено");
        } else {
            sendMessage.setText("Напоминание не найдено");
        }
        commandInProgressRepository.delete(commandInProgress);
        return List.of(sendMessage);
    }
}
