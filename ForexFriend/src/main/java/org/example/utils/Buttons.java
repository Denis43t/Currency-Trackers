package org.example.utils;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Buttons {

    private final TelegramClient telegramClient;
    private Currency selectedCurrency = Currency.BOTH;

    public Buttons(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public Currency getCurrency() {
        return selectedCurrency;
    }

    public void setCurrencySelection(String callData) {
        switch (callData) {
            case "settings_currency_usd":
                selectedCurrency = Currency.USD;
                break;
            case "settings_currency_eur":
                selectedCurrency = Currency.EUR;
                break;
            case "settings_currency_both":
                selectedCurrency = Currency.BOTH;
                break;
        }
    }

    public void handleSettings(long chatId, long messageId) {
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("Кількість знаків після коми")
                .callbackData("settings_precision")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("Валюта")
                .callbackData("settings_currency")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Банк")
                .callbackData("settings_bank")
                .build();

        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("Час сповіщень")
                .callbackData("settings_notification_time")
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(button1);
        row1.add(button2);

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(button3);
        row2.add(button4);

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .build();

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

    public void handleCurrencySettings(long chatId, long messageId) {
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("USD")
                .callbackData("settings_currency_usd")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("EUR")
                .callbackData("settings_currency_eur")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Both")
                .callbackData("settings_currency_both")
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(button1);
        row1.add(button2);
        row1.add(button3);

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(row1)
                .build();

        EditMessageText newMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId((int) messageId)
                .text("Оберіть валюту:")
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            telegramClient.execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
