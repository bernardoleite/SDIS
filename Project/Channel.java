import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.channels.FileChannel;
import java.util.Random;


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


    private Random rand = new Random();

    private ArrayOfFiles currentFiles;


    public class Listener_Channel implements Runnable {

        String threadName;

        public Listener_Channel(){
        }

        public void run(){
          try {
            while(true) {
                byte[] incomingData = new byte[64000];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                serverSocket.receive(incomingPacket);

                Message receivedMessage = treatData(incomingData);
                int i = currentFiles.hasChunkStore(receivedMessage.getFileId() + "." + Integer.toString(receivedMessage.getChunkNo()));

                if(receivedMessage.getCommand().equals("STORED") && Integer.parseInt(receivedMessage.getSenderId()) != port_number && i != -1) {
                  currentFiles.chunksStore.get(i).incrementPerceivedReplicationDeg();
                }

                if(receivedMessage.getCommand().equals("DELETE") && Integer.parseInt(receivedMessage.getSenderId()) != port_number) {
                  System.out.println("DELETE CHUNKS");
                  deleteChunks(receivedMessage.getFileId());
                }

            }
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }
    }

    public class Backup_CheckMsg implements Runnable {

        String threadName;

        public Backup_CheckMsg(String name){
            threadName = name;
        }

        public void run(){
            while(true){

                while(true){
                    //Removed print
                    if(!backup_with_channel.getMessage().equals("nada"))
                        break;
                }


                send_Message(backup_with_channel.getMessage());
                try {
                  int j = rand.nextInt(400);

                  Thread.sleep(j);
                } catch (Exception e) {
                  e.printStackTrace();
                }

            }
        }
    }

    public static boolean deleteChunks(final String prefix) {
        boolean success = true;
        String path = "dest";
        try (DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(path), prefix + "*")) {
            for (final Path newDirectoryStreamItem : newDirectoryStream) {
                Files.delete(newDirectoryStreamItem);
            }
        } catch (final Exception e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }


    public void send_Message(String message){
        try{

            packetToSend = new DatagramPacket(message.getBytes() ,message.getBytes().length, mcast_addr, mcast_port);
            serverSocket.send(packetToSend);

        }
        catch(Exception e){
            e.printStackTrace();
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


  private Message treatData(byte[] incomingData) {
        String string = new String(incomingData);


        String[] parts = string.split("\r\n\r\n");

        String[] header = parts[0].split(" ");

        if(header[0].equals("STORED"))
          return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3], Integer.parseInt(header[4]));

        else
          return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3]);
  }


	public Channel(String name, InetAddress mcast_addr, int mcast_port, String command, int port_number, Chat backup_with_channel, ArrayOfFiles currentFiles){
		    this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.port_number = port_number;
        this.backup_with_channel = backup_with_channel;
        this.currentFiles = currentFiles;

	}

  public Channel(String name, InetAddress mcast_addr, int mcast_port, String command, int port_number, Chat backup_with_channel){
        this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.port_number = port_number;
        this.backup_with_channel = backup_with_channel;
  }

  public Channel(String name, InetAddress mcast_addr, int mcast_port, String command, String file_name, int port_number, ArrayOfFiles currentFiles){
        this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.file_name = file_name;
        this.port_number = port_number;
        this.currentFiles = currentFiles;

  }

	public void run()  {

        connect_multicast();

        System.out.println("Main Channel Service Enabled!");

        //Send Store and escuta mc
        if(command.equals("RECEIVER")) {


        //Thread Responsible to, in case of Backup, check Message String and Send Message ----- BACKUP
        Thread backup_check_message = new Thread(new Backup_CheckMsg("backup_check_message"));
        backup_check_message.start();

        Thread listener_channel = new Thread(new Listener_Channel());
        listener_channel.start();

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

        //Send GetChunk
        else if (command.equals("RESTORE")) {


        }

        //Send DELETE
        else if (command.equals("DELETE")) {
          System.out.println(file_name);
          int i = currentFiles.hasFile(file_name);

          if(i == -1) {
            System.out.println("Didn't Backup this file");
            System.exit(2);
          }
          Message message = new Message("DELETE", 1, Integer.toString(port_number), currentFiles.files.get(i).getFileId());

          DatagramPacket deletemsg = new DatagramPacket(message.toString().getBytes() ,message.toString().getBytes().length, mcast_addr, mcast_port);

          for(int j = 0; j < 5; j++) {
            try {
              serverSocket.send(deletemsg);
              Thread.sleep(500);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
          }
        }


      }

}
