package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;

public class Chat {
    String username;
    ArrayList<Message> allMessages;

    public Chat(String username, String username1) {
        this.username = username;
        allMessages = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }


    public ArrayList<Message> getAllMessages() {
        return allMessages;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public void setAllMessages(ArrayList<Message> allMessages) {
        this.allMessages = allMessages;
    }
}
