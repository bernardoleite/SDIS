import java.util.*;
import java.io.*;
import java.net.*;

public class ChunkInfo implements Serializable{

	private String Id;
	private int PerceivedReplicationDeg;
	private int size;


	public ChunkInfo(String Id, int PerceivedReplicationDeg, int size){
		this.Id = Id;
		this.PerceivedReplicationDeg = PerceivedReplicationDeg;
		this.size = size;
	}

	public String getId(){
		return Id;
	}

	public int getPerceivedReplicationDeg(){
		return PerceivedReplicationDeg;
	}

	public int getSize(){
		return size;
	}


	public void incrementPerceivedReplicationDeg() {
		PerceivedReplicationDeg++;
	}
}
