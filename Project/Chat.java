package compile;

import java.util.*;

class Chat {

   //used between  PEER_RECEIVER-BACKUP and CHANNEL-BACKUP
   private volatile String message = "nada";

   //used between PEER_RECEIVER-CHANNEL and RESTORE
   private volatile Message msgchunk = new Message("nada");

   //used between PEER_INITIATOR-BACKUP and CHANNEL-BACKUP
   private volatile ArrayList<String> inbox = new ArrayList<String>();

   //used between PEER_INITIATOR-RESTORE and CHANNEL-RESTORE
   private volatile ArrayList<String> getchunks = new ArrayList<String>();

   //used between PEER_RECEIVER-CHANNEL and BACKUP-RECEIVER
   private volatile byte[] emergencyPutChunk = new byte[64500];
   private volatile String emergency = "nada";

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

   public void setMsgChunk(Message msg) {
      this.msgchunk = msg;
   }

   public Message getMsgChunk() {
      return this.msgchunk;
   }
   public void setEmergencyPutChunk(byte[] msg) {
       this.emergencyPutChunk = msg;
   }

   public byte[] getEmergencyPutChunk() {
      return this.emergencyPutChunk;
   }
   public void setEmergency(String msg) {
       this.emergency = msg;
   }

   public String getEmergency() {
      return this.emergency;
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
