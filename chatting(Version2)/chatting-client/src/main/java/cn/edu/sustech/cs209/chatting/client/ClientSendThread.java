package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientSendThread extends Thread {

    Client client;
    ObjectOutputStream os;
    ObjectInputStream is;

    ClientSendThread(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        Message message;
        try {
            os = new ObjectOutputStream(client.getUpdateSocket().getOutputStream());
            is = new ObjectInputStream(client.getUpdateSocket().getInputStream());
        } catch (Exception e) {
            client.connectionFail();
            return;
        }
        while (true) {
            try {
                message = (Message) is.readObject();
                if (message.getType().equals(MessageType.UpdateOnlineClient)) {
                    Controller.controller.updateOnlineClient(message.getOnlineClient());
                    int num = message.getOnlineClient().size();
                    Platform.runLater(() -> Controller.controller.updateNumLabel(num));
                }
            } catch (Exception e) {
                client.connectionFail();
                break;
            }
        }
    }
}
