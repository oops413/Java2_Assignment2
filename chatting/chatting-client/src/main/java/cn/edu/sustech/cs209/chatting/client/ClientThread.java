package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.Room;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;

import static cn.edu.sustech.cs209.chatting.client.Client.*;


public class ClientThread extends Thread {
    Client client;
    ObjectOutputStream os;
    ObjectInputStream is;
    ClientThread(Client client){
        this.client = client;
    }
    @Override
    public void run() {
        Message message;
        try {
            os = new ObjectOutputStream(client.getOnlineSocket().getOutputStream());
            is = new ObjectInputStream(client.getOnlineSocket().getInputStream());
        } catch (Exception e){
            client.connectionFail();
            return;
        }
        while(true){
            try {
                System.out.println("进去了吗？");
                message = (Message) is.readObject();

                System.out.println("消息是： "+message.getType().toString());
                System.out.println("sender是 "+message.getSentBy());
                System.out.println("room是 "+message.getRoomName());
                String visualSender;
                if(message.getType().equals(MessageType.ReceiveMessage)) {
                    //TODO:Control提示新消息
                    System.out.println("私聊 " + message.isReceiveGroup());
                    if (!message.isReceiveGroup()) {
                        visualSender = message.getRoomName();
                    } else {
                        visualSender = message.getSentBy();
                    }
                    System.out.println("sender  " + visualSender);
                    if (!allPrivateChat.containsKey(visualSender)) {
                        ArrayList<Message> M = new ArrayList<>();
                        M.add(message);
                        allPrivateChat.put(visualSender, M);
                        System.out.println("应该过了这里");
                    } else {
                        Message finalMessage = message;
                        allPrivateChat.forEach((key, value) -> {
                            if (key.equals(visualSender)) {
                                value.add(finalMessage);
                                value.sort(new Comparator<Message>() {
                                    @Override
                                    public int compare(Message o1, Message o2) {
                                        return (int) (o1.getTimestamp() - o2.getTimestamp());
                                    }
                                });
                            }
                        });
                    }
                    String s = visualSender + "  new Message(s)";
                    if (chatWith.equals(visualSender)) {
                        Platform.runLater(() -> Controller.controller.updateRightPanel());
                    } else {
                        if (!(allChatUser.contains(visualSender) || (allChatUser.contains(s)))) {
                            System.out.println("更新 " + allPrivateChat.size());
                            //未更新allPrivateChat
                            allChatUser.add(s);


                            System.out.println("要在这里更新allchatuser");
                            for (int i = 0; i < allChatUser.size(); i++) {
                                System.out.println(allChatUser.get(i));
                            }


                        } else if (allChatUser.contains(visualSender)) {
                            for (int i = 0; i < allChatUser.size(); i++) {
                                if (allChatUser.get(i).equals(visualSender)) {
                                    s = visualSender + "  new Message(s)";
                                    allChatUser.set(i, s);
                                }
                            }
                        }
                        Platform.runLater(() -> Controller.controller.updateLeftPanel());
                        System.out.println("更新了吗");
                    }
                }
                else if(message.getType().equals(MessageType.CreateGroup)){
                    Room room = message.getRoom();
                    String roomName = room.getRoomName();
                    ArrayList<String> members = room.getRoomMember();
                    allJoinRoom.put(roomName,members);
                    allPrivateChat.put(roomName, new ArrayList<>());
                    allChatUser.add(roomName);
                }
                else if(message.getType().equals(MessageType.SomeOneQUit)) {
                    String some = message.getSentBy();
                    String room = message.getRoomName();
                    ArrayList<String> newMember = allJoinRoom.get(room);
                    newMember.remove(some);
                    allJoinRoom.put(room, newMember);
                    Platform.runLater(()->Controller.controller.updateBottomPanel());
                }
            } catch (Exception e) {
                client.connectionFail();
                break;
            }
        }
    }
}
