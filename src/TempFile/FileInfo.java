import java.text.SimpleDateFormat;
import java.util.*;
class FileInfo {

	private String FileName = null;
	private long FileLength = 0;
	private String FileChangeTime = null;
	private String MainInfo = null;
	private boolean Mark = false;
	
	FileInfo(String MainInfo){	//格式: FileName;FileLength;FileChangeTime(yy/MM/DD);
		this.MainInfo = MainInfo;
		
		String[] s = MainInfo.split(";");
		
		this.FileName = s[1];
		this.FileLength = Long.valueOf(s[2]);
		this.FileChangeTime = s[3];
		
		this.Mark = s[0].equals("T")?true:false;
		
	}
	
	public String getFileName() {
		return this.FileName;
	}
	
	public void setFileName(String NewName) {
		this.FileName = NewName;
	}
	
	public Long getFileLength() {		//此处记录的是未经压缩和分段存储的文件大小
		return this.FileLength;
	}
	
	public void setFileLength(long FileLength) {
		this.FileLength = FileLength;
	}
	
	public String getFileChangeTime() {
		return this.FileChangeTime;
	}
	
	public void SetFileChangeTime(String NewTime) {
		this.FileChangeTime = NewTime;
	}
	
	public void SetFileChangeTime() {
		SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd");//设置日期格式
		this.FileChangeTime = df.format(new Date());
	}
	
	public void DeleteMark() {
		this.Mark = false;
	}
	
	public String getMainInfo() {
		MainInfo = this.Mark?"T;":"F;";
		
		MainInfo = MainInfo+this.FileName+";"+String.valueOf(this.FileLength)+";"+this.FileChangeTime;
		
		return MainInfo;
		
	}
	
}
