

import java.lang.*;
import java.util.*;

public class ThreadPool {				//线程池类
	
	private static ThreadPool instance_ = null;		//实例
	
	//优先级常数
	public static final int LOW_PRIORITY = 0;
	public static final int NORMAL_PRIORITY = 1;
	public static final int HIGH_PRIORITY = 2;
	
	private List[] idleThreads_;		//线程池
	private boolean shutDown_ = false; 		//是否关闭
	private int threadCreationCounter_ = 0;		//创建的线程个数
	private boolean debug_ = false;	 	//是否调试
	
	
	private ThreadPool() {				//线程池对象初始化
		List[] idleThreads = {new Vector(5),new Vector(5),new Vector(5)};		//新建3个vector来存放3种优先级的线程
		idleThreads_ = idleThreads;
		threadCreationCounter_ = 0;
	}
	
	public int getCreationCounter() {
		return threadCreationCounter_;
	}
	
	public static ThreadPool instance() {		//实例
		if(instance_ == null) {
			instance_ = new ThreadPool();
		}
		return instance_;
	}
	
	public boolean isDebug() {
		return debug_;
	}
	
	public boolean isShutDown() {
		return shutDown_;	
	}
	
	protected synchronized void repool(PooledThread repoolingThread) {	//将线程重新放回池种的同步方法
		if(!shutDown_) {
			if(debug_) {
				System.out.println("threadPool.repool() : repooling");
			}
			
			switch(repoolingThread.getPriority()) {
			case Thread.MIN_PRIORITY : {idleThreads_[LOW_PRIORITY].add(repoolingThread);}break;
			case Thread.NORM_PRIORITY : {idleThreads_[NORMAL_PRIORITY].add(repoolingThread);}break;
			case Thread.MAX_PRIORITY : {idleThreads_[HIGH_PRIORITY].add(repoolingThread);}break;
			default : throw new IllegalAccessError("Illegal priority");
			}
			notifyAll();
			System.out.println("repooling over");
		}
		
		else {
			if(debug_) {
				System.out.println("Thread will shutDown");
			}
			repoolingThread.shutDown();
			if(debug_)  System.out.println("thread shutDown");
		}
	}
	
	
	
	public void setDebug(boolean newDebug) {
		debug_ = newDebug;
	}
	
	public synchronized void shutdown() {		//关闭线程池中的所有线程
		shutDown_ = true;
		if(debug_) {
			System.out.println("ThreadPool : shutting down");
		}
		for(int prioIndex = 0;prioIndex<=HIGH_PRIORITY;prioIndex++) {		//遍历关闭所有线程
			List prioThreads = idleThreads_[prioIndex];
			for(int threadIndex = 0; threadIndex< prioThreads.size(); threadIndex++) {
				PooledThread idleThread = (PooledThread) prioThreads.get(threadIndex);
				idleThread.shutDown();
			}
		}
		notifyAll();
		if(debug_) {
			System.out.println("ThreadPool : shutdown over");
		}	
	}
	
	public synchronized void start(Runnable target, int priority) { 
		PooledThread thread = null;			//执行任务线程
		List idleList = idleThreads_[priority];
		if(!idleList.isEmpty()) {			//存在空闲线程时直接使用空闲线程进行任务
			int lastIndex = idleList.size() -1 ;
			thread = (PooledThread) idleList.get(lastIndex);
			idleList.remove(lastIndex);		//将执行任务的线程从线程池中取出
			thread.setTarget(target);		//唤醒线程进行任务
		}
		
		else {		//如果线程池中无 则新建一个线程来进行
			System.out.println("Starting a new Thread");
			threadCreationCounter_++;			//线程池线程数量自加
			thread = new PooledThread(target, "PooledThread #"+threadCreationCounter_, this);
			idleThreads_[priority].add(thread);
			switch(priority)
			{
			case LOW_PRIORITY : thread.setPriority(Thread.MIN_PRIORITY);break;
			case NORMAL_PRIORITY : thread.setPriority(Thread.NORM_PRIORITY);break;
			case HIGH_PRIORITY : thread.setPriority(Thread.MAX_PRIORITY); break;
			default : thread.setPriority(Thread.NORM_PRIORITY);break;
			}	
			thread.start();  		//初启动此线程		
		}
		System.out.println(thread.getName());
		
	}
	
	public static void main(String[] args) {
		System.out.println("Testing ThreadPool");
		System.out.println("Creating ...");
		ThreadPool pool = ThreadPool.instance();
		pool.setDebug(true);
		class TestRunner implements Runnable {
			public int count = 0;
			public void run() {
				System.out.println("This is the NO."+pool.threadCreationCounter_);
				synchronized (this) {
					try {
						System.out.println("sleep");
						wait(5000);
					}catch(InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				System.out.println("Over");
				count++;
			}
		}
		//System.out.println("starting a new Thread ....");
		TestRunner runner = new TestRunner();
		pool.start(runner, NORMAL_PRIORITY);
		
		TestRunner  runner2 = new TestRunner();
		pool.start(runner2, LOW_PRIORITY);
		
		
		
		//pool.shutdown();
	}
	
}
