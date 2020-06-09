package Core;

import java.io.*;
import java.util.Date;

public class FileInfo {
    public String filePath;
    public String fileName;
    public String MD5;
    public long fileSize;
    public long currentSize;
    public Date lastEditTime;

    public FileInfo(String filePath, String MD5, long fileSize, long millisec) {
        this.filePath = filePath;
        this.fileName = new File(filePath).getName();
        this.MD5 = MD5;
        this.fileSize = fileSize;
        this.currentSize = 0;
        this.lastEditTime = new Date(millisec);
    }

    public FileInfo(String fileInfoPath) {
        try {
            File file = new File(fileInfoPath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            filePath = bufferedReader.readLine();
            fileName = new File(filePath).getName();
            MD5 = bufferedReader.readLine();
            fileSize = Long.parseLong(bufferedReader.readLine());
            currentSize = Long.parseLong(bufferedReader.readLine());
            lastEditTime = new Date(Long.parseLong(bufferedReader.readLine()));
        }
        catch (Exception e ) {
            e.printStackTrace();
        }

    }

    public void SaveFileInfo() {
        try {
            File fileInfo = new File(filePath + ".fileinfo");
            FileOutputStream fo = new FileOutputStream(fileInfo);
            fo.write(ToString().getBytes());
            fo.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateCurrentSize(Long currentSize) {
        this.currentSize = currentSize;
    }

    public String ToString() {
        String rst = "";
        rst += filePath + "\n";
        rst += MD5 + "\n";
        rst += Long.toString(fileSize) + "\n";
        rst += Long.toString(currentSize) + "\n";
        rst += Long.toString(lastEditTime.getTime()) + "\n";
        return rst;
    }

    public static void main(String args[]) {
        FileInfo fileInfo = new FileInfo("C:\\Users\\SherlockXujianguo\\Desktop\\a.fileinfo");
        fileInfo.UpdateCurrentSize(100l);
        Debug.Log(fileInfo.filePath);
        Debug.Log(fileInfo.fileName);
        Debug.Log(fileInfo.fileSize);
        Debug.Log(fileInfo.currentSize);
        Debug.Log(fileInfo.lastEditTime);
    }
}
