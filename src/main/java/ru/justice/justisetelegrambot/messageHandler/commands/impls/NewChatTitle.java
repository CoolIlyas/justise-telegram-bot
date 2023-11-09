package ru.justice.justisetelegrambot.messageHandler.commands.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.messageHandler.commands.Command;
import ru.justice.justisetelegrambot.repositories.ChatRepository;

import java.util.List;
import java.util.Objects;

/**
 * Команда реагирует на изменения названия чатов и обновляет запись в бд.
 */
@Component
@RequiredArgsConstructor
public class NewChatTitle implements Command {
    private final ChatRepository repository;

    @Override
    public boolean isCalled(Update update) {
        return Objects.nonNull(update.getMessage().getNewChatTitle());
    }

    @Override
    public List<Object> handle(Update update, ChatEntity chat) {
        chat.setTitle(update.getMessage().getNewChatTitle());
        repository.save(chat);
        return null;
    }

    @Override
    public String description() {
        return null;
    }
}
