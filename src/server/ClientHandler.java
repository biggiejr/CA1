/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

/**
 *
 * @author Bancho
 */
public class ClientHandler extends Thread implements ProtocolStrings {
    
    private ChatServer server;
    private Socket socket;
    private PrintWriter output;
    private String userName;
    
    public ClientHandler(ChatServer server, Socket socket, String userName){
        this.server = server;
        this.socket = socket;
        this.userName = userName;
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void handleClient() throws IOException {
        Scanner input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
        
        String message;
        do {            
            try {
                message = input.nextLine(); //IMPORTANT blocking call
                handleMsg(message);
            } catch (NoSuchElementException e) {
                message = DISCONNECTED;
            }
        } while (!message.equals(STOP) && !message.equals(DISCONNECTED));
        
        
        if (!message.equals(DISCONNECTED)) {
            output.println(STOP); //Echo the stop message back to the client for a nice closedown
        }
        
        socket.close();
        server.removeClient(userName);
        server.sendUserListToAll();
        
        if (message.equals(DISCONNECTED)) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Closed a Connection Abruptly");
        }else{
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Closed a Connection");
        }
    }
    
    
    private void handleMsg(String message){
        String[] splitted = message.split("#");
        String protocol = splitted[0];
        switch(protocol){
            case USER:
                server.removeClient(userName);
                userName = splitted[1];
                server.addClient(userName, this);
                server.sendUserListToAll();
                break;
            case MSG:
                String receivers = splitted[1];
                String msg = splitted[2];
                server.sendMsg(userName, receivers, msg);
                break;
            case STOP:
                //nothing here, I guess
                break;
        }
    }
    
    
    public PrintWriter getWriter(){
        return output;
    }
    
    public String getUserName(){
        return userName;
    }
    
}
