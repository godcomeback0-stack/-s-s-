package org.telegram.telegrambots.bots;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class GeminiAI {
    private final String apiKey;
    private final OkHttpClient client;

    public GeminiAI(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public String getResponse(String userMessage, List<String> history) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

            JsonObject requestBody = new JsonObject();
            
            JsonArray contents = new JsonArray();
            
            // System instruction
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "user");
            JsonObject systemContent = new JsonObject();
            systemContent.addProperty("text", "You are Payal, a friendly and helpful AI assistant. " +
                    "You chat like a real person, use emojis sometimes, and are very engaging. " +
                    "You have a cheerful personality and love helping people. " +
                    "Keep responses short and conversational (1-2 sentences). " +
                    "If someone asks about the bot owner, tell them to use /owner command.");
            systemMessage.add("parts", systemContent);
            contents.add(systemMessage);

            // Add chat history
            for (String entry : history) {
                JsonObject message = new JsonObject();
                String[] parts = entry.split(": ", 2);
                if (parts.length == 2) {
                    message.addProperty("role", parts[0].equals("user") ? "user" : "model");
                    JsonObject content = new JsonObject();
                    content.addProperty("text", parts[1]);
                    message.add("parts", content);
                    contents.add(message);
                }
            }

            // Add current user message
            JsonObject userMessageObj = new JsonObject();
            userMessageObj.addProperty("role", "user");
            JsonObject userContent = new JsonObject();
            userContent.addProperty("text", userMessage);
            userMessageObj.add("parts", userContent);
            contents.add(userMessageObj);

            requestBody.add("contents", contents);

            // Generation config
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.9);
            generationConfig.addProperty("topK", 40);
            generationConfig.addProperty("topP", 0.95);
            generationConfig.addProperty("maxOutputTokens", 200);
            requestBody.add("generationConfig", generationConfig);

            // Make API request
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(
                            MediaType.parse("application/json"),
                            requestBody.toString()
                    ))
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                return parts.get(0).getAsJsonObject().get("text").getAsString();
            } else if (jsonResponse.has("error")) {
                return "😅 Oops! Something went wrong. Please try again later.";
            }

            return "🤔 Hmm, I'm not sure about that. Can you ask something else?";
            
        } catch (IOException e) {
            e.printStackTrace();
            return "😅 Sorry, I'm having trouble connecting. Please try again!";
        }
    }
}