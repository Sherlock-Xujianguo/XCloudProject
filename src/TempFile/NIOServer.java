


import java.net.*;
import java.io.*;
import java.util.*;

import javax.crypto.CipherInputStream;

import java.nio.*;
import java.nio.channels.*;

public class NIOServer {
	private static final int SERVER_PORT = 4000;
	private ServerSocketChannel channel;		//服务器管道
	private LinkedList clients;					//客户端连接列表
	private Selector readSelector;				//选择器
	private ByteBuffer buffer;					//缓冲区
	private static final int TIMEOUT = 3000; 
	//private static InputStream FileIn = null;
	//private OutputStream FileOut = null;
	
	
	public static void handleAccept(SelectionKey key,char mood) throws IOException{		//处理接收请求
		ServerSocketChannel ssChannel = (ServerSocketChannel)key.channel();		//接收服务器通道
		SocketChannel sc = ssChannel.accept();	//接收通道
		
		sc.configureBlocking(false);											//非阻塞方式
		switch(mood) {
		case 'R': sc.register(key.selector(), SelectionKey.OP_READ);break;	//注册通道到选择器并附加一个缓冲区
		case 'W': sc.register(key.selector(), SelectionKey.OP_WRITE);break;
		default : break;
		}
		
	}
	
	public static void handleRead(SelectionKey key,String fileName) throws IOException{			//处理读取数据请求
		SocketChannel sc = (SocketChannel)key.channel();						//数据读取通道
		//ByteBuffer buffer = (ByteBuffer)key.attachment();						//缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		FileChannel fileChannel = (new FileOutputStream(fileName)).getChannel();			//获取文件通道
		System.out.println("read begin");
		long bytesRead = sc.read(buffer);										//将通道中的数据读取到缓冲区中
		while(bytesRead>=0) {				
			buffer.flip();														//缓冲区初始化
			while(buffer.hasRemaining()) {										//读取
				//System.out.println((char)buffer.getChar());
				fileChannel.write(buffer);
			}
			buffer.compact();
			//System.out.println();												//完成当前元素读
			//buffer.clear();														//缓冲区清空
			bytesRead = sc.read(buffer);										//再次读取
			
		}
		if (bytesRead == -1) {													//读取完毕 通道关闭
			sc.close();
			fileChannel.close();												
		}
		
	}
	
	public static void handleWrite(SelectionKey key,String fileName) throws IOException{		//处理写入数据请求
		//ByteBuffer buffer = (ByteBuffer)key.attachment();						//缓冲区
		//buffer.flip();															//缓冲区初始化
		SocketChannel sc = (SocketChannel)key.channel();						//数据写入通道
		FileChannel fileChannel = (new FileInputStream(fileName)).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		buffer.clear();
		System.out.println("send begin");
		int bytesRead = fileChannel.read(buffer);
		System.out.println(bytesRead);
		while(bytesRead>=0) {
			System.out.println(bytesRead);
			buffer.flip();
			while(buffer.hasRemaining()) {									
				sc.write(buffer);													//将缓冲区中的数据写入到通道中
			}
			buffer.compact();														//缓冲区当前标识向前
			bytesRead = fileChannel.read(buffer);
		}
		if(bytesRead == -1) {
			sc.close();
			fileChannel.close();
		}
	}
	
	public static void selector() {												//选择器重载方法
		Selector selector = null;												//选择器
		ServerSocketChannel ssc = null;											//服务器通道
		try {
			selector = Selector.open();											//选择器开
			ssc = ServerSocketChannel.open();									//服务器通道开
			ssc.socket().bind(new InetSocketAddress(4000));						//绑定端口
			ssc.configureBlocking(false);
			ssc.register(selector, SelectionKey.OP_ACCEPT);						//注册通道到选择器
			char mood = 'R';
			int i = 0 ;
			while(true){
			if(selector.select(TIMEOUT) == 0) {
				System.out.println("++++++++0");
				continue;
			}
			
			Iterator<SelectionKey> iter = selector.selectedKeys().iterator();		//迭代器
			while(iter.hasNext()) {
				System.out.println(i++);
					SelectionKey key = iter.next();								
					if(key.isAcceptable()) {									//迭代处理key
						handleAccept(key,mood);
						mood = 'W';
						System.out.println("aaa");
					}
					else if(key.isReadable()) {
						handleRead(key,"SERVERINo.bin");
						ssc.register(selector, SelectionKey.OP_ACCEPT);
					}
					else if(key.isWritable()&&key.isValid()) {
						handleWrite(key,"SERVERINo.bin");
					}
					else if(key.isConnectable()) {
						System.out.println("isConnect = true");
					}
					iter.remove();
				}
			}
			}catch(IOException ie) {
				ie.printStackTrace();
				System.out.println("ERROR");
		}
		finally {
			try {
				if(selector!=null) {
					selector.close();
				}
				if(ssc!=null) {
					ssc.close();
				}
			}catch(IOException e) {
				System.out.println("close error");
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		selector();
	}

}
