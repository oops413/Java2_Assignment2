package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {
    private final ServerSocket ss;
    static Socket socket;
    static Socket onlineSocket;
    static Socket updateSocket;
    static Map<String, ServerThread> allThread = new LinkedHashMap<String, ServerThread>();

    public Server(ServerSocket s) {
        ss = s;
    }

    //TODO:退出后allThread也要删掉
    public void startServer() throws IOException {
        while (true) {
            socket = ss.accept();
            onlineSocket = ss.accept();
            updateSocket = ss.accept();
            ServerThread st = new ServerThread(socket, onlineSocket, updateSocket);
            st.start();
        }
    }

    public static Map<String, ServerThread> getAllThread() {
        return allThread;
    }

    public Socket getSocket() {
        return socket;
    }

}
