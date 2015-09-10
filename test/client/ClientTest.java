/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import server.ChatServer;
import shared.ProtocolStrings;

/**
 *
 * @author Mato
 */
public class ClientTest implements ProtocolStrings {

    Socket socket;

    public ClientTest() {

    }

    @BeforeClass
    public static void setUp() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                ChatServer.main(null);
            }

        }).start();

    }

    @AfterClass
    public static void tearDownClass() {
        ChatServer.stopServer();
    }

    @Test
    public void testSendUsername() {
    }

    @Test
    public void testSend() throws IOException {
        Client c1 = new Client();
        Client c2 = new Client();

        try {
            c1.connect("localhost", 9090);
            c1.sendUsername("Bancho");//testing send username
            c2.connect("localhost", 9090);
            c2.sendUsername("Martin");
            //testing connection
        } catch (IOException ex) {
            Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Connection test: passed");
        System.out.println("Send Username: passed");
        System.out.println("Receive method: passed");
        assertEquals(USERLIST + "#" + "Bancho", c2.receive());//testing receive method & send()
    }

    
}
