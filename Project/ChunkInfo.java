import java.util.*;
import java.io.*;
import java.net.*;

public class ChunkInfo implements Serializable{

	private String Id;
	private int PerceivedReplicationDeg;
	private int DesiredReplicationDeg;
	private int size;


	public ChunkInfo(String Id, int PerceivedReplicationDeg, int DesiredReplicationDeg, int size){
		this.Id = Id;
		this.PerceivedReplicationDeg = PerceivedReplicationDeg;
		this.DesiredReplicationDeg = DesiredReplicationDeg;
		this.size = size;
	}

	public String getId(){
		return Id;
	}

	public int getPerceivedReplicationDeg(){
		return PerceivedReplicationDeg;
	}

	public int getDesiredReplicationDeg(){
		return DesiredReplicationDeg;
	}

	public int getSize(){
		return size;
	}

	public void incrementPerceivedReplicationDeg() {
		PerceivedReplicationDeg++;
	}
	public void decrementPerceivedReplicationDeg() {
		PerceivedReplicationDeg--;
	}
}
