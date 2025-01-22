package com.messaging.rcs;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class ChatButtonSender {

    // Method to create chat buttons
    public static JSONObject createButton(String title, String payload) {
        JSONObject button = new JSONObject();
        button.put("type", "button");
        button.put("title", title);
        button.put("payload", payload);
        return button;
    }

    // Method to send buttons as a chat message
    public static JSONObject sendChatButtons() {
        JSONObject message = new JSONObject();
        message.put("type", "interactive");

        // List of buttons
        List<JSONObject> buttons = new ArrayList<>();
        buttons.add(createButton("Below 1 Lac", "below_1_lac"));
        buttons.add(createButton("1-2 Lac", "1_to_2_lac"));
        buttons.add(createButton("2-3 Lac", "2_to_3_lac"));
        buttons.add(createButton("3-4 Lac", "3_to_4_lac"));

        message.put("buttons", buttons);
        return message;
    }

    public static void main(String[] args) {
        JSONObject chatMessage = sendChatButtons();
        // Send the message using your messaging platform's API
        System.out.println(chatMessage.toString());
    }
}
