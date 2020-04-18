package Core;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;

public class FileNode implements Serializable {
    public String _name;
    public Boolean _isDirectory;
    public FileNode[] _listFiles;
    public String _md5;

    public FileNode(String filePath) throws Exception {
        File file = new File (filePath);
        _name = file.getName();
        _isDirectory = file.isDirectory();
        if (_isDirectory) {
            int length = 0;
            File[] fileList = file.listFiles();
            if (fileList == null) {
                length = 0;
                _listFiles = null;
                _md5 = "";
            }
            else {

                length = fileList.length;
                _listFiles = new FileNode[length];
                for (int i = 0; i < length; i++) {
                    _listFiles[i] = new FileNode(fileList[i].getPath());
                }

                MessageDigest digest = MessageDigest.getInstance("MD5");
                for (int i = 0; i < length; i++) {
                    digest.update(_listFiles[i]._md5.getBytes());
                }
                BigInteger bigInteger = new BigInteger(1, digest.digest());
                _md5 = bigInteger.toString(16);
            }
        }
        else {
            _listFiles = null;
            _md5 = MD5.GetFileMD5(file);
        }
    }

    @Override
    public String toString() {
        String rst = "FileNode{" +
                ", _name=" + _name +
                ", _isDirectory=" + _isDirectory +
                ", _md5=" + _md5 +
                ", _listFiles=[";
        if (_listFiles != null) {
            for (FileNode fn:_listFiles) {
                rst += fn.toString();
            }
        }
        rst += "]}";
        return rst;
    }

    public void print() {
        print(0);
    }

    private void print(int level) {
        System.out.print(" ".repeat(level));
        if (_isDirectory) {
            System.out.print("*");
        }
        else {
            System.out.print("-");
        }
        System.out.print(_name);
        System.out.println(": " + _md5);
        if (_listFiles == null) {
            return;
        }
        else {
            for (FileNode fn:_listFiles) {
                fn.print(level + 1);
            }
        }
    }
}
