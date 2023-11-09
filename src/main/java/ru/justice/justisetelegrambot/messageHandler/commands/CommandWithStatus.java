package ru.justice.justisetelegrambot.messageHandler.commands;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Команда у которой может быть статус выполнения.
 */
public interface CommandWithStatus  extends Command {
    /**
     * Обработать команду у которой есть статус.
     * @param update сообщение
     * @param commandInProgress текущий прогресс команды
     * @return ответ пользователю
     */
    List<Object> handleCommandWithStatus(Update update, CommandInProgress commandInProgress);

    /**
     * Создать и сохранить команду.
     * @param chat чат
     * @param commandInProgressRepository
     */
    default void createAndSaveCommandInProgress(ChatEntity chat,
                                                String status,
                                                CommandInProgressRepository commandInProgressRepository) {
        CommandInProgress commandInProgress = new CommandInProgress();
        commandInProgress.setChat(chat);
        commandInProgress.setStatus(status);
        commandInProgress.setCommandClass(this.getClass());
        commandInProgress.setCreatedTime(LocalDateTime.now());
        commandInProgressRepository.save(commandInProgress);
    }
}
