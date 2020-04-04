
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.*;
import java.security.*;
import java.text.SimpleDateFormat;
import java.io.*;

class Contorl implements Runnable{
	
	private Scanner input = null;
	private MainClient MC = null;
	private boolean IsClose = false;
	
	Contorl(MainClient MC){
		this.MC = MC;
		input = new Scanner(System.in);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!IsClose) {
			String CMD = input.next();
			switch(CMD) {
			case "Login" : {System.out.println("Please input UserName and PWD");System.out.println(MC.Login(input.next(), input.next()));}break;
			
			//case "FileShow" : {MC.FileShow();};break;
			
			case "Logon" : {
				System.out.println("Please input UserName and PWD");System.out.println(MC.Logon(input.next(), input.next()));
			};break;
			
			case "DirMake" : {
				System.out.println("Please input the new DirName");System.out.println(MC.DirMake(input.next()));
			};break;
			
			case "DirRename":{
				System.out.println("Please input the OldName,Dirnum,NewName");System.out.println(MC.DirRename(input.next(), Long.valueOf(input.next()), input.next()));
			};break;
			
			case "DirDelete" : {
				System.out.println("Please input the Dirnum,Dirname");System.out.println(MC.DirDelete(input.nextLong(), input.next()));
			};break;
			
			case "DirCheck" : {
				System.out.println("Please input the Dirnum");System.out.println(MC.DirCheck(input.nextLong()).getDirChangeTime());
			};break;
			
			case "DirIn" : {
				System.out.println("please input the Dirnum");System.out.println(MC.DirIn(input.nextLong()));
			};break;
			
			case "Flush" : {
				System.out.println(MC.DirFlush());
			};break;
			
			case "Back" : {
				System.out.println(MC.Back());
			};break;
			
			case "FileTranslate" : {
				System.out.println("Please input the mode,LocalPath,FileName,Speed");System.out.println(MC.FileTranslate(input.next(), input.next(), input.next(), input.nextLong(), new CAB()));
			};break;
			
			case "FileReName" : {
				System.out.println("Please input the Filenum NewName OldName");System.out.println(MC.FileRename(input.nextLong(), input.next(), input.next()));
			};break;
			
			case "FileDelete" : {
				System.out.println("Please input the FileName");System.out.println(MC.FileDelete(input.next()));
			};break;
			
			case "FileCheck" : {
				System.out.println("please input the Filenum");System.out.println(MC.FileCheck(input.nextLong()).getFileChangeTime());
			};break;
			
			//case "FileDelete" : {System.out.println("Please input the FileName you want to delete");MC.FileDelete(input.next());};break;
			
			//case "FileTranslate" : {System.out.println("please input the mode the FileName theLimitSpeed");MC.FileTranslate(input.next(), input.next(), Long.valueOf(input.next()));};break;
			
			case "Close" : MC.Close();this.IsClose = true;break;
			
			default : break;
			}
		}
	}
	
}


public class MainClient {
	
	private KeyPair keyPair = null;
	private String RSAPrivateKey = null;
	private String RSAPublicKey = null;
	private String RSAKey = null;
	
	private String DESKey = null;
	
	private boolean IsLogin = false;
	
	Socket client = null;
	
	private DataInputStream DIS = null;
	private DataOutputStream DOS = null;
	
	private String Path = null;
	private String UserName = null;
	
	private String hostName = null;
	private SocketClient FileClient = null;
	
	MainClient(String hostName,int port){
		try {
			client = new Socket(hostName,port);
			
			DIS = new DataInputStream(client.getInputStream());
			DOS = new DataOutputStream(client.getOutputStream());
			
			this.keyPair = RSA.getKeyPair();
			this.RSAPublicKey = RSA.getPublicKey(keyPair);
			this.RSAPrivateKey = RSA.getPrivateKey(keyPair);
			
			this.hostName = hostName;
			
			//System.out.println("This RSA:"+RSAPublicKey);
			
			DOS.writeUTF(RSAPublicKey);
			this.RSAKey = DIS.readUTF();
			
			//System.out.println("RSA: "+RSAKey);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	MainClient(){
		this("182.92.197.26",4001);
	}
	
	public void LongStringSend(String txt) throws IOException, Exception {
		
			int begin = 0;
			//char[] Data = txt.toCharArray();
			int size = txt.length();
			while(size-begin>53) {
				DOS.writeUTF(RSA.encryptByPrivateKey(txt.substring(begin, begin+53), RSAPrivateKey));
				
				begin+=53;
			}
			DOS.writeUTF(RSA.encryptByPrivateKey(txt.substring(begin), RSAPrivateKey));
			
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
		
	}
	
	public String LongStringGet() throws IOException, Exception {
			String CMD = new String();
			String txt = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
			while(!txt.equals("Over")) {
				CMD = CMD+txt;
				txt = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
			}
			
			return CMD;
	}
	
	public void Close() {
		try {
			if(DOS!=null) {
				try {
					DOS.writeUTF(RSA.encryptByPrivateKey("Close:", RSAPrivateKey));
					DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//登录
	public String Login(String UserName,String PWD) {			//返回登入信息
		try {
			
			DOS.writeUTF(RSA.encryptByPrivateKey("login:"+UserName+":"+PWD, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
			
			String[] CMDS = CMD.split(":");
			
			if(CMDS[0].equals("login")) {
				if(CMDS[1].equals("Success")) {
					//System.out.println("Login Success");
					
					this.IsLogin = true;
					
					this.Path = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
					
					this.DESKey = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
					
					//System.out.println("Path: "+Path+" DES: "+DESKey);
					
					this.UserName = UserName;
					
					return "T";
				}
				else {
					switch(CMDS[2]) {
					case "1" : return "F:1-ErrorUserName";
					case "2" : return "F:2-ErrorPWD";
					}
					//System.out.println("Login Fail");
				}
			}else {
				System.out.println("ERROR");
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "F:404-LinkLose";
	}
	
	public String Logon(String UserName,String PWD) {				//返回一个字符串 格式T/F:Failreason
		try {
			
			DOS.writeUTF(RSA.encryptByPrivateKey("logon:"+UserName+":"+PWD, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
			
			String[] CMDS = CMD.split(":");
			if(CMDS[0].equals("Logon")) {
				if(CMDS[1].equals("True")) {
					return "T";
				}
				else {
					switch(CMDS[2]) {
					case "1" : return "F:1-UserName exits";
					case "2" : return "F:2-Error form of UserName";
					case "3" : return "F:3-Error form of PWD";
					}
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "F:404-LinkLose";
	}
	
	public String DirIn(long DirNum) {								//返回一个字符串 显示当前进入文件夹信息 格式：DirInfo;FileName-num,FileName-num...;DirName@num
		
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("DirIn:"+DirNum, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = LongStringGet();
			
			return CMD;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public String[] getLastUpdate() {							//返回一个字符串数组 为据上次登入退出的总文件夹信息更新
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("LastUpdate:", RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = LongStringGet();
			
			String[] CMDS = CMD.split(":");
			
			if(CMDS[0].equals("LastUpdate")) {
				return CMDS[1].split(";");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String DirFlush() {										//刷新当前文件夹 返回值与DirIn 相同
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("DirFlush:", RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = LongStringGet();
			
			//System.out.println(CMD);
			
			return CMD;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public DirInfoObject DirCheck(long DirNum) {							//返回一个文件夹信息类
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("DirCheck:"+DirNum, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = LongStringGet();
			String[] CMDS = CMD.split(":");
			
			if(CMDS[0].equals("DirCheck")) {
				//System.out.println(CMDS[1].split(";")[0]);
				return new DirInfoObject("T@@"+CMDS[1].split(";")[0]);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public FileInfo FileCheck(long FileNum) {						//返回一个文件信息类
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("FileCheck:"+FileNum, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
			String[] CMDS = CMD.split(":");
			
			if(CMDS[0].equals("FileCheck")) {
				return new FileInfo(CMDS[1]);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public boolean DirRename(String OldName,long DirNum,String NewName) {							//返回一个bool值确认是否完成（更改）
		try {
			String txt ="DirRename:"+OldName+":"+DirNum+":"+NewName;
			
			LongStringSend(txt);
			
			return DIS.readBoolean();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	public boolean DirMake(String DirName) {						//在当前目录下新建一个文件夹
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("DirMake:"+DirName, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			return DIS.readBoolean();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean DirDelete(long Dirnum,String DirName) {							//删除指定文件夹(更新)
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("DirDelete:"+Dirnum+":"+DirName, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			return DIS.readBoolean();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean FileDelete(String FileName) {    					//删除指定文件 （更改）
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("FileDelete:"+FileName, RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			return DIS.readBoolean();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean FileRename(long Filenum,String NewName,String OldName) {		//指定文件重命名
		try {
			String txt = "FileRename:"+Filenum+":"+NewName+":"+OldName;
			
			LongStringSend(txt);
			
			return DIS.readBoolean();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public String getPath() {					//获取当前路径
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("GetPath:", RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = LongStringGet();
			
			String[] CMDS = CMD.split(":");
			
			if(CMDS[0].equals("GetPath")) {
				return CMDS[1];
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String Back() {
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("Back:", RSAPrivateKey));
			DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			
			String CMD = LongStringGet();
			
			return CMD;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	
	//文件传输 成功启动返回true 否则返回false
	public boolean FileTranslate(String mode,String LocalPath,String FileName,long LimitedSpeed,CallBack CB) {				//读/写 本地文件名 远程文件名 限速
		try {
						
			if(mode.equals("Send")) {							//文件传输
				File file = new File(LocalPath+FileName);
				
				if(!file.exists()) {
					System.out.println("FileNotExits");
					return false;
				}
				
				String txt = "FileSend:"+FileName+":"+file.length();

				LongStringSend(txt);
				String Path = LongStringGet();
				
				String toFileName = Path+FileName;

				
				
				boolean reply = DIS.readBoolean();
				
				if(reply) {
					//System.out.println("Send test start");
					FileClient = new SocketClient(hostName,4000,"Send",LocalPath+FileName,Long.toString(LimitedSpeed),toFileName,DESKey,CB);
					FileClient.ClientFirstStart("Send", LocalPath+FileName, Long.toString(LimitedSpeed), toFileName);
					
					//System.out.println("Start");
					
					return true;
				}
				else return false;
				
			}
			else if(mode.equals("Read")) {
				
				DOS.writeUTF(RSA.encryptByPrivateKey("FileRead:"+FileName, RSAPrivateKey));
				DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
				
				
				String Path = LongStringGet();
				boolean reply = DIS.readBoolean();
				String toFileName = Path+FileName;
				
				if(reply) {
					//System.out.println("Read test start");
					FileClient = new SocketClient(hostName,4000,"Read",LocalPath+FileName,Long.toString(LimitedSpeed),toFileName,DESKey,CB);
					FileClient.ClientFirstStart("Read", LocalPath+FileName, Long.toString(LimitedSpeed), toFileName);
					
					//System.out.println("Start");
					
					return true;
				}
				else return false;
	
			}
			else {
				System.out.println("mode error");
				return false;
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public long getSpeed() {
		return this.FileClient.getSpeedWatch().getSpeed();
	}
	
	public void Pause() {
		this.FileClient.Pause();
	}
	
	public void Action() {
		this.FileClient.Active();
	}
	
	public static void main(String[] args) {
		
		MainClient MC = new MainClient();
		
		Contorl C = new Contorl(MC);
		
		Thread thread = new Thread(C);
		
		thread.start();
		
	//	if(MC.client.isClosed())System.out.println("Close");
		
		//MC.Logon("User1", "12345678");
		
		//System.out.println(MC.Login("User1", "12345678"));
		
		//System.out.println(MC.getLastUpdate()[0]);
		
		//System.out.println(MC.DirIn(0));
		
		//System.out.println(MC.FileDelete("JPGB.jpg"));
		
		//System.out.println(MC.DirIn(3));
		
		//System.out.println(MC.FileRename(15, "JPG.jpg", "JPGB.jpg"));
		
		//FileInfo FI = MC.FileCheck(15);
		//System.out.println(FI.getFileName()+" "+FI.getFileLength());
		
		//System.out.println(MC.DirCheck(0).getDirChangeTime());
		
		//MC.FileDelete("JPGA.jpg");
		
		//MC.DirDelete(2, "DirB");
		
	//	System.out.println(MC.FileTranslate("Read", "D:\\Data\\", "six1.mkv", -1, new CBB()));
		
		//System.out.println(MC.getPath());
		
		//System.out.println(MC.DirMake("DirB"));
		
	//	System.out.println(MC.DirFlush());
		
	//	System.out.println(MC.Back());
		
		//System.out.println(MC.DirRename("DirB", 2, "DirC"));
		
		//System.out.println(;
		
		//System.out.println(MC.DirMake("dirB"));
		
	//	System.out.println(MC.DirFlush());
		
	//	MC.Close();
		
		
	}
	
}

class CBB implements CallBack{

	@Override
	public void callback() {
		// TODO Auto-generated method stub
		System.out.println("Over test");
	}
	
}
