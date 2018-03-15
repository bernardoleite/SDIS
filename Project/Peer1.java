import java.io.*;
import java.net.*;

public class Peer1 {
    
    final static String INET_ADDR = "224.0.0.3";
    final static int mcast_port = 8888;
    private InetAddress mcast_addr;

    public void run(String[] args) throws Exception {

        System.setProperty("java.net.preferIPv4Stack", "true");

        //Backup Channel
            Thread multicast_backup = new Thread(new Backup("IP:PORT", mcast_addr, mcast_port));
            multicast_backup.start();

            multicast_backup.send_file();

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
            peer.run(args);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }

    }
}