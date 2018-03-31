import java.util.*;
import java.io.*;
import java.net.*;

public class Peer1 {

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
    public void run(String[] args, String command) throws Exception {

      /*  se existe data.bin
          deserialize currentFiles
        senao
          criar novo*/

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

        //LONELY PEER
        if(command.equals("RECEIVER")) {
            Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_backup_port, "RECEIVER", backup_with_channel, port_number, currentFiles));
            multicast_backup.start();

            Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "RECEIVER", port_number, backup_with_channel, restore_with_channel, currentFiles));
            multicast_channel.start();

            Thread multicast_restore = new Thread(new Restore("IP:PORT", mcast_addr, mcast_restore_port, "RECEIVER", port_number, restore_with_channel, currentFiles));
            multicast_restore.start();
        }
        //Backup Channel
        if(command.equals("BACKUP")) {

            Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_backup_port, "BACKUP", args[2], Integer.parseInt(args[3]), port_number, backup_with_channel, currentFiles));
            multicast_backup.start();
            Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "BACKUP", port_number, backup_with_channel));
            multicast_channel.start();
        }
        if(command.equals("RESTORE")) {

            Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "RESTORE", port_number, restore_with_channel));
            multicast_channel.start();

            Thread multicast_restore = new Thread(new Restore("IP:PORT", mcast_addr, mcast_restore_port, "RESTORE", args[2], port_number, restore_with_channel, currentFiles));
            multicast_restore.start();
        }

        if(command.equals("STATE")) {
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

        if(command.equals("DELETE")) {

            Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "DELETE", args[2], port_number, currentFiles));
            multicast_channel.start();

        }
        if(command.equals("RECLAIM")) {

            if(Double.parseDouble(args[2]) == 0){
                System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                currentFiles.setMaximumSpace(maxAllowed);
                System.out.println("Current Maximum Storage: " + maxAllowed + " KBytes");
                System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
            }
            else if(Double.parseDouble(args[2]) == maxAllowed){
                System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                currentFiles.setMaximumSpace(maxAllowed);
                System.out.println("Current Maximum Storage: " + maxAllowed + " KBytes");
                System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
            }
            else if(Double.parseDouble(args[2]) > maxAllowed){
                System.out.println("Rejected... Max Disk Usage is " + maxAllowed + " KBytes");
                System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
            }
            else if(Double.parseDouble(args[2]) < maxAllowed && Double.parseDouble(args[2]) > currentFiles.maximumSpace){
                System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                currentFiles.setMaximumSpace(Double.parseDouble(args[2]));
                System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
            }
            else if(Double.parseDouble(args[2]) < maxAllowed && Double.parseDouble(args[2]) < currentFiles.maximumSpace && Double.parseDouble(args[2]) > currentFiles.getFolderSize()){
                System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                currentFiles.setMaximumSpace(Double.parseDouble(args[2]));
                System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");
            }
            else if(Double.parseDouble(args[2]) < maxAllowed && Double.parseDouble(args[2]) < currentFiles.maximumSpace && Double.parseDouble(args[2]) < currentFiles.getFolderSize()){
                System.out.println("Accepted with Risk...Some Chunks have to be removed!");
                System.out.println("Previous Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                currentFiles.setMaximumSpace(Double.parseDouble(args[2]));
                System.out.println("Current Maximum Storage: " + currentFiles.getMaximumSpace() + " KBytes");
                System.out.println("Current Storage Usage: " + currentFiles.getFolderSize() + " KBytes");


                Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "REMOVE", port_number, backup_with_channel, restore_with_channel, currentFiles));
                multicast_channel.start();
            }

        }
        Thread.sleep(500);

    }

    public static void main(String[] args) {

        try
        {
            Peer1 peer = new Peer1();
            String command = peer.checkCommands(args);
            if(!command.equals("Error")) {
              peer.run(args, command);
            }
            else {
              System.out.println("Invalid Args!!!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }

    }

    public String checkCommands (String[] args) {

            if(args.length < 1){
                return "ERROR";
            }

            if(args[0].indexOf(':') < 0){
                this.port_number = Integer.parseInt(args[0]);
            }
            else if (args[0].indexOf(":") == 0){
                String[] parts = args[0].split(":");
                this.port_number = Integer.parseInt(parts[1]);
            }
            else{
                String[] parts = args[0].split(":");
                this.ip_address =  parts[0];
                this.port_number = Integer.parseInt(parts[1]);
            }

            if (args.length == 1){
                return "RECEIVER";
            }
            else {
                if (args[1].equals("BACKUP")){
                    return "BACKUP";
                }
                else if (args[1].equals("RESTORE")){
                    return "RESTORE";
                }
                else if (args[1].equals("DELETE")){
                    return "DELETE";
                }
                else if (args[1].equals("RECLAIM")){
                    return "RECLAIM";
                }
                else if (args[1].equals("STATE")){
                    return "STATE";
                }
                else
                    return "ERROR";
            }

    }
}
