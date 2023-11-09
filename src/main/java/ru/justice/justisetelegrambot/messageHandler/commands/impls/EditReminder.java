package ru.justice.justisetelegrambot.messageHandler.commands.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.justice.justisetelegrambot.entities.ChatEntity;
import ru.justice.justisetelegrambot.entities.CommandInProgress;
import ru.justice.justisetelegrambot.entities.Reminder.Reminder;
import ru.justice.justisetelegrambot.entities.Reminder.ReminderService;
import ru.justice.justisetelegrambot.messageHandler.commands.CommandWithStatus;
import ru.justice.justisetelegrambot.repositories.ChatRepository;
import ru.justice.justisetelegrambot.repositories.CommandInProgressRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Команда для редактирования напоминаний.
 */
@Component
@RequiredArgsConstructor
public class EditReminder implements CommandWithStatus {
    private final CommandInProgressRepository commandInProgressRepository;
    private final ReminderService reminderService;
    @Override
    public boolean isCalled(Update update) {
        return update.getMessage().hasText() && update.getMessage().getText().startsWith("/editReminder");
    }

    @Override
    public List<Object> handle(Update update, ChatEntity chat) {
        createAndSaveCommandInProgress(chat, Status.ReminderTitle.toString(), commandInProgressRepository);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId());
        sendMessage.setText("Введите название напоминания, которое нужно изменить");
        return List.of(sendMessage);
    }

    @Override
    public String description() {
        return "/editReminder Редактировать напоминание";
    }

    @Override
    public List<Object> handleCommandWithStatus(Update update, CommandInProgress commandInProgress) {
        Status status = Status.valueOf(Status.class, commandInProgress.getStatus());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(commandInProgress.getChat().getId());
        switch (status) {
            case ReminderTitle:
                Optional<Reminder> reminderOptional = reminderService.getReminderByName(update.getMessage().getText());
                if(reminderOptional.isPresent()) {
                    sendMessage.setText("Выберите атрибут, который необходимо изменить");
                    commandInProgress.setStatus(Status.ChooseWhatToEdit.toString());
                    commandInProgress.getAttributes().put(status.toString(), reminderOptional.get().getId().toString());

                    //Делаем 3 кнопки
                    InlineKeyboardButton nameButton = new InlineKeyboardButton("Название");
                    nameButton.setCallbackData(ParameterToEdit.Title.toString());
                    InlineKeyboardButton cronButton = new InlineKeyboardButton("Расписание");
                    cronButton.setCallbackData(ParameterToEdit.Cron.toString());
                    InlineKeyboardButton textButton = new InlineKeyboardButton("Текст");
                    textButton.setCallbackData(ParameterToEdit.Text.toString());
                    List<InlineKeyboardButton> keyboardButtonsRow = List.of(nameButton, cronButton, textButton);
                    List<List<InlineKeyboardButton>> rowList= List.of(keyboardButtonsRow);
                    InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.setKeyboard(rowList);
                    sendMessage.setReplyMarkup(inlineKeyboardMarkup);

                    commandInProgressRepository.save(commandInProgress);
                } else {
                    sendMessage.setText("Напоминание не найдено. Попробуйте снова");
                }
                break;
            case ChooseWhatToEdit:
                //Реагирует только на нажатие кнопки
                if (update.getCallbackQuery() == null || update.getCallbackQuery().getData() == null) {
                    return List.of();
                }
                ParameterToEdit parameterToEdit = ParameterToEdit.valueOf(update.getCallbackQuery().getData());
                commandInProgress.getAttributes().put(status.toString(), parameterToEdit.toString());
                switch (parameterToEdit) {
                    case Title:
                        sendMessage.setText("Введите новое название");
                        break;
                    case Text:
                        sendMessage.setText("Введите новый текст");
                        break;
                    case Cron:
                        sendMessage.setText("Введите новое расписание в формате крон");
                        break;
                }
                commandInProgress.setStatus(Status.Editing.toString());
                commandInProgressRepository.save(commandInProgress);
                break;
            case Editing:
                Reminder reminder = reminderService
                        .getReminderById(commandInProgress.getAttributes().get(Status.ReminderTitle.toString())).get();
                ParameterToEdit param = ParameterToEdit
                        .valueOf(commandInProgress.getAttributes().get(Status.ChooseWhatToEdit.toString()));
                String newValue = update.getMessage().getText();
                switch (param) {
                    case Title:
                        reminderService.updateName(reminder, newValue);
                        break;
                    case Cron:
                        reminderService.updateCron(reminder, newValue);
                        break;
                    case Text:
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            String format = mapper.writeValueAsString(update.getMessage().getEntities());
                            reminderService.updateText(reminder, newValue, format);
                        } catch (JsonProcessingException e) {
                            //do nothing
                        }
                }
                sendMessage.setText("Напоминание обновлено");
                commandInProgressRepository.delete(commandInProgress);
                break;
        }

        return List.of(sendMessage);
    }

    private enum Status {
        ReminderTitle,
        ChooseWhatToEdit,
        Editing
    }

    private enum ParameterToEdit {
        Title,
        Cron,
        Text,
        Format
    }
}
