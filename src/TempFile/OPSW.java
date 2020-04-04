import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.*;


public class OPSW {
	public RandomAccessFile RAF = null;
	public OutputStream OS = null;	
	
	OPSW(OutputStream OS,RandomAccessFile RAF){
		this.RAF = RAF;
		this.OS = OS;
	}
}
