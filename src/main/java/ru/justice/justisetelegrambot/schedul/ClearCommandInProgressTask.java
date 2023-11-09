package ru.justice.justisetelegrambot.schedul;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClearCommandInProgressTask {
    private final CommandInProgressRepository repository;


    /**
     * Джоб удаляет все команды старше 10 минут.
     */
    @Scheduled(cron = "${application.clear-command-cron}")
    public void clearCommandInProgress() {
        repository.deleteAll(repository.findByCreatedTimeBefore(LocalDateTime.now().minusMinutes(10)));
    }
}
