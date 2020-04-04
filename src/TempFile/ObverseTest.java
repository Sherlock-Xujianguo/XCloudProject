import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

public class ObverseTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RunThread RT = new RunThread();
		Listener L = new Listener();
		RT.addObserver(L);
		
		Thread thread = new Thread(RT);
		thread.start();
		
		Thread thread2 = new Thread(L);
		thread2.start();
		
	}

}



@SuppressWarnings("deprecation")
class RunThread extends Observable implements Runnable{

	private int index = 0;
	
	public void DoSomeThing() {
		super.setChanged();
		notifyObservers();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(index < 10) {
			try{TimeUnit.SECONDS.sleep(1);}catch(InterruptedException ie) {}
			DoSomeThing();
			index++;
		}
			
	}
	
}


class Listener implements Observer,Runnable{
	long Start = 0;
	long End = 0;
	int index = 0;
	
	
	Listener(){
		Start = 0;
		End = 0;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		Start = End;
		End = index;
		if(Start == End) {System.out.println("Run time out!");}
		else
			System.out.println("Start: "+Start+" End: "+End+" Spead: "+(int)(End-Start));
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(index<10) {
			index++;
			System.out.println("index: "+index);
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			}catch(InterruptedException e) {}
		}
	}
	
}
