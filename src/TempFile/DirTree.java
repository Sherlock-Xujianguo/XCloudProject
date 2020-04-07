package TempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;



class TreeNode{
	private String[] Linetxt = null;
	
	private String Infotxt = null;
	
	private String DirName = null;
	private long LastNum = 0;
	private long NextNum = 0;
	private long SonNum = 0;
	
	private Vector<String> FileTxtList = null;
	private String Filetxt = null;
	
	TreeNode(String txt){
		this.Linetxt = txt.split(";",3);
		
		this.Infotxt = Linetxt[0];
		this.Filetxt = Linetxt[2];
		
		
		String[] S = Linetxt[1].split("@@");
		
		DirName = S[0];
		LastNum = Long.valueOf(S[1]);
		NextNum = Long.valueOf(S[2]);
		SonNum = Long.valueOf(S[3]);
		
		S = Filetxt.split(",");
		
		FileTxtList = new Vector<String>();
		
		for(int i = 0;i<S.length;i++) {
			FileTxtList.add(S[i]);
		}
	}
	
	public long getSonNum() {
		return SonNum;
	}
	
	public void setSonNum(long num) {
		this.SonNum = num;
	}
	
	public long getLastNum() {
		return LastNum;
	}
	
	public void setLastNum(long num) {
		this.LastNum = num;
	}
	
	public long getNextNum() {
		return NextNum;
	}
	
	public void setNextNum(long num) {
		this.NextNum = num;
	}
	
	public String getDirName() {
		return DirName;
	}
	
	public void setDirName(String Name) {
		this.DirName = Name;
	}
	
	public String getInfotxt() {
		return Infotxt;
	}
	
	public void setinfotxt(String txt) {
		this.Infotxt = txt;
	}
	
	public String getFiletxt() {
		
		this.Filetxt = "";
		
		for(int i =0 ;i<FileTxtList.size();i++) {
			
			if(FileTxtList.get(i).equals("")) continue;
			
			this.Filetxt = this.Filetxt+FileTxtList.get(i)+",";
		}
		return this.Filetxt;
	}
	
	public void setFiletxt(String txt) {
		this.Filetxt = txt;
	}
	
	public void FileAdd(String FileName,long Filenum) {
		String txt = FileName+"-"+Filenum;
		
		this.FileTxtList.add(txt);
	}
	
	public long setFileName(String oldName,String NewName) {
		String txt = "";
		for(int i = 0;i<FileTxtList.size();i++) {
			txt = FileTxtList.get(i);
			if(txt.split("-")[0].equals(oldName)) {
				FileTxtList.set(i, NewName+"-"+txt.split("-")[1]);
				return Long.valueOf(txt.split("-")[1]);
			}
		}
		return -1;
	}
	
	public long FileDelete(String Name,FileTree FT) {
		String txt = "";
		for(int i = 0;i<FileTxtList.size();i++) {
			txt = FileTxtList.get(i);
			if(txt.split("-")[0].equals(Name)) {
				FileTxtList.remove(i);
				FT.FileDelete(Long.valueOf(txt.split("-")[1]));
				
				return Long.valueOf(txt.split("-")[1]);
			}
		}
		return -1;
	}
	
	public void allFileDelete(FileTree FT) {
		String txt = "";
		for(int i = 0;i<FileTxtList.size();i++) {
			txt = FileTxtList.get(i);
			if(txt.equals("")) continue;
			FT.FileDelete(Long.valueOf(txt.split("-")[1]));
		}
	}
	
	public void deleteMark(FileTree FT) {
		String[] s = this.Infotxt.split("@@");
		this.Infotxt = "F@@"+s[1];
		
		for(int i = 0;i<this.FileTxtList.size();i++) {							//相关文件删除标记
			if(this.FileTxtList.get(i).equals("")) continue;
			FT.FileDelete(Long.valueOf(this.FileTxtList.get(i).split("-")[1]));
		}
		
	}
	
	public String getLinetxt() {
		
		
		String Line = Infotxt+";"+DirName+"@@"+LastNum+"@@"+NextNum+"@@"+SonNum+";"+this.getFiletxt()+"#";
		
		return Line;
	}
	
	
}

class DirInfoObject {
	
	private String DirChangeTime = null;
	private boolean Expand = false;
	private long ExpandNum = 0;
	
	private long Size = -1;			//未做统计为-1
	private long numbers = -1;		
	
	public boolean IsUsing = true;
	private String Infotxt = null;
	
	
	
	DirInfoObject(String Infotxt){		//格式：Using(T/F)@@DirChangeTime(YYYY/MM/DD HH/MM/SS)@@Expand(T/F)@@ExpandNum@@Size@@numbers
		String[] S = Infotxt.split("@@");
		if(S[0].equals("F")) this.IsUsing = false;
		else this.IsUsing = true;
		DirChangeTime = S[1];
		if(S[2].equals("T")) {
			Expand = true;
			ExpandNum = Long.valueOf(S[3]);
		}
		else Expand = false;
		
		Size = Long.valueOf(S[4]);
		numbers = Long.valueOf(S[5]);
		System.out.println(this.numbers);
	}
	
	public String getDirChangeTime() {
		return DirChangeTime;
	}
	
	public boolean IsExpand() {
		return Expand;
	}
	
	public long getExpand() {
		return ExpandNum;
	}
	
	public long getSize() {
		return Size;
	}
	
	public long getNumbers() {
		return numbers;
	}
	
	public void setDirChangeTime(String NewTime) {
		this.DirChangeTime = NewTime;
	}
	
	public void setDirChangeTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH/mm/ss");//设置日期格式
		this.DirChangeTime = df.format(new Date());
	}
	
	public void setExpand(long num) {
		this.Expand = true;
		this.ExpandNum = num;
	}
	
	public void setSize(long newSize) {
		this.Size = newSize;
	}
	
	public void setNum(long num) {
		this.numbers = num;
	}
	
	public void setIsUsing(boolean F) {
		this.IsUsing = F;
	}
	
	public void numAdd() {
		this.numbers++;
	}
	
	public void numSub() {
		this.numbers--;
	}
	
	public String getInfoTxt() {
		
		String Infotxt = "";
		
		Infotxt = this.IsUsing?"T@@":"F@@";
		
		Infotxt = Infotxt+this.DirChangeTime+"@@";
		if(Expand) {
			Infotxt = Infotxt+"T@@"+this.ExpandNum+"@@";
		}
		else {
			Infotxt = Infotxt+"F@@-1@@";
		}
		Infotxt = Infotxt+this.Size+"@@"+this.numbers;
		
		return Infotxt;
	}
	
	
}

public class DirTree {

	
	//文件内每行存储形式 ： T/F@@Info@@;DirName@@LastNum@@NextNum@@SonNum;FileName;FileName;....#
	private FileTree FT = null;
	
	private String[] SumTree = null;
	
	private static final long Sum = 200;
	
	private File BaseFile = null;
	
	private Vector<Long> History = null;	//路径记录数组 可改为Vector进行存储
	
	private long DirSum = 0;				//当前记录中已记录文件夹个数（后从记录文件中读出）
	
	private String MainInfo = null;
	
	private String LastInfo = "";
	
	private int BaseSize = 10002;
	
	public void BulidTree(String FileName) {
		try {
			RandomAccessFile RAF = new RandomAccessFile(new File(FileName), "rw");
			for(int i = 0;i<Sum;i++) {
				for(int j=0;j<10000;j++) {
					RAF.write(" ".getBytes());
				}
				RAF.write("#\n".getBytes());
			}
			RAF.write("1@@\n".getBytes());
			
			RAF = FilePort.getRAF(new File(FileName), 0);
			RAF.write("T@@2000/01/01 00/00/00@@F@@-1@@-1@@0;ROOT@@0@@0@@0;#".getBytes());
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String initTree(String FileName) {				//返回较上次登入后的修改记录
		SumTree = new String[10000];
		
		BaseFile = new File(FileName);
		
		History = new Vector<Long>();
		//History.add((long)0);
		
		try {
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, Sum*BaseSize);
			MainInfo = RAF.readLine();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		if(this.MainInfo.split("@@").length>1) {
			LastInfo = this.MainInfo.split("@@")[1];
		}
		this.DirSum = Long.valueOf(this.MainInfo.split("@@")[0]);
		
		this.MainInfo = "";
		
		return LastInfo;
	}
	
	public String LastInfo() {
		return this.LastInfo;
	}
	
	public String initTree(String DirTreeName,FileTree FT) {		//添加了文件树的操作
		this.FT = FT;
		
		return this.initTree(DirTreeName);
	}

	public String getPath() {
		String path = "";
		try {
		FileInputStream FIS = null;
		byte[] bData = new byte[BaseSize];
		
		for(int i = 0;i<History.size();i++) {
			FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
			FIS.read(bData, 0, bData.length);
			TreeNode HNode = new TreeNode(new String(bData).split("#")[0]);
			
			path = path+HNode.getDirName()+"/";
		}
		
		return path;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
				
	}
	
	public long getNum(long sum) {				//哈希函数 从空余列中获取值
		long num = (sum)%197;
		
		try {
			
			//File file = new File(FileName);
			
			FileInputStream FIS = FilePort.getFIS(BaseFile, num*BaseSize);		//跳至指定行观察是否为空闲行
			
			byte bData[] = new byte[1];
			FIS.read(bData,0,1);
			
			int i = 1;
			
			while(new String(bData).equals("T")) {				//当为占用行时发生冲突 启动冲突处理函数
				
				System.out.println(new String(bData));	
				
				num+=i*i;
				i++;
				num = num%200;
				FIS = FilePort.getFIS(BaseFile, num*BaseSize);
				
				FIS.read(bData,0,1);
				
			}
			
			System.out.println(new String(bData));
		
			return num;						//无冲突 直接返回可用行
		
		} catch (Exception e) {				//出错 返回0（0行为根目录必占用）
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

	public String DirCheck(long DirNum) {						//进入文件夹 返回文件夹信息 格式：DirInfo;FileName;FileName;FileName;DirName@@DirNum;DirName@@DirNum;
		
		
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*BaseSize);		//跳转至指定记录行
			
			byte bData[] = new byte[BaseSize];
			
			FIS.read(bData, 0, bData.length);
			String LineInfo = new String(bData).split("#")[0];
			
			TreeNode LineNode = new TreeNode(LineInfo);
			
			//String DirInfo = LineInfoArgs[0].split("@@")[1];
			
			String DirInfo = LineNode.getInfotxt().split("@@",2)[1]+";"+LineNode.getFiletxt()+";";
			
			long SonDirNum = LineNode.getSonNum();
			
			while(SonDirNum>0) {
				FIS = FilePort.getFIS(BaseFile, SonDirNum*BaseSize);
				
				FIS.read(bData, 0, bData.length);
				String SonDirInfo = (new String(bData).split("#")[0]);
				
				LineNode = new TreeNode(SonDirInfo);
				DirInfo = DirInfo+LineNode.getDirName()+"@@"+SonDirNum+",";
				
				SonDirNum = LineNode.getNextNum();
				//DirInfo = DirInfo+"@@"+SonDirNum;
				
			}
			
			return DirInfo;
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public String DirFlush() {
		long num = History.get(History.size()-1);
		
		return this.DirCheck(num);
		
	}
	
	public String DirIn(long DirNum) {		//检查文件夹 只返回文件夹相关信息
		try {
		
			History.add(DirNum);				//将此文件夹路径存入记录数组
			
			return this.DirCheck(DirNum);
			
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean DirMake(long DirNum,String NewName) {		//在指定文件夹下新建文件夹 (因为记录条原因 建议使用重写方法) 成功返回true
		
		long NewDirNum = this.getNum(DirSum);
		
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*BaseSize);
			byte[] bData = new byte[BaseSize];
			
			FIS.read(bData, 0, bData.length);
			
			long preNum = DirNum;
			String LastLine = new String(bData).split("#")[0];
			TreeNode LastNode = new TreeNode(LastLine);
			
			long lastNum = LastNode.getSonNum();
			if(lastNum>0) {
			while(lastNum>0) {
				preNum = lastNum;
				
				FIS = FilePort.getFIS(BaseFile, lastNum*BaseSize);
				FIS.read(bData, 0, bData.length);
				
				LastLine = new String(bData).split("#")[0];
				
				LastNode = new TreeNode(LastLine);
				
				lastNum = LastNode.getNextNum();

			}
			LastNode.setNextNum(NewDirNum);
			}else {
				LastNode.setSonNum(NewDirNum);
			}
			//LastLine = Args[0]+Args2[0]+Args2[1]+NewDirNum+Args2[3]+Args[2];		//前一文件夹记录变动
			
			DirInfoObject DIO = new DirInfoObject("T@@2000/01/01 00/00/00@@F@@-1@@-1@@0");
			DIO.setDirChangeTime();
			
			String NewInfo = DIO.getInfoTxt()+";"+NewName+"@@"+preNum+"@@0@@0;;#";
			
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, preNum*BaseSize);			//修改上一文件夹记录
			RAF.write(LastNode.getLinetxt().getBytes());
			
			RAF = FilePort.getRAF(BaseFile, NewDirNum*BaseSize);						//修改新文件夹记录
			RAF.write(NewInfo.getBytes());
			
			this.DirSum++;
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean DirMake(String NewName) {	//在当前目录下新建文件夹(建议使用此方法进行新建)
		
		String path = "";
		
		long DirNum = 0;
		
		FileInputStream FIS = null;
		byte[] bData = new byte[BaseSize];
		try {
		for(int i =0;i<History.size();i++) {
			FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
			FIS.read(bData, 0, bData.length);
			TreeNode HNode = new TreeNode(new String(bData).split("#")[0]);
			
			path = path+HNode.getDirName()+"/";
			
			if(i == History.size()-1) {
				DirInfoObject DIO = new DirInfoObject(HNode.getInfotxt());
				DIO.setDirChangeTime();
				DIO.numAdd();
				HNode.setinfotxt(DIO.getInfoTxt());
				DirNum = History.get(i);
			}
		}
		//修改记录
		this.MainInfo = this.MainInfo+"A "+path+NewName+";";
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return this.DirMake(DirNum,NewName);
		
	}
	
	public boolean DirDelete(long DirNum) {		//删除文件夹
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*BaseSize);
			byte[] bData = new byte[BaseSize];
			
			FIS.read(bData, 0, BaseSize);
			
			String Line = new String(bData).split("#")[0];
			TreeNode Node = new TreeNode(Line);
			
			long lastNum = Node.getLastNum();
			long nextNum = Node.getNextNum();
			
			String path ="";
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
				FIS.read(bData, 0, bData.length);
				TreeNode HNode = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+HNode.getDirName()+"/";
				
				if(i == History.size()-1) {
					DirInfoObject DIO = new DirInfoObject(HNode.getInfotxt());
					DIO.setDirChangeTime();
					HNode.setinfotxt(DIO.getInfoTxt());
				}
			}
			//修改记录
			this.MainInfo = this.MainInfo+"D "+path+Node.getDirName()+";";
			
			if(!(lastNum<0)) {
				FIS = FilePort.getFIS(BaseFile, lastNum*BaseSize);
				FIS.read(bData, 0, BaseSize);
			
				String LastLine = new String(bData).split("#")[0];		//修改上一记录文件
				TreeNode LastNode = new TreeNode(LastLine);
				if(LastNode.getSonNum()==DirNum) {			//判断是同级文件夹还是子文件夹
					LastNode.setSonNum(nextNum);
				}
				else {
					LastNode.setNextNum(nextNum);
				}			//前后两节点相连
				
				RandomAccessFile RAF = FilePort.getRAF(BaseFile, lastNum*BaseSize);		//修改后文本写入
				RAF.write(LastNode.getLinetxt().getBytes());
				
			}
			
			
			if(nextNum>0) {
				FIS = FilePort.getFIS(BaseFile, nextNum*BaseSize);
				FIS.read(bData,0,BaseSize);
			
				String NextLine = new String(bData).split("#")[0];
				TreeNode NextNode = new TreeNode(NextLine);
				
				NextNode.setLastNum(lastNum);
				
				RandomAccessFile RAF = FilePort.getRAF(BaseFile, nextNum*BaseSize);
				RAF.write(NextNode.getLinetxt().getBytes());
		
			}
			
			Node.deleteMark(this.FT);
			Node.allFileDelete(this.FT);
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, DirNum*BaseSize);
			RAF.write(Node.getLinetxt().getBytes());
			
			if(Node.getSonNum()>0) {
				DirNum = Node.getSonNum();
				FIS = FilePort.getFIS(BaseFile, Node.getSonNum()*BaseSize);
				FIS.read(bData, 0, BaseSize);
				Node = new TreeNode(new String(bData).split("#")[0]);
				
				DBS(Node);
			
				RAF = FilePort.getRAF(BaseFile, DirNum*BaseSize);
				RAF.write(Node.getLinetxt().getBytes());
				this.DirSum--;
			}
			this.DirSum--;
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void DBS(TreeNode Node) {			//返回后修改导入
		Node.deleteMark(this.FT);
		Node.allFileDelete(this.FT);
		if(Node.getNextNum() == 0) {
			if(Node.getSonNum() == 0) {
				//Node.deleteMark(this.FT);
				return ;
			}
			
			else {
				try {
					FileInputStream FIS = FilePort.getFIS(BaseFile, Node.getSonNum()*BaseSize);
					byte[] bData = new byte[BaseSize];
					FIS.read(bData, 0, bData.length);
					TreeNode Son = new TreeNode(new String(bData).split("#")[0]);
					
					DBS(Son);
					
					RandomAccessFile RAF = FilePort.getRAF(BaseFile, Node.getSonNum()*BaseSize);
					RAF.write(Son.getLinetxt().getBytes());
					this.DirSum--;
					
					//Node.deleteMark(this.FT);
					return ;
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		else {
			try {
				FileInputStream FIS = FilePort.getFIS(BaseFile, Node.getNextNum()*BaseSize);
				byte[] bData = new byte[BaseSize];
				FIS.read(bData, 0, bData.length);
				TreeNode Next = new TreeNode(new String(bData).split("#")[0]);
				
				DBS(Next);
				
				RandomAccessFile RAF = FilePort.getRAF(BaseFile, Node.getNextNum()*BaseSize);
				RAF.write(Next.getLinetxt().getBytes());
				this.DirSum--;
				
				//Node.deleteMark(this.FT);
				return ;
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		//Node.deleteMark(this.FT);
	}
	
	public void FileChange(String FileName) {	//文件修改  只修改当前正在导入文件信息 最后关闭时写入 (未封装FT操作）
		
		String path = "";						//目录
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[BaseSize];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"/";
			}
			
			this.MainInfo = this.MainInfo+"M "+path+FileName+";";
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	public void FileAdd(String FileName,long FileLength) {		//文件增加记录(封装了FT操作)
		String path = "";						//目录
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[BaseSize];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"/";
				
				if(i == History.size()-1) {
					DirInfoObject DIO = new DirInfoObject(Node.getInfotxt());
					DIO.numAdd();
					DIO.setDirChangeTime();
					Node.setinfotxt(DIO.getInfoTxt());
					long FileNum = this.FT.NewFile(FileName, FileLength);
					Node.FileAdd(FileName, FileNum);
					
					System.out.println(Node.getFiletxt());
					
					System.out.println(Node.getLinetxt()+"\n"+History.get(i));
					
					RandomAccessFile RAF = FilePort.getRAF(BaseFile, History.get(i)*BaseSize);
					RAF.write(Node.getLinetxt().getBytes());
					
				}
			}
			
			this.MainInfo = this.MainInfo+"A "+path+FileName+";";

		}catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public void FileRename(String OldName,String NewName) {			//文件名修改 未封装FT操作
		String path = "";						//目录
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[BaseSize];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"/";
				
				if(i == History.size()-1) {
					Node.setFileName(OldName, NewName);
					
					RandomAccessFile RAF = FilePort.getRAF(BaseFile, History.get(i)*BaseSize);
					RAF.write(Node.getLinetxt().getBytes());
					
				}
			}
			
			this.MainInfo = this.MainInfo+"M "+path+NewName+";";

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void FileDelete(String FileName) {		//封装了FT操作
		String path = "";
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[BaseSize];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*BaseSize);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"/";
				
				if(i == History.size()-1) {
					DirInfoObject DIO = new DirInfoObject(Node.getInfotxt());
					DIO.numSub();
					DIO.setDirChangeTime();
					Node.setinfotxt(DIO.getInfoTxt());
					Node.FileDelete(FileName,this.FT);
					
					RandomAccessFile RAF = FilePort.getRAF(BaseFile, History.get(i)*BaseSize);
					RAF.write(Node.getLinetxt().getBytes());
					
					RAF.close();
					
					//this.DirSum--;
				}
			}
			this.MainInfo = this.MainInfo+"D "+path+FileName+";";
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String DirBack() {					//目录后退 返回父文件夹信息
		
		if(History.size() == 0 ) {				//判断 当到达最后文件夹时报错返回null
			System.out.println("No father Dir Error");
			return null;								
		}
		
		long FatherNum = History.get(History.size()-2);		//记录路径数组中删除
		History.remove(History.size()-1);
		
		System.out.println(FatherNum);
		
		return this.DirCheck(FatherNum);
		
	}
	
	public boolean DirRename(long num,String Name) {
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, num*BaseSize);
			byte[] bData = new byte[BaseSize];
			
			FIS.read(bData, 0, bData.length);
			
			FIS.close();
			TreeNode TN = new TreeNode(new String(bData).split("#")[0]);
			
			TN.setDirName(Name);
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, num*BaseSize);
			RAF.write(TN.getLinetxt().getBytes());
			
			RAF.close();
			
			return true;
			
		}catch(Exception e) {
			return false;
		}
	}
	

	public void Close() {						//将此次登入后修改记录计入文件
		try {
		RandomAccessFile RAF = FilePort.getRAF(BaseFile, Sum*BaseSize);
		
		RAF.write((String.valueOf(this.DirSum)+"@@"+this.MainInfo+" \n").getBytes());
		}catch(Exception e) {
			System.out.println("Close Error!!!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DirTree DT = new DirTree();
		FileTree FT = new FileTree();
		
		//DT.BulidTree("Treetest2.txt");
		//FT.BulidTree("FileTreetest.txt");
		FT.InitTree("FileTreetest.txt");
		DT.initTree("Treetest2.txt", FT);
		
		System.out.println(DT.LastInfo());
		
		System.out.println(DT.DirIn(0));
		
		//DT.DirDelete(1);
		
		//System.out.println(DT.DirIn(1));
		
		//DT.DirMake("Dir2");
		
		//DT.DirMake("Dir3");
		
		//DT.FileAdd("File1", 10000);
		
		//DT.DirMake("Dir1");
		
		System.out.println(DT.DirFlush());
		
		
		DT.Close();
		
		//DT.BulidTree("Treetest2.txt");
		
	
	}

}
