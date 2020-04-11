package Core;

import java.io.*;

public class FileTree implements Serializable{

    public static void main(String[] args) {
        try {
            String path = "/Users/sherlock_xujianguo/XCloudProject/src/Core";
            FileNode fn = new FileNode(path);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("SerializeFile.dat"));
            oos.writeObject(fn);

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("SerializeFile.dat"));
            FileNode fnr = (FileNode)ois.readObject();
            fnr.print();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SerializableFileObject(String Path) throws Exception {
        File file = new File(Path);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("SerializeFile.dat"));
        oos.writeObject(file);
    }
}
