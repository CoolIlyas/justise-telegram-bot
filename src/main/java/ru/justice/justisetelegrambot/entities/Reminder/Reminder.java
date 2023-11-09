package ru.justice.justisetelegrambot.entities.Reminder;

import lombok.Getter;
import org.hibernate.Hibernate;
import ru.justice.justisetelegrambot.entities.ChatEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность напоминания
 */
@Getter
@Entity
public class Reminder {

    protected Reminder(){}
    @Id
    private UUID id;

    @Column(unique = true)
    @NotBlank
    private String name;

    @NotBlank
    private String cron;

    @Column(length = 2000)
    @NotBlank
    private String text;

    @NotNull
    @Column(length = 2000)
    private String messageEntities;

    @NotNull
    @ManyToOne
    @JoinColumn
    private ChatEntity chat;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Reminder reminder = (Reminder) o;
        return getId() != null && Objects.equals(getId(), reminder.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    protected void setId(UUID id) {
        this.id = id;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setCron(String cron) {
        this.cron = cron;
    }

    protected void setText(String text) {
        this.text = text;
    }

    protected void setChat(ChatEntity chat) {
        this.chat = chat;
    }

    protected void setMessageEntities(String messageEntities) {
        this.messageEntities = messageEntities;
    }
}
