package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;

public class Request implements Serializable {
    MessageType require;
    User user;
    Message message;
    Room room;

    public Request() {
    }

    ;

    public MessageType getRequire() {
        return require;
    }

    public User getUser() {
        return user;
    }

    public void setRequire(MessageType require) {
        this.require = require;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
