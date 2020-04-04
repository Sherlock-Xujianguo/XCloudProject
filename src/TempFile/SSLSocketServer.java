
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.*;
import javax.net.*;
import javax.net.ssl.*;

@SuppressWarnings("deprecation")
class ServerSpeedLimit implements Runnable,Observer{			//服务端速度限制功能 删去速度显示
	
	public long start = 0;
	public long end = 0;
	public boolean Nclose = true;
	private long sum = 0;
	private CreatThread server = null;
	
	ServerSpeedLimit(CreatThread server){
		this.server = server;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		Nclose = false;				//关闭计时器
		System.out.println("close");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(Nclose) {
			try {
				TimeUnit.SECONDS.sleep(1);
			}catch(InterruptedException ie) {System.out.println("clock error");}
			if(Nclose) {
				server.Sindex = 0;		//重置限制条件
				server.limit = true;
				System.out.println("flush");
			}
			
		}
		return ;
	}

}

@SuppressWarnings("deprecation")
class CreatThread extends Observable implements Runnable{		//单次接收请求线程
	
	InputStream in = null;
	OPSW out = null;
	Socket client = null;
	String[] SS = null;
	
	public long index = 0;
	public boolean limit = true;
	private long LimitSpeed = -1;
	public long Sindex = 0;
	
	private String DESPassWord = null; 
	
	CreatThread(Socket socket) throws Exception{		//线程建立
		this.client = socket;
	}
	
	public void FileSend(String[] SS) {
		File file = new File(SS[3]);
		
		System.out.println(SS[3]);
		
		this.LimitSpeed = Long.parseLong(SS[2]);
		
		int num = 0;
		if(!file.exists()) {			//文件存在检验
			File zfile = new File(SS[3]+".gz");
			if(zfile.exists())	file = zfile;
			else {
				num =FilePort.PartFileExist(SS[3]+".gz");
				if(num == 0) {
				System.out.println("File not exist");
				return ;
				}
			}
			
		}
		
		else num = 1;
		
		try {
			DataOutputStream DOS = new DataOutputStream(client.getOutputStream());		//文件属性对齐
			DataInputStream DIS = new DataInputStream(client.getInputStream());
				
			if(num>1) {
				DOS.writeUTF(SS[3]+"@SF");				//标志为分组文件 分组文件获取交由客户端进行处理
				DOS.writeLong(num);
				DIS.close();
				DOS.close();
				return ;
			}
			DOS.writeUTF(file.getName());
			DOS.writeLong(file.length());					
			if(client.isClosed()) System.out.println("CLose!");
			TimeUnit.SECONDS.sleep(1);
			long length = DIS.readLong();				//获取文件开始传输位置
			
			System.out.println(length);
			
			FileInputStream FIS = FilePort.getFIS(file, length);		//开启文件读取流

			in = IOStream.BufferedIn(IOStream.DESIn(IOStream.DataIn(FIS), DESPassWord));
			out.OS = IOStream.BufferedOut(IOStream.Dataout(client.getOutputStream()));
			
			System.out.println("Over");
			return ;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("File send start error");
			e.printStackTrace();
		}
	
	}
	
	public void FileGet(String[] SS) {
		try {
		DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
		DataInputStream DIS = new DataInputStream(client.getInputStream());
		this.LimitSpeed = -1;
		
		String FileName = DIS.readUTF();				//服务端直接接收从客户端发来的文件
		
		System.out.println(FileName);
		
		long fileLength = DIS.readLong();
		
		System.out.println(fileLength);
		
		File file = new File(FileName);
		FileOutputStream FOS;
		if(file.exists()) {
			if(file.length()<fileLength) {
				DOS.writeLong(file.length());
				FOS = new FileOutputStream(file,true);
				in =client.getInputStream();
				in = IOStream.BufferedIn(IOStream.DataIn(in));
				out.OS = IOStream.DESOut(IOStream.Dataout(FOS), DESPassWord);
			}
			else {
				file.delete();
				DOS.writeLong(0);
				FOS = new FileOutputStream(file,true);
				in = client.getInputStream();
				in = IOStream.BufferedIn(IOStream.DataIn(in));
				out.OS = IOStream.DESOut(IOStream.Dataout(FOS), DESPassWord);
			}
		}else {
			DOS.writeLong(0);
			FOS = new FileOutputStream(file,true);
			in = client.getInputStream();
			in = IOStream.BufferedIn(IOStream.DataIn(in));
			out.OS = IOStream.DESOut(IOStream.Dataout(FOS), DESPassWord);
		}
		
		}catch(Exception ie) {
			try{
				ie.printStackTrace();
				client.close();
				System.out.println("This Client close");
				return ;
			}catch(Exception e) {
				System.out.println("file get start error");
				e.printStackTrace();	
			}
			
		}
			
	}
	
	public void setInputStream(InputStream INS) {		//输入流
		in = new BufferedInputStream(INS);
	}
	
	public void setOutputStream(OutputStream OPS) {		//输出流
		out.OS = new BufferedOutputStream(OPS);
	}
	
	public InputStream getClientInput() throws IOException{		//嵌套字输入流
		return client==null?null:client.getInputStream();
	}
	
	public OutputStream getClientoutput() throws IOException{	//嵌套字输出流
		return client==null?null:client.getOutputStream();
	}
	
	public void run() {											//文件中转线程启动
		try{
			DataInputStream DIS = new DataInputStream(client.getInputStream());
			String command = DIS.readUTF();
			
			System.out.println(command);
			
			SS = command.split("#");
			
			this.DESPassWord = SS[4];
			
			System.out.println(DESPassWord);
			
			out = new OPSW(null, null);
			
		}catch(IOException ie) {
			System.out.println("client start error!");
			ie.printStackTrace();
		}
		switch(SS[0]) {
		case "Read" : FileSend(SS);break;			//文件接收与读取准备
		case "Send" : FileGet(SS);break;					
		}
		
		//传输开始
		System.out.println("running");
		if(client.isClosed()) {System.out.println("Client is closed");return ;}
		else System.out.println("file translate start!");
		try {
			byte[] bData = new byte[1024];
			int length = 0;
			OutputStream out_;
			ServerSpeedLimit SSLimit = new ServerSpeedLimit(this);			//添加速度监听器
			this.addObserver(SSLimit);
			Thread ThreadSSW = new Thread(SSLimit);
			ThreadSSW.start();
			
			if(this.out.OS!=null)  {									//迎合断点续传需要RandomAccessFile进行
				out_ =this.out.OS;
				while(SSLimit.Nclose) {
					if(this.LimitSpeed<0 || this.limit) {
						while(limit&&(length = in.read(bData, 0, bData.length))!=-1) {
							index++;
							Sindex++;
							System.out.println(length);
							out_.write(bData, 0, length);
							out_.flush();
							if(SS[0].equals("Read")&&LimitSpeed>0&&Sindex>=LimitSpeed) limit = false;
						}
						if(length == -1) {
							super.setChanged();
							notifyObservers();
						}
					}
					else {
						try {
							TimeUnit.MILLISECONDS.sleep(10);			//限速睡眠小段时间
						}catch(InterruptedException ie) {
							System.out.println("Speed limit error");
						}
					}
				}
				out.OS.close();
			}
			else {
				RandomAccessFile RAF = this.out.RAF;
				while((length = in.read(bData, 0, bData.length))!=-1) {
					System.out.println(length);
					RAF.write(bData, 0, length);
				}
				super.setChanged();
				notifyObservers();
				RAF.close();
			}
			in.close();
		}catch(Exception e) {
			System.out.println("File translate error");
			super.setChanged();
			notifyObservers();
			e.printStackTrace();
		}
	}
}


public class SSLSocketServer {					//服务器
	static int port = 4000;
	static ServerSocket server = null;
	static ExecutorService pool = null;
	private String SSLPWD = "123456789";
	private String RC4PassWord;
	private String DESPassWord;
	private String SSLKeyPath = "SSLKey";
	
	public SSLSocketServer(int port) {			//没用
		this.port = port;
	}
	
	private static ServerSocket getSocket(int thePort) {				//服务器嵌套字建立
		ServerSocket s = null;
		try {
			//服务器无法安装SSL证书 作废
			//String key = "F:\\JAVA\\JavaSocket\\src\\SSLKey";		//证书名
			//char keyStorePass[] = "123456789".toCharArray();		//证书密码
			//char keyPassword[] = "123456789".toCharArray();		//证书别称所使用的密码
			//KeyStore ks = KeyStore.getInstance("JKS");			//创建JKS密钥库
			//ks.load(new FileInputStream(key), keyStorePass);	
			//KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");	//创建管理密钥库的管理器
			//kmf.init(ks, keyPassword);			//初始化
			//SSLContext sslContext = SSLContext.getInstance("SSLv3");
			//sslContext.init(kmf.getKeyManagers(), null, null);
			//SSLServerSocketFactory factory = sslContext.getServerSocketFactory();	//创建服务器嵌套字工厂
			//s = (SSLServerSocket)factory.createServerSocket(thePort);				//创建服务器嵌套字
			//s.setWantClientAuth(false);				//单向验证 取消对客户端的验证
			s = new ServerSocket(thePort);
			
			pool = Executors.newCachedThreadPool();
					
			server = s;
		}catch(Exception e) {
			System.out.println("Server Socket start error");
			e.printStackTrace();
		}
		
		return s;
	}
	
	private void Accept() {			//接收函数 开启线程
		CreatThread client_ = null;
		try {
			Socket client = server.accept();
			System.out.println("accept a new client");
			client_ = new CreatThread(client);
			//输入输出流建设（以下用于测试 )		
			pool.execute(client_);			//开启线程池中线程
			
		}catch(Exception e) {
			System.out.println("client accept error");
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public static void main(String[] args) {				//测试函数
		SSLSocketServer Server = new SSLSocketServer(4000);
		Server.getSocket(4000);
		if(Server.server == null) System.out.println("Server not start!");
		else System.out.println("server is ready");
		
		while(true) {
			Server.Accept();
		}
	};
}
