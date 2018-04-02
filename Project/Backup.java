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
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

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

    //total chunks of a file
    private ArrayList<Chunk> chunksToSend = new ArrayList<Chunk>();

    //Chat with Main Channel
    private Chat backup_with_channel;

    private Random rand = new Random();



    //save Files of backup
    private ArrayOfFiles currentFiles;

    String filebin;


    private String file_id;


    public class Backup_CheckEmg implements Runnable {

        String threadName;

        public Backup_CheckEmg(String name){
            threadName = name;
        }

        public void run(){
            while(true){

                while(true){
                    //Removed print
                    if(!backup_with_channel.getEmergency().equals("nada")) {
                        break;
                    }
                }

                send_Message(backup_with_channel.getEmergencyPutChunk());
                backup_with_channel.setEmergency("nada");
                try {
                  int j = rand.nextInt(400);

                  Thread.sleep(j);
                } catch (Exception e) {
                  e.printStackTrace();
                }

            }
        }
    }

    public void send_Message(byte[] message){
        try{
            System.out.println("I will send this message: "+message.length);
            DatagramPacket packetToSend = new DatagramPacket(message ,message.length, mcast_addr, mcast_port);
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


        try {

            RandomAccessFile aFile = new RandomAccessFile(file_name, "r");
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(64000);
            int j = 1;
            while(inChannel.read(buffer) > 0)	{
               buffer.flip();
               byte[] arr = new byte[buffer.remaining()];
               buffer.get(arr, 0, arr.length);
               chunksToSend.add(new Chunk(file_id, j++, replication_deg, arr));
               buffer.clear(); // do something with the data and clear/compact it.
      			}
        } catch(Exception e){
          e.printStackTrace();
        }

    }

    public void send_chunk(int i) {

        try{
            Message message = new Message("PUTCHUNK", 1, Integer.toString(port_number), chunksToSend.get(i).getFileId(), chunksToSend.get(i).getChunkNo(), chunksToSend.get(i).getReplication_Deg(), chunksToSend.get(i).getBody());
            byte[] send = concatBytes(message.toString().getBytes(), message.getBody());
            chunk = new DatagramPacket(send, send.length, mcast_addr, mcast_port);

            serverSocket.send(chunk);
            System.out.println("SIZE OF BODY: " + message.getBody().length);
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



	  public Backup(String name, InetAddress mcast_addr, int mcast_port, String command, String file_name, int replication_deg, int port_number, Chat backup_with_channel, ArrayOfFiles currentFiles){
		    this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.file_name = file_name;
        this.replication_deg = replication_deg;
        this.command = command;
        this.port_number = port_number;
        this.backup_with_channel = backup_with_channel;
        this.currentFiles = currentFiles;
    }

    public Backup(String name, InetAddress mcast_addr, int mcast_port, String command, Chat backup_with_channel, int port_number, ArrayOfFiles currentFiles){
		    this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
        this.backup_with_channel = backup_with_channel;
        this.port_number = port_number;
        this.currentFiles = currentFiles;

	}

  private void writeBytesToFileNio(Message receivedMessage) {

        try {

            Path path = Paths.get(destinationPath + receivedMessage.getFileId() + "." + receivedMessage.getChunkNo());

            FileOutputStream out = new FileOutputStream(destinationPath + receivedMessage.getFileId() + "." + receivedMessage.getChunkNo());

            out.write(receivedMessage.getBody());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

  }

    private void codify_fileId() {
      try {
        Path file = Paths.get(file_name);
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        Sha sha = new Sha();
        file_id = sha.hash256(attr.toString());
      } catch(Exception e) {
        e.printStackTrace();
      }
    }


  private Message treatData(DatagramPacket incomingPacket) {
        String string = new String(incomingPacket.getData());


        String[] parts = string.split("\r\n\r\n");
        byte[] body = extractBody(incomingPacket);
        System.out.println("SIZE OF BODY: " +body.length);
        String[] header = parts[0].split(" ");

        return new Message(header[0], Integer.parseInt(header[1]), header[2], header[3], Integer.parseInt(header[4]), Integer.parseInt(header[5]), body);

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

    body = Arrays.copyOfRange(incomingPacket.getData(), bodyStartIndex,
        incomingPacket.getLength());
        return body;
  }

  public void serialize_Object(){

    filebin = "data.bin";
    try{
      ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filebin));
      os.writeObject(currentFiles);
      os.close();
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }

  }
  public boolean hasSpace(int ChunkSize){
        double size = (double) ChunkSize;
        double thousand = 1000.0;
        if((currentFiles.currentSpace + size/thousand) < currentFiles.maximumSpace)
            return true;
        else
            return false;
  }

	public void run()  {

		    System.out.println("Backup Service Enabled!");

        connect_multicast();

        //This thread will be listening Chat with Channel and in Case of Emergency PutChunk sends it.
        Thread backup_check_emergency = new Thread(new Backup_CheckEmg("backup_check_emergency"));
        backup_check_emergency.start();

        if(command.equals("BACKUP")) {

          codify_fileId();
          read_file();

          currentFiles.files.add(new FileInfo(file_name,file_id, replication_deg ));

          for(int i = 0; i < chunksToSend.size(); i++) {
            //check if replication degreee is equal to store messages on chat inbox
            int j = 1000;
            do {

                backup_with_channel.clearInbox();
                send_chunk(i);
                try {
                  Thread.sleep(j);
                } catch (Exception e) {
                  e.printStackTrace();
                }
                j = j*2;
                for(int k = 0; k < backup_with_channel.getInbox().size(); k++) {
                  System.out.println(backup_with_channel.getInbox().get(k).trim());
                }
            } while(backup_with_channel.getInbox().size() < replication_deg);
            currentFiles.files.get(currentFiles.files.size()-1).addChunkInfo(new ChunkInfo(file_id + "." + Integer.toString(i+1), backup_with_channel.getInbox().size(), replication_deg, chunksToSend.get(i).getBody().length));
            System.out.println("Sending next CHUNK!");
          }
          System.out.println("All Chunks Sended");

          serialize_Object();

          System.exit(1);
          }

        else {
          try {
            while(true) {
                byte[] incomingData = new byte[64500];

                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

                serverSocket.receive(incomingPacket);

                Message receivedMessage = treatData(incomingPacket);

                int i = currentFiles.hasChunkStore(receivedMessage.getFileId() + "." + Integer.toString(receivedMessage.getChunkNo()));

                //if chunk doesn't exists and there is space to save it, accept chunk
                if(i == -1 && hasSpace(receivedMessage.getBody().length))
                  currentFiles.chunksStore.add(new ChunkInfo(receivedMessage.getFileId() + "." + Integer.toString(receivedMessage.getChunkNo()), 1, receivedMessage.getReplication_Deg(), receivedMessage.getBody().length));

                //if chunk doesn't exists and there is no space, do not accept chunk
                if(!(i == -1 && !hasSpace(receivedMessage.getBody().length))){

                    Message msg = new Message("STORED", 1, Integer.toString(port_number), receivedMessage.getFileId(), receivedMessage.getChunkNo());
                    backup_with_channel.setMessage(msg.toString());
                    writeBytesToFileNio(receivedMessage);
                    System.out.println("Chunk received and saved to HDD!");
                    serialize_Object();

                }

                backup_with_channel.setMessage("nada");


            }
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }


      }

}
