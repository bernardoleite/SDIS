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
    private ArrayList<FileInfo> files;

    //array of GETCHUNKs used to send to Chat
    private ArrayList<String> getchunks;


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

	public Restore(String name, InetAddress mcast_addr, int mcast_port, String command, String file_name, int port_number, Chat restore_with_channel, ArrayList<FileInfo> files){
		this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.file_name = file_name;
        this.port_number = port_number;
        this.restore_with_channel = restore_with_channel;
        this.files = files;
	}

    public void make_GETCHUNK_array(){

        getchunks = new ArrayList<String>();

        for( int i = 0 ; i < files.size(); i++){

            if(files.get(i).getFileId().equals("FileId Pretendido")){
                for(int j = 0; j < files.get(i).getChunksInfo().size(); j++)
                {   
                Message msg = new Message("GETCHUNK", 1, Integer.toString(port_number), files.get(i).getFileId(), files.get(i).getChunksInfo().get(j).getId());
                getchunks.add(msg.toString());
                }
            } 

        }
    }

    public void send_GETCHUNK_array_toChat(){
        restore_with_channel.setGetChunks(getchunks);
    }


	public void run()  {

        connect_multicast();

        System.out.println("Restore Channel Service Enabled!");

        //Here I will be waiting for Chunks to come
        if(command.equals("RESTORE")) {

            //prepare GETCHUNKS array
            make_GETCHUNK_array();

            //send previous array to Chat
            send_GETCHUNK_array_toChat();


            System.out.println("Ready to Receive the Chunks I requested");

        }

        //I am PEER-RECEIVER and I will send a requested chunk through here
        else if (command.equals("RECEIVER")) {

            System.out.println("Ready to Receive in Restore");
        }


      }

}
