package org.example;

import org.example.service.BotService;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        BotService botService=new BotService();
        try {
            String botToken =System.getenv("BOT_TOKEN");
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new BotService());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}