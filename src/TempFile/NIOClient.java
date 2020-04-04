

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.channels.*;

public class NIOClient {
	private ByteBuffer buffer = ByteBuffer.allocate(1024);	//缓冲区
	private SocketChannel socketChannel = null;				//客户端通道
	NIOClient(){
		try {
			socketChannel = SocketChannel.open();		//开通道
			socketChannel.configureBlocking(false);		//非阻塞方式
			
			socketChannel.connect(new InetSocketAddress("localhost",4000));			//建立连接
			if(socketChannel.finishConnect()) {				//如果连接启动
				int i = 0;
				while(true) {
					TimeUnit.SECONDS.sleep(1); 				//睡眠
					String info = "AAAAAAAAAA";
					buffer.clear();							//初始化清空缓冲区
					buffer.put(info.getBytes());			//将info信息存入到缓冲区中
					buffer.flip();							//标记缓冲区中的limit位置
					while(buffer.hasRemaining()) {			//当读取位和limit位间存在元素时 读取buffer中的元素 
						System.out.println((char)buffer.getChar());
						socketChannel.write(buffer);		//向管道中写入buffer元素
					}
				}
			}
		}
		catch(IOException | InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(socketChannel!=null) {
					socketChannel.close();
				}
			}catch(IOException ie) {
				System.out.println("close error");
			}
		}
	}
	
	NIOClient(String fileName1,String fileName2){
		try{
			ByteFileSend(fileName1);
			System.out.println("send over");
			ByteFileGet(fileName2);
			System.out.println("get over");
		}
		catch(Exception e) {
			System.out.println("error");
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	public void ByteFileSend(String fileName) throws Exception{
		FileChannel fileChannel = (new FileInputStream(fileName)).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		
		socketChannel.connect(new InetSocketAddress("localhost",4000));
		if(socketChannel.finishConnect()) {
			System.out.println("Send Begin.");
			buffer.clear();
			TimeUnit.SECONDS.sleep(1);
			int bytesRead = fileChannel.read(buffer);
			
			while (bytesRead != -1) {
				buffer.flip();
				while(buffer.hasRemaining()) {
					socketChannel.write(buffer);
				}
				buffer.compact();
				bytesRead = fileChannel.read(buffer);
			}
			if(bytesRead == -1) {
				fileChannel.close();
				socketChannel.close();
			}
		}
		
		
	}
	
	public void ByteFileGet(String fileName) throws Exception{
		FileChannel fileChannel = (new FileOutputStream(fileName)).getChannel();
		socketChannel = SocketChannel.open();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress("localhost",4000));
		if(socketChannel.finishConnect()) {
			System.out.println("Get Begin.");
			TimeUnit.SECONDS.sleep(1);
			int bytesRead = socketChannel.read(buffer);
			System.out.println(bytesRead);
			while(bytesRead!=-1) {
				System.out.println(bytesRead);
				buffer.flip();
				while(buffer.hasRemaining()) {
					fileChannel.write(buffer);
				}
				buffer.compact();			//buffer写入重定位
				bytesRead = socketChannel.read(buffer);
			}
			if(bytesRead == -1) {
				fileChannel.close();
				socketChannel.close();
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		new NIOClient("in.bin","out.bin");
	}
}
