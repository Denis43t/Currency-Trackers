package org.example.service;

import org.example.properties.Constants;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BotService implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient = new OkHttpTelegramClient
            (System.getenv("BOT_TOKEN"));
    private HashMap<String, String> userSettings = new HashMap<>();

    private ConcurrentHashMap<String, Thread> runningThreads = new ConcurrentHashMap<>();


    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString()
                    , update.getMessage().getText());
            userSettings.put(sendMessage.getChatId(), getTimeOfSendingNotifications(sendMessage));
            //надає клавіатуру як що був веден час
            if (sendMessage.getText().equalsIgnoreCase("час")) {
                sendCustomKeyboardTime(sendMessage.getChatId());
            }
            scheduleSendingCurrencyRate(userSettings, sendMessage.getChatId());
        }
    }

    //перевіряє чи був введен час для свопіщення
    public String getTimeOfSendingNotifications(SendMessage message) {
        if (Constants.variantsOfTime.stream().anyMatch(t -> t.equals(message.getText()))) {
            return message.getText();
        }
        return "-1";
    }

    //створює клавіатуру для вибору часу
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

    public void scheduleSendingCurrencyRate(HashMap userSettings, String chatId) {
        if (userSettings.get(chatId).equals("-1") || userSettings.get(chatId) == null) {
            return;
        }

        if (runningThreads.containsKey(chatId) && runningThreads.get(chatId).isAlive()) {
            return;
        }

        SendMessage sendMessage = new SendMessage(chatId, "aaa");

        Thread senderScheduleCurrencyRate = new Thread(() -> {
            while (true) {
                Date date = new Date();
                String hours = String.valueOf(date.getHours());
                if (hours.equals(userSettings.get(chatId))) {
                    if (date.getMinutes() == 10) {
                        try {
                            telegramClient.execute(sendMessage);
                            Thread.sleep(1000*60);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        senderScheduleCurrencyRate.setDaemon(true);
        senderScheduleCurrencyRate.start();

        runningThreads.put(chatId, senderScheduleCurrencyRate);
    }

}