
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.*;
import javax.net.ssl.*;

import javax.net.SocketFactory;



import java.lang.*;

class splitSpeedWatch implements Runnable{						//配合SpeedWatch一起使用 用于显示速度
	private ArrayList<SpeedWatch> SpeedList = null;
	
	public long speed = 0;										//直接调用
	
	public splitSpeedWatch() {
		// TODO Auto-generated constructor stub
		SpeedList = new ArrayList<SpeedWatch>();
	}
	
	public void addSpeedWatch(SpeedWatch SW) {
		SpeedList.add(SW);
	}
	
	public long getSpeed() {
		return this.speed;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			TimeUnit.MILLISECONDS.sleep(100);
			while(SpeedList!=null&&SpeedList.get(0).Nclose) {
				TimeUnit.SECONDS.sleep(1);
				speed = 0;
				for(int i = 0;i<SpeedList.size();i++) {
					speed+=SpeedList.get(i).Speed();
				}
				System.out.println("The Speed: "+speed);
			}
		}catch(InterruptedException ie) {
			System.out.println("SpeedWatch error");
		}
		
	}
	
}


@SuppressWarnings("deprecation")
class SpeedWatch implements Runnable,Observer{
	
	public long start = 0;
	public long end = 0;
	public boolean Nclose = true;	//传输通道关闭为false
	private long sum = 0;			//用于计算
	private ClientFileTranslate Client = null;
	
	SpeedWatch(ClientFileTranslate Client){
		this.Client = Client;
	}
	
	public long Speed() {
		start = end;
		end = Client.index;
		//System.out.println("Start: "+start+"end: "+end+"Speed: "+(end-start)+" kb/s");
		return end-start;
	}
	
	@Override
	public synchronized void run() {		//添加同步锁 保证计算速度时不传输
		// TODO Auto-generated method stub
		while(Nclose) {
			try {
				TimeUnit.SECONDS.sleep(1);
			}catch(InterruptedException ie) {System.out.println("Speed watch error");}
			if(Nclose) {
				//Speed();			//显示前1S内的传输速度
				Client.Sindex =  0;	//重置限速开关
				Client.limit = true;
			}
			else {System.out.println("Closed");}
		}
		return ;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		Nclose = false;
		System.out.println("will close");
	}
	
}

@SuppressWarnings("deprecation")
class ClientFileTranslate extends Observable implements Runnable{				//文件传输类
	Socket client = null;
	InputStream INS = null;
	OPSW OPS = null;
	String RC4PassWord = null;
	CallBack CB = null;
	
	public int index = 0;		//统计传输数量
	public int Sindex = 0;		//统计每秒传输数量
	public boolean limit = true; 		//限速开关 true时保持传输 false时 睡眠停止传输
	
	public long LimitSpeed = -1;		//限制最大传输速度 -1时不限速
	public SpeedWatch SW = null;
	
	private String mode = null;
	
	private boolean IsPasue = false;
	
	ClientFileTranslate(Socket client,InputStream INS,OPSW OPS,String RC4PassWord,CallBack CB,long LimitSpeed,String mode) {		//初始化
		// TODO Auto-generated constructor stub
		this.client = client;
		this.INS = INS;
		this.OPS = OPS;
		this.RC4PassWord = RC4PassWord;
		this.CB = CB;
		this.index = 0;
		this.Sindex  = 0;
		this.limit = true;
		this.LimitSpeed = LimitSpeed;
		this.mode = mode;
		
		SW = new SpeedWatch(this);		//新建监视器
	}
	
	public synchronized void Pause() {
		this.IsPasue = true;
	}
	
	public synchronized void Action() {
		this.IsPasue = false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
		byte[] bData = new byte[1024];
		int length = 0;
		byte[]sData;
		
		this.addObserver(SW);
		
		Thread thread = new Thread(SW);
		thread.start();
		
		if(OPS.OS!=null) {				//文件输出限速
			while(SW.Nclose) {
				if(LimitSpeed<0||limit) {				//检测是否限速
					while(!IsPasue&&limit&&(length = INS.read(bData, 0, bData.length))!=-1) {
						//System.out.println(length);
						index++;
						Sindex++;
						//System.out.println(index);
						OPS.OS.write(bData, 0, length);
						OPS.OS.flush();
						if(mode.equals("Send")&&LimitSpeed>0&&Sindex>=LimitSpeed) limit = false;			//达到最高速度限速
					}
					if(length == -1) {
						super.setChanged();
						notifyObservers();
						
					}
				}
				else {
					try {
						TimeUnit.MILLISECONDS.sleep(10);			//限速睡眠1小段时间
					}catch(InterruptedException ie) {
						System.out.println("Speed limit error");
					}
				}
			}
		
			OPS.OS.close();
			INS.close();
		}
	
		else {						//文件输入由文件发送端进行限速控制 因此不需要进行限速管理
			while((length = INS.read(bData, 0, bData.length))!=-1) {
					//System.out.println(length);
					//sData = RC4.HloveyRC4(bData, RC4PassWord);
					index++;
					Sindex++;
					OPS.RAF.write(bData, 0, length);
				//	if(Sindex>LimitSpeed) limit = false;		//与上对称
			}
			super.setChanged();
			notifyObservers();
			
			OPS.RAF.close();
			INS.close();
		}
		CB.callback();
		
	}catch(IOException ie) {
		System.out.println("File Translate error");
		ie.printStackTrace();
	}
	}

}

class MergeFileCallBack implements CallBack{				//分段文件接收后整合类
	private FileSplit FS = null;
	private String FileName = null;
	private int FileNum = 0;
	private SocketClient Client = null;
	
	private int index = 0;
	
	
	
	MergeFileCallBack(String FileName,int FileNum,SocketClient Client){
		this.FileName = FileName;
		this.FileNum = FileNum;
		this.Client = Client;
	}
	
	public synchronized void callback() {
		index++;
		if(index >= FileNum) {
			FS = new FileSplit();
			try {
				FS.mergePartFiles(FileNum, FileName+".gz", new MFFileCallBack(FileName, FileNum,FS, Client));
			}catch(Exception e) {
				System.out.println("File Merge Error");
				e.printStackTrace();
			}
		}
	}
		
}

class MFFileCallBack implements CallBack{ 				//合并后解压
	private String FileName = null;
	private int FileNum = 0;
	private CallBack CB = null;
	private int index = 0;
	private FileSplit FS = null;
	
	
	MFFileCallBack(String FileName,int FileNum,FileSplit FS,CallBack CB){
		this.FileName = FileName;
		this.FileNum = FileNum;
		this.CB = CB;
		this.FS = FS;
	}
	
	public synchronized void callback(){
		index++;
		if(index >=FileNum) {
			System.out.println("file merge over");
			FS.threadPool.shutdown();
			FilePort.GZtoFile(FileName+".gz", FileName);
			File file = new File(FileName+".gz");
			file.delete();
			
			for(int i = 0;i<FileNum;i++) {
				file = new File(FileName+".gz_"+(int)(i+1)+".part");
				file.delete();
			}
			
			CB.callback(); 				//启动客户端最后回调函数
		}
	}
	
}

class SFSumCallBack implements CallBack{

	private SFClientCallBack SFCCB;
	
	SFSumCallBack(SFClientCallBack SFCCB){
		this.SFCCB = SFCCB;
	}
	
	@Override
	public void callback() {
		// TODO Auto-generated method stub
		
		SFCCB.Sum();
		return ;
	}
	
	
}

class SFClientCallBack implements CallBack{			//文件分割传输辅助类
	private FileSplit FS = null;
	private String hostName;
	private int HTTPS_PORT = 0;
	private String mood = null;
	private String NewFileName = null;
	private String LSpeed = null;
	private String SFileName = null;
	private int num = 0;
	private ArrayList<SocketClient>ClientList = null;
	private ArrayList<Thread> ThreadList = null;
	private long LimitSpeed = -1;
	
	private String DESPassWord = null;
	
	private Vector<ClientFileTranslate> TranslateList = null;
	private CallBack callBack = null;
	private splitSpeedWatch SSW = null;
	
	private int tempnum = 0;
	
	SFClientCallBack(FileSplit FS,String hostName,int port, String mood,String NewFileName,String LSpeed,String SFileName,ArrayList<SocketClient>ClientList,ArrayList<Thread>ThreadList,String DESPassWord,Vector<ClientFileTranslate> TranslateList,CallBack callBack,splitSpeedWatch SSW){
		this.FS = FS;
		this.hostName = hostName;
		this.HTTPS_PORT = port;
		this.mood = mood;
		this.NewFileName = NewFileName;
		this.LSpeed = LSpeed;
		this.SFileName = SFileName;
		this.ClientList = ClientList;
		this.ThreadList = ThreadList;
		
		this.DESPassWord = DESPassWord;
		
		this.TranslateList = TranslateList;
		
		this.callBack = callBack;
		this.SSW = SSW;
		
		
		LimitSpeed = Long.parseLong(LSpeed);
		try {
		FS.splitBySize(NewFileName, 1024*1024*10,SFClientCallBack.this);
		System.out.println("all num:"+FS.num);
		FS.splitStart();
		}catch (Exception e) {}
		
	}
	
	public void Sum() {
		this.tempnum++;
		
		System.out.println("Split: "+tempnum);
		if(this.tempnum >= num) {
			System.out.println("Spilt Here");
			
			this.callBack.callback();
		}
	}
	
	@Override
	public synchronized void callback() {
		// TODO Auto-generated method stub
		this.num++;
		System.out.println("num: "+num);
		if(num >= FS.num) {
			FS.threadPool.shutdown();
			try {
			FileSend();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ;
	}
	
	public void FileSend() throws Exception{
		
		SplitFileSendCallBack SFSCB = new SplitFileSendCallBack(FS.num,NewFileName,this.callBack);
		
		SFSumCallBack SFCB = new SFSumCallBack(this);
		//File file = new File(NewFileName);
		//file.delete();
		if(LimitSpeed>0) {
			for(int i = 0;i<FS.num;i++) {													//开启多个传输客户端嵌套字
				System.out.println("Split send start");
				
				SocketClient SSC = new SocketClient(this.hostName, this.HTTPS_PORT,this.mood,NewFileName+"_"+(int)(i+1)+".part",this.LSpeed,SFileName+".gz_"+(int)(i+1)+".part",this.DESPassWord,SFCB);
				
				System.out.println(i+"begin");
				
				if (SSC.client.isClosed())
					System.out.println("CLOSEDDDDD");
				else System.out.println("START");
				SSC.INS = IOStream.DESIn(IOStream.DataIn(SSC.FileSend(NewFileName+"_"+(int)(i+1)+".part",SFileName+".gz_"+(int)(i+1)+".part")),this.DESPassWord);
				SSC.OPS.OS = IOStream.BufferedOut(IOStream.Dataout(SSC.client.getOutputStream()));
				
				ClientList.add(SSC);
				SSW = new splitSpeedWatch();
				ClientFileTranslate CFT = new ClientFileTranslate((Socket)SSC.client, SSC.INS, SSC.OPS, "...",SFSCB,LimitSpeed,"Send");
				SSW.addSpeedWatch(CFT.SW);
				System.out.println("start run");
				Thread threadSW = new Thread(SSW);
				threadSW.start();
				CFT.run(); 					//直接启动run函数来单一执行
			}

		}
		else {
			SSW = new splitSpeedWatch();
		for(int i = 0;i<FS.num;i++) {													//开启多个传输客户端嵌套字
			System.out.println("Split send start");
			
			SocketClient SSC = new SocketClient(this.hostName, this.HTTPS_PORT,this.mood,NewFileName+"_"+(int)(i+1)+".part",this.LSpeed,SFileName+".gz_"+(int)(i+1)+".part",this.DESPassWord,SFCB);
			
			System.out.println(i+"begin");
			
			if (SSC.client.isClosed())
				System.out.println("CLOSEDDDDD");
			else System.out.println("START");
			
			SSC.INS = IOStream.DESIn(IOStream.DataIn(SSC.FileSend(NewFileName+"_"+(int)(i+1)+".part",SFileName+".gz_"+(int)(i+1)+".part")),this.DESPassWord);
			SSC.OPS.OS = IOStream.BufferedOut(IOStream.Dataout(SSC.client.getOutputStream()));
			
			ClientList.add(SSC);
			
			ClientFileTranslate CFT = new ClientFileTranslate((Socket)SSC.client, SSC.INS, SSC.OPS, "...",SFSCB,-1,"Send");
			SSW.addSpeedWatch(CFT.SW);
			Thread thread = new Thread(CFT);
			System.out.println("start run");
			ThreadList.add(thread);
			thread.start();
		}
		Thread threadSW = new Thread(SSW);
		threadSW.start();
		
	//	for(int i = 0;i<ThreadList.size();i++) {
		//	Thread thread = ThreadList.get(i);
		//	System.out.println(i);
			
	}
		
	}
	
}

class SplitFileSendCallBack implements CallBack{
	int SFnum = 0;
	int index = 0;
	
	private CallBack callBack = null;
	private String FileName = null;
	
	public SplitFileSendCallBack(int num,String FileName,CallBack callBack) {
		// TODO Auto-generated constructor stub
		this.FileName = FileName;
		this.SFnum = num;
		this.callBack = callBack;
	}
	
	
	
	public synchronized void callback() {
		index++;
		if(index >= SFnum) {
			System.out.println("All file send over");
			
			File file1 = new File(FileName);
			file1.delete();								
			
			System.out.println("Spilt Here");
				for(int i = 0;i<SFnum;i++) {
					File file = new File(FileName+"_"+(int)(i+1)+".part");
					file.delete();
				}
			this.callBack.callback();
			return ;
		}
	}
}

class GzipFileSendCallBack implements CallBack{
	private CallBack callBack;
	private String FileName = null;
	
	public GzipFileSendCallBack(CallBack callBack,String FileName) {
		// TODO Auto-generated constructor stub
		this.callBack = callBack;
		this.FileName = FileName;
	}

	@Override
	public void callback() {
		// TODO Auto-generated method stub
		System.out.println("gzip delete start");
		File file = new File(FileName+".gz");
		file.delete();
		callBack.callback();
	}
	
	
}


public class SocketClient implements CallBack{		//增加回调接口
	int HTTPS_PORT = 4000;
	String hostName = "182.92.197.26";
	InetAddress hostAddress = null;
	SocketFactory factory = null;
	Socket client = null;
	private String DESPassWord = null;
	private String RC4PassWord = "123456789";
	private String SSLPWD = "123456789";
	private String SSLKeyPath = "SSLKey";
	private splitSpeedWatch SSW = null;
	
	private Vector<ClientFileTranslate> TranslateList = null;
	
	InputStream INS = null;											//传输流
	OPSW OPS = null;
	String mood = null;
	String LSpeed = null;
	String FileName = null;
	String SFileName = null;
	
	private CallBack callBack = null;
	
	public SocketClient(String hostName,int port,String mood,String FileName,String LSpeed,String SFileName,String DESPassWord,CallBack callBack) throws Exception {				
		//SSLContext context  = SSLContext.getInstance("SSL");			//SSL环境初始化
		
		this.HTTPS_PORT  = port;
		this.hostName = hostName;
		this.mood = mood;
		this.LSpeed = LSpeed;
		this.FileName = FileName;
		this.SFileName = SFileName;
		this.DESPassWord = DESPassWord;
		this.OPS = new OPSW(null, null);
		this.callBack = callBack;
		
		
		client = new Socket(hostName,port);			//得到套接字
		
		TranslateList = new Vector<ClientFileTranslate>();
		
		DataOutputStream DOS = new DataOutputStream(client.getOutputStream());		//向服务器发送传输命令
		DOS.writeUTF(mood+"#"+FileName+"#"+LSpeed+"#"+SFileName+"#"+DESPassWord);
		
	}
	
	/*public SocketClient(InetAddress hostAddress,int port) throws IOException{
		this.hostAddress = hostAddress;
		this.HTTPS_PORT = port;
		//factory = SocketFactory.getDefault();
		//client = factory.createSocket(hostAddress, port);
	}*/
	
	public OutputStream getClientOutputStream() throws IOException{			//嵌套字输出流
		return client==null?null:client.getOutputStream();
	}
	
	public InputStream getClientInputStream()	throws IOException{			//嵌套字输入流
		return client==null?null:client.getInputStream();
	}
	
	public void Pause() {
		for(int i = 0;i<this.TranslateList.size();i++) {
			TranslateList.get(i).Pause();
		}
	}
	
	public void Active() {
		for(int i = 0;i<this.TranslateList.size();i++) {
			TranslateList.get(i).Action();
		}
	}
	
	public void ClientFirstStart(String mood,String FileName,String LSpeed,String SFileName) {	//mood:读/写 FileName：操作文件名 LSpeed：限制速度	 在接收请求后执行 SFileName 目的文件名
		try {	
			
			switch(mood) {
			case "Send" :														
			//传输操作
			{
				long LS = Long.parseLong(LSpeed);		//获取传输速度
				
				File file = new File(FileName);
				if(!file.exists()) {
					System.out.println("File not exits!");
					return ;
				}
				//文件大小解析
				switch(FilePort.FileSize(file)) {
				case 1 : 
					{
						INS = IOStream.DESIn(IOStream.DataIn(FileSend(FileName,SFileName)), DESPassWord);
						
						OPS.OS = IOStream.BufferedOut(IOStream.Dataout(client.getOutputStream()));
						
						ClientFileTranslate CFT = new ClientFileTranslate((Socket)client, INS, OPS, RC4PassWord,SocketClient.this,LS,"Send");			//创建传输线程
						
						this.TranslateList.add(CFT);
						
						Thread thread = new Thread(CFT);
						SSW = new splitSpeedWatch();
						SSW.addSpeedWatch(CFT.SW);
						Thread threadSW = new Thread(SSW);
						thread.start();
						threadSW.start();
						
					};break;
				case 2 : 					//大于10M的文件压缩传输
				{
					SFileName += ".gz";
					
					FilePort.GZipFile(FileName);
					
					INS = IOStream.DESIn(IOStream.DataIn(FileSend(FileName+".gz",SFileName)), DESPassWord);	//压缩传输流
					
					OPS.OS = IOStream.BufferedOut(IOStream.Dataout(client.getOutputStream()));
					
					GzipFileSendCallBack GZSCB = new GzipFileSendCallBack(this, FileName);
					ClientFileTranslate CFT = new ClientFileTranslate((Socket)client, INS, OPS, RC4PassWord,GZSCB,LS,"Send");			//创建传输线程
					
					this.TranslateList.add(CFT);
					
					Thread thread = new Thread(CFT);
					SSW = new splitSpeedWatch();
					SSW.addSpeedWatch(CFT.SW);
					Thread threadSW = new Thread(SSW);
					thread.start();
					threadSW.start();
				};break;
				case 3 : {					//大于1G的文件分段压缩传输（要修改）
					client.close();		//关闭当前嵌套字
					System.out.println("ZS in here");
					String NewFileName = FileName+".gz";
					File NewFile = new File(NewFileName);
					
					System.out.println("GZip start");
					if(!NewFile.exists())
						NewFileName = FilePort.GZipFile(FileName);
					
					ArrayList<SocketClient> ClientList = new ArrayList<SocketClient>();
					ArrayList<Thread> ThreadList = new ArrayList<Thread>();
					
					System.out.println(NewFileName);
					
					FileSplit FS = new FileSplit();
					System.out.println("spliting");
					//启用带回调函数的类实例进行传输
					SFClientCallBack SFCCB = new SFClientCallBack(FS, this.hostName, this.HTTPS_PORT, this.mood, NewFileName, this.LSpeed, SFileName, ClientList, ThreadList,this.DESPassWord,this.TranslateList,this,this.SSW);
					
				};break;
				default : break;
				}
			};break;//传输初始化完成
			case "Read" :
			//接收操作
			{
				DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
				DataInputStream DIS = new DataInputStream(client.getInputStream());
				
				String SocketFileName = DIS.readUTF();				//服务端直接接收从客户端发来的文件
				long fileLength = DIS.readLong();					//单个文件返回长度 分段文件返回文件数
				
				System.out.println(SocketFileName+" "+fileLength);
				
				SSW = new splitSpeedWatch();

				Thread threadSW = new Thread(SSW);
				
				String[] FNS = SocketFileName.split("@");
				if(FNS.length>1&&FNS[FNS.length-1].equals("SF") ) {					//若为分段文件
					client.close();			//关闭当前嵌套字
					System.out.println("in S");
					ArrayList<SocketClient> ClientList = new ArrayList<SocketClient>();
					ArrayList<Thread> ThreadList = new ArrayList<Thread>();
					
					MergeFileCallBack MFCB = new MergeFileCallBack(FileName, (int)fileLength, SocketClient.this);
					
					long speedlimit = Long.parseLong(this.LSpeed);
					speedlimit=(long)Math.ceil(speedlimit/fileLength);			//分段限速
					this.LSpeed = Long.toString(speedlimit);
					
					for(int i = 0;i<fileLength;i++) {
						String NewFileName  = FileName+".gz_"+(int)(i+1)+".part";			//分段文件各文件名 待修改
						System.out.println("Split file read start");
						SocketClient SSC = new SocketClient(hostName, HTTPS_PORT,this.mood,NewFileName,this.LSpeed,SFileName+".gz_"+(i+1)+".part",this.DESPassWord,this.callBack);
						
						DataInputStream SDOS = new DataInputStream(SSC.client.getInputStream());
						String SplitFileName = SDOS.readUTF();
						long sflength = SDOS.readLong();
						
						System.out.println(SplitFileName+" "+sflength);
						
						SSC.INS = IOStream.BufferedIn(IOStream.DataIn(SSC.client.getInputStream()));
						
						SSC.OPS.OS = SSC.FileGet(NewFileName,SFileName+"gz_"+(i+1)+".part",sflength);
						ClientList.add(SSC);
						
						ClientFileTranslate CFT = new ClientFileTranslate((Socket)SSC.client, SSC.INS, SSC.OPS, this.RC4PassWord,MFCB,-1,"Read");		//限速由传输发送方控制 但需要对分段限速进行技术
						this.TranslateList.add(CFT);
						
						
						SSW.addSpeedWatch(CFT.SW);
						
						Thread thread = new Thread(CFT);
						ThreadList.add(thread);
						thread.start();
						//需要对分段文件进行整合
					}
					threadSW.start();
				}
				
				else {
					System.out.println("right");
					if(client.isClosed()) {System.out.println("Client is closed");
					}
					else System.out.println("Client is still work");
					
					INS = IOStream.BufferedIn(IOStream.DataIn(client.getInputStream()));
					OPS.OS = FileGet(FileName,SocketFileName,fileLength);
					
					ClientFileTranslate CFT = new ClientFileTranslate((Socket)client, INS, OPS, this.RC4PassWord,new CAB(),-1,"Read");
					
					this.TranslateList.add(CFT);
					
					SSW.addSpeedWatch(CFT.SW);
					
					threadSW.start();
					CFT.run();
					
					
					System.out.println(SocketFileName);
					
					if((SocketFileName.split("\\.")[SocketFileName.split("\\.").length-1]).equals("gz")) {				//压缩文件直接使用服务端文件名
						String NFileName = "";
						for(int i=0;i<SocketFileName.split("\\.").length-1;i++)
							NFileName+=SocketFileName.split("\\.")[i];
						
						System.out.println("gzFile: "+SocketFileName+" File: "+FileName);
						
						FilePort.GZtoFile(FileName+".gz", FileName);			//解压
						
						File file = new File(FileName+".gz");
						file.delete();
						this.callBack.callback();
					}
					}
				};break;
			}
		}catch(Exception ie) {
			System.out.println("Client start error");
			ie.printStackTrace();
		}
	}
	
	public FileInputStream FileSend(String fileName,String SFileName) {					//传输文件流初始化
		File file = new File(fileName);
		int num = 0;
		if(!file.exists()){System.out.println("file not exits!");return null;}
		try {
			DataOutputStream DOS =new DataOutputStream(client.getOutputStream());
			DataInputStream DIS = new DataInputStream(client.getInputStream());
			
			DOS.writeUTF(SFileName);
			DOS.writeLong(file.length());
			
			long fileLength = DIS.readLong();
			
			FileInputStream FIS = FilePort.getFIS(file, fileLength);		//文件传输位置定位
			
			return FIS;
		}catch(IOException ie) {
			System.out.println("File send start error");
			ie.printStackTrace();
			return null;
		}
	}
		
	public OutputStream FileGet(String FileName,String SFileName,long fileLength) {
		try {
			DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
			DataInputStream DIS = new DataInputStream(client.getInputStream());
			
			System.out.println(SFileName);
			String[] SFileNameS = SFileName.split("\\.");
			if(SFileNameS[SFileNameS.length-1].equals("gz")&&!FileName.split("\\.")[FileName.split("\\.").length-1].equals("gz")) {
				FileName = FileName+".gz";		//若为压缩文件直接使用服务端保存文件的文件名
			}
			
			File file = new File(FileName);
			FileOutputStream FOS;
			if(file.exists()) {
				if(file.length()<fileLength) {
					DOS.writeLong(file.length());
					System.out.println("here right");
					FOS = new FileOutputStream(file,true);
					INS =client.getInputStream();
					OPS.OS = FOS;
				}
				else {
					file.delete();
					DOS.writeLong(0);
					FOS = new FileOutputStream(file);
					INS = client.getInputStream();
					OPS.OS = FOS;
				}
			}else {
				DOS.writeLong(0);
				FOS = new FileOutputStream(file);
				INS = client.getInputStream();
				OPS.OS = FOS;
			}
			OutputStream OS = IOStream.DESOut(IOStream.Dataout(FOS), DESPassWord);
			return OS;
		}catch(Exception ie) {
			System.out.println("file get start error");
			ie.printStackTrace();
			return null;
		}
	}
	
	public void FileTranslate(InputStream INS, OPSW OPS) throws IOException{		//文件中转
		byte[] bData = new byte[1024];
		int length;
		
		if(OPS.OS!=null) {
		while((length = INS.read(bData, 0, bData.length))!=-1) {
			System.out.println(length);
				OPS.OS.write(bData, 0, length);
				OPS.OS.flush();
			}
			OPS.OS.close();
			INS.close();
		}
		
		else {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				OPS.RAF.write(bData, 0, length);
			}
			OPS.RAF.close();
			INS.close();
		}
	}
	
	public void RC4FileTranslate() throws IOException{
		byte[] bData = new byte[1024];
		int length;
		byte[]sData;
		if(OPS.OS!=null) {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				//sData = RC4.HloveyRC4(bData, RC4PassWord);
				OPS.OS.write(bData, 0, length);
				OPS.OS.flush();
			}
			OPS.OS.close();
			INS.close();
		}
	
		else {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				//sData = RC4.HloveyRC4(bData, RC4PassWord);
				OPS.RAF.write(bData, 0, length);
			}
			OPS.RAF.close();
			INS.close();
		}
	}
	
	public splitSpeedWatch getSpeedWatch() {
		return this.SSW;
	}
	
	public static void main(String[] args) {
		try {
			
			CAB c = new CAB();
			
			SocketClient Client = new SocketClient("182.92.197.26",4000,args[0],args[1],args[2],args[3],args[4],c);			//命令行格式  Read/Send 本地文件 限速 目的文件
			Client.ClientFirstStart(args[0], args[1], args[2],args[3]);
			//Client.FileTranslate(new FileInputStream("./in.bin"), new BufferedOutputStream(Client.getClientOutputStream())); 
		}catch(Exception ie) {}
		
	}


	@Override
	public void callback() {
		// TODO Auto-generated method stub
		System.out.println("file translate over");
		
		this.callBack.callback();
	}	
	
}

class CAB implements CallBack{

	@Override
	public void callback() {
		// TODO Auto-generated method stub
		System.out.println("Over");
	}
	
}