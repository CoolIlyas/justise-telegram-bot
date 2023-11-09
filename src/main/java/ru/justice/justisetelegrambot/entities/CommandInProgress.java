package ru.justice.justisetelegrambot.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Команды которые сейчас находятся в процессе.
 */
@Getter
@Setter
@Entity
public class CommandInProgress {

    @Id
    @GeneratedValue
    UUID uuid;

    @NotNull
    @ManyToOne
    @JoinColumn
    ChatEntity chat;

    /**
     * Число, которому соответствует энам статуса данной команды.
     */
    String status;

    /**
     * Класс команды.
     */
    @NotNull
    Class commandClass;

    /**
     * Время и дата создания.
     */
    @NotNull
    LocalDateTime createdTime;

    @ElementCollection
    @CollectionTable(name = "command_attribute_value",
            joinColumns = {@JoinColumn(name = "command_id")})
    @MapKeyColumn(name = "attribute")
    @Column(name = "value", length = 2000)
    private Map<String, String> attributes = new HashMap<>();
}
