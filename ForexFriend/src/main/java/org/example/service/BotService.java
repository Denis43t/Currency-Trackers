package org.example.service;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class BotService implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient
            (System.getenv("BOT_TOKEN"));

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString()
                    , update.getMessage().getText());

            if (sendMessage.getText().equals("час")) {
                sendCustomKeyboard(sendMessage.getChatId());
            }
        }
    }

    public void sendCustomKeyboard(String chatId) {
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
}