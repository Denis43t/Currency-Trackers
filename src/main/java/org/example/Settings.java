package org.example;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Settings {

    private final TelegramClient telegramClient;

    public Settings(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void handleSettings(long chatId, long messageId) {
        // Create the buttons
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("Кількість знаків після коми")
                .callbackData("settings_1")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("Валюта")
                .callbackData("settings_2")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Банк")
                .callbackData("settings_3")
                .build();

        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("Час сповіщень")
                .callbackData("settings_4")
                .build();

        // Create keyboard rows
        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(button1);
        row1.add(button2);

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(button3);
        row2.add(button4);

        // Create keyboard markup
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .build();

        // Create a message with the keyboard
        EditMessageText newMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId((int) messageId)
                .text("Оберіть один з параметрів налаштування:")
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            telegramClient.execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
