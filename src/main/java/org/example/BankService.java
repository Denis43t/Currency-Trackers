package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BankService {
    private static final String PRIVAT_API_URL = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
    //private static final String MONO_API_URL = "https://api.monobank.ua/bank/currency";
    private static final String NBU_API_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";

    public String getExchangeRates(Currency currency) throws IOException {
        String privatRates = getPrivatBankRates(currency);
        //String monoRates = getMonoBankRates(currency);
        String nbuRates = getNbuRates(currency);

        return "Курси валют:\n\n" + privatRates + "\n" + "\n" + nbuRates;
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

    /*private String getMonoBankRates(Currency currency) throws IOException {
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
        JsonNode dataNode = mapper.readTree(inline.toString());

        StringBuilder rates = new StringBuilder("MonoBank:\n");
        for (JsonNode node : dataNode) {
            int currencyCodeA = node.get("currencyCodeA").asInt();
            int currencyCodeB = node.get("currencyCodeB").asInt();

            if (currencyCodeB == 980) { // UAH
                String ccy = getCurrencyCode(currencyCodeA);
                if (currency == Currency.BOTH || (currency == Currency.USD && ccy.equals("USD")) || (currency == Currency.EUR && ccy.equals("EUR"))) {
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
    }*/

    private String getNbuRates(Currency currency) throws IOException {
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
