import java.net.*;
import java.io.*;
import java.util.ArrayList;

class Server {

    private class LicencePlates {
        private String Owner;
        private String PlateNumber;

        private LicencePlates(String Owner, String PlateNumber){
            this.Owner = Owner;
            this.PlateNumber = PlateNumber;
        }

        public String getOwner(){
            return Owner;
        }

        public String getPlateNumber(){
            return PlateNumber;
        }
    }

    public static boolean isNumeric(String str)  
    {  
      try  
      {  
        double d = Double.parseDouble(str);  
      }  
      catch(NumberFormatException nfe)  
      {  
        return false;  
      }  
      return true;  
    }
	
    public static void main(String[] args) {

        try
        {
            Server server = new Server ();
            server.run(args);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }

    }

    private int Port;
    private String[] MsgReceived;
    private ArrayList<LicencePlates> Users = new ArrayList<LicencePlates>();
    private String Response;
    private DatagramSocket ds;
    private InetAddress ip;
    private int clientPort;

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

    public void populateDataBase(){
    	Users.add(new LicencePlates("Bernas","AB-AD-GW"));
    	Users.add(new LicencePlates("Simon","PP-XX-GW"));
    	Users.add(new LicencePlates("Lais","DD-AD-IU"));
    }

    public void sendReply() throws Exception{
    	byte [] b = Response.getBytes();
    	DatagramPacket dp = new DatagramPacket(b,b.length,ip,clientPort);
    	ds.send(dp);
    	System.out.println("My Reply: "+ Response);    	
    }


    public void run(String[] args) throws Exception{
        if(args.length == 1 && isNumeric(args[0])){
            Port = Integer.parseInt(args[0]);
            System.out.println("Connected to Port " + Port);
        }
        else
            {System.out.println("!!!Please, check your parameters!!!"); System.exit(0);}

 		populateDataBase();

    	ds = new DatagramSocket(Port);
    	byte [] b = new byte[100];
    	DatagramPacket dp = new DatagramPacket(b,b.length);


   for(int i=0 ; i<10; i++){

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



}