package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class ChatServer implements ProtocolStrings {

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");
    private ConcurrentHashMap<String, ClientHandler> clients;

    public static void stopServer() {
        keepRunning = false;
    }

    private void runServer() {
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");
        clients = new ConcurrentHashMap<String, ClientHandler>();

        Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Sever started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Connected to a client");
                String tempUserName = "user" + new Random().nextInt(10000);;
                ClientHandler client = new ClientHandler(this, socket);
                clients.put(tempUserName, client);
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Connected clients: " + clients.size());
                new Thread(client).start();
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeClient(String userName){
        clients.remove(userName);
    }
    
    public void addClient(String userName, ClientHandler client){
        clients.put(userName, client);
    }
    
    public synchronized void sendUserListToAll(){
        ArrayList<String> userList = new ArrayList<String>();
        for (String userName : clients.keySet()) {
            userList.add(userName);
        }
        String userNames = "";
        for (ClientHandler ch : clients.values()) {
            PrintWriter output = ch.getWriter();
            for (String userName : userList) {
                if (!userName.equals(ch.getUserName())) {
                    userNames += userName + ",";
                }
            }
            output.println(USERLIST + "#" + userNames);
        }
    }
    
    public synchronized void sendMsg(String receivers, String msg){
        
    }

    public static void main(String[] args) {
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, ChatServer.class.getName());
        try {
            new ChatServer().runServer();
        } finally {
            Utils.closeLogger(ChatServer.class.getName());
        }
    }
}
