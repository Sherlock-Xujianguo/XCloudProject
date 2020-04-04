
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.lang.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;



public class IOStream {

	
	
	//数据流
	public static InputStream DataIn(InputStream in) throws IOException{
		in = new DataInputStream(in);
		return in;
	}
	
	public static OutputStream Dataout(OutputStream out) throws IOException{
		out = new DataOutputStream(out);
		return out;
	}
	
	//缓冲流
	public static InputStream BufferedIn(InputStream in) throws IOException{
		in = new BufferedInputStream(in);
		return in;
	}
	
	public static OutputStream BufferedOut(OutputStream out) throws IOException{		
		out = new BufferedOutputStream(out);
		return out;
	}
	
	//Gz压缩流
	public static InputStream GZipIn(InputStream in) throws IOException{					//解压流
		in = new GZIPInputStream(in);
		return in;
	}
	
	public static OutputStream GZipout(OutputStream out) throws IOException{					//压缩流
		out = new GZIPOutputStream(out);
		return out;
	}
	
	//DES加密流
	public static InputStream DESIn(InputStream in,String PassWord) throws Exception{		//DES加密
		SecretKey Skey = new SecretKeySpec(PassWord.getBytes(),"DES") ;		//密匙建立
		Cipher cipher = Cipher.getInstance("DES");							//密码建立
		cipher.init(Cipher.ENCRYPT_MODE, Skey);
		
		in = new CipherInputStream(in, cipher);//加密流
		return in;
	}
	
	public static OutputStream DESOut(OutputStream out,String PassWord) throws Exception{		//DES解密
		SecretKey SKey = new SecretKeySpec(PassWord.getBytes(), "DES");	//密匙建立
		Cipher cipher = Cipher.getInstance("DES");						//解密密码建立
		cipher.init(Cipher.DECRYPT_MODE, SKey);							
		
		out = new CipherOutputStream(out, cipher);						//解密流
		return out;
	}
	
	public static void main(String[] args) {
		File file = new File("outTest2.bin");
		
		File file2 = new File("TTT.bin");
		
		int length = 0;
		
		byte[] b = new byte[1024];
		try {
			FileOutputStream FOS = new FileOutputStream(file);
		
			FileInputStream FIS = new FileInputStream(file2);

			InputStream IS = IOStream.DESIn(FIS, "12345678");
			
			while((length = IS.read(b, 0, b.length))!= -1) {
				System.out.println(length);
				FOS.write(b, 0, length);
				FOS.flush();
			}
			System.out.println("Over");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
}
