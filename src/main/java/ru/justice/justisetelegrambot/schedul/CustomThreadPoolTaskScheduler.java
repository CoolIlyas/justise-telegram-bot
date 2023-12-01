package ru.justice.justisetelegrambot.schedul;


import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import ru.justice.justisetelegrambot.JusticeBot;
import ru.justice.justisetelegrambot.entities.Reminder.Reminder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * Шедулер для работы с напоминаниями.
 */
@Component
public class CustomThreadPoolTaskScheduler extends ThreadPoolTaskScheduler {

    private final JusticeBot bot;

    /**
     * Конструктор.
     * @param bot Бот. Нужен для отправки сообщений. Lazy из-за циклической зависимости.
     */
    public CustomThreadPoolTaskScheduler(@Lazy JusticeBot bot) {
        this.bot = bot;
        this.setPoolSize(5);
        this.setThreadNamePrefix("ThreadPoolTaskScheduler");
        this.scheduledFutureMap = new HashMap<>();
    }

    private final Map<String, ScheduledFuture> scheduledFutureMap;

    /**
     * Отмена напоминания.
     * @param reminder напоминание, которое отменяем
     */
    public void cancel(Reminder reminder) {
        ScheduledFuture scheduledFuture = scheduledFutureMap.remove(reminder.getId().toString());
        if (Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * Шедулит напоминания. Сначала отменяет если уже запланировано.
     * @param reminder
     */
    public void plan(Reminder reminder) {
        cancel(reminder);
        ScheduledFuture scheduledFuture = schedule(() -> bot.sendReminder(reminder.getText(), reminder.getMessageEntities(), reminder.getChat().getId()),
                new CronTrigger(reminder.getCron()));
        scheduledFutureMap.put(reminder.getId().toString(), scheduledFuture);
    }
}
