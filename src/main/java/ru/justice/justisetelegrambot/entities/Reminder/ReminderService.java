package ru.justice.justisetelegrambot.entities.Reminder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.repositories.ChatRepository;
import ru.justice.justisetelegrambot.repositories.ReminderRepository;
import ru.justice.justisetelegrambot.schedul.CustomThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с напоминаниями.
 */
@Component
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository repository;
    private final CustomThreadPoolTaskScheduler taskScheduler;

    /**
     * Метод шедулит все напоминания из бд при запуске бота.
     */
    @PostConstruct
    private void initTasks() {
        List<Reminder> reminders = repository.findAll();
        reminders.forEach(taskScheduler::plan);
    }

    /**
     * Создание нового напоминания.
     * @param name название напоминания
     * @param cron крон
     * @param text текст напоминания
     * @param chat чат entity
     * @param messageEntities форматирование сообщения
     * @return объект класса Reminder
     */
    public Reminder createNewReminder(String name, String cron, String text, ChatEntity chat, String messageEntities) {
        Reminder reminder = new Reminder();
        reminder.setId(UUID.randomUUID());
        reminder.setCron(cron);
        reminder.setName(name);
        reminder.setText(text);
        reminder.setChat(chat);
        reminder.setMessageEntities(messageEntities);
        repository.save(reminder);
        taskScheduler.plan(reminder);
        return reminder;
    }

    /**
     * Удаление напоминания.
     * @param reminder напоминание, которое удаляем
     */
    public void deleteReminder(Reminder reminder) {
        taskScheduler.cancel(reminder);
        repository.delete(reminder);
    }

    /**
     * Обновление текста у напоминания.
     * @param reminder напоминание у которого обновляем текст
     * @param newText новый текст
     * @param messageEntities форматирование сообщения
     */
    public void updateText(Reminder reminder, String newText, String messageEntities) {
        reminder.setText(newText);
        reminder.setMessageEntities(messageEntities);
        repository.save(reminder);
        taskScheduler.plan(reminder);
    }

    /**
     * Обновление расписания у напоминания
     * @param reminder обновляемое напоминание
     * @param newCron новый крон
     */
    public void updateCron(Reminder reminder, String newCron) {
        reminder.setCron(newCron);
        repository.save(reminder);
        taskScheduler.plan(reminder);
    }

    /**
     * Обновление названия у напоминания.
     * @param reminder обновляемое напоминание
     * @param newName название напоминания
     */
    public void updateName(Reminder reminder, String newName) {
        reminder.setName(newName);
        repository.save(reminder);
        taskScheduler.plan(reminder);
    }

    /**
     * Получить напоминание по названию.
     * @param name название напоминания
     * @return optional, который может содержать напоминание
     */
    public Optional<Reminder> getReminderByName(String name) {
        return repository.findByName(name);
    }

    /**
     * Получить напоминание по Id.
     * @param id название напоминания
     * @return optional, который может содержать напоминание
     */
    public Optional<Reminder> getReminderById(String id) {
        return getReminderById(UUID.fromString(id));
    }

    /**
     * Получить напоминание по Id.
     * @param id название напоминания
     * @return optional, который может содержать напоминание
     */
    public Optional<Reminder> getReminderById(UUID id) {
        return repository.findById(id);
    }
}
