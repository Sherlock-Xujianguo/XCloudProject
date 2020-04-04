
import java.io.*;
import java.awt.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FilePort{
	
	public static int FileSize(File file) {
		if(file.length()<1024*1024*10){
			return 1;
		}
		else if(file.length()<1024*1024*100) {
			return 2;
		}
		else return 3;
	}
	
	
	
	public static RandomAccessFile getRAF(File file,long length) throws IOException{			//接收文件时 如果文件存在则将文件输入位跳到上次结尾处
		RandomAccessFile tmFile = new RandomAccessFile(file, "rw");
		tmFile.seek(length);
		return tmFile;
	}
	
	public static FileInputStream getFIS(File file,long length) throws IOException{			//传输文件时 如果是上次传输中断则继续传输
		FileInputStream FIS = new FileInputStream(file);
		FIS.skip(length);
		return FIS;
	}
	
	public static int PartFileExist(String fileName) {
		int num = 0;
		File file = new File(fileName+"_"+(num+1)+".part");
		for (num=0;file.exists();num++) {
			file = new File(fileName+"_"+(num+1)+".part");
		}
		
		return num-1;		//从1开始
	}
	
	public static String GZipFile(String FileName) {
		FileInputStream FIS;
		BufferedOutputStream BOS;
		FileOutputStream FOS;
		BufferedInputStream BIS;
		
		try {
			FIS = new FileInputStream(new File(FileName));
			//ZipEntry ZE = new ZipEntry()
			byte[] bData = new byte[1024*10];
			String oldFileName = FileName+".gz";
			BOS = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(oldFileName)));
			int length;
			while((length = FIS.read(bData, 0, bData.length))!=-1) {
				BOS.write(bData, 0, bData.length);
				BOS.flush();
			}
			BOS.close();
			FIS.close();
			return (oldFileName);
		}catch(Exception ie) {
			System.out.println("error");
			System.err.println(ie);
			ie.printStackTrace();
			return null;
		}
	}
	
	public static void GZtoFile(String FileName1,String FileName2) {
			
		try {
			BufferedInputStream BIS = new BufferedInputStream(new GZIPInputStream(new FileInputStream(FileName1)));
			BufferedOutputStream BOS = new BufferedOutputStream(new FileOutputStream(FileName2));
			
			byte[] bData = new byte[1024*10];
			int length;
			while((length = BIS.read(bData, 0, bData.length))!=-1) {
				TimeUnit.MILLISECONDS.sleep(3);
				System.out.println(length);
				BOS.write(bData, 0, length);
				BOS.flush();
			}
			BIS.close();
			BOS.close();
				
		}catch(Exception e) {
			System.out.println("ERROR");
			System.err.println(e);
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(GZipFile("SumTree_.txt"));
	}
}


