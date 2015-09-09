package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class Client extends Observable implements Runnable, ProtocolStrings {

    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;
    private String receivers;
    private String username;

    public void connect(String address, int port) throws UnknownHostException, IOException {
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendUsername(String username) {
        output.println(username);
    }

    public void send(String msg, String receivers) {
        output.println(ProtocolStrings.MSG + "#" + receivers + "#" + msg);

    }

    public void stop() throws IOException {
        output.println(ProtocolStrings.STOP);
    }

    public void receive() {
        String msg = input.nextLine();
        if (msg.contains(ProtocolStrings.USERLIST + "#")) {
            String[] divideString = msg.split("#");
            String sender = divideString[1];
            String[] divideNames = sender.split(",");
                List UserList = new ArrayList<String>();
            if (divideNames.length > 1) {
                for (String name : divideNames) {
                    UserList.add(name);
                }
                setChanged();
                notifyObservers(UserList);
            } else {
                UserList.add(sender);
                setChanged();
                notifyObservers(UserList);
            }
        }
        
        if(msg.contains(ProtocolStrings.MSG + "#")){
          
            
            setChanged();
            notifyObservers(msg);
        }
        if (msg.equals(ProtocolStrings.STOP)) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String[] args) {
        int port = 9090;
        String ip = "localhost";
        if (args.length == 2) {
            port = Integer.parseInt(args[0]);
            ip = args[1];
        }
        try {
            Client tester = new Client();
            tester.connect(ip, port);

            //Important Blocking call         
            tester.stop();
            //System.in.read();      
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            String msg = input.nextLine();
            receive();

        }
    }
}
