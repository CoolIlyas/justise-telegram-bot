package ru.justice.justisetelegrambot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.justice.justisetelegrambot.entities.ChatEntity;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    Optional<ChatEntity> findByTitle(String title);
}
