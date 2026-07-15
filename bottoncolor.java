package org.telegram.telegrambots.bots;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ButtonColors {
    
    // Custom color codes for Telegram inline buttons
    // Note: Telegram doesn't support direct color changes for inline buttons,
    // but we can use emojis and text formatting to create visual distinction
    
    public static InlineKeyboardButton createColoredButton(String text, String color, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        
        // Add color indicators using emojis and formatting
        String coloredText = getColoredText(text, color);
        button.setText(coloredText);
        button.setCallbackData(callbackData);
        
        return button;
    }
    
    public static InlineKeyboardButton createUrlButton(String text, String color, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        
        // Add color indicators using emojis and formatting
        String coloredText = getColoredText(text, color);
        button.setText(coloredText);
        button.setUrl(url);
        
        return button;
    }
    
    private static String getColoredText(String text, String color) {
        switch (color.toLowerCase()) {
            case "green":
                return "🟢 " + text;
            case "blue":
                return "🔵 " + text;
            case "red":
                return "🔴 " + text;
            case "purple":
                return "🟣 " + text;
            case "yellow":
                return "🟡 " + text;
            case "orange":
                return "🟠 " + text;
            case "white":
                return "⚪ " + text;
            case "black":
                return "⚫ " + text;
            default:
                return text;
        }
    }
    
    // Color emoji mapping
    public static final String GREEN_CIRCLE = "🟢";
    public static final String BLUE_CIRCLE = "🔵";
    public static final String RED_CIRCLE = "🔴";
    public static final String PURPLE_CIRCLE = "🟣";
    public static final String YELLOW_CIRCLE = "🟡";
    public static final String ORANGE_CIRCLE = "🟠";
    public static final String WHITE_CIRCLE = "⚪";
    public static final String BLACK_CIRCLE = "⚫";
}