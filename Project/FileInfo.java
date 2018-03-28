import java.util.*;
import java.io.*;
import java.net.*;

public class FileInfo implements Serializable {

	private String pathname;
	private String fileId;
	private int desiredReplicationDeg;

	private ArrayList<ChunkInfo> chunksInfo = new ArrayList<ChunkInfo>();


	public FileInfo(String pathname, String fileId, int desiredReplicationDeg ){
		this.pathname = pathname;
		this.fileId = fileId;
		this.desiredReplicationDeg = desiredReplicationDeg;
	}

	public void addChunkInfo(ChunkInfo chunkinfo){
		chunksInfo.add(chunkinfo);
	}

	public ArrayList<ChunkInfo> getChunksInfo(){
		return chunksInfo;
	}

	public String getFileId(){
		return fileId;
	}

	
}