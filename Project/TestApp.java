package compile;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class TestApp {

    private int port_number;
    private String ip_address;


    private TestApp() {}

    public static void main(String[] args) {
        TestApp t = new TestApp();
        t.run(args);
    }

    public void run(String[] args) {
      String command = checkCommands(args);
      String host = (args.length < 1) ? null : args[0];
      System.out.println("Executing: " + command);
      System.out.println("Port Number: " + port_number);

      try {
        TimeUnit.SECONDS.sleep(1);

          Registry registry = LocateRegistry.getRegistry("localhost");
          RMI_Interface stub = (RMI_Interface) registry.lookup(Integer.toString(port_number));

          if(command.equals("RECEIVER")) {
            System.out.println(port_number);
            stub.receiver(port_number);
          }
          else if(command.equals("BACKUP"))
            stub.backup(args[2], Integer.parseInt(args[3]), port_number);
          else if(command.equals("RESTORE"))
            stub.restore(args[2], port_number);
          else if(command.equals("DELETE"))
            stub.delete(args[2], port_number);
          else if(command.equals("RECLAIM"))
            stub.reclaim(args[2], port_number);
          else if(command.equals("STATE"))
            stub.state();
          TimeUnit.SECONDS.sleep(1);

      } catch (Exception e) {
          System.err.println("Client exception: " + e.toString());
          e.printStackTrace();
      }
    }

    public  String checkCommands (String[] args) {

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
