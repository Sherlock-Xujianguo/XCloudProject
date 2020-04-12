package Core;

import java.io.*;

public class FileTree implements Serializable{
    public static void SaveClientFileTree(String path) {
        File file = new File(Setting.Client._fileTreeDataPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        SaveFileTree(path, Setting.Client._fileTreeDataName);
    }

    public static void SaveServerFileTree(String path) {
        File file = new File(Setting.Server._fileTreeDataPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        SaveFileTree(path, Setting.Server._fileTreeDataName);
    }

    private static void SaveFileTree(String path, String savePath) {
        try {
            FileNode fn = new FileNode(path);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savePath));
            oos.writeObject(fn);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileNode GetFileTree(String savePath) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(savePath));
            FileNode fn = (FileNode) ois.readObject();
            return fn;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void RestoreClientFileTree(FileNode fn) {
        RestoreFileTree(fn, Setting.Client._defaultDirectoryPath);
    }

    public static void RestoreServerFileTree(FileNode fn) {
        RestoreFileTree(fn, Setting.Server._defaultDirectoryPath);
    }

    public static void RestoreFileTree(FileNode fn, String targetPath) {
        try {
            File target = new File(targetPath);
            if (!target.exists()) {
                target.mkdirs();
            }

            targetPath = targetPath + Setting._envSep + fn._name;
            File f = new File(targetPath);
            if (fn._isDirectory) {
                if (!f.exists()) {
                    f.mkdirs();
                }
            }
            else {
                if (!f.exists()) {
                    f.createNewFile();
                }
            }

            if (fn._listFiles == null) {
                return;
            }

            for (FileNode fntemp:fn._listFiles) {
                RestoreFileTree(fntemp, targetPath);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String path = "C:\\Users\\13974\\Documents\\XCloudProject\\src";
            // SaveClientFileTree(path);

            FileNode fn = GetFileTree(Setting.Server._fileTreeDataName);
            fn.print();
            RestoreClientFileTree(fn);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
