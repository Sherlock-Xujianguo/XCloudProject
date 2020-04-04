package TempFile;

import java.net.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.*;
import java.security.*;
import java.io.*;
import java.sql.*;

class ChatSocket implements Runnable{
	
	private KeyPair keyPair = null;					//交流密匙
	private String RSAPrivateKey = null;
	private String RSAPublicKey = null;
	private String RSAKey = null;
	
	private String DESKey = null;					//传输密匙
	
	private String UserName = "USERTEST";			//用户信息（初始默认）
	private String UserPWD = "TESTPWD";
	private String Path = "TestPath";
	//private String DESPWD = null;
	
	private Socket client = null;					//socket
	
	private DataInputStream DIS = null;
	private DataOutputStream DOS = null;
	
	private boolean IsLogin = false;				//检查是否登入
	
	private PostgreSQL SQL = null;					//数据库调用
	
	private static final String BTableName = "BaseDataTable";		//用户信息库
	
	private static final String BasePath = "/home/UserDir/";			//存储根目录（待修改）
	
	private DirTree DT = null;						//操作目录树
	private FileTree FT = null;
	
	ChatSocket(Socket client){
		this.client = client;
		try {
			
			SQL = new PostgreSQL();
			this.DT = new DirTree();
			this.FT = new FileTree();
			
			DIS = new DataInputStream(client.getInputStream());
			DOS = new DataOutputStream(client.getOutputStream());
			
			this.keyPair = RSA.getKeyPair();
			this.RSAPublicKey = RSA.getPublicKey(keyPair);
			this.RSAPrivateKey = RSA.getPrivateKey(keyPair);
			System.out.println("This RSA:"+RSAPublicKey);
			
			this.RSAKey = DIS.readUTF();
			DOS.writeUTF(RSAPublicKey);
			System.out.println("RSA: "+RSAKey);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	ChatSocket(){
		//SQL = new PostgreSQL();
	}
	
	//关闭
	public void Close() {
		try {
			this.client.close();
			this.SQL.Close();
			this.DT.Close();
			
			System.out.println("Close");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//注册(新增建造目录树文件和文件树文件)
	public void Logon(String[] CMDS) {			//注册格式：Logon:UserName:UserPWD;
		try {
			ResultSet result = SQL.Search(BTableName, "*", "WHERE UserName = "+CMDS[1]);
			if(result!=null) {
				DOS.writeUTF(RSA.encryptByPrivateKey("Logon:False:1", RSAPrivateKey));		//用户名已存在
				return ;
			}
			else {
				
				if(CMDS[2].length()>16) {
					DOS.writeUTF(RSA.encryptByPrivateKey("Logon:False:3", RSAPrivateKey));
					return ;
				}
				
				File dir = new File(BasePath+CMDS[1]);
				if(!dir.exists()) {
					dir.mkdir();
				}
				
				dir = new File(BasePath+CMDS[1]+"/ROOT");
				dir.mkdir();
				
				SQL.Insert(BTableName, "(UserName,UserPWD,UserPath,UUID) VALUES ('"+CMDS[1]+"','"+HashCipher.Cihper(CMDS[2])+"','"+BasePath+CMDS[1]+"/','"+UUIDMake.getNewUUID()+"')");
				
				DOS.writeUTF(RSA.encryptByPrivateKey("Logon:True", RSAPrivateKey));
				
				System.out.println("Over");		//用户记录入库
				
				DT.BulidTree(BasePath+CMDS[1]+"/DirRecord");		//记录树建立
				
				FT.BulidTree(BasePath+CMDS[1]+"/FileRecord");
				
			}
			
		}catch(Exception e) {
			try {
				DOS.writeUTF(RSA.encryptByPrivateKey("Logon:False:404", RSAPrivateKey));
				e.printStackTrace();
			}catch(Exception ie) {
				ie.printStackTrace();
			}
		}
	}
	
	//登录
	public boolean Login(String[] CMDS) {
		try {
			
			ResultSet result = SQL.Search(BTableName, "*", "Where UserName = '"+CMDS[1]+"'");
			
			if(result!=null&&result.next())
				
			{
				
				//System.out.println(result.getString("UserPWD"));
				//System.out.println((HashCipher.Cihper(CMDS[2])));
				
				char ca1[] = result.getString("UserPWD").toCharArray();
				
				char ca2[] = HashCipher.Cihper(CMDS[2]).toCharArray();
				
				for(int i = 0;i<ca2.length;i++) {
					if(ca1[i]!=ca2[i]) {
						System.out.println(i);
						
						DOS.writeUTF(RSA.encryptByPrivateKey("login:Fail:2", RSAPrivateKey));
						
						return false;
					}
				}
				
				System.out.println("True");
				
				this.IsLogin = true;
				this.Path = BasePath+CMDS[1]+"/";				//初始化path
				
				DOS.writeUTF(RSA.encryptByPrivateKey("login:Success", RSAPrivateKey));
				
				this.FT.InitTree(BasePath+CMDS[1]+"/FileRecord");
				
				this.DT.initTree(BasePath+CMDS[1]+"/DirRecord",FT);			//启动记录文件功能
				
				long key = new Date().getTime();
				while(key<100000000) {
					key*=10;
				}
				key = key%100000000;
				
				this.DESKey = Long.toString(key);
				
				System.out.println(DESKey.length());
				
				if(DESKey.length()<8) {
					DESKey = DESKey+"0";
				}
				
				DOS.writeUTF(RSA.encryptByPrivateKey(Path, RSAPrivateKey));
				DOS.writeUTF(RSA.encryptByPrivateKey(DESKey, RSAPrivateKey));
				
				
				return true;
			}
			else DOS.writeUTF(RSA.encryptByPrivateKey("login:Fail:1", RSAPrivateKey));
			return false;
		}catch(Exception e) {
			e.printStackTrace();
			
			try {
				DOS.writeUTF(RSA.encryptByPrivateKey("404", RSAPrivateKey));
			}catch(Exception ie) {
				ie.printStackTrace();
			}
			
			return false;
		}
	}
	
	//文件夹进入(改为从记录文件中展示 直接返回文件夹下记录段 到客户端下进行查看)
	public void DirIn(String[] CMDS) {			//CMDS格式： DirIn:DirNum;
		try {
			
			String txt = DT.DirIn(Long.valueOf(CMDS[1]));
			
			LongStringSend(txt);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getLastUpdate(String[] CMDS) {
		try {
			String txt = "LastUpdate:"+DT.LastInfo();
			
			if(DT.LastInfo().equals("")) txt = "LastUpdate: ";
			
			System.out.println(txt);
			
			LongStringSend(txt);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void DirFlush(String[] CMDS) {
		try {
			String txt = DT.DirFlush();
			
			System.out.println(txt);
			
			LongStringSend(txt);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void DirCheck(String[] CMDS) {
		try {
			String txt = "DirCheck:"+DT.DirCheck(Long.valueOf(CMDS[1]));
			
			LongStringSend(txt);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void FileCheck(String[] CMDS) {
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("FileCheck:"+FT.FileCheck(Long.valueOf(CMDS[1])), RSAPrivateKey));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void DirBack(String[] CMDS) {
		try {
			String txt = DT.DirBack();
			
			LongStringSend(txt);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void DirRename(String[] CMDS) {
		try {
			DT.DirRename(Long.valueOf(CMDS[2]), CMDS[3]);
			
			String path = this.Path+"/"+DT.getPath();
			
			File file = new File(path+CMDS[1]);
			
			file.renameTo(new File(path+CMDS[3]));
			
			DOS.writeBoolean(true);
		}catch(Exception e) {
			e.printStackTrace();
			try {
				DOS.writeBoolean(false);
			}catch(IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	


/** 
 * 删除单个文件 
 * @param   sPath    被删除文件的文件名 
 * @return 单个文件删除成功返回true，否则返回false 
 */  

public static boolean deleteFile(String sPath) {  

    boolean flag = false;  
    File file = new File(sPath);  

    // 路径为文件且不为空则进行删除  
    if (file.isFile() && file.exists()) {  
        file.delete();  
        flag = true;  
    }  
    return flag;  
}

	
/** 

 * 删除目录（文件夹）以及目录下的文件 

 * @param   sPath 被删除目录的文件路径 

 * @return  目录删除成功返回true，否则返回false 

 */  

public static boolean deleteDirectory(String sPath) {  
	
    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
    if (!sPath.endsWith(File.separator)) {  
        sPath = sPath + File.separator;  
    }  
    File dirFile = new File(sPath);  

    //如果dir对应的文件不存在，或者不是一个目录，则退出  
    if (!dirFile.exists() || !dirFile.isDirectory()) {  
        return false;  
    }  

    boolean flag = true;  

    //删除文件夹下的所有文件(包括子目录)  
    File[] files = dirFile.listFiles();  

    for (int i = 0; i < files.length; i++) {  
        //删除子文件  
        if (files[i].isFile()) {  
            flag = deleteFile(files[i].getAbsolutePath());  
            if (!flag) break;  
        } //删除子目录  
        else {  
            flag = deleteDirectory(files[i].getAbsolutePath());  
            if (!flag) break;  
        }  
    }  

    if (!flag) return false;  
    //删除当前目录  
    if (dirFile.delete()) {  

        return true;  
    } else {  
    	return false;  
    }  
}

	
	public void DirDelete(String[] CMDS) {
		try {
			String Path = DT.getPath();
			
			System.out.println(this.Path+Path+CMDS[2]);
			
			File file = new File(this.Path+Path+CMDS[2]);
			DT.DirDelete(Long.valueOf(CMDS[1]));
			if(!file.exists()) {
				DOS.writeBoolean(false);
			}
			else {
				deleteDirectory(this.Path+Path+CMDS[2]);
			}
			
			DOS.writeBoolean(true);
		}catch(Exception e) {
			e.printStackTrace();
			
			try {
				DOS.writeBoolean(false);
			}catch(Exception ie){
				ie.printStackTrace();
			}
		}
	}

	
	//文件重命名
	public void FileRename(String[] CMDS) {			//CMDS格式： FileRename:FileNum:newName:oldName
		FT.FileRename(Long.valueOf(CMDS[1]), CMDS[2]);
		DT.FileRename(CMDS[3], CMDS[2]);
		
		String path = DT.getPath();
		
		File file = new File(this.Path+path+CMDS[3]);
		
		try {
			DOS.writeBoolean(file.renameTo(new File(this.Path+path+CMDS[2])));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//文件删除
	public void FileDelete(String[] CMDS) {			//CMDS格式：FileDelete:FileName;
		try {
			
			String Path = DT.getPath();
			File file = new File(this.Path+Path+CMDS[1]);
			
			if(file.exists())
			{
				file.delete();							//文件删除
			}
			else {
				file = new File(this.Path+Path+CMDS[1]+".gz");
				System.out.println("Here");
				if(file.exists()) {
					System.out.println("right");
					file.delete();
				}else {
					int num =FilePort.PartFileExist(this.Path+Path+CMDS[1]+".gz");
					for(int i = 0;i<num;i++) {
						file = new File(this.Path+Path+CMDS[1]+".gz_"+i+".part");
						file.delete();
					}
				}
			}
			
			DT.FileDelete(CMDS[1]);
			
			DOS.writeBoolean(true); 				//完成提示
			
		}catch(Exception e) {
			e.printStackTrace();
			try {
				DOS.writeBoolean(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	
	public void DirMake(String[] CMDS) {				//CMDS格式： DirMake:DirName;
		
		String path = DT.getPath();
		System.out.println(this.Path+path+CMDS[1]);
		File file = new File(this.Path+path+CMDS[1]);
		try {
			boolean t = file.mkdir();
			DOS.writeBoolean(t);
			if(t) {
				DT.DirMake(CMDS[1]);
			}
		}catch(Exception e) {
			try {
				
				e.printStackTrace();
				
				DOS.writeBoolean(false);
			}catch(IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	//文件传输
	public void FileGet(String[] CMDS) {				//CMDS格式：FileSend:FileName:Filelength:FileGetTime:FileDES (FileIsUsing 自动改为true）
		try{
			
			LongStringSend(Path+DT.getPath());
			DT.FileAdd(CMDS[1], Long.valueOf(CMDS[2]));
			DOS.writeBoolean(true);
		
		}catch(Exception e) {
			try {
				DOS.writeBoolean(false);
				e.printStackTrace();
			}catch(IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	public void FileSend(String[] CMDS) {				//CMDS格式： FileRead:FileName
		try {
			
			LongStringSend(Path+DT.getPath());
			DOS.writeBoolean(true);
		}catch(Exception e) {
			
			e.printStackTrace();
			
			try {
				DOS.writeBoolean(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void GetPath(String[] CMDS) {
		String txt = "GetPath:"+DT.getPath();
		try {
			LongStringSend(txt);
		}catch(IOException e) {
			e.printStackTrace();
		}catch(Exception e) {
			try {
				DOS.writeUTF(RSA.encryptByPrivateKey("GetPath:", RSAPrivateKey));
				DOS.writeUTF(RSA.encryptByPrivateKey("Over", RSAPrivateKey));
			}catch(Exception ie) {
				ie.printStackTrace();
			}
		}
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
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			while(!client.isClosed()) {
				String CMD = LongStringGet();
				System.out.println(CMD);
				
				String[] CMDS = CMD.split(":");
				
				switch(CMDS[0]) {
				
				case "login" : Login(CMDS);break;
				case "logon" : Logon(CMDS);break;
				case "FileDelete" : FileDelete(CMDS);break;
				case "DirIn" : DirIn(CMDS);break;
				case "DirMake" : DirMake(CMDS);break;
				case "DirFlush" : DirFlush(CMDS);break;
				case "GetPath" : GetPath(CMDS);break;
				case "DirRename" : DirRename(CMDS);break;
				case "Back" : DirBack(CMDS);break;
				case "LastUpdate" : getLastUpdate(CMDS);break;
				//case "FileShow" : FileShow(CMDS);break;
				case "Close" : Close();break;
				case "DirDelete" : DirDelete(CMDS);break;
				case "FileCheck" : FileCheck(CMDS);break;
				case "FileRename" : FileRename(CMDS);break;
				case "FileSend" : FileGet(CMDS);break;
				case "FileRead" : FileSend(CMDS);break;
				case "DirCheck" : DirCheck(CMDS);break;
				default : break;
				
				}			
			}
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
}

public class MainServer {
	static int PORT = 4001;					//信息服务器端口
	static ServerSocket SERVER = null;		//服务器嵌套字
	static ExecutorService pool = null;		//线程池
	
	
	
	MainServer(int port) {
		this.PORT = port;
	}
	
	MainServer(){
		this(4001);
	}
	
	public static ServerSocket init() {
		try {
			SERVER  = new ServerSocket(PORT);			//开启服务嵌套字
			pool = Executors.newCachedThreadPool();		//开启线程池
		}catch(Exception e) {
			System.out.println("Server start error");
			e.printStackTrace();
		}
		
		return SERVER;
	}
	
	public void Accept() {
		try {
			while(true) {
				Socket client = SERVER.accept();
			
				ChatSocket CS= new ChatSocket(client);
			
				pool.execute(CS);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		MainServer MS = new MainServer();
		
		MS.init();
		
		MS.Accept();
		//ChatSocket CS = new ChatSocket();
		
		//String[] CMDS = {"Login","TestUser","PassWord"};
		
		//CS.Logon(CMDS);
		//System.out.println(CS.Login(CMDS));
	}
	
}
