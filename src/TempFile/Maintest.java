
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;
import java.util.Vector;


public class Maintest implements Runnable {

	private Scanner input = null;
	private DirTree DT;
	private boolean Close = false;
	
	Maintest(DirTree DT){
		this.DT = DT;
		input = new Scanner(System.in);
		this.Close = true;
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(this.Close) {
			switch(input.next()) {
			case "Bulid" : {System.out.println("pleas input the tree file Name: ");DT.BulidTree(input.next());};break;
			
			case "Init" : {System.out.println("pleas input the tree file Name:"); System.out.println(DT.initTree(input.next()));};break;
			
			case "DirMake" : {
				System.out.println("pleas input the new dir name:");DT.DirMake(input.next());
			};break;
			
			case "DirIn" : {
				System.out.println("pleas input the In num:");System.out.println(DT.DirIn(input.nextLong()));
			};break;
			
			case "break" : {
				System.out.println(DT.DirBack());
			};break;
			
			case "Close":{DT.Close();
				this.Close = false;
			};break;
			
			default : break;
		}
		}
		
	}
	
	public static void main(String[] args) {
		
		String Dir = "F:\\UserDir\\User1\\ROOT\\dirA";
		
		//File dir = new File(Dir);
		
		System.out.println(ChatSocket.deleteDirectory(Dir));
		
		
		
		
	}
	
}