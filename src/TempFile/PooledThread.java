

import java.lang.*;

public class PooledThread extends Thread{	//线程池中线程
	private ThreadPool pool_;			//线程所在线程池
	private Runnable target_;			//线程任务
	private boolean shutDown_ = false ;		//是否关闭
	private boolean idle_ = false;			//设置是否让线程处于等待状态
	
	private PooledThread() {				//新建一个线程
		super();
	}
	
	private PooledThread(Runnable target) {
		super(target);					
	}
	
	private PooledThread(Runnable target, String name) {
		super(target,name);
	}
	
	public PooledThread(Runnable target,String name,ThreadPool pool) {
		super(name);
		target_ = target;
		pool_ = pool;
	}
	
	private PooledThread(String name) {
		super(name);
	}
	
	private PooledThread(ThreadGroup group, Runnable target)
	{	
		super(group,target);
	}
	
	private PooledThread(ThreadGroup group,Runnable target,String name) {
		super(group,target,name);
	}
	
	
	public Runnable getTarget() {
		return target_;
	}
	
	public boolean isIdle() {		//返回当前状态
		return idle_;
	}
	
	//工作线程要求在执行完代码后返回线程池中储存 而不是被java回收机制回收资源 所以run（）方法将一直持续运行
	
	public void run() {
		//除非池关闭 否则循环不结束 每一次循环执行一个target
		while(!shutDown_) {
			idle_ = false;
			if(target_!=null) {		//如果存在任务则执行
				target_.run();
			}
			idle_ = true;			//执行完毕
			try {
				pool_.repool(this);		//通知将线程重新放回池中
				//进入池后睡眠 等待下一次唤醒
				synchronized (this) {
					wait();		//睡眠
				}
			}catch(InterruptedException InE) {
				System.out.println("run error");
				InE.printStackTrace();
			}
			idle_ = false;
		}
	}
	
	public synchronized void setTarget(Runnable newTarget) {
		this.target_ = newTarget;		//新任务
		notifyAll();			//唤醒睡眠线程
	}
	
	public synchronized void shutDown() {	//关闭
		shutDown_ = true;				
		notifyAll();			//唤醒线程 结束run循环
	}
}
