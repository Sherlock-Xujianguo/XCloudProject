import java.net.*;
import java.io.*;
import java.lang.*;


public class ChartSever {
	
	static int SEVER_PORT = 4000;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ServerSocket severSocket;
		InputStream IS;
		OutputStream OS;
		DataInputStream DIS;
		PrintStream PS;
		String s;
		try {
			severSocket = new ServerSocket(SEVER_PORT);
			System.out.println("Sever started!\n****************\n");
			Socket clientSocket = severSocket.accept();
			IS = clientSocket.getInputStream();
			OS = clientSocket.getOutputStream();
			DIS = new DataInputStream(IS);
			PS = new PrintStream(OS);
			DataInputStream in = new DataInputStream(System.in);
			while(true) {
				System.out.println("--------------");
				System.out.println("Waiting...\n");
				s= DIS.readLine();
				System.out.println("the user send: "+s);
				if(s.trim().equals("Signout")) break;
				System.out.println("you sed: ");
				s=in.readLine();
				PS.println(s);
				if(s.trim().equals("Close")) break;
			}
			DIS.close();
			PS.close();
			IS.close();
			OS.close();
			clientSocket.close();
			severSocket.close();
		}
		catch(Exception e) {
			System.out.println("Error");
			System.err.println(e);
		}
	}

}
