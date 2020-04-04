import java.io.*;
import java.net.*;

import jdk.jfr.Threshold;


public class ClientTest {

    public static void main(String[] args) {
    	String hostname = "localhost";
    	InputStream IS;
    	OutputStream OS;
    	DataInputStream DIS;
    	PrintStream PS;
    	try {
    		Socket theSocket = new Socket(hostname,4000);
    		System.out.println("Link started!\n****************\n");
    		IS = theSocket.getInputStream();
    		OS = theSocket.getOutputStream();
    		DIS = new DataInputStream(IS);
    		PS = new PrintStream(OS);
    		DataInputStream in = new DataInputStream(System.in);
    		while(true) {
    			System.out.println("you say: ");
    			String s = in.readLine();
    			PS.println(s);
    			if(s.trim().equals("Signout")) break;
    			else {
    				System.out.println("\nPlease Wait rhe sever...\n");
    			}
    			s=DIS.readLine();
    			System.out.println("the sever said: "+s);
    			if(s.trim().equals("Close")) break;
    		}
    		DIS.close();
    		PS.close();
    		IS.close();
    		OS.close();
    		theSocket.close();
    	}
    	catch(IOException e) {
    		System.out.println("error");
    		System.err.println(e);
    	}
    }

}