import java.util.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Service implements Runnable {

	private String name;

    //HARDCODE
    private int srvc_port = 2000;

    private String[] MsgReceived;
    private ArrayList<LicencePlates> Users = new ArrayList<LicencePlates>();
    private String Response;
    private DatagramSocket ds;
    private InetAddress ip;
    private int clientPort;

	public Service(String name){
		this.name = name;
        //DEVE RECEBER MAIS ARGS
	}

    public String treatAndResponse(){

	   if(MsgReceived[0].equals("REGISTER"))
        {
	
            LicencePlates newUser = new LicencePlates(MsgReceived[2],MsgReceived[1]);
            
            for(LicencePlates curInstance: Users){
                if(curInstance.getPlateNumber().equals(newUser.getPlateNumber()))
                	return "-1";
         	}

         	//adicionar veiculo e return nr veiculos
         	Users.add(new LicencePlates(MsgReceived[2],MsgReceived[1]));
         	return Integer.toString(Users.size());    	

        }
    	
    	else if(MsgReceived[0].equals("LOOKUP"))
        {

        	//need this for solving lenght issues
        	String s = MsgReceived[1];
        	s = s.substring(0, Math.min(s.length(), 8));

            for(LicencePlates curInstance: Users){
                if(s.equals(curInstance.getPlateNumber()))
            		{return curInstance.getOwner();}
         	}

         	return "NOT_FOUND";
        }

        else
        	return "ERROR";

    }


    public void sendReply() throws Exception{
    	byte [] b = Response.getBytes();
    	DatagramPacket dp = new DatagramPacket(b,b.length,ip,clientPort);
    	ds.send(dp);
    	System.out.println("My Reply: "+ Response);    	
    }

    public void populateDataBase(){
    	Users.add(new LicencePlates("Bernas","AB-AD-GW"));
    	Users.add(new LicencePlates("Simon","PP-XX-GW"));
    	Users.add(new LicencePlates("Lais","DD-AD-IU"));
    }


    public void receiveRequest() throws Exception{

        ds = new DatagramSocket(srvc_port);

    while(true){

        byte [] b = new byte[100];
        DatagramPacket dp = new DatagramPacket(b,b.length);

        //Receive Request
        ds.receive(dp);

        //get know the IP and Port Origins
        ip = dp.getAddress();
        clientPort = dp.getPort();

        String msg = new String (b);
        System.out.println("I received this message: "+msg);
        MsgReceived = msg.split("\\s+");

        //Message Analysis
        		Response = treatAndResponse();

        //Send Reply
        		sendReply();
 
        }

    }

	public void run() {

		try{

		populateDataBase();
		receiveRequest();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}