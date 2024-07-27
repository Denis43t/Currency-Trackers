package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;

import static java.lang.Math.toIntExact;

public class BotService implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final Settings settings;

    public BotService(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        settings = new Settings(telegramClient);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                sendStartMessage(chatId);
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callData) {
                case "update_msg_text":
                    sendExchangeRates(chatId, messageId);
                    break;
                case "settings":
                    settings.handleSettings(chatId, messageId);
                    break;
                case "settings_currency":
                    settings.handleCurrencySettings(chatId, messageId);
                    break;
                default:
                    if (callData.startsWith("settings_currency_")) {
                        settings.setCurrencySelection(callData);
                        sendExchangeRates(chatId, messageId);
                    }
                    break;
            }
        }
    }

    private void sendStartMessage(long chatId) {
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text("Отримати Інформацію")
                .callbackData("update_msg_text")
                .build();

        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text("Налаштування")
                .callbackData("settings")
                .build();

        InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
        keyboardRow.add(button1);
        keyboardRow.add(button2);

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(keyboardRow)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Ласкаво просимо! \nЦей бот допоможе відстежити актуальний курс валют.")
                .replyMarkup(keyboardMarkup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendExchangeRates(long chatId, long messageId) {
        String answer;
        try {
            BankService bankApi = new BankService();
            answer = bankApi.getExchangeRates(settings.getCurrency());
        } catch (IOException e) {
            answer = "Не вдалося отримати курс валют. Спробуйте пізніше.";
            e.printStackTrace();
        }

        EditMessageText newMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(toIntExact(messageId))
                .text(answer)
                .build();
        try {
            telegramClient.execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
