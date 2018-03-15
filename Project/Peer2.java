import java.io.*;
import java.net.*;

public class Peer2 {
    
    final static String INET_ADDR = "224.0.0.3";
    final static int PORT = 8888;

    private InetAddress address;
    private MulticastSocket clientSocket;

    private FileEvent fileEvent = null;


    public void connect_multicast() throws Exception{

        address = InetAddress.getByName(INET_ADDR);
        clientSocket = new MulticastSocket(PORT);
        clientSocket.joinGroup(address);

    }

    public void treatData(DatagramPacket incomingPacket) throws Exception{
        byte[] data = incomingPacket.getData();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream( in );
        fileEvent = (FileEvent) is.readObject();
        if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
            System.out.println("Some issue happened while packing the data @ client side");
            System.exit(0);
        }
        createAndWriteFile(); // writing the file to hard disk        
    }

    public void run(String[] args) throws Exception{

        System.setProperty("java.net.preferIPv4Stack", "true");
        
        try{

            connect_multicast();

            byte[] incomingData = new byte[1024 * 1000 * 50];
               
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);

            clientSocket.receive(incomingPacket);

            treatData(incomingPacket);

            System.out.println("File received and saved to HDD!");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createAndWriteFile() {
        String outputFile = fileEvent.getDestinationDirectory() + fileEvent.getFilename();
        if (!new File(fileEvent.getDestinationDirectory()).exists()) {
            new File(fileEvent.getDestinationDirectory()).mkdirs();
        }
        File dstFile = new File(outputFile);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(dstFile);
            fileOutputStream.write(fileEvent.getFileData());
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("Output file : " + outputFile + " is successfully saved ");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {

        try
        {
            Peer2 peer = new Peer2();
            peer.run(args);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
        }

    }
}
