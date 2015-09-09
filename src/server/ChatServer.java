package server;

import server.exceptions.NonExistantReceiverException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class ChatServer implements ProtocolStrings {

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");
    private ConcurrentHashMap<String, ClientHandler> users;

    public static void stopServer() {
        keepRunning = false;
    }

    private void runServer() {
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");
        users = new ConcurrentHashMap<String, ClientHandler>();

        Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Sever started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Anonymous client has connected");
                ClientHandler client = new ClientHandler(this, socket);
                new Thread(client).start();
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeClient(String userName){
        users.remove(userName);
    }
    
    public void addClient(String userName, ClientHandler client){
        users.put(userName, client);
    }
    
    
    public synchronized void sendUserListToAll(){
        ArrayList<String> userList = new ArrayList<String>();
        for (String userName : users.keySet()) {
            userList.add(userName);
        }
        
        for (ClientHandler ch : users.values()) {
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
        //Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Logged-in users: " + users.size());
    }
    
    
    public synchronized void sendMsg(String sender, String receivers, String msg) throws NonExistantReceiverException{
        if(!receivers.equals("*")){
            String[] splitted = receivers.split(",");
            for (String receiver : splitted) {
                if (!users.containsKey(receiver)) {
                    throw new NonExistantReceiverException(receiver);
                }
                PrintWriter output = users.get(receiver).getWriter();
                output.println(MSG + "#" + sender + "#" + msg);
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, sender + " sent the message \"" + msg + "\" to " + receivers);
            }
        }else{
            for (ClientHandler ch : users.values()) {
                if (!ch.getUserName().equals(sender)) {
                    ch.getWriter().println(MSG + "#" + sender + "#" + msg);
                }
            }
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, sender + " sent the message \"" + msg + "\" to everyone");
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
