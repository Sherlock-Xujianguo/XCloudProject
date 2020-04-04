

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.lang.*;

class splitFileTarget implements Runnable{
	int count;
	int byteSize;
	String PartFileName;
	File originFile = null;
	CallBack CB = null;
	int startPos;
	
	splitFileTarget(int count,String PartFileName,File originFile,int byteSize,CallBack CB){
		this.count = count;
		this.PartFileName = PartFileName;
		this.originFile = originFile;
		this.byteSize = byteSize;
		this.CB = CB;
		startPos = byteSize*(count-1);
		
		
	}
	
	public void run() {
		RandomAccessFile rFile; 
		OutputStream OS;
		try {
			System.out.println(PartFileName);
			rFile = new RandomAccessFile(originFile,"r");
			byte[] b = new byte[byteSize];
			rFile.seek(startPos);
			int length = rFile.read(b); 
			OS = new FileOutputStream(PartFileName);
			OS.write(b,0,length);
			OS.flush();
			OS.close();
			rFile.close();
		}catch(IOException ie) {
			System.out.println("partFile : "+count+"write error");
			ie.printStackTrace();
		}
	
		CB.callback(); 				//分割函数的回调函数
		
	}
	
}

class mergeFile implements Runnable {
	long startPos;
	String mergeFileName;
	File partFile;
	CallBack CB = null;
	
	mergeFile(File partFile,String mergeFileName,long startPos,CallBack CB)
	{
		this.partFile = partFile;
		this.mergeFileName = mergeFileName;
		this.startPos = startPos;
		this.CB = CB;
	}
	
	public void run() {
		RandomAccessFile rFile ;
		try {
			rFile = new RandomAccessFile(mergeFileName,"rw");
			rFile.seek(startPos);
			FileInputStream FIS = new FileInputStream(partFile);
			byte[] bData = new byte[1024];
			int length;
			while((length = FIS.read(bData, 0, bData.length))!=-1) {
				rFile.write(bData, 0, length);
				startPos+=length;
				rFile.seek(startPos);
			}
			FIS.close();
			rFile.close();
		}catch(IOException ie) {
			System.out.println("partFile : add error");
			ie.printStackTrace();
		}
		
		CB.callback(); 			//整合文件的回调函数
	}
}

public class FileSplit{
	public ExecutorService threadPool = null;
	List<splitFileTarget> fileParts = null;
	int num = 0;
	
	public List<splitFileTarget> splitBySize(String fileName,int byteSize,CallBack CB) throws IOException{		//添加了回调接口
		List<splitFileTarget> parts = new ArrayList<splitFileTarget>();
		File file = new File(fileName);						//导入文件
		System.out.println(file.length()+"   "+byteSize);
		int count = (int) Math.ceil(file.length()/(double)byteSize);
		System.out.println(count);
		this.num = count;
		int countLen = (count+"").length();
		threadPool = Executors.newCachedThreadPool();
		
		System.out.println(fileName);
		
		for(int i = 0; i<count; i++) {
			String PartfileName = fileName+"_"+((int)(i+1))+".part";
			System.out.println("i: "+ i+PartfileName);
			splitFileTarget st = new splitFileTarget(i+1, PartfileName,file,byteSize,CB);
			parts.add(st);
		}
		fileParts = parts;
		return parts;
	}
	
	public void splitStart() {
		try {
			//threadPool.setDebug(true);
			//System.out.println("FilePartSize: "+ fileParts.size());
			for(int i=0;i<fileParts.size();i++) {
				TimeUnit.MILLISECONDS.sleep(10);
				threadPool.execute(fileParts.get(i));			
				}
		}catch(Exception e) {
			System.out.println("ThreadPool begin error");
			e.printStackTrace();
		}
		//threadPool.shutdown();
	}
	
	public void mergePartFiles(int partFileCount,String mergeFileName,CallBack CB) throws IOException{	//添加了回调接口
		ArrayList<mergeFile> partFiles = new ArrayList<mergeFile>();
		int allLength = 0;
		for(int i = 0;i<partFileCount;i++) {
			File partfile = new File(mergeFileName+"_"+((int)(i+1))+".part");
			mergeFile tpFile = new mergeFile(partfile,mergeFileName,allLength,CB);
			partFiles.add(tpFile);
			allLength+=partfile.length();
		}
		RandomAccessFile RAFile = new RandomAccessFile(mergeFileName,"rw");
		RAFile.setLength(allLength);
		RAFile.close();
		if(threadPool == null) {
			threadPool = Executors.newCachedThreadPool();
		}
		try {
			//threadPool.setDebug(true);
			for(int i =0 ;i<partFileCount;i++) {
				threadPool.execute(partFiles.get(i));
			}
		}catch(Exception e) {
			System.out.println("file merge error");
			e.printStackTrace();
		}
		threadPool.shutdown();
	}
	
	
	
	
	
	public static void main(String[] args) {
		FileSplit FStest = new FileSplit();
		CB aaa = new CB();
		try{
			//FStest.splitBySize("F:\\JAVA\\JavaSocket\\src\\in.bin.gz", (int)(1024*30),aaa);
			//FStest.splitStart();
			//FStest.threadPool.shutdown();
			FStest.mergePartFiles(6,"F:\\UserDir\\User1\\ROOT\\DirB\\in.bin.gz",aaa);
		}catch(Exception ie) {
			System.out.println("error");
			ie.printStackTrace();
		}
	}

}

class CB implements CallBack{
	
	CB(){
	}
	
	public void callback() {
		System.out.println("aaaa");
	}
}




