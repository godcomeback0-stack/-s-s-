package org.telegram.telegrambots.bots;

import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PayalBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN") != null ? 
            System.getenv("BOT_TOKEN") : "YOUR_BOT_TOKEN";
    private static final String BOT_USERNAME = "PayalBot";
    private static final String CHANNEL_USERNAME = "@your_channel";
    private static final String GROUP_USERNAME = "@your_group";
    private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY") != null ? 
            System.getenv("GEMINI_API_KEY") : "YOUR_GEMINI_API_KEY";
    private static final String OWNER_USERNAME = "@your_username"; // Replace with owner's username

    private final ConcurrentHashMap<Long, Boolean> activeGroups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<String>> chatHistory = new ConcurrentHashMap<>();
    private final GeminiAI geminiAI;

    public PayalBot() {
        this.geminiAI = new GeminiAI(GEMINI_API_KEY);
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();
                String text = message.getText();
                long chatId = message.getChatId();
                String chatType = message.getChat().getType();

                if (chatType.equals("private")) {
                    handlePrivateMessage(chatId, text);
                } else if (chatType.equals("group") || chatType.equals("supergroup")) {
                    handleGroupMessage(chatId, text, message);
                }
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePrivateMessage(long chatId, String text) throws TelegramApiException {
        if (text.equalsIgnoreCase("/start")) {
            sendWelcomeMessage(chatId);
        } else if (text.equalsIgnoreCase("add me group") || text.equalsIgnoreCase("/addgroup")) {
            sendAddToGroupButton(chatId);
        } else if (text.equalsIgnoreCase("/help")) {
            sendHelpMessage(chatId);
        } else if (text.equalsIgnoreCase("/owner")) {
            sendOwnerInfo(chatId);
        } else {
            // AI chat in private
            String aiResponse = geminiAI.getResponse(text, chatHistory.getOrDefault(chatId, new ArrayList<>()));
            updateChatHistory(chatId, "user", text);
            updateChatHistory(chatId, "assistant", aiResponse);
            sendMessage(chatId, aiResponse);
        }
    }

    private void sendWelcomeMessage(long chatId) throws TelegramApiException {
        // Send image
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto("https://your-image-url.com/payal-banner.jpg"); // Replace with your image URL
        
        String welcomeText = "🌟 *Welcome to Payal Bot!*\n\n" +
                "🤖 I'm Payal, your AI-powered virtual assistant!\n" +
                "💬 I can chat like a human and understand context.\n\n" +
                "📢 *Join our Community:*\n" +
                "📱 Channel: " + CHANNEL_USERNAME + "\n" +
                "👥 Group: " + GROUP_USERNAME + "\n\n" +
                "🔹 *Commands:*\n" +
                "/start - Show this message\n" +
                "/addgroup - Add me to your group\n" +
                "/owner - Contact bot owner\n" +
                "/help - Get help\n\n" +
                "💡 Use the buttons below to get started!";

        try {
            execute(sendPhoto);
        } catch (Exception e) {
            // Continue without image if fails
        }
        
        // Send message with colored buttons
        sendMainMenu(chatId, welcomeText);
    }

    private void sendMainMenu(long chatId, String text) throws TelegramApiException {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Row 1: Add Me Group button (Green)
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton addGroupBtn = new InlineKeyboardButton();
        addGroupBtn.setText("➕ Add Me to Group");
        addGroupBtn.setUrl("https://t.me/" + BOT_USERNAME + "?startgroup=true");
        // Set button color - Green (Telegram supports color through callback_data)
        addGroupBtn.setCallbackData("add_group_main");
        row1.add(addGroupBtn);
        rows.add(row1);

        // Row 2: Owner button (Blue) and Help button (Yellow)
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        
        InlineKeyboardButton ownerBtn = new InlineKeyboardButton();
        ownerBtn.setText("👤 Contact Owner");
        ownerBtn.setUrl("https://t.me/" + OWNER_USERNAME.replace("@", ""));
        ownerBtn.setCallbackData("owner_contact");
        row2.add(ownerBtn);

        InlineKeyboardButton helpBtn = new InlineKeyboardButton();
        helpBtn.setText("❓ Help");
        helpBtn.setCallbackData("help_menu");
        row2.add(helpBtn);
        rows.add(row2);

        // Row 3: Channel button (Red) and Group button (Purple)
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        
        InlineKeyboardButton channelBtn = new InlineKeyboardButton();
        channelBtn.setText("📢 Join Channel");
        channelBtn.setUrl("https://t.me/" + CHANNEL_USERNAME.replace("@", ""));
        channelBtn.setCallbackData("join_channel");
        row3.add(channelBtn);

        InlineKeyboardButton groupBtn = new InlineKeyboardButton();
        groupBtn.setText("👥 Join Group");
        groupBtn.setUrl("https://t.me/" + GROUP_USERNAME.replace("@", ""));
        groupBtn.setCallbackData("join_group");
        row3.add(groupBtn);
        rows.add(row3);

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(markup);
        message.enableMarkdown(true);
        execute(message);
    }

    private void sendAddToGroupButton(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Row 1: Add to Group (Green)
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton addBtn = new InlineKeyboardButton();
        addBtn.setText("✅ Add Me to Group");
        addBtn.setUrl("https://t.me/" + BOT_USERNAME + "?startgroup=true");
        addBtn.setCallbackData("add_group_btn");
        row1.add(addBtn);
        rows.add(row1);

        // Row 2: Owner Info (Blue) and Back (Gray)
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        
        InlineKeyboardButton ownerBtn = new InlineKeyboardButton();
        ownerBtn.setText("👤 Owner");
        ownerBtn.setCallbackData("show_owner");
        row2.add(ownerBtn);

        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("🔙 Back");
        backBtn.setCallbackData("back_main");
        row2.add(backBtn);
        rows.add(row2);

        markup.setKeyboard(rows);

        String messageText = "🔘 *Click the button below to add me to your group:*\n\n" +
                "📝 After adding, I'll automatically start chatting!\n" +
                "⚠️ Make sure to make me admin for full features.\n\n" +
                "👤 For any issues, contact the owner.";

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);
        message.setReplyMarkup(markup);
        message.enableMarkdown(true);
        execute(message);
    }

    private void sendOwnerInfo(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Row 1: Contact Owner (Blue)
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton contactBtn = new InlineKeyboardButton();
        contactBtn.setText("📱 Contact Owner");
        contactBtn.setUrl("https://t.me/" + OWNER_USERNAME.replace("@", ""));
        contactBtn.setCallbackData("contact_owner");
        row1.add(contactBtn);
        rows.add(row1);

        // Row 2: Back button (Gray)
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("🔙 Back to Menu");
        backBtn.setCallbackData("back_main");
        row2.add(backBtn);
        rows.add(row2);

        markup.setKeyboard(rows);

        String ownerText = "👤 *Bot Owner Information*\n\n" +
                "👨‍💻 Owner: " + OWNER_USERNAME + "\n" +
                "📧 Email: owner@payalbot.com\n" +
                "🌐 Website: www.payalbot.com\n\n" +
                "💬 For support, bugs, or feedback, contact the owner directly!";

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(ownerText);
        message.setReplyMarkup(markup);
        message.enableMarkdown(true);
        execute(message);
    }

    private void sendHelpMessage(long chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Row 1: Add Group (Green)
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton addBtn = new InlineKeyboardButton();
        addBtn.setText("➕ Add to Group");
        addBtn.setUrl("https://t.me/" + BOT_USERNAME + "?startgroup=true");
        addBtn.setCallbackData("add_from_help");
        row1.add(addBtn);
        rows.add(row1);

        // Row 2: Owner (Blue) and Back (Gray)
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        
        InlineKeyboardButton ownerBtn = new InlineKeyboardButton();
        ownerBtn.setText("👤 Owner");
        ownerBtn.setCallbackData("show_owner");
        row2.add(ownerBtn);

        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("🔙 Back");
        backBtn.setCallbackData("back_main");
        row2.add(backBtn);
        rows.add(row2);

        markup.setKeyboard(rows);

        String helpText = "🤖 *Payal Bot Help*\n\n" +
                "📌 *Features:*\n" +
                "• AI-powered conversations using Gemini\n" +
                "• Group chat support with @mentions\n" +
                "• Context-aware responses\n" +
                "• Human-like interaction\n\n" +
                "📝 *Commands:*\n" +
                "/start - Welcome menu\n" +
                "/addgroup - Add bot to group\n" +
                "/owner - Contact owner\n" +
                "/help - Show this help\n\n" +
                "💬 *How to use in groups:*\n" +
                "1️⃣ Add me to your group\n" +
                "2️⃣ Make me admin\n" +
                "3️⃣ Mention @PayalBot or reply to my messages\n" +
                "4️⃣ I'll respond like a real person!";

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(helpText);
        message.setReplyMarkup(markup);
        message.enableMarkdown(true);
        execute(message);
    }

    private void handleGroupMessage(long chatId, String text, Message message) throws TelegramApiException {
        // Check if group is active
        if (!activeGroups.containsKey(chatId)) {
            // Check if bot is admin
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(String.valueOf(chatId));
            getChatMember.setUserId(message.getFrom().getId());
            ChatMember chatMember = execute(getChatMember);
            
            if (chatMember.getStatus().equals("administrator") || chatMember.getStatus().equals("creator")) {
                activeGroups.put(chatId, true);
                sendMessage(chatId, "✅ Payal is now active in this group! I'll chat like a human! 🤖");
            }
        }

        if (activeGroups.containsKey(chatId) && activeGroups.get(chatId)) {
            // Handle bot mentions and replies
            String botMention = "@" + getBotUsername();
            if (text.contains(botMention) || text.startsWith("/") || message.getReplyToMessage() != null) {
                // Process message with AI
                String userMessage = text.replace(botMention, "").trim();
                if (userMessage.isEmpty()) {
                    userMessage = "Hello Payal!";
                }
                
                // Get AI response
                String aiResponse = geminiAI.getResponse(userMessage, chatHistory.getOrDefault(chatId, new ArrayList<>()));
                updateChatHistory(chatId, "user", userMessage);
                updateChatHistory(chatId, "assistant", aiResponse);
                sendMessage(chatId, aiResponse);
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        switch (data) {
            case "add_group_main":
            case "add_group_btn":
            case "add_from_help":
                sendAddToGroupButton(chatId);
                break;
                
            case "show_owner":
            case "owner_contact":
                sendOwnerInfo(chatId);
                break;
                
            case "help_menu":
                sendHelpMessage(chatId);
                break;
                
            case "back_main":
                sendMainMenu(chatId, "🌟 *Welcome back! How can I help you?*");
                break;
                
            case "join_channel":
                // Already handled by URL
                break;
                
            case "join_group":
                // Already handled by URL
                break;
                
            case "contact_owner":
                // Already handled by URL
                break;
                
            default:
                if (data.startsWith("add_group:")) {
                    String groupId = data.split(":")[1];
                    String groupTitle = data.split(":")[2];
                    activeGroups.put(Long.parseLong(groupId), true);
                    sendMessage(chatId, "✅ Bot added to group: " + groupTitle + " 🎉");
                    sendMessage(Long.parseLong(groupId), "👋 Hello everyone! I'm Payal, your AI friend! Let's chat! 💖");
                }
                break;
        }

        // Answer callback query to remove loading state
        org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery answer = new org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQuery.getId());
        execute(answer);
    }

    private void updateChatHistory(long chatId, String role, String message) {
        chatHistory.computeIfAbsent(chatId, k -> new ArrayList<>());
        List<String> history = chatHistory.get(chatId);
        history.add(role + ": " + message);
        // Keep only last 20 messages
        if (history.size() > 20) {
            history.subList(0, history.size() - 20).clear();
        }
    }

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        execute(message);
    }
}