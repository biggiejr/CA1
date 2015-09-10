/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.exceptions.EmptyUserNameSuppliedException;
import server.exceptions.InvalidProtocolException;
import server.exceptions.NonExistantReceiverException;
import server.exceptions.NotLoggedInException;
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
    private boolean loggedIn;
    
    public ClientHandler(ChatServer server, Socket socket){
        this.server = server;
        this.socket = socket;
        loggedIn = false;
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
        
        String message = "";
        do {            
            try {
                message = input.nextLine(); //IMPORTANT blocking call
                handleMsg(message);
            } catch (NoSuchElementException e) {
                message = DISCONNECTED;
            } catch (InvalidProtocolException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "Received invalid protocol. Server received: \"" + ex.getMessage() + "\"", ex);
            } catch (NotLoggedInException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "Client attempted to send a msg without having logged-in first", ex);
            } catch (EmptyUserNameSuppliedException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "Client suppied an empty user name", ex);
            } catch (NonExistantReceiverException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.INFO, "Client attempted to send a msg to a non-existant user: " + ex.getMessage(), ex);
            }
        } while (!message.equals(STOP) && !message.equals(DISCONNECTED));
        
        
        if (message.equals(STOP)) {
            output.println(STOP); //Echo the stop message back to the client for a nice closedown
        }
        
        socket.close();
        if (loggedIn) {
            server.removeClient(userName);
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, userName + " has disconnected");
        }else{
            Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, "Anonymous client has disconnected");
        }
        server.sendUserListToAll();
    }
    
    
    private void handleMsg(String message) throws InvalidProtocolException, NotLoggedInException, EmptyUserNameSuppliedException, NonExistantReceiverException{
        String[] splitted = message.split("#");
        String protocol = splitted[0];
        switch(protocol){
            case USER:
                if (splitted.length <= 1) {
                    throw new EmptyUserNameSuppliedException();
                }
                userName = splitted[1];
                server.addClient(userName, this);
                Logger.getLogger(ChatServer.class.getName()).log(Level.INFO, userName + " has logged-in");
                server.sendUserListToAll();
                loggedIn = true;
                break;
            case MSG:
                if (splitted.length <= 2) {
                    throw new InvalidProtocolException(message);
                }
                if (loggedIn == false) {
                    throw new NotLoggedInException();
                }
                String receivers = splitted[1];
                String msg = splitted[2];
                server.sendMsg(userName, receivers, msg);
                break;
            case STOP:
                //nothing here, I guess
                break;
            default:
                throw new InvalidProtocolException(message);
        }
    }
    
    
    public PrintWriter getWriter(){
        return output;
    }
    
    public String getUserName(){
        return userName;
    }
    
}
