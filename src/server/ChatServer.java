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
                ClientHandler client = new ClientHandler(this, socket, tempUserName);
                clients.put(tempUserName, client);
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
        
        for (ClientHandler ch : clients.values()) {
            PrintWriter output = ch.getWriter();
            String userNames = "";
            for (String userName : userList) {
                if (!userName.equals(ch.getUserName())) {
                    userNames += userName + ",";
                }
            }
            if (userNames.length() > 0) {
                userNames = userNames.substring(0, userNames.length() - 1); //removing the last comma from the string
            }
            output.println(USERLIST + "#" + userNames);
        }
        Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Connected users: " + clients.size());
    }
    
    
    public synchronized void sendMsg(String sender, String receivers, String msg){
        String[] splitted = receivers.split(",");
        for (String receiver : splitted) {
            PrintWriter output = clients.get(receiver).getWriter();
            output.println(MSG + "#" + sender + "#" + msg);
        }
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
