package Core;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {

    public static String GetFileMD5(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buff = new byte[1024];
        int len;
        FileInputStream in = new FileInputStream(file);
        while ((len = in.read(buff, 0, buff.length)) != -1) {
            digest.update(buff, 0, len);
        }
        in.close();
        BigInteger bigInteger = new BigInteger(1, digest.digest());
        return bigInteger.toString(16);
    }

    public static void main(String[] args) {
        try {
            File file = new File(Setting.Client._defaultDirectoryPath + Setting._envSep + "b_d1f86e7a9efb1421bd7f0b996593abd9.jpg");
            Debug.Log(file.getParent());
            Debug.Log(GetFileMD5(file));
            file = new File(Setting.Server._defaultDirectoryPath + Setting._envSep + "b_d1f86e7a9efb1421bd7f0b996593abd9.jpg");
            Debug.Log(GetFileMD5(file));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
