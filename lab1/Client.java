//UDP Socket in Java
import java.net.*;
import java.io.*;


class Client {

	private DatagramSocket ds;
	
    public static void main(String[] args) {

        try
        {
            Client server = new Client();
            server.run(args);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
    	
    }

    public void receiveReply() throws Exception{
    	byte [] b = new byte[100];
    	DatagramPacket dp = new DatagramPacket(b,b.length);
    	ds.receive(dp);
    	String msg = new String (b);
    	System.out.println("I received this message: "+msg);
    }

    public void run(String[] args) throws Exception{

    	String Request = "LOOKUP PP-XX-GW";

    	//Send Request
    	ds = new DatagramSocket();
    	byte [] b = Request.getBytes();
    	InetAddress ip = InetAddress.getByName("localhost");
    	int port = 2000;
    	DatagramPacket dp = new DatagramPacket(b,b.length,ip,port);
    	ds.send(dp);
    	System.out.println("My Request: "+Request);

    	//Receive Reply
    	receiveReply();



    }



}
