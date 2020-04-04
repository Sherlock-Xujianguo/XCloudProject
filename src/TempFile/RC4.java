
import java.io.*;
import java.lang.*;
import java.util.*;


public class RC4 {

	public static byte[] HloveyRC4(byte[] InputData,String aKey)   
    {   
        int[] iS = new int[256];   
        byte[] iK = new byte[256];   

        for (int i=0;i<256;i++)   
            iS[i]=i;   

        int j = 1;   

        for (short i= 0;i<256;i++)   
        {   
            iK[i]=(byte)aKey.charAt((i % aKey.length()));   
        }   

        j=0;   

        for (int i=0;i<255;i++)   
        {   
            j=(j+iS[i]+iK[i]) % 256;   
            int temp = iS[i];   
            iS[i]=iS[j];   
            iS[j]=temp;   
        }   

        int i=0;   
        j=0;   	//aaaaaaaaaaaaa   
        byte[] OutputData = new byte[InputData.length];   //aaaaaaa
        for(int x = 0;x<InputData.length;x++)   //aaaaaaa
        {   
            i = (i+1) % 256;   
            j = (j+iS[i]) % 256;   
            int temp = iS[i];   
            iS[i]=iS[j];   
            iS[j]=temp;   
            int t = (iS[i]+(iS[j] % 256)) % 256;   
            int iY = iS[t];   
            int iCY = iY;   
            OutputData[x] =(byte) (InputData[x] ^ iCY) ;      
        }   

        return OutputData;   

    }  
	
	public static void main(String[] args) {
		String key = "123456";
		
		File InFile = new File("in.bin");
		File OutFile = new File("Out.bin");
		
		byte[] bData = new byte[1024];
		byte[] bData_ = new byte[1024];
		int length;
		try {
			FileInputStream FIS = new FileInputStream(InFile);
			FileOutputStream FOS = new FileOutputStream(OutFile);
		
			while((length = FIS.read(bData, 0, bData.length))!=-1) {
				bData_ = HloveyRC4(bData, key);
				FOS.write(bData_, 0, length);
				FOS.flush();
			}
			FIS.close();
			FOS.close();
			
			FIS = new FileInputStream(OutFile);
			InFile = new File("InTest.bin");
			FOS = new FileOutputStream(InFile);
			
			while((length = FIS.read(bData, 0, bData.length))!=-1) {
				bData_ = HloveyRC4(bData, key);
				FOS.write(bData_, 0, length);
				FOS.flush();
			}
			FIS.close();
			FOS.close();
			
		}catch(IOException ie) {
			System.out.println("error");
			ie.printStackTrace();
		}
	}
}
