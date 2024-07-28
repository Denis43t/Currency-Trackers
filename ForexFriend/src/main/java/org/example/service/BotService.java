package org.example.service;

import org.example.properties.Constants;
import org.example.utils.Buttons;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

public class BotService implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient = new OkHttpTelegramClient
            (System.getenv("BOT_TOKEN"));

    Buttons buttons =new Buttons(telegramClient);
    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString()
                    , update.getMessage().getText());
            //надає клавіатуру як що був веден час
            if (sendMessage.getText().equalsIgnoreCase("час")) {
                sendCustomKeyboardTime(sendMessage.getChatId());
            }
        }

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
                    buttons.handleSettings(chatId, messageId);
                    break;
                case "settings_currency":
                    buttons.handleCurrencySettings(chatId, messageId);
                    break;
                default:
                    if (callData.startsWith("settings_currency_")) {
                        buttons.setCurrencySelection(callData);
                        sendExchangeRates(chatId, messageId);
                    }
                    break;
            }
        }
    }

    //перевіряє чи був введен час для свопіщення
    public String getTimeOfSendingNotifications(SendMessage message){
        if (Constants.variantsOfTime.stream().anyMatch(t->t.equals(message.getText()))){
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
}