import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;


public class Backup implements Runnable {

    //File info
    private String file_name;
    private int replication_deg;
    private String command;
    private int port_number;

    //Name of this Thread
	  private String name;

    //Ip and Port
    private InetAddress mcast_addr;
    final static String INET_ADDR = "224.0.0.3";
    private int mcast_port;

    //The Backup MulticastSocket
    private MulticastSocket serverSocket;

    //Source and Dest
    private String destinationPath = "dest/";

    //A packet(chunck)
    private DatagramPacket chunk;

    private byte[] data;

    private ArrayList<Chunk> chunksToSend = new ArrayList<Chunk>();

    //Chat with Main Channel
    private Chat backup_with_channel;

    private Random rand = new Random();


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

    public static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

    public void read_file()  {

      try{
        RandomAccessFile aFile = new RandomAccessFile
                (file_name, "r");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(64000);
        int j = 1;
        while(inChannel.read(buffer) > 0)
        {
            buffer.flip();

            byte[] arr = new byte[buffer.remaining()];

            buffer.get(arr, 0, arr.length);
            String str = removeExtension(file_name);


            chunksToSend.add(new Chunk(str, j++, replication_deg, arr));
            buffer.clear(); // do something with the data and clear/compact it.
        }
        inChannel.close();
        aFile.close();
      }
      catch(Exception e){
        e.printStackTrace();
      }

    }

   public void send_chunk(int i) {

        try{
            Message message = new Message("PUTCHUNK", 1, Integer.toString(port_number), chunksToSend.get(i).getFileId(), chunksToSend.get(i).getChunNo(), chunksToSend.get(i).getReplication_Deg(), new String(chunksToSend.get(i).getBody()));
            System.out.println(message.toString().getBytes().length);
            System.out.println("??????????????????????????????????");
            chunk = new DatagramPacket(message.toString().getBytes() ,message.toString().getBytes().length, mcast_addr, mcast_port);
            serverSocket.send(chunk);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


	public Backup(String name, InetAddress mcast_addr, int mcast_port, String command, String file_name, int replication_deg, int port_number, Chat backup_with_channel){
		this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.file_name = file_name;
        this.replication_deg = replication_deg;
        this.command = command;
        this.port_number = port_number;
        this.backup_with_channel = backup_with_channel;
	}

  public Backup(String name, InetAddress mcast_addr, int mcast_port, String command, Chat backup_with_channel, int port_number){
		this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.backup_with_channel = backup_with_channel;
        this.port_number = port_number;
	}

  private void writeBytesToFileNio(Message receivedMessage) {

        try {
            Path path = Paths.get(destinationPath + receivedMessage.getFileId() + "." + receivedMessage.getChunNo() + ".txt");

            FileOutputStream out = new FileOutputStream(destinationPath + receivedMessage.getFileId() + "." + receivedMessage.getChunNo() + ".txt");

            out.write(receivedMessage.getBody().getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

  }

  private Message treatData(byte[] incomingData) {
        String string = new String(incomingData);


        String[] parts = string.split("\r\n\r\n");

        String[] header = parts[0].split(" ");

        return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3], Integer.parseInt(header[4]), Integer.parseInt(header[5]), parts[1]);

  }

	public void run()  {

		    System.out.println("Backup Service Enabled!");

        connect_multicast();

        if(command.equals("BACKUP")) {

          read_file();

          for(int i = 0; i < chunksToSend.size(); i++) {
            //check if replication degreee is equal to store messages on chat inbox
            int j = 1000;
            do {
                backup_with_channel.clearInbox();
                send_chunk(i);
                try {
                  Thread.sleep(j);
                  System.out.println("J: " +j);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                j = j*2;
            } while(backup_with_channel.getInbox().size() < replication_deg);
            System.out.println("Sending next CHUNK!");
          }
          System.out.println("All Chunks Sended");
          System.exit(1);
        }
        else {
          try {
            while(true) {
                byte[] incomingData = new byte[64000];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

                serverSocket.receive(incomingPacket);

                Message receivedMessage = treatData(incomingData);
                backup_with_channel.access(false);
                Message msg = new Message("STORED", 1, Integer.toString(port_number), receivedMessage.getFileId(), receivedMessage.getChunNo());
                System.out.println("SET Message");
                backup_with_channel.setMessage(msg.toString());
                backup_with_channel.access(true);
                writeBytesToFileNio(receivedMessage);
                backup_with_channel.setMessage("nada");

                System.out.println("Chunk received and saved to HDD!");

                try {
                  int j = rand.nextInt(400);

                  Thread.sleep(j);
                } catch (Exception e) {
                  e.printStackTrace();
                }
            }
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }


      }

}
