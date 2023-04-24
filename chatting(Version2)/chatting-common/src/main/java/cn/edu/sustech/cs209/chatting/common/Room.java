package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;

//创建群聊
public class Room implements Serializable {
    ArrayList<String> roomMember;
    String roomName;

    public Room(String name, ArrayList<String> Member) {
        roomName = "Room " + name;
        roomMember = Member;
    }

    public ArrayList<String> getRoomMember() {
        return roomMember;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomMember(ArrayList<String> roomMember) {
        this.roomMember = roomMember;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
