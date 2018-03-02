import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Client2 {


    private int srvc_port;
    private String mcast_addr;
    private int mcast_port;
    private String srvc_addr;

    private String oper;
    private String opnd;
    private String opnd2;

    private MulticastSocket clientSocket;
    private DatagramSocket ds;
    private String Request;

    public void receiveReply() throws Exception{
        byte [] b = new byte[100];
        DatagramPacket dp = new DatagramPacket(b,b.length);
        ds.receive(dp);
        String msg = new String (b);
        System.out.println("I received this message: "+msg);
    }

    public void sendRequest() throws Exception{
        ds = new DatagramSocket();
        byte [] b = Request.getBytes();
        InetAddress ip = InetAddress.getByName(srvc_addr);
        DatagramPacket dp = new DatagramPacket(b,b.length,ip,srvc_port);
        ds.send(dp);
        System.out.println("My Request: "+Request);       
    }

    public void checkArgs(String[] args){

        this.mcast_addr = args[0];
        this.mcast_port = Integer.parseInt(args[1]);

        if(args.length == 4){

        this.oper = args[2];
        this.opnd = args[3];

        this.Request = args[2]+" "+args[3];
 
        }
        else if (args.length == 5){
        
        this.oper = args[2];
        this.opnd = args[3];
        this.opnd2= args[4];

        this.Request = args[2]+" "+args[3]+" "+args[4];

        }
         else
            {System.out.println("!!!Please, check your parameters!!!"); System.exit(0);}    
        

    }

    public void connectToMulticastGroup() throws Exception{

        InetAddress address = InetAddress.getByName(mcast_addr);
        
        clientSocket = new MulticastSocket(mcast_port);

        //Joint the Multicast group.
        clientSocket.joinGroup(address);

    }

    public void learnServiceAddr() throws Exception{

        byte[] buf = new byte[256];

        // Receive the information and print it.
         DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
        clientSocket.receive(msgPacket);

        String msg = new String(buf, 0, buf.length);
        System.out.println("I've learned Socket Service Info! : " + msg);

        String[] MsgReceived = msg.split("\\s+");

        //HARDCODED
        this.srvc_addr = "localhost";

        //need this for solving lenght issues
            String s = MsgReceived[3];
            s = s.substring(0, Math.min(s.length(), 4));

        this.srvc_port = Integer.parseInt(s);

    }


    public static void main(String[] args) throws UnknownHostException{

        try
        {
            Client2 client = new Client2();
            client.run(args);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }
        
    }

    public void run(String[] args) throws Exception{

        System.setProperty("java.net.preferIPv4Stack", "true");

        checkArgs(args); 

        connectToMulticastGroup();

        learnServiceAddr();

        //Send Request
        sendRequest();

        //Receive Reply
        receiveReply();
    }

}
