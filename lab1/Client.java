//UDP Socket in Java
import java.net.*;
import java.io.*;
import java.util.Scanner;

class Client {

	private DatagramSocket ds;
	private String Request;
	
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

    public void sendRequest() throws Exception{
        ds = new DatagramSocket();
        byte [] b = Request.getBytes();
        InetAddress ip = InetAddress.getByName("localhost");
        int port = 2000;
        DatagramPacket dp = new DatagramPacket(b,b.length,ip,port);
        ds.send(dp);
        System.out.println("My Request: "+Request);       
    }

    public void receiveReply() throws Exception{
    	byte [] b = new byte[100];
    	DatagramPacket dp = new DatagramPacket(b,b.length);
    	ds.receive(dp);
    	String msg = new String (b);
    	System.out.println("I received this message: "+msg);
    }

    public int seeIfErrors(String [] analyseString){

    	if(analyseString[0].equals("LOOKUP") || analyseString[0].equals("REGISTER") ){
    		if(analyseString[1].length() == 8) return 1;
    	}
        else 
            return -1;

        return -1;

    }

    public void run(String[] args) throws Exception{

		System.out.println("Please, type a valid request: ");
		Scanner scanner = new Scanner(System.in);
		Request = scanner.nextLine();
		String [] analyseString = Request.split("\\s+");

		//check if any errors
		if(seeIfErrors(analyseString)==-1) System.exit(1);

    	//Send Request
        sendRequest();

    	//Receive Reply
    	receiveReply();



    }



}
