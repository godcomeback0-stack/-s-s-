package org.telegram.telegrambots.meta;

import org.telegram.telegrambots.bots.PayalBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

public class Main {
    public static void main(String[] args) {
        try {
            // Start health check server for Render
            startHealthServer();
            
            // Start Telegram Bot
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            PayalBot bot = new PayalBot();
            botsApi.registerBot(bot);
            
            System.out.println("✅ Payal Bot started successfully!");
            System.out.println("🤖 Bot is ready to chat!");
            System.out.println("🌐 Health check available at: http://localhost:8080/health");
            
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.err.println("❌ Failed to start bot: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Failed to start health server: " + e.getMessage());
        }
    }

    private static void startHealthServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/health", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });
        server.setExecutor(null);
        server.start();
        System.out.println("✅ Health check server started on port 8080");
    }
}