
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class FileTree {							//格式：T/F;FileName;FileLength;FileChangeTime
	
	private long Sum = 5000;
	private long FileNum = 0;
	
	private File BaseFile = null;
	
	FileTree(String FileName){					//第一行：Sum@@FilNum
		this.BaseFile = new File(FileName);
		
	}
	
	FileTree(){
		
	}
	
	public boolean BulidTree(String FileName) {		//建立新树
		this.BaseFile = new File(FileName);
		try {
			RandomAccessFile RAF= new RandomAccessFile(BaseFile, "rw");
			RAF.write("10000@@0#        \n".getBytes());
			
			for(long i = 0;i<10000;i++) {
				for(int j = 0;j<100;j++) {
					RAF.write(" ".getBytes());
				}RAF.write("#\n".getBytes());
			}
			
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean InitTree(String FileName) {
		this.BaseFile = new File(FileName);
		
		try {
			RandomAccessFile RAF = new RandomAccessFile(BaseFile,"rw");
			
			String s = RAF.readLine().split("#")[0];
			
			this.Sum = Long.valueOf(s.split("@@")[0]);
			this.FileNum = Long.valueOf(s.split("@@")[1]);
			
			return true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public long getNum() {
		long num = (++FileNum)%997;
		
		num*=5;
		try {
			
			//File file = new File(FileName);
			
			FileInputStream FIS = FilePort.getFIS(BaseFile, 18+num*102);		//跳至指定行观察是否为空闲行
			
			byte bData[] = new byte[1];
			FIS.read(bData,0,1);
			
			int i = 1;
			
			while(new String(bData).equals("T")) {				//当为占用行时发生冲突 启动冲突处理函数
				
				System.out.println(new String(bData));	
				
				num+=i*i;
				i++;
				num = num%5000;
				FIS = FilePort.getFIS(BaseFile, 18+num*102);
				
				FIS.read(bData,0,1);
				
			}
			
			System.out.println(new String(bData));
		
			return num;						//无冲突 直接返回可用行
		
		} catch (Exception e) {				//出错 返回0（0行为根目录必占用）
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}
	
	public long NewFile(String FileName,long FileLength) {
		
		long num = this.getNum();
		
		try{
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, 18+num*102);
			
			RAF.write(("T;"+FileName+";"+FileLength+";"+new SimpleDateFormat("yy/MM/dd").format(new Date())+"#").getBytes());
			
			RAF = FilePort.getRAF(BaseFile, 0);
			RAF.write((this.Sum+"@@"+this.FileNum+"#").getBytes());
			
			RAF.close();
			return num;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public boolean FileChange(long num,String FileName,long FileLength) {
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, 18+num*102);
			byte[] bData = new byte[102];
			FIS.read(bData, 0, bData.length);
			
			FileInfo FI = new FileInfo(new String(bData).split("#")[0]);
			
			FI.setFileName(FileName);
			FI.setFileLength(FileLength);
			FI.SetFileChangeTime();
			FIS.close();
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, 18+num*102);
			RAF.write(FI.getMainInfo().getBytes());
			
			return true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean FileRename(long num,String FileName) {
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, 18+num*102);
			byte[] bData = new byte[102];
			FIS.read(bData, 0, bData.length);
			
			FileInfo FI = new FileInfo(new String(bData).split("#")[0]);
			
			FI.setFileName(FileName);
			FI.SetFileChangeTime();
			FIS.close();
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, 18+num*102);
			RAF.write(FI.getMainInfo().getBytes());
			
			return true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	
	public boolean FileDelete(long num) {
		try {
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, 18+num*102);
			
			RAF.write("F".getBytes());
			
			this.FileNum--;
			
			RAF = FilePort.getRAF(BaseFile, 0);
			
			RAF.write((this.Sum+"@@"+this.FileNum+"#").getBytes());
			RAF.close();
			return true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String FileCheck(long num) {
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, 18+num*102);
			byte[] bData = new byte[102];
			FIS.read(bData, 0, bData.length);
		
			
			FIS.close();
			return (new String(bData).split("#")[0]);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	

	public static void main(String[] args) {
		FileTree FT = new FileTree();
		
		//FT.BulidTree("FileTree.txt");
		
		FT.InitTree("FileTree.txt");
		System.out.println(FT.FileDelete(5));
		
		//System.out.println(FT.FileCheck(5).getMainInfo());
	}
}
