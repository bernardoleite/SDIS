import java.util.*;
import java.io.*;
import java.net.*;


public class Backup implements Runnable {

    //Name of this Thread
	private String name;

    //Ip and Port
    private InetAddress mcast_addr;
    final static String INET_ADDR = "224.0.0.3";
    private int mcast_port;

    //The Backup MulticastSocket
    private MulticastSocket serverSocket;

    //handles a file
    private FileEvent event = null;

    //Source and Dest
    private String sourceFilePath = "/Users/bernardo/Desktop/orig/ola.rtf";
    private String destinationPath = "/Users/bernardo/Desktop/dest/";

    //A packet(chunck)
    private DatagramPacket chunk;

    private byte[] data;
    

    public FileEvent getFileEvent() {
        FileEvent fileEvent = new FileEvent();
        String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());
        String path = sourceFilePath.substring(0, sourceFilePath.lastIndexOf("/") + 1);
        fileEvent.setDestinationDirectory(destinationPath);
        fileEvent.setFilename(fileName);
        fileEvent.setSourceDirectory(sourceFilePath);
        File file = new File(sourceFilePath);
        if (file.isFile()) {
            try {
                DataInputStream diStream = new DataInputStream(new FileInputStream(file));
                long len = (int) file.length();
                byte[] fileBytes = new byte[(int) len];
                int read = 0;
                int numRead = 0;
                while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
                    read = read + numRead;
                }
                fileEvent.setFileSize(len);
                fileEvent.setFileData(fileBytes);
                fileEvent.setStatus("Success");
            } catch (Exception e) {
                e.printStackTrace();
                fileEvent.setStatus("Error");
            }
        } else {
            System.out.println("path specified is not pointing to a file");
            fileEvent.setStatus("Error");
        }
        return fileEvent;
    }

    public void prepare_file() {

        try{
        byte[] incomingData = new byte[1024];
        event = getFileEvent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(event);
        data = outputStream.toByteArray(); 
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

    public void send_file() {

        try{
        chunk = new DatagramPacket(data ,data.length, mcast_addr, mcast_port);
        serverSocket.send(chunk);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


	public Backup(String name, InetAddress mcast_addr, int mcast_port){
		this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
	}

	public void run()  {

		System.out.println("Backup Service Enabled!");

        connect_multicast();

        prepare_file();

        send_file();

        System.out.println("Server sent packet with a chunck!");

		
	}
}