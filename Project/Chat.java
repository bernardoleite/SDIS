import java.util.*;

class Chat {

   //used between  PEER_RECEIVER-BACKUP and CHANNEL-BACKUP
   private volatile String message = "nada";

   //used between PEER_RECEIVER-CHANNEL and RESTORE
   private volatile String msgchunk = "nada";

   //used between PEER_INITIATOR-BACKUP and CHANNEL-BACKUP
   private volatile ArrayList<String> inbox = new ArrayList<String>();

   //used between PEER_INITIATOR-RESTORE and CHANNEL-RESTORE
   private volatile ArrayList<String> getchunks = new ArrayList<String>();

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

   public void setMsgChunk(String msg) {
      this.msgchunk = msg;
   }

   public String getMsgChunk() {
      return this.msgchunk;
   }

   public void setGetChunks(ArrayList<String> getchunks) {
      this.getchunks = getchunks;
   }

   public ArrayList<String> getGetChunks() {
       return getchunks;
   }

   public void clearGetChunks() {
       getchunks = new ArrayList<String>();
   }

}
