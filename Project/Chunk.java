public class Chunk {

    private String fileId;
    private int chunkNo;
    private int replication_deg;
    private byte[] body;

  public Chunk(String fileId, int chunkNo, int replication_deg, byte[] body){
    this.fileId = fileId;
    this.chunkNo = chunkNo;
    this.replication_deg = replication_deg;
    this.body = body;
  }

  public String getFileId() {
    return fileId;
  }

  public int getChunkNo() {
    return chunkNo;
  }

  public int getReplication_Deg() {
    return replication_deg;
  }

  public byte[] getBody() {
    return body;
  }
}
