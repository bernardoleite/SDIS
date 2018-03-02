import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
	/*
    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;
    final static int port = 2000;*/
public class Server {

	private int srvc_port;
	private String mcast_addr;
	private int mcast_port;
	private String srvc_addr = "localhost/127.0.0.1";

    public void checkArgs(String[] args){

        if(args.length == 3){
            srvc_port = Integer.parseInt(args[0]);
            mcast_addr = args[1];
            mcast_port = Integer.parseInt(args[2]);
        }
        else
            {System.out.println("!!!Please, check your parameters!!!"); System.exit(0);}    

    }

    public static void main(String[] args) {

        try {
            Server server = new Server();
            server.run(args);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run(String[] args) throws Exception{
		
		checkArgs(args); 	

        Thread advThread = new Thread(new Advertise("Advertise", srvc_port, mcast_addr, mcast_port, srvc_addr));
        advThread.start();

        Thread servThread = new Thread(new Service("Service"));
        servThread.start();

    }
}