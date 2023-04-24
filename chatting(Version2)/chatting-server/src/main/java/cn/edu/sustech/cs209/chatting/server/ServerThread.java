package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static cn.edu.sustech.cs209.chatting.server.Main.allClient;

public class ServerThread extends Thread {
    private final Socket socket;
    private final Socket onlineSocket;
    private final Socket updateSocket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private ObjectOutputStream onOs;
    private ObjectInputStream onIs;
    private ObjectOutputStream upOs;
    private ObjectInputStream upIs;
    private String thisUser;

    public ServerThread(Socket socket, Socket onlineSocket, Socket updateSocket) {
        this.socket = socket;
        this.onlineSocket = onlineSocket;
        this.updateSocket = updateSocket;
        thisUser = "";
    }

    @Override
    public void run() {
        try {
            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
            Request request;
            MessageType instruction;
            User user;
            Message message = new Message();
            Message upMessage = new Message();
            Message onMessage = new Message();
            while (true) {
                request = (Request) is.readObject();
                instruction = request.getRequire();
                user = request.getUser();
                System.out.println("=====服务端收到====");
                System.out.println(instruction);
                System.out.println(thisUser);
                System.out.println(this);
                switch (instruction) {
                    case LogIn: {
                        message = checkUser(user);
                        //TODO: 如果成功，updateSocket 所有server发送更新
                        //os.reset();
                        os.writeObject(message);
                        //os.flush();
                        if (message.getType().equals(MessageType.LogSuccess)) {
                            onIs = new ObjectInputStream(onlineSocket.getInputStream());
                            onOs = new ObjectOutputStream(onlineSocket.getOutputStream());
                            upIs = new ObjectInputStream(updateSocket.getInputStream());
                            upOs = new ObjectOutputStream(updateSocket.getOutputStream());
                            thisUser = user.getName();
                            Server.allThread.put(thisUser, this);
                            upMessage.setType(MessageType.UpdateOnlineClient);
                            upMessage.setOnlineClient(getOnlineUser());
                            Server.allThread.forEach((key, value) -> {
                                try {
                                    value.getUpOs().reset();
                                    value.getUpOs().writeObject(upMessage);
                                    value.getUpOs().flush();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                        break;
                    }
                    case RegisterIn: {
                        message = Register(user);
                        //os.reset();
                        os.writeObject(message);
                        //os.flush();
                        break;
                    }
                    case Exit: {
                        message.setType(MessageType.ExitSuccess);
                        for (int i = 0; i < allClient.size(); i++) {
                            if (allClient.get(i).getName().equals(user.getName())) {
                                allClient.get(i).setLogged(false);
                            }
                        }
                        user.setLogged(false);

                        //TODO: 如果成功，updateSocket 发送更新指令
                        upMessage.setType(MessageType.UpdateOnlineClient);
                        upMessage.setOnlineClient(getOnlineUser());
                        Server.allThread.remove(thisUser);
                        Server.allThread.forEach((key, value) -> {
                            try {
                                value.getUpOs().reset();
                                value.getUpOs().writeObject(upMessage);
                                value.getUpOs().flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        });

                        os.reset();
                        os.writeObject(message);
                        os.flush();
                        System.out.println("某客户normally断开连接");
                        break;
                    }
                    //TODO:发送消息
                    case SendMessage: {
                        Message thisMessage = request.getMessage();

                        String two = thisMessage.getSendTo();

                        System.out.println("内容： " + thisMessage.getData());

                        onMessage = thisMessage;
                        onMessage.setType(MessageType.ReceiveMessage);

                        System.out.println(onMessage.getSentBy());

                        if (Server.allThread.containsKey(two)) {
                            Message finalOnMessage = onMessage;

                            finalOnMessage.setReceiveGroup(true);
                            Server.allThread.forEach((key, value) -> {
                                if (key.equals(two)) {
                                    try {
                                        value.getOnOs().reset();
                                        value.getOnOs().writeObject(finalOnMessage);
                                        value.getOnOs().flush();
                                        System.out.println("发送收信人更新指令");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        break;
                    }
                    case CreateGroup: {
                        String userName = user.getName();
                        Room room = request.getRoom();
                        Main.allRoom.add(room);
                        System.out.println("创建的房间 " + room.getRoomName());
                        Message thisMessage = new Message();
                        thisMessage.setType(MessageType.CreateGroup);
                        thisMessage.setRoom(room);

                        room.getRoomMember().forEach(o -> {
                            if (!o.equals(userName)) {
                                if (Server.allThread.containsKey(o)) {
                                    ObjectOutputStream temp = Server.allThread.get(o).getOnOs();
                                    try {
                                        temp.reset();
                                        temp.writeObject(thisMessage);
                                        temp.flush();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        break;
                    }
                    case SendGroupMessage: {
                        Message thisMessage = request.getMessage();
                        String room = thisMessage.getRoomName();

                        onMessage = thisMessage;
                        onMessage.setType(MessageType.ReceiveMessage);

                        onMessage.setReceiveGroup(false); //false表群聊
                        Message finalOnMessage1 = onMessage;
                        Main.allRoom.forEach(o -> {
                            if (o.getRoomName().equals(room)) {
                                o.getRoomMember().forEach(t -> {
                                    if (Server.allThread.containsKey(t) && !t.equals(finalOnMessage1.getSentBy())) {
                                        ObjectOutputStream tempOs = Server.allThread.get(t).getOnOs();
                                        try {
                                            System.out.println("进到发群聊里了 ");
                                            System.out.println(finalOnMessage1.getSentBy());
                                            System.out.println(finalOnMessage1.getRoomName());
                                            tempOs.reset();
                                            tempOs.writeObject(finalOnMessage1);
                                            tempOs.flush();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                        break;
                    }
                    case SomeOneQUit: {
                        Message thisMessage = request.getMessage();
                        String room = thisMessage.getRoomName();
                        String name = thisMessage.getSentBy();
                        Main.allRoom.forEach(o -> {
                            if (o.getRoomName().equals(room)) {
                                ArrayList<String> nowUser = o.getRoomMember();
                                nowUser.remove(name);
                                o.setRoomMember(nowUser);
                                nowUser.forEach(t -> {
                                    if (Server.allThread.containsKey(t)) {
                                        ObjectOutputStream temp = Server.allThread.get(t).getOnOs();
                                        Message message1 = new Message();
                                        message1.setRoomName(room);
                                        message1.setSentBy(name);
                                        message1.setType(MessageType.SomeOneQUit);
                                        try {
                                            temp.reset();
                                            temp.writeObject(message1);
                                            temp.flush();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Message checkUser(User user) throws IOException {
        Message message = new Message();
        for (int i = 0; i < allClient.size(); i++) {
            if (allClient.get(i).getName().equals(user.getName())) {
                if (allClient.get(i).getPassword().equals(user.getPassword())) {
                    if (allClient.get(i).isLogged()) {
                        message.setType(MessageType.HaveLogged);
                    } else {
                        message.setType(MessageType.LogSuccess);
                        allClient.get(i).setLogged(true);
                    }
                } else {
                    message.setType(MessageType.PassError);
                }
                return message;
            }
        }
        message.setType(MessageType.NoRegister);
        return message;
    }

    public static Message Register(User user) {
        Message message = new Message();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\52976\\Desktop\\cs209\\chatting\\chatting-server\\src\\main\\resources\\user.csv", true));
            boolean f = true;
            for (int i = 0; i < allClient.size(); i++) {
                if (allClient.get(i).getName().equals(user.getName())) {
                    f = false;
                }
            }
            if (!f) {
                message.setType(MessageType.RegisterFail);
            } else {
                allClient.add(user);
                message.setType(MessageType.RegisterSuccess);
            }
            System.out.println("Message类型 " + message.getType().toString());
            writer.newLine();
            writer.write(user.getName() + "," + user.getPassword());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    public static ArrayList<User> getOnlineUser() {

        ArrayList<User> onlineClient = new ArrayList<>();
        for (int i = 0; i < allClient.size(); i++) {
            if (allClient.get(i).isLogged()) {
                onlineClient.add(allClient.get(i));
            }
        }
        return onlineClient;
    }

    public Socket getSocket() {
        return socket;
    }

    public Socket getOnlineSocket() {
        return onlineSocket;
    }

    public Socket getUpdateSocket() {
        return updateSocket;
    }

    public ObjectOutputStream getOs() {
        return os;
    }

    public ObjectInputStream getIs() {
        return is;
    }

    public ObjectOutputStream getOnOs() {
        return onOs;
    }

    public ObjectInputStream getOnIs() {
        return onIs;
    }

    public ObjectOutputStream getUpOs() {
        return upOs;
    }

    public ObjectInputStream getUpIs() {
        return upIs;
    }

}
