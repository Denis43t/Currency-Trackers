package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BankApi {
    private static final String PRIVAT_API_URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    //private static final String MONO_API_URL = "https://api.monobank.ua/bank/currency";
    private static final String NBU_API_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    public String getExchangeRates() throws IOException {
        String privatRates = getPrivatBankRates();
        //String monoRates = getMonoBankRates();
        String nbuRates = getNbuRates();

        return "Курси валют:\n\n" + privatRates + "\n"  + "\n" + nbuRates;
    }

    private String getPrivatBankRates() throws IOException {
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
        JsonNode node = mapper.readTree(inline.toString());
        StringBuilder result = new StringBuilder("ПриватБанк:\n");

        for (JsonNode rate : node) {
            String currency = rate.get("ccy").asText();
            String baseCurrency = rate.get("base_ccy").asText();
            String buy = rate.get("buy").asText();
            String sale = rate.get("sale").asText();
            result.append(String.format("%s/%s: Купівля = %s, Продаж = %s\n", currency, baseCurrency, buy, sale));
        }

        return result.toString();
    }

    /*private String getMonoBankRates() throws IOException {
        URL url = new URL(MONO_API_URL);
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
        JsonNode node = mapper.readTree(inline.toString());
        StringBuilder result = new StringBuilder("Монобанк:\n");

        for (JsonNode rate : node) {
            int currencyCodeA = rate.get("currencyCodeA").asInt();
            int currencyCodeB = rate.get("currencyCodeB").asInt();
            if (currencyCodeB != 980) { // Filter out non-UAH rates
                continue;
            }
            double rateBuy = rate.has("rateBuy") ? rate.get("rateBuy").asDouble() : 0;
            double rateSell = rate.has("rateSell") ? rate.get("rateSell").asDouble() : 0;

            String currencyA = getCurrencyCode(currencyCodeA);
            result.append(String.format("%s/UAH: Купівля = %.2f, Продаж = %.2f\n", currencyA, rateBuy, rateSell));
        }

        return result.toString();
    }*/

    private String getNbuRates() throws IOException {
        URL url = new URL(NBU_API_URL);
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
        JsonNode node = mapper.readTree(inline.toString());
        StringBuilder result = new StringBuilder("НБУ:\n");

        for (JsonNode rate : node) {
            String currency = rate.get("cc").asText();
            double rateValue = rate.get("rate").asDouble();
            result.append(String.format("%s/UAH: %.2f\n", currency, rateValue));
        }

        return result.toString();
    }

    private String getCurrencyCode(int code) {
        switch (code) {
            case 840:
                return "USD";
            case 978:
                return "EUR";
            default:
                return "Unknown";
        }
    }
}

