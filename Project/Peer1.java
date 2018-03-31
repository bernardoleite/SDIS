package compile;

import java.util.*;
import java.io.*;
import java.net.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Peer1 implements RMI_Interface {

    final static int mcast_backup_port = 8888;
    final static int mcast_channel_port = 4444;
    final static int mcast_restore_port = 5555;

    private int port_number ;
    private String ip_address ;

    private InetAddress mcast_addr;

    //store information
    private ArrayOfFiles currentFiles = new ArrayOfFiles();

    private String filebin = "data.bin";
    private double maxAllowed = 8000.0;

    public void deserialize_Object(){

      try{
          ObjectInputStream is = new ObjectInputStream(new FileInputStream(filebin));
          currentFiles = (ArrayOfFiles)is.readObject();
          is.close();
      }
      catch(Exception e){
          e.printStackTrace();
      }

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

    public void receiver(int port_number) {
      System.out.println("HERE!!!!!!!!!!!!!!!!!!");
      File f = new File("data.bin");
      if(f.exists() && !f.isDirectory()) {
        deserialize_Object();
      }
      else {
        currentFiles = new ArrayOfFiles();
      }

      System.setProperty("java.net.preferIPv4Stack", "true");


      //chat used for BACKUP-MAIN_CHANNEL communication
      Chat backup_with_channel = new Chat();

      //chat used for RESTORE-MAIN_CHANNEL communication
      Chat restore_with_channel = new Chat();

      Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_backup_port, "RECEIVER", backup_with_channel, port_number, currentFiles));
      multicast_backup.start();

      Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "RECEIVER", port_number, backup_with_channel, restore_with_channel, currentFiles));
      multicast_channel.start();

      Thread multicast_restore = new Thread(new Restore("IP:PORT", mcast_addr, mcast_restore_port, "RECEIVER", port_number, restore_with_channel, currentFiles));
      multicast_restore.start();
    }

    public void reclaim(String value, int port_number) {

      File f = new File("data.bin");
      if(f.exists() && !f.isDirectory()) {
        deserialize_Object();
      }
      else {
        currentFiles = new ArrayOfFiles();
      }

      System.setProperty("java.net.preferIPv4Stack", "true");


      //chat used for BACKUP-MAIN_CHANNEL communication
      Chat backup_with_channel = new Chat();

      //chat used for RESTORE-MAIN_CHANNEL communication
      Chat restore_with_channel = new Chat();

      if(Double.parseDouble(value) == 0){
          System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          currentFiles.setMaximumSpace(maxAllowed);
          System.out.println("Current Maximum Storage: " + maxAllowed + " KBytes");
          System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
      }
      else if(Double.parseDouble(value) == maxAllowed){
          System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          currentFiles.setMaximumSpace(maxAllowed);
          System.out.println("Current Maximum Storage: " + maxAllowed + " KBytes");
          System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
      }
      else if(Double.parseDouble(value) > maxAllowed){
          System.out.println("Rejected... Max Disk Usage is " + maxAllowed + " KBytes");
          System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
      }
      else if(Double.parseDouble(value) < maxAllowed && Double.parseDouble(value) > currentFiles.maximumSpace){
          System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          currentFiles.setMaximumSpace(Double.parseDouble(value));
          System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
      }
      else if(Double.parseDouble(value) < maxAllowed && Double.parseDouble(value) < currentFiles.maximumSpace && Double.parseDouble(value) > currentFiles.getFolderSize()){
          System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          currentFiles.setMaximumSpace(Double.parseDouble(value));
          System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
      }
      else if(Double.parseDouble(value) < maxAllowed && Double.parseDouble(value) < currentFiles.maximumSpace && Double.parseDouble(value) < currentFiles.getFolderSize()){
          System.out.println("Accepted with Risk...Some Chunks have to be removed!");
          System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          currentFiles.setMaximumSpace(Double.parseDouble(value));
          System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
          System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");


          Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "REMOVE", port_number, backup_with_channel, restore_with_channel, currentFiles));
          multicast_channel.start();
      }
    }

    public void state() {

      File f = new File("data.bin");
      if(f.exists() && !f.isDirectory()) {
        deserialize_Object();
      }
      else {
        currentFiles = new ArrayOfFiles();
      }

      System.setProperty("java.net.preferIPv4Stack", "true");

      if(currentFiles.files.size() > 0)
        System.out.println("Files Backup:");
      for(int i = 0; i < currentFiles.files.size(); i++) {
        System.out.println("File:");
        System.out.println("Pathname: " + currentFiles.files.get(i).getPathName());
        System.out.println("FileId: " + currentFiles.files.get(i).getFileId());
        System.out.println("Desired Replication Degree: " + currentFiles.files.get(i).getDesiredReplicationDeg());
        System.out.println();
        System.out.println("Chunks:");
        for(int j = 0; j < currentFiles.files.get(i).getChunksInfo().size(); j++) {
          System.out.println("Id: " + currentFiles.files.get(i).getChunksInfo().get(j).getId());
          System.out.println("Perceived Replication Degree: " + currentFiles.files.get(i).getChunksInfo().get(j).getPerceivedReplicationDeg());
          System.out.println();
        }
      }
      System.out.println();
      if(currentFiles.chunksStore.size() > 0)
        System.out.println("Chunks Stored:");
      for(int i = 0; i < currentFiles.chunksStore.size(); i++) {
        System.out.println("Chunk:");
        System.out.println();
        System.out.println("Id: " + currentFiles.chunksStore.get(i).getId());
        System.out.println("Size: " + currentFiles.chunksStore.get(i).getSize());
        System.out.println("Perceived Replication Degree: " + currentFiles.chunksStore.get(i).getPerceivedReplicationDeg());
        System.out.println();
      }
    }

    public void delete(String file_name, int port_number) {

      File f = new File("data.bin");
      if(f.exists() && !f.isDirectory()) {
        deserialize_Object();
      }
      else {
        currentFiles = new ArrayOfFiles();
      }

      System.setProperty("java.net.preferIPv4Stack", "true");

      Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "DELETE", file_name, port_number, currentFiles));
      multicast_channel.start();
    }

    public void restore(String file_name, int port_number) {
      File f = new File("data.bin");
      if(f.exists() && !f.isDirectory()) {
        deserialize_Object();
      }
      else {
        currentFiles = new ArrayOfFiles();
      }

      //chat used for RESTORE-MAIN_CHANNEL communication
      Chat restore_with_channel = new Chat();

      System.setProperty("java.net.preferIPv4Stack", "true");

      Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "RESTORE", port_number, restore_with_channel));
      multicast_channel.start();

      Thread multicast_restore = new Thread(new Restore("IP:PORT", mcast_addr, mcast_restore_port, "RESTORE", file_name, port_number, restore_with_channel, currentFiles));
      multicast_restore.start();

    }


    public void backup(String file_name, int replication_deg, int port_number) {
      File f = new File("data.bin");
      if(f.exists() && !f.isDirectory()) {
        deserialize_Object();
      }
      else {
        currentFiles = new ArrayOfFiles();
      }

      //chat used for RESTORE-MAIN_CHANNEL communication
      Chat backup_with_channel = new Chat();

      System.setProperty("java.net.preferIPv4Stack", "true");

      Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_backup_port, "BACKUP", file_name, replication_deg, port_number, backup_with_channel, currentFiles));
      multicast_backup.start();

      Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "BACKUP", port_number, backup_with_channel));
      multicast_channel.start();

    }


    public static void main(String[] args) {

        try
        {
          Peer1 obj = new Peer1();
          RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(obj, 0);

          // Bind the remote object's stub in the registry
          Registry registry = LocateRegistry.getRegistry();
          registry.rebind(args[0], stub);

          System.err.println("Server ready");
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }

    }

}
