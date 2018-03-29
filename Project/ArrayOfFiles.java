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

	public int hasFile(String pathname) {
		for(int i = 0; i < files.size(); i++) {
			System.out.println(files.get(i).getPathName());
			if(files.get(i).getPathName().equals(pathname))
				return i;
		}
		return -1;
	}

}
