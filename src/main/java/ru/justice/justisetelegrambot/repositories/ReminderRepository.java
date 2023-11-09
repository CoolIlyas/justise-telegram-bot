package ru.justice.justisetelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.Reminder.Reminder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    Optional<Reminder> findByName(String name);

    List<Reminder> findByChat(ChatEntity chat);
}