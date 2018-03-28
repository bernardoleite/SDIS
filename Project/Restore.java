import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;


public class Restore implements Runnable {

    //File info
    private String file_name;

    //id of peer
    private int port_number;

    //Name of this Thread
	private String name;

    //Ip and Port
    private InetAddress mcast_addr;
    final static String INET_ADDR = "224.0.0.6";
    private int mcast_port;

    //The Backup MulticastSocket
    private MulticastSocket serverSocket;

    private String command;


    //A packet(chunck)
    private DatagramPacket packetToSend;

    //Chat with Main Channel
    private Chat restore_with_channel;


    private Random rand = new Random();

    //files whose had been BACK UP
    private ArrayOfFiles currentFiles;

    //array of GETCHUNKs used to send to Chat
    private ArrayList<String> getchunks;


    private boolean receivedChunk = false;

    private Message getchunkmsg;

    public class Listener_Restore implements Runnable {

        String threadName;

        public Listener_Restore(){
        }

        public void run(){
          try {
            while(true) {
                byte[] incomingData = new byte[64000];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                serverSocket.receive(incomingPacket);

                Message receivedMessage = treatData(incomingData);

                if(getchunkmsg != null && receivedMessage.getCommand().equals("CHUNK") && getchunkmsg.getFileId().equals(receivedMessage.getFileId()) && getchunkmsg.getChunkNo() == receivedMessage.getChunkNo()) {
                    receivedChunk = true;
                }

            }
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }
    }

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

	public Restore(String name, InetAddress mcast_addr, int mcast_port, String command, String file_name, int port_number, Chat restore_with_channel, ArrayOfFiles currentFiles) {
		    this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.file_name = file_name;
        this.port_number = port_number;
        this.restore_with_channel = restore_with_channel;
        this.currentFiles = currentFiles;
	}

    public void make_GETCHUNK_array() {

        getchunks = new ArrayList<String>();

        for( int i = 0 ; i < currentFiles.files.size(); i++){

            if(currentFiles.files.get(i).getPathName().equals(file_name)) {
                for(int j = 0; j < currentFiles.files.get(i).getChunksInfo().size(); j++) {

                  String[] parts = currentFiles.files.get(i).getChunksInfo().get(j).getId().split("\\.");

                  Message msg = new Message("GETCHUNK", 1, Integer.toString(port_number), parts[0], Integer.parseInt(parts[1]));
                  System.out.println(msg.toString());
                  System.out.println();
                  getchunks.add(msg.toString());
                }
            }

        }
    }

    public void send_GETCHUNK_array_toChat() {
        restore_with_channel.setGetChunks(getchunks);
    }

    private Message treatData(byte[] incomingData) {
          String string = new String(incomingData);


          String[] parts = string.split("\r\n\r\n");

          String[] header = parts[0].split(" ");

          return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3], Integer.parseInt(header[4]), parts[1]);

    }

	public void run() {

        connect_multicast();

        System.out.println("Restore Channel Service Enabled!");

        //Here I will be waiting for Chunks to come
        if(command.equals("RESTORE")) {

            //prepare GETCHUNKS array
            make_GETCHUNK_array();

            //send previous array to Chat
            send_GETCHUNK_array_toChat();
            try {
              while(true) {
                byte[] incomingData = new byte[64000];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

                serverSocket.receive(incomingPacket);

                Message receivedMessage = treatData(incomingData);

                System.out.println(receivedMessage.toString());
              }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            System.out.println("Ready to Receive the Chunks I requested");

        }

        //I am PEER-RECEIVER and I will send a requested chunk through here
        else if (command.equals("RECEIVER")) {

            Thread listener_restore = new Thread(new Listener_Restore());
            listener_restore.start();
/*
            while(true) {
              //ve se no chat getChunkmsg
              //atualiza getchunkmsg e receivedChunk
              //sleep
              //se receivedChunk == false envia chunk
              //senao nao faz nada
              //set chat msg to nada
            }*/

            System.out.println("Ready to Receive in Restore");
        }


      }

}
