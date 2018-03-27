import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class Channel implements Runnable {

    //File info
    private String file_name;

    //id of peer
    private int port_number;

    //Name of this Thread
	private String name;

    //Ip and Port
    private InetAddress mcast_addr;
    final static String INET_ADDR = "224.0.0.5";
    private int mcast_port;

    //The Backup MulticastSocket
    private MulticastSocket serverSocket;

    private String command;


    //A packet(chunck)
    private DatagramPacket packetToSend;

    //Chat with Backup
    private Chat backup_with_channel;





    public void connect_multicast() {

        try{
        mcast_addr = InetAddress.getByName(INET_ADDR);
        serverSocket = new MulticastSocket(mcast_port);
        serverSocket.joinGroup(mcast_addr);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


  private Message treatData(byte[] incomingData) {
        String string = new String(incomingData);


        String[] parts = string.split("\r\n\r\n");

        String[] header = parts[0].split(" ");

        return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3], Integer.parseInt(header[4]));

  }


	public Channel(String name, InetAddress mcast_addr, int mcast_port, String command, int port_number, Chat backup_with_channel){
		this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.port_number = port_number;
        this.backup_with_channel = backup_with_channel;
	}

    public void send_Message(String message){
        try{

            System.out.println("/////////////////////////////");
            packetToSend = new DatagramPacket(message.getBytes() ,message.getBytes().length, mcast_addr, mcast_port);
            serverSocket.send(packetToSend);

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }



	public void run()  {

        connect_multicast();

        System.out.println("Main Channel Service Enabled!");

        if(command.equals("RECEIVER")) {
            while(true){

                while(true){
                    //Removed print
                    if(!backup_with_channel.getMessage().equals("nada"))
                        break;
                }
                send_Message(backup_with_channel.getMessage());

            }
        }
        else if (command.equals("BACKUP")) {

          try {
            while(true) {
                byte[] incomingData = new byte[64000];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                serverSocket.receive(incomingPacket);

                //add message to array
                backup_with_channel.addMsgInbox(new String(incomingData));

                String string = new String(incomingData);

            }
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }


      }

}
