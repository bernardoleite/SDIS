public class Message {

    private String command;
    private String senderId;
    private String fileId;
    private int version;
    private int chunkNo;
    private int replication_deg;
    private String body;

//PUTCHUNK
  public Message(String command, int version, String senderId, String fileId, int chunkNo, int replication_deg, String body){
    this.command = command;
    this.senderId = senderId;
    this.fileId = fileId;
    this.chunkNo = chunkNo;
    this.replication_deg = replication_deg;
    this.body = body;
    this.version = version;

  }

  public String getFileId() {
    return fileId;
  }

  public int getChunNo() {
    return chunkNo;
  }

  public int getReplication_Deg() {
    return replication_deg;
  }

  public String getBody() {
    return body;
  }

//CHUNK
  public Message(String command, int version, String senderId, String fileId, int chunkNo, String body){
    this.command = command;
    this.senderId = senderId;
    this.fileId = fileId;
    this.chunkNo = chunkNo;
    this.replication_deg = -1;
    this.body = body;
    this.version = version;

  }

//DELETE
    public Message(String command, int version, String senderId, String fileId){
    this.command = command;
    this.version = version;
    this.senderId = senderId;
    this.fileId = fileId;
    this.chunkNo = -1;
    this.replication_deg = -1;
  }

//STORED RESTORE RECLAIM
    public Message(String command, int version, String senderId, String fileId, int chunkNo){
    this.command = command;
    this.version = version;
    this.senderId = senderId;
    this.fileId = fileId;
    this.chunkNo = chunkNo;
    this.replication_deg = -1;
    this.body = "";
  }

  public String toString() {
    if(replication_deg == -1) {
      //DELETE
      if(chunkNo == -1) {
        return command + " " + version + " " + senderId + " " + fileId + "\r\n\r\n";
      }
      else {
        //STORED RESTORE RECLAIM
        if(body.equals("")) {
          return command + " " + version + " " + senderId + " " + fileId + " " + chunkNo + "\r\n\r\n";
        }
        //CHUNK
        else {
          return command + " " + version + " " + senderId + " " + fileId +  " " + chunkNo + "\r\n\r\n" + body; 
        }
      }
    }
    else {
      //PUTCHUNK
      return command + " " + version + " " + senderId + " " + fileId + " " + chunkNo + " " + replication_deg + "\r\n\r\n" + body;
    }
  }

}
