import java.util.*;
import java.io.*;
import java.net.*;

public class ChunkInfo implements Serializable{

	private int Id;
	private int PerceivedReplicationDeg;
	private int size;


	public ChunkInfo(int Id, int PerceivedReplicationDeg, int size){
		this.Id = Id;
		this.PerceivedReplicationDeg = PerceivedReplicationDeg;
		this.size = size;
	}

	public int getId(){
		return Id;
	}

	public int getPerceivedReplicationDeg(){
		return PerceivedReplicationDeg;
	}

	public int getSize(){
		return size;
	}
	
}