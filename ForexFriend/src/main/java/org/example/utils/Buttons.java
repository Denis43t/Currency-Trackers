package org.example.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class Buttons {

    private final TelegramClient telegramClient;
    private Currency selectedCurrency = Currency.BOTH;
    public static Banks selectedBanks = Banks.MONO;

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

    public void setBankSelection(String callData) {
        switch (callData) {
            case "settings_bank_nbu":
                selectedBanks = Banks.NBU;
                break;
            case "settings_bank_mono":
                selectedBanks = Banks.MONO;
                break;
            case "settings_bank_privat":
                selectedBanks = Banks.PRIVAT;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + callData);
        }
    }

    public void sendCustomKeyboardTime(String chatId) {
        SendMessage message = new SendMessage(chatId, "оберіть час оповіщення");

        List<KeyboardRow> keyboard = new ArrayList<>();
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);

        KeyboardRow row = new KeyboardRow();
        row.add("9");
        row.add("10");
        row.add("11");
        keyboard.add(row);

        row = new KeyboardRow();
        row.add("12");
        row.add("13");
        row.add("14");
        keyboard.add(row);

        row = new KeyboardRow();
        row.add("15");
        row.add("16");
        row.add("17");
        keyboard.add(row);

        row = new KeyboardRow();
        row.add("18");
        row.add("ввимкнути повідомлення");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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

    public void handleCurrencySettings(long chatId, long messageId, String callData) {
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

        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("Повернутися в головне меню")
                .callbackData("return_to_main_menu")
                .build();

        switch (callData) {
            case "settings_currency_usd":
                button1 = InlineKeyboardButton.builder()
                        .text("USD✅")
                        .callbackData("settings_currency_usd")
                        .build();
                break;
            case "settings_currency_eur":
                button2 = InlineKeyboardButton.builder()
                        .text("EUR✅")
                        .callbackData("settings_currency_eur")
                        .build();
                break;
            case "settings_currency_both":
                button3 = InlineKeyboardButton.builder()
                        .text("Both✅")
                        .callbackData("settings_currency_both")
                        .build();
                break;
        }

        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(button1);
        row1.add(button2);
        row1.add(button3);

        InlineKeyboardRow row2 = new InlineKeyboardRow();
        row2.add(button4);

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
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


    public void handleBanksSettings(long chatId, long messageId, String callData) {
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("НБУ")
                .callbackData("settings_bank_nbu")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("ПриватБанк")
                .callbackData("settings_bank_privat")
                .build();

        InlineKeyboardButton button3 = InlineKeyboardButton.builder()
                .text("Монобанк")
                .callbackData("settings_bank_mono")
                .build();
        InlineKeyboardButton button4 = InlineKeyboardButton.builder()
                .text("Повернутися в головне меню")
                .callbackData("return_to_main_menu")
                .build();


        switch (callData) {
            case "settings_bank_nbu":
                button1 = InlineKeyboardButton.builder()
                        .text("НБУ✅")
                        .callbackData("settings_bank_nbu")
                        .build();
                break;
            case "settings_bank_mono":
                button3 = InlineKeyboardButton.builder()
                        .text("Монобанк✅")
                        .callbackData("settings_bank_mono")
                        .build();
                break;
            case "settings_bank_privat":
                button2 = InlineKeyboardButton.builder()
                        .text("ПриватБанк✅")
                        .callbackData("settings_bank_privat")
                        .build();
                break;
        }


        InlineKeyboardRow row1 = new InlineKeyboardRow();
        row1.add(button1);
        row1.add(button2);
        row1.add(button3);
        InlineKeyboardRow row2=new InlineKeyboardRow();
        row2.add(button4);

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(row1)
                .keyboardRow(row2)
                .build();

        EditMessageText newMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId((int) messageId)
                .text("Оберіть банк:")
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            telegramClient.execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
