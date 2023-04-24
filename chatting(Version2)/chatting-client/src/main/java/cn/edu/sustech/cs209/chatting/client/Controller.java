package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cn.edu.sustech.cs209.chatting.client.Client.*;

public class Controller implements Initializable {
    public static Controller controller = new Controller();
    public ArrayList<String> onlineUser = new ArrayList<>();
    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<String> chatList = new ListView<>();
    @FXML
    Label currentUsername;
    @FXML
    Label currentOnlineCnt;
    @FXML
    TextArea inputArea;
    @FXML
    Label currentGM;

    static String username;
    static String pass;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        checkUser(Main.client);
        username = Main.client.getUser().getName();
        currentUsername.setText("Current User: " + username);
        chatContentList.setCellFactory(new MessageCellFactory());
        controller = this;
    }

    /*
    public static void log(Optional<String> input){
        int port = 8919;
        Socket socket = null;
        PrintWriter out = null;
        Scanner in = null;

        if (input.isPresent() && !input.get().isEmpty()) {
            try {
                String name = input.get();
                socket = new Socket("localhost", port);
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                out = new PrintWriter(outputStream);
                in = new Scanner(inputStream);
                out.println("Log in");
                out.flush();
                out.println(name);
                out.flush();

                String i = in.nextLine();

                if(i.equals("No")){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("You are not registered, Please register in");
                    alert.showAndWait();
                    Optional<String> newInput = logDialog(2);
                    if(newInput.isPresent() && !newInput.get().isEmpty()) {
                        String newName = newInput.get();
                        out.println("Register in");
                        out.flush();
                        out.println(newName);
                        out.flush();
                        log(logDialog(1));
                    }
                    else{
                        Platform.exit();
                    }
                } else if(i.equals("Exist")){
                    existError();
                    log(logDialog(1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }
    }

     */
    @FXML
    public void createPrivateChat() {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll(onlineUser);
        //TODO:点击确定后创建私聊，主线程发送信息，新建该对话。 ServerThread接收到后创建Chat(User1name,User2name,Arraylist<Message>),对应server同理
        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        if (user.get() != null) {
            String s = user.get() + "  new Message(s)";
            if (allChatUser.contains(user.get()) || allChatUser.contains(s)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Tips");
                alert.setContentText("You have create this chat");
                alert.setHeaderText(null);
                alert.showAndWait();
                chatWith = user.get();
                this.goToTheScene(chatWith);
            } else {
                String other = user.get();
                allChatUser.add(other);
                //加入到聊天MAP
                ArrayList<Message> M = new ArrayList<>();
                allPrivateChat.put(other, M);

                System.out.println("这明明不是空！ " + allPrivateChat.size());

                updateLeftPanel();
            }
        }
    }

    void updateLeftPanel() {
        System.out.println("进去更新左页面");
        for (int i = 0; i < allChatUser.size(); i++) {
            System.out.println(allChatUser.get(i));
        }
        ObservableList<String> srl = FXCollections.observableArrayList(allChatUser);
        chatList.setItems(srl);
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */

    //TODO:跳出选择选择人员，跳出取名框框
    @FXML
    public void createGroupChat() {
        ArrayList<String> thisRoomMember = new ArrayList<>();
        //建立box
        Stage selectMember = new Stage();
        selectMember.setTitle("NEW");
        selectMember.setHeight(300);
        selectMember.setWidth(300);
        VBox vbox = new VBox();
        CheckBox[] allCheckBox = new CheckBox[onlineUser.size()];
        for (int i = 0; i < onlineUser.size(); i++) {
            allCheckBox[i] = new CheckBox();
            allCheckBox[i].setText(onlineUser.get(i));
        }
        vbox.getChildren().add(new Label("choose Members"));
        vbox.getChildren().addAll(allCheckBox);
        Button yes = new Button("OK");
        vbox.getChildren().add(yes);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        Scene scene = new Scene(vbox);
        selectMember.setScene(scene);
        selectMember.show();


        yes.setOnAction(e -> {
            for (int i = 0; i < allCheckBox.length; i++) {
                if (allCheckBox[i].isSelected()) {
                    thisRoomMember.add(allCheckBox[i].getText());
                }
            }
            selectMember.close();
            if (thisRoomMember.size() != 0) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Name the Room");
                dialog.setHeaderText(null);
                dialog.setContentText("Name: \n" + "(we will put name after 'Room')");
                this.createRoom(dialog.showAndWait().get(), thisRoomMember);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("NONE");
                alert.setHeaderText(null);
                alert.setContentText("No Member Is Chosen");
                alert.showAndWait();
            }
        });


    }

    //TODO:（不用管是否存在）创建room并将名字放入allChatUser中，在allPrivateChat中新建 左边刷新
    //TODO: 缺少更新下面和中间
    public void createRoom(String inputName, ArrayList<String> members) {
        members.add(Main.client.user.getName());
        Room room = new Room(inputName, members);
        allChatUser.add(room.getRoomName());
        ArrayList<Message> chatHistory = new ArrayList<>();
        allPrivateChat.put(room.getRoomName(), chatHistory);
        allJoinRoom.put(room.getRoomName(), members);
        Request request = new Request();
        request.setRequire(MessageType.CreateGroup);
        request.setRoom(room);
        request.setUser(Main.client.user);
        try {
            os.reset();
            os.writeObject(request);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.updateLeftPanel();
    }
    //TODO: 点击时同样触发goToScene
    //TODO: drag时触发quitGroupChat  在thread收到消息时+消息提示， 在gotoScene时取消消息提示 （allChatUser）
    //TODO： 点击发送时候发送request， 向所有人发送群聊消息并刷新

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void goToScene() {
        //update到对应的聊天框中
        chatWith = chatList.getSelectionModel().selectedItemProperty().get();
        goToTheScene(chatWith);
    }

    @FXML
    public void doSendMessage() {
        Main.client.sendTextOrEmoji(inputArea, 1);
    }

    @FXML
    public void doSendEmoji() {
        Main.client.sendTextOrEmoji(inputArea, 2);
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */

    @FXML
    public void quitGroupChat() {
        String roomName = chatList.getSelectionModel().getSelectedItem();
        if (roomName == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("EXIT REALLY?");
        alert.setContentText(null);
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.get().equals(ButtonType.OK)) {
            return;
        }
        System.out.println("roomName " + roomName);
        quitTheGroupChat(roomName);
        chatContentList.setItems(FXCollections.observableArrayList(new ArrayList<>()));
        currentGM.setText("Not in Group Chatting");
    }

    static class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        setText("");
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    public void updateOnlineClient(ArrayList<User> onlineClient) {
        onlineUser.clear();
        for (User user : onlineClient) {
            onlineUser.add(user.getName());
        }
        onlineUser.remove(Main.client.user.getName());
    }

    //右下角人数更新
    public void updateNumLabel(int num) {
        currentOnlineCnt.setText("Online: " + num);
    }

    //聊天界面更新: 发送完消息/点开框都要更新
    public void updateRightPanel() {
        System.out.println("进来了嘛！");
        System.out.println(allPrivateChat.size());
        ObservableList<Message> srl
                = FXCollections.observableArrayList(allPrivateChat.get(chatWith));
        chatContentList.setItems(srl);
        System.out.println("跑完更新右边");
    }

    public void updateBottomPanel() {
        String show = "GroupMember: ";
        String room;
        if (chatWith.contains("new Message(s)")) {
            room = chatWith.substring(0, chatWith.length() - 16);
        } else {
            room = chatWith;
        }

        if (allJoinRoom.get(room).size() <= 3) {
            for (int i = 0; i < allJoinRoom.get(room).size(); i++) {
                show = show.concat(allJoinRoom.get(room).get(i)).concat(" ");
            }
        } else {
            for (int i = 0; i < 3; i++) {
                show = show.concat(allJoinRoom.get(room).get(i)).concat(" ");
            }
            show = show.concat("...");
        }
        currentGM.setText(show);
    }

    public void goToTheScene(String ChatUser) {
        if (ChatUser == null) {
            return;
        }
        if (ChatUser.contains("Room")) {
            this.updateBottomPanel();
        } else {
            currentGM.setText("Not in Group Chatting");
        }
        if (ChatUser.contains("new Message(s)")) {
            String s = ChatUser.substring(0, ChatUser.length() - 16);

            System.out.println(s);
            System.out.println(ChatUser);

            for (int i = 0; i < allChatUser.size(); i++) {
                if (allChatUser.get(i).equals(ChatUser)) {
                    allChatUser.set(i, s);
                }
            }
            ChatUser = s;
            chatWith = s;
            this.updateLeftPanel();
        }
        System.out.println("聊天对象 " + ChatUser);
        this.updateRightPanel();
    }

    public void quitTheGroupChat(String roomName) {
        if (roomName.contains("new Message(s)")) {
            roomName = roomName.substring(0, roomName.length() - 16);
        }
        for (int i = 0; i < allChatUser.size(); i++) {
            if (allChatUser.get(i).contains(roomName)) {
                allChatUser.remove(allChatUser.get(i));
            }
        }
        //TODO: check
        System.out.println("检查有无删除");
        for (String s : allChatUser) {
            System.out.println(s);
        }

        allPrivateChat.remove(roomName);
        //TODO:发送信息给server，更新room内人员, service发送给其他人要求更新
        Request request = new Request();
        Message message = new Message();
        message.setRoomName(roomName);
        message.setSentBy(Main.client.user.getName());
        request.setRequire(MessageType.SomeOneQUit);
        request.setMessage(message);

        try {
            os.reset();
            os.writeObject(request);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.updateLeftPanel();
    }
}
