package ru.justice.justisetelegrambot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.justice.justisetelegrambot.messageHandler.MessageHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Бот
 */
@Component
public class JusticeBot extends TelegramLongPollingBot {

    /**
     * Обработчик сообщений
     */
    private final MessageHandler messageHandler;

    @Value("${application.bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public JusticeBot(@Value("${application.bot.token}") String botToken, MessageHandler messageHandler) {
        super(botToken);
        this.messageHandler = messageHandler;
    }

    /**
     * Все сообщения бота приходят сюда.
     * @param update Update received
     */
    @Override
    public void onUpdateReceived(Update update) {
        List<Object> answers = messageHandler.handle(update);
        answers.forEach(answer -> {
            try {
                if (answer instanceof SendMessage) {
                    execute((SendMessage) answer);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    /**
     * Метод используется сервисами для отправки напоминаний.
     * @param text текст напоминания
     * @param chatId чат в который отправится сообщение
     * @param format форматирование текста
     */
    public void sendReminder(String text, String format, Long chatId) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);

            //Устанавливаем форматирование у сообщения, если оно было
            if (format != null && !format.equals("null")) {
                ObjectMapper mapper = new ObjectMapper();
                MessageEntity[] entities = mapper.readValue(format, MessageEntity[].class);
                sendMessage.setEntities(Arrays.asList(entities));
            }

            execute(sendMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
