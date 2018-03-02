import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Advertise implements Runnable {

	private String msg;

	private DatagramSocket serverSocket;
    private InetAddress addr;

	private String name;
	private int srvc_port;
	private String srvc_addr = "localhost/127.0.0.1";
	private String mcast_addr;
	private int mcast_port;

	private int secondsPassed = 0;


	public void advertMulticast() throws IOException{
		DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, addr, mcast_port);
        serverSocket.send(msgPacket);
		System.out.println("multicast:" + mcast_addr + " " + mcast_port + " " + srvc_addr + " " + srvc_port);	
	}


	Timer myTimer = new Timer();
	TimerTask task = new TimerTask(){
		public void run() {
			secondsPassed++;

			try{
			  advertMulticast();
			}catch(IOException e){
			  e.printStackTrace();
			}

		}
	};

	public Advertise(String name, int srvc_port, String mcast_addr, int mcast_port, String srvc_addr){
		this.name = name;
		this.srvc_port = srvc_port;
		this.srvc_addr = srvc_addr;
		this.mcast_addr = mcast_addr;
		this.mcast_port = mcast_port;
	}

	public void run() {

	 // Open a new DatagramSocket, which will be used to send the data.
		try{

		System.setProperty("java.net.preferIPv4Stack", "true");

		serverSocket = new DatagramSocket();
        addr = InetAddress.getByName(mcast_addr);
        this.msg = "multicast:" + mcast_addr + " " + mcast_port + " " + srvc_addr + " " + srvc_port;

        myTimer.scheduleAtFixedRate(task,1000,1000);

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}