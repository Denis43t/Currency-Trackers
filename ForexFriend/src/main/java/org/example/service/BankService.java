package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.utils.Buttons;
import org.example.utils.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static org.example.utils.Banks.*;

public class BankService {
    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    private final TelegramClient telegramClient = new OkHttpTelegramClient
            (System.getenv("BOT_TOKEN"));


    private static final String PRIVAT_API_URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    private static final String MONO_API_URL = "https://api.monobank.ua/bank/currency";
    private static final String NBU_API_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private static final int MAX_RETRIES = 5;
    private static final int BASE_DELAY = 1000; // 1 second

    public String getExchangeRates(Currency currency) throws IOException {

        String privatRates = getPrivatBankRates(currency);
        String monoRates = getMonoBankRates(currency);
        String nbuRates = getNbuRates(currency);
        switch (Buttons.selectedBanks){
            case NBU -> {
                return nbuRates;
            }
            case PRIVAT -> {
                return privatRates;
            }
            case MONO -> {
                return monoRates;
            }
        }


        return "Курси валют:\n\n" + privatRates + "\n" + monoRates + "\n" + nbuRates;
    }

    private String getPrivatBankRates(Currency currency) throws IOException {
        URL url = new URL(PRIVAT_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        }

        StringBuilder inline = new StringBuilder();
        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            inline.append(scanner.nextLine());
        }
        scanner.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataNode = mapper.readTree(inline.toString());

        StringBuilder rates = new StringBuilder("PrivatBank:\n");
        for (JsonNode node : dataNode) {
            String ccy = node.get("ccy").asText();
            if (currency == Currency.BOTH || (currency == Currency.USD && ccy.equals("USD")) || (currency == Currency.EUR && ccy.equals("EUR"))) {
                rates.append(ccy)
                        .append(": Buy = ")
                        .append(node.get("buy").asText())
                        .append(", Sale = ")
                        .append(node.get("sale").asText())
                        .append("\n");
            }
        }
        return rates.toString();
    }

    private String getMonoBankRates(Currency currency) throws IOException {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                URL url = new URL(MONO_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    StringBuilder inline = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        inline.append(inputLine);
                    }
                    in.close();
                    connection.disconnect();

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode dataNode = mapper.readTree(inline.toString());

                    StringBuilder rates = new StringBuilder("MonoBank:\n");
                    for (JsonNode node : dataNode) {
                        int currencyCodeA = node.get("currencyCodeA").asInt();
                        int currencyCodeB = node.get("currencyCodeB").asInt();

                        if (currencyCodeB == 980) { // UAH
                            String ccy = getCurrencyCode(currencyCodeA);
                            boolean shouldAdd = false;
                            if (currency == Currency.BOTH) {
                                shouldAdd = "USD".equals(ccy) || "EUR".equals(ccy);
                            } else if (currency == Currency.USD) {
                                shouldAdd = "USD".equals(ccy);
                            } else if (currency == Currency.EUR) {
                                shouldAdd = "EUR".equals(ccy);
                            }
                            if (shouldAdd) {
                                rates.append(ccy)
                                        .append(": Buy = ")
                                        .append(node.has("rateBuy") ? node.get("rateBuy").asText() : "N/A")
                                        .append(", Sale = ")
                                        .append(node.has("rateSell") ? node.get("rateSell").asText() : "N/A")
                                        .append("\n");
                            }
                        }
                    }
                    return rates.toString();
                } else if (responseCode == 429) {
                    attempts++;
                    int delay = BASE_DELAY * (int) Math.pow(2, attempts);
                    logger.warn("Rate limit exceeded. Retrying in " + delay / 1000 + " seconds...");
                    Thread.sleep(delay);
                } else {
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request was interrupted", e);
            }
        }
        throw new RuntimeException("Failed to fetch data from MonoBank after " + MAX_RETRIES + " attempts");
    }


    private String getNbuRates(Currency currency) throws IOException {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                URL url = new URL(NBU_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    StringBuilder inline = new StringBuilder();
                    Scanner scanner = new Scanner(url.openStream());
                    while (scanner.hasNext()) {
                        inline.append(scanner.nextLine());
                    }
                    scanner.close();

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode dataNode = mapper.readTree(inline.toString());

                    StringBuilder rates = new StringBuilder("NBU:\n");
                    for (JsonNode node : dataNode) {
                        String ccy = node.get("cc").asText();
                        if ((currency == Currency.BOTH && (ccy.equals("USD") || ccy.equals("EUR"))) ||
                                (currency == Currency.USD && ccy.equals("USD")) ||
                                (currency == Currency.EUR && ccy.equals("EUR"))) {
                            rates.append(ccy)
                                    .append(": Rate = ")
                                    .append(node.get("rate").asText())
                                    .append("\n");
                        }
                    }
                    return rates.toString();
                } else if (responseCode == 520) {
                    attempts++;
                    int delay = BASE_DELAY * (int) Math.pow(2, attempts);
                    logger.warn("Unknown error from NBU (HTTP 520). Retrying in " + delay / 1000 + " seconds...");
                    logger.warn("Response message: " + connection.getResponseMessage());
                    Thread.sleep(delay);
                } else {
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Request was interrupted", e);
            }
        }
        throw new RuntimeException("Failed to fetch data from NBU after " + MAX_RETRIES + " attempts");
    }

    private String getCurrencyCode(int currencyCode) {
        switch (currencyCode) {
            case 840:
                return "USD";
            case 978:
                return "EUR";
            default:
                return "UNKNOWN";
        }
    }
}
