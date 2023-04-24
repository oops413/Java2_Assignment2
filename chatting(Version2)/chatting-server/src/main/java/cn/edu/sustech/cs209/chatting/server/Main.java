package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Room;
import cn.edu.sustech.cs209.chatting.common.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {   //存所有User对象
    static ArrayList<User> allClient = new ArrayList<>();
    static ArrayList<Room> allRoom = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8919);
        System.out.println("Starting server");
        try (BufferedReader b = Files.newBufferedReader(Paths.get("C:\\Users\\52976\\Desktop\\cs209\\chatting\\chatting-server\\src\\main\\resources\\user.csv"))) {
            String line;
            while ((line = b.readLine()) != null && !line.equals("")) {
                String[] lines = line.split(",");
                String userName = lines[0];
                String pass = lines[1];
                allClient.add(new User(userName, pass));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Server server = new Server(ss);
        server.startServer();
    }
}
