package org.example.service;

import org.example.properties.Constants;
import org.example.utils.Buttons;
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
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.toIntExact;

public class BotService implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient = new OkHttpTelegramClient
            (System.getenv("BOT_TOKEN"));

    Buttons buttons = new Buttons(telegramClient);

    private HashMap<String, String> userSettings = new HashMap<>();

    private ConcurrentHashMap<String, Thread> runningThreads = new ConcurrentHashMap<>();

    private BankService bankApi = new BankService();

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString()
                    , update.getMessage().getText());

            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();


            userSettings.put(sendMessage.getChatId(), getTimeOfSendingNotifications(sendMessage));

            try {
                scheduleSendingCurrencyRate(userSettings, sendMessage.getChatId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


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
                    buttons.handleSettings(chatId, messageId);
                    break;
                case "settings_currency":
                    buttons.handleCurrencySettings(chatId, messageId);
                    break;
                case "settings_bank":
                    buttons.handleBanksSettings(chatId, messageId, callData);
                    break;
                case "return_to_main_menu":
                    sendStartMessage(chatId);
                    break;
                default:
                    if (callData.startsWith("settings_currency_")) {
                        buttons.setCurrencySelection(callData);
                        sendExchangeRates(chatId, messageId);
                    }
                    if (callData.startsWith("settings_bank_")) {
                        buttons.handleBanksSettings(chatId, messageId, callData);
                        if (!callData.equals("settings_bank_return_to_main_menu")) {
                            buttons.setBankSelection(callData);
                        }
                    }
                    break;
            }

        }
    }

    //перевіряє чи був введен час для свопіщення
    public String getTimeOfSendingNotifications(SendMessage message) {
        if (Constants.variantsOfTime.stream().anyMatch(t -> t.equals(message.getText()))) {
            return message.getText();
        }
        return "-1";
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

            answer = bankApi.getExchangeRates(buttons.getCurrency());
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

    public void scheduleSendingCurrencyRate(HashMap userSettings, String chatId) throws IOException {
        if (userSettings.get(chatId).equals("-1") || userSettings.get(chatId) == null) {
            return;
        }

        if (runningThreads.containsKey(chatId) && runningThreads.get(chatId).isAlive()) {
            return;
        }

        SendMessage sendMessage = new SendMessage(chatId, bankApi.getExchangeRates(buttons.getCurrency()));

        Thread senderScheduleCurrencyRate = new Thread(() -> {
            while (true) {
                Date date = new Date();
                String hours = String.valueOf(date.getHours());
                if (hours.equals(userSettings.get(chatId))) {
                    if (date.getMinutes() == 0) {
                        try {
                            telegramClient.execute(sendMessage);
                            Thread.sleep(1000 * 60);
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