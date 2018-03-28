import java.util.*;
import java.io.*;
import java.net.*;

public class ArrayOfFiles implements Serializable{
	public ArrayList<FileInfo> files = new ArrayList<FileInfo>();
	public ArrayList<ChunkInfo> chunksStore = new ArrayList<ChunkInfo>();

	public int hasChunkStore(String id) {
		for(int i = 0; i < chunksStore.size(); i++) {
			if(chunksStore.get(i).getId().equals(id))
				return i;
		}
		return -1;
	}


}
