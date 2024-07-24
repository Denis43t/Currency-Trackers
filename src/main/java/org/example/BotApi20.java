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

public class BotApi20 implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final Settings settings;

    public BotApi20(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        settings = new Settings(telegramClient); // Initialize Settings with the same client
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if (message_text.equals("/start")) {
                InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                        .text("Отримати Інформацію")
                        .callbackData("update_msg_text")
                        .build();

                InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                        .text("Налаштування")
                        .callbackData("extra_info")
                        .build();

                InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
                keyboardRow.add(button1);
                keyboardRow.add(button2);

                InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                        .keyboardRow(keyboardRow)
                        .build();

                SendMessage message = SendMessage.builder()
                        .chatId(chat_id)
                        .text("Ласкаво просимо! \n" +"Цей бот допоможе відстежити актуальний курс валют.")
                        .replyMarkup(keyboardMarkup)
                        .build();

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("update_msg_text")) {
                String answer;
                try {
                    BankApi bankApi = new BankApi();
                    answer = bankApi.getExchangeRates();
                } catch (IOException e) {
                    answer = "Не вдалося отримати курс валют. Спробуйте пізніше.";
                    e.printStackTrace();
                }

                EditMessageText new_message = EditMessageText.builder()
                        .chatId(chat_id)
                        .messageId(toIntExact(message_id))
                        .text(answer)
                        .build();
                try {
                    telegramClient.execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (call_data.equals("extra_info")) {
                settings.handleSettings(chat_id, message_id); // Handle settings button click
            } else if (call_data.startsWith("settings_")) {
                String answer = "Вибрана опція: " + call_data;
                EditMessageText new_message = EditMessageText.builder()
                        .chatId(chat_id)
                        .messageId(toIntExact(message_id))
                        .text(answer)
                        .build();
                try {
                    telegramClient.execute(new_message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}