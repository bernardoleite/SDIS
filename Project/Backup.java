import java.util.*;
import java.io.*;
import java.net.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


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
    private String destinationPath = "C:/Users/jsaraiva/github/SDIS/Project/dest/";

    //A packet(chunck)
    private DatagramPacket chunk;

    private byte[] data;



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

    public void read_file()  {

      try{
      FileInputStream fileInputStream = null;
      System.out.println(file_name);
      File file = new File("/Users/bernardo/Desktop/" + file_name);
      data = new byte[(int) file.length()];

      //read file into bytes[]
      fileInputStream = new FileInputStream(file);
      fileInputStream.read(data);
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

  private void writeBytesToFileNio(byte[] bFile, String fileDest) {

        try {
            Path path = Paths.get(fileDest);
            Files.write(path, bFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public void run()  {

		System.out.println("Backup Service Enabled!");

        connect_multicast();

        if(command.equals("SEND")) {

          read_file();

          send_file();

          System.out.println("Server sent packet with a chunck!");

        }
        else {
          try {
            byte[] incomingData = new byte[9366];

            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

            serverSocket.receive(incomingPacket);

            writeBytesToFileNio(incomingData, "/Users/bernardo/Desktop/SDIS/small.jpg");

            System.out.println("File received and saved to HDD!");
          } catch (Exception ex) {
              ex.printStackTrace();
          }
        }


      }

}
