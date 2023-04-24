package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.Request;
import cn.edu.sustech.cs209.chatting.common.User;
import com.vdurmont.emoji.EmojiParser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Client {
    Socket socket;
    User user;
    static ObjectOutputStream os;
    static ObjectInputStream is;
    private Socket onlineSocket;
    private Socket updateSocket;
    static ArrayList<String> allChatUser = new ArrayList<>();  //记录包括room和user
    static String chatWith = "";
    static Map<String, ArrayList<Message>> allPrivateChat = new HashMap<String, ArrayList<Message>>();
    static Map<String, ArrayList<String>> allJoinRoom = new HashMap<>();

    public Client(Socket socket, Socket onlineSocket, Socket updateSocket){
        try{
            user = new User();
            this.socket = socket;
            this.onlineSocket = onlineSocket;
            this.updateSocket = updateSocket;
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getOnlineSocket() {
        return onlineSocket;
    }

    public Socket getUpdateSocket() {
        return updateSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public User getUser() {
        return user;
    }

    public ObjectOutputStream getOs() {
        return os;
    }

    public ObjectInputStream getIs() {
        return is;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOs(ObjectOutputStream os) {
        this.os = os;
    }

    public void setIs(ObjectInputStream is) {
        this.is = is;
    }

    public static void checkUser(Client client){
        Optional<Pair<String, String>> newUser = logDialog(1);
        Request request;
        if(newUser.isPresent()) {
            client.user.setName(newUser.get().getKey());
            client.user.setPassword(newUser.get().getValue());
            request = new Request();
            request.setUser(client.user);
            request.setRequire(MessageType.LogIn);
            try{
                os.reset();
                os.writeObject(request);
                os.flush();
            while(true) {
                Message message = (Message) is.readObject();
                switch (message.getType()) {
                    case LogSuccess: {
                        ClientThread ct = new ClientThread(client);
                        ClientSendThread cst = new ClientSendThread(Main.client);
                        cst.start();
                        ct.start();
                        return;
                    }
                    case PassError: {
                        showError(1);
                        checkUser(client);
                        return;
                    }
                    case HaveLogged: {
                        showError(2);
                        checkUser(client);
                        return;
                    }
                    case NoRegister: {
                        showError(3);
                        System.out.println("干嘛");
                        registerUser(client);
                        break;
                    }
                    case RegisterSuccess: {
                        System.out.println("kehu");
                        checkUser(client);
                        return;
                    }
                    case RegisterFail: {
                        showError(4);
                        registerUser(client);
                        return;
                    }
                }
            }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        //else
    }

    public static void registerUser(Client client){
        Optional<Pair<String, String>> newUser = logDialog(2);
        System.out.println("无语死了");
        Request request;
        if(newUser.isPresent()) {
            client.user.setName(newUser.get().getKey());
            client.user.setPassword(newUser.get().getValue());
            request = new Request();
            request.setUser(client.user);
            request.setRequire(MessageType.RegisterIn);
            try {
                //os.reset();
                os.writeObject(request);
                //os.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Optional<Pair<String, String>> logDialog(int cases) {   //1 for log 2 for re
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        if(cases == 1){
            dialog.setTitle("Log In");
        } else{
            dialog.setTitle("Register In");
        }
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20,150,10,10));
        TextField userText = new TextField();
        userText.setPromptText("username");
        PasswordField password = new PasswordField();
        password.setPromptText("password");
        grid.add(new Label("username:"),0,0);
        grid.add(userText,1,0);
        grid.add(new Label("password"),0,1);
        grid.add(password,1,1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(userText.getText(), password.getText());
            }
            return null;
        });
        return dialog.showAndWait();
    }

    public static void showError(int i){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");

        switch (i) {
            case 1: {
                alert.setContentText("Your Password is wrong, Please try again");
                break;
            }
            case 2:{
                alert.setContentText("This client is online, please try another");
                break;
            }
            case 3:{
                alert.setContentText("You are not registered, Please register in");
                break;
            }
            case 4: {
                alert.setContentText("The name is used, Please choose another");
                break;
            }
        }

        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void sendMessage(Request request){
        try{
            os.reset();
            os.writeObject(request);
            os.flush();
            System.out.println("发送给服务端了！");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendTextOrEmoji(TextArea t, int num){     //1代表消息2表情
        if(t.getText()==null||t.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setTitle("BLANK");
            alert.setContentText("Please input something!");
            alert.showAndWait();
        }else {
            Request request = new Request();
            Message message = new Message();
            String s = t.getText();
            //表情和文字作区分
            if(num == 2){
                s = EmojiParser.parseToUnicode(s);
            }
            message.setData(s);

            System.out.println("发送 " + s);

            if(chatWith.contains("Room")){   //群聊
                message.setRoomName(chatWith);
                request.setRequire(MessageType.SendGroupMessage);
                //
            }
            else{
                request.setRequire(MessageType.SendMessage);//私聊
                //TODO:检测这个人是否在线并弹窗
                message.setSendTo(chatWith);
                if (!Controller.controller.onlineUser.contains(chatWith)) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText(null);
                    alert.setTitle("BLANK");
                    alert.setContentText("He/She is offline. Still send?");
                    Optional<ButtonType> result = alert.showAndWait();
                    //TODO:确认则发送，取消则return
                    if (!result.get().equals(ButtonType.OK)) {
                        alert.close();
                        return;
                    } else {
                        t.setText("");
                    }
                }
            }
            message.setTimestamp(System.currentTimeMillis());
            message.setSentBy(Main.client.getUser().getName());
            allPrivateChat.get(chatWith).add(message);
            request.setMessage(message);
            Main.client.sendMessage(request);
            t.setText("");
            Controller.controller.updateRightPanel();
        }
    }

    public synchronized void connectionFail(){
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connection Fail!");
            alert.setContentText("Sorry but connection with server fails");
            alert.setHeaderText(null);
            alert.showAndWait();

            Platform.exit();
            System.exit(0);}
        );
    }
}
