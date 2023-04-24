package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.Request;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

public class Main extends Application {
    static Socket socket;
    static Socket onlineSocket;
    static Socket updateSocket;
    static Client client;

    public static void main(String[] args) throws IOException {
        socket = new Socket("localhost", 8919);
        onlineSocket = new Socket("localhost", 8919);
        updateSocket = new Socket("localhost", 8919);
        client = new Client(socket, onlineSocket, updateSocket);
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
        //TODO: 退出顺序？
        Platform.setImplicitExit(false);
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("REALLY EXIT?");
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get().equals(ButtonType.OK)) {
                Request request = new Request();
                request.setRequire(MessageType.Exit);
                request.setUser(client.getUser());
                try {
                    client.getOs().reset();
                    client.getOs().writeObject(request);
                    client.getOs().flush();

                    Message message = (Message) client.getIs().readObject();

                    if (message.getType().equals(MessageType.ExitSuccess)) {
                        for (int i = 0; i < Client.allChatUser.size(); i++) {
                            if (Client.allChatUser.get(i).contains("Room")) {
                                Controller.controller.quitTheGroupChat(Client.allChatUser.get(i));
                            }
                        }
                        socket.close();
                        client.getIs().close();
                        client.getOs().close();
                        onlineSocket.close();
                        updateSocket.close();
                        stage.close();
                    }
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                event.consume();
                stage.show();
            }
        });
    }


}
