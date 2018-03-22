import java.util.*;
import java.io.*;
import java.net.*;


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

    //handles a file
    private FileEvent event = null;

    //Source and Dest
    private String destinationPath = "C:/Users/jsaraiva/github/SDIS/Project/dest/";

    //A packet(chunck)
    private DatagramPacket chunk;

    private byte[] data;

    //Receiver
    private FileEvent fileEvent = null;

    public FileEvent getFileEvent() throws FileNotFoundException {
        FileEvent fileEvent = new FileEvent();
        String fileName = file_name.substring(file_name.lastIndexOf("/") + 1, file_name.length());
        String path = file_name.substring(0, file_name.lastIndexOf("/") + 1);

        fileEvent.setFilename(fileName);
        File file = new File(file_name);

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


	public Backup(String name, InetAddress mcast_addr, int mcast_port, String command, String file_name, int replication_deg, int port_number ){
		    this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.file_name = file_name;
        this.replication_deg = replication_deg;
        this.command = command;
        this.port_number = port_number;
	}

  public Backup(String name, InetAddress mcast_addr, int mcast_port, String command){
		    this.name = name;
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.command = command;
	}

	public void run()  {

		System.out.println("Backup Service Enabled!");

        connect_multicast();

        if(command.equals("SEND")) {
          prepare_file();

          send_file();
          System.out.println("Server sent packet with a chunck!");

        }
        else {
          try {
            byte[] incomingData = new byte[1024 * 1000 * 50];

            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

            serverSocket.receive(incomingPacket);

            treatData(incomingPacket);

            System.out.println("File received and saved to HDD!");
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }


      }

      public void createAndWriteFile() {
          String outputFile = destinationPath + fileEvent.getFilename();
          if (!new File(destinationPath).exists()) {
              new File(destinationPath).mkdirs();
          }
          File dstFile = new File(outputFile);
          FileOutputStream fileOutputStream = null;
          try {
              fileOutputStream = new FileOutputStream(dstFile);
              fileOutputStream.write(fileEvent.getFileData());
              fileOutputStream.flush();
              fileOutputStream.close();
              System.out.println("Output file : " + outputFile + " is successfully saved ");

          } catch (FileNotFoundException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          }

      }

      public void treatData(DatagramPacket incomingPacket) throws Exception{
          byte[] data = incomingPacket.getData();
          ByteArrayInputStream in = new ByteArrayInputStream(data);
          ObjectInputStream is = new ObjectInputStream( in );
          fileEvent = (FileEvent) is.readObject();
          if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
              System.out.println("Some issue happened while packing the data @ client side");
              System.exit(0);
          }
        createAndWriteFile(); // writing the file to hard disk
      }
}
