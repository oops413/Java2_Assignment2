package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private MessageType type;

    private ArrayList<User> onlineClient;

    private String roomName;

    private boolean receiveGroup;

    private Room room;    //仅仅用于在createRoom时传给其他人

    public Message() {
        this.receiveGroup = true;
    }

    public Message(Long timestamp, String sentBy, String sendTo, String data, MessageType type) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
        this.receiveGroup = true;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isReceiveGroup() {
        return receiveGroup;
    }

    public void setReceiveGroup(boolean receiveGroup) {
        this.receiveGroup = receiveGroup;
    }

    public MessageType getType() {
        return type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public ArrayList<User> getOnlineClient() {
        return onlineClient;
    }

    public void setOnlineClient(ArrayList<User> onlineClient) {
        this.onlineClient = onlineClient;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
