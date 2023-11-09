package ru.justice.justisetelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CommandInProgressRepository  extends JpaRepository<CommandInProgress, UUID> {
    CommandInProgress findByChat(ChatEntity chat);

    List<CommandInProgress> findByCreatedTimeBefore(LocalDateTime dateTime);
}
