package compile;

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

    private String destinationPath = "restore/";


    //files whose had been BACK UP
    private ArrayOfFiles currentFiles;

    //array of GETCHUNKs used to send to Chat
    private ArrayList<String> getchunks;

    private ArrayList<Message> chunkmsgs = new ArrayList<Message>();
    private ArrayList<Message> restoredChunk = new ArrayList<Message>();

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
                Message receivedMessage = treatData(incomingPacket);
                if(Integer.parseInt(receivedMessage.getSenderId()) != port_number)
                  chunkmsgs.add(receivedMessage);
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

  public Restore(String name, InetAddress mcast_addr, int mcast_port, String command, int port_number, Chat restore_with_channel, ArrayOfFiles currentFiles) {
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
                  getchunks.add(msg.toString());
                }
            }

        }
    }

    public void send_GETCHUNK_array_toChat() {
        restore_with_channel.setGetChunks(getchunks);
    }

    private Message treatData(DatagramPacket incomingPacket) {
          String string = new String(incomingPacket.getData());


          String[] parts = string.split("\r\n\r\n");
          byte[] body = extractBody(incomingPacket);
          String[] header = parts[0].split(" ");
          System.out.println(body);
          return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3], Integer.parseInt(header[4]), body);

    }

    private byte[] extractBody(DatagramPacket incomingPacket) {
      ByteArrayInputStream stream = new ByteArrayInputStream(incomingPacket.getData());
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(stream));

      String line = null;
      int headerLinesLengthSum = 0;
      int numLines = 0;
      byte[] body;
      String crlf = "/r/n";
      do {
        try {
          line = reader.readLine();

          headerLinesLengthSum += line.length();

          numLines++;
        } catch (IOException e) {
          e.printStackTrace();
        }
      } while (!line.isEmpty());

      int bodyStartIndex = headerLinesLengthSum + numLines + 2;

      body = Arrays.copyOfRange(incomingPacket.getData(), bodyStartIndex, incomingPacket.getLength());
      return body;
    }

    public void send_Message(Message message){
        try{
            byte[] send = concatBytes(message.toString().getBytes(), message.getBody());

            packetToSend = new DatagramPacket(send, send.length, mcast_addr, mcast_port);
            serverSocket.send(packetToSend);

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static byte[] concatBytes(byte[] a, byte[] b) {
          int aLen = a.length;
          int bLen = b.length;

          byte[] c = new byte[aLen + bLen];

          System.arraycopy(a, 0, c, 0, aLen);
          System.arraycopy(b, 0, c, aLen, bLen);

          return c;
    }

    public void write_file(byte[] file_body) {
      try {

          FileOutputStream out = new FileOutputStream(destinationPath + file_name);

          out.write(file_body);
          out.close();
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    public boolean checkChunkMsgs(Message stringmsg) {
        for (int i = 0; i < chunkmsgs.size(); i++) {

          if(chunkmsgs.get(i).getFileId().equals(stringmsg.getFileId()) && chunkmsgs.get(i).getChunkNo() == stringmsg.getChunkNo()) {
            return true;
          }
        }
        return false;
    }


    public boolean hasRestoredChunk(Message stringmsg) {
        for (int i = 0; i < restoredChunk.size(); i++) {

          if(restoredChunk.get(i).getChunkNo() == stringmsg.getChunkNo()) {
            return true;
          }
        }
        return false;
    }


	  public void run() {

        connect_multicast();

        System.out.println("Restore Channel Service Enabled!");

        //Here I will be waiting for Chunks to come
        if(command.equals("RESTORE")) {

            //prepare GETCHUNKS array
            make_GETCHUNK_array();
            int number_of_chunks = getchunks.size();
            int size = 0;
            byte[] allBodies = new byte[0];
            //send previous array to Chat
            send_GETCHUNK_array_toChat();
            try {
              while(true) {
                byte[] incomingData = new byte[65000];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                serverSocket.receive(incomingPacket);

                Message receivedMessage = treatData(incomingPacket);
                if(!hasRestoredChunk(receivedMessage)) {
                  size++;
                  allBodies = concatBytes(allBodies, receivedMessage.getBody());
                  restoredChunk.add(receivedMessage);
                }

                if(size == number_of_chunks) {
                  break;
                }
              }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            write_file(allBodies);
            System.exit(3);
        }

        //I am PEER-RECEIVER and I will send a requested chunk through here
        else if (command.equals("RECEIVER")) {

            Thread listener_restore = new Thread(new Listener_Restore());
            listener_restore.start();

            System.out.println("Ready to Receive in Restore");

            Message msg;

            while(true) {

              while(true){
                if(!restore_with_channel.getMsgChunk().getCommand().equals("nada")) {
                  msg = restore_with_channel.getMsgChunk();
                  break;
                }
              }
              try {
                int j = rand.nextInt(400);

                Thread.sleep(j);
              } catch (Exception e) {
                e.printStackTrace();
              }


              if(!checkChunkMsgs(msg)) {
                send_Message(msg);
                chunkmsgs.add(msg);

              }

            }

        }

      }

}
