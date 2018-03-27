import java.util.*;

class Chat {

   private volatile String message = "nada";

   //array of inbox messages
   private volatile ArrayList<String> inbox = new ArrayList<String>();


   public String getMessage() {
      	return message;
   }

   public ArrayList<String> getInbox() {
   		return inbox;
   }

   public void addMsgInbox(String message) {
   		inbox.add(message);
         System.out.println("Inbox: " + inbox.size());
   }

   public void clearInbox() {
   		inbox = new ArrayList<String>();
   }

   public void setMessage(String msg) {
      this.message = msg;
   }

}