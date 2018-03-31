import java.util.*;
import java.io.*;
import java.net.*;

public class ArrayOfFiles implements Serializable{
	public ArrayList<FileInfo> files = new ArrayList<FileInfo>();
	public ArrayList<ChunkInfo> chunksStore = new ArrayList<ChunkInfo>();
	public double maximumSpace = 8000.0;
	public double currentSpace;

	public ArrayOfFiles(){
		this.currentSpace = folderSize(new File("dest"));
	}

	//refresh currentSpace (KBytes)
	public void refreshCurrentSpace(){
		File myFolder = new File("dest");
		this.currentSpace = folderSize(myFolder);
	}

	//calculates currentSpace (KBytes)
	public static double folderSize(File directory) {
    double length = 0;
    for (File file : directory.listFiles()) {
        if (file.isFile())
            length += file.length();
        else
            length += folderSize(file);
    }
    return length/1000;
	}
	//returns currentSpace (KBytes)
	public double getFolderSize(){
		refreshCurrentSpace();
		return this.currentSpace;
	}

	//set maximumSpace
	public void setMaximumSpace(double KBytes){
		this.maximumSpace = KBytes;
	}

	//get maximumSpace
	public double getMaximumSpace(){
		return this.maximumSpace;
	}

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
