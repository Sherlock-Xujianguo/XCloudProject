package Core;

import java.io.File;
import java.io.Serializable;

public class FileNode implements Serializable {
    public String _name;
    public Boolean _isDirectory;
    public FileNode[] _listFiles;

    public FileNode(String filePath) throws Exception {
        File file = new File (filePath);
        _name = file.getName();
        _isDirectory = file.isDirectory();
        if (_isDirectory) {
            int length;
            File[] fileList = file.listFiles();
            if (fileList == null) {
                length = 0;
                _listFiles = null;
            }
            else {
                length = fileList.length;
                _listFiles = new FileNode[length];
                for (int i = 0; i < length; i++) {
                    _listFiles[i] = new FileNode(fileList[i].getPath());
                }
            }
        }
        else {
            _listFiles = null;
        }
    }

    @Override
    public String toString() {
        String rst = "FileNode{" +
                "_name=" + _name +
                "_isDirectory=" + _isDirectory +
                "_listFiles=[";
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

    public void print(int level) {
        System.out.print(" ".repeat(level));
        if (_isDirectory) {
            System.out.print("*");
        }
        else {
            System.out.print("-");
        }
        System.out.println(_name);
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
