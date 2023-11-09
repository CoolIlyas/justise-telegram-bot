package ru.justice.justisetelegrambot.messageHandler.commands.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.Reminder.Reminder;
import ru.justice.justisetelegrambot.messageHandler.commands.Command;
import ru.justice.justisetelegrambot.repositories.ReminderRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Показать все напоминания для данного чата.
 */
@Component
@RequiredArgsConstructor
public class ShowReminders implements Command {

    private final ReminderRepository repository;

    @Override
    public boolean isCalled(Update update) {
        return update.getMessage().hasText() && update.getMessage().getText().startsWith("/showReminders");
    }

    @Override
    public List<Object> handle(Update update, ChatEntity chat) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        List<Reminder> reminders = repository.findByChat(chat);
        if (!reminders.isEmpty()) {
            String remindersString = repository.findByChat(chat).stream()
                    .map(Reminder::getName)
                    .collect(Collectors.joining("\n"));
            sendMessage.setText("Список напоминаний:\n" + remindersString);
        } else {
            sendMessage.setText("Нет напоминаний для данного чата");
        }
        return List.of(sendMessage);
    }

    @Override
    public String description() {
        return "/showReminders Показать все напоминания для данного чата";
    }
}
