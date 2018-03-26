import java.io.*;
import java.net.*;

public class Peer1 {

   // final static String INET_ADDR = "224.0.0.3";
    final static int mcast_backup_port = 8888;
    final static int mcast_channel_port = 4444;

    private int port_number ;
    private String ip_address ;

    private InetAddress mcast_addr;

    public void run(String[] args, String command) throws Exception {

        System.setProperty("java.net.preferIPv4Stack", "true");

        Chat backup_with_channel = new Chat();

        //LONELY PEER
        if(command.equals("RECEIVER")){
          Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_backup_port, "RECEIVER", backup_with_channel, port_number));
          multicast_backup.start();

          Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "RECEIVER", port_number, backup_with_channel));
          multicast_channel.start();
        }
        //Backup Channel
        if(command.equals("BACKUP")){
            Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_backup_port, "BACKUP", args[2], Integer.parseInt(args[3]), port_number, backup_with_channel));
            multicast_backup.start();
            Thread multicast_channel = new Thread(new Channel("IP:PORT", mcast_addr, mcast_channel_port, "BACKUP", port_number, backup_with_channel));
            multicast_channel.start();
        }

        //!!!Threads for each Service of Peer!!!
/*
        //Main Channel
            Thread multicast_channel = new Thread(new Channel("IP:PORT",mcast_addr, mcast_port));
            multicast_channel.start();

        //Backup Channel
            Thread multicast_backup = new Thread(new Backup("IP:PORT",mcast_addr, mcast_port));
            multicast_backup.start();

        //Restore Channel
            Thread multicast_restore = new Thread(new Restore("IP:PORT",mcast_addr, mcast_port));
            multicast_restore.start();
*/

        Thread.sleep(500);

    }

    public static void main(String[] args) {

        try
        {
            Peer1 peer = new Peer1();
            String command = peer.checkCommands(args);
            peer.run(args, command);
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
