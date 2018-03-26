import java.util.*;

class Chat {

   String message = "nada";

   //array of inbox messages
   ArrayList<String> inbox = new ArrayList<String>();


   boolean canI = true;

   public String getMessage() {
   		if(canI)
      		return message;
      	else
      		return "nada";
   }

   public ArrayList<String> getInbox() {
   		return inbox;
   }

   public void addMsgInbox(String message) {
   		System.out.println("Inbox: " + inbox.size());
   		inbox.add(message);
   }

   public void clearInbox() {
   		inbox = new ArrayList<String>();
   }

   public void setMessage(String msg) {
      this.message = msg;
   }

   public void access(boolean b) {
   	this.canI = b;
   }

}