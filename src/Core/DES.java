package Core;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.util.Base64;

public class DES {
    static final int KEY_SIZE = 56;
    static final String KEY_ALGORITHM = "DES";

    public static byte[] GetKeyByte() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey generateKey = keyGenerator.generateKey();
        return generateKey.getEncoded();
    }

    public static String GetKeyString() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey generateKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(generateKey.getEncoded());
    }

    public static byte[] Key2Byte(SecretKey key) throws Exception {
        return key.getEncoded();
    }

    public static String Key2String(SecretKey key) throws Exception {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey String2Key(String string) {
        byte[] decodedKey = Base64.getDecoder().decode(string);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
    }

    // 加密流
    public static InputStream DESInputStream(InputStream inputStream, byte[] key) throws Exception {
        SecretKey sKey = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sKey);

        return new CipherInputStream(inputStream, cipher);
    }

    public static OutputStream DESOutputStream(OutputStream outputStream, byte[] key) throws Exception {
        SecretKey sKey = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sKey);

        return new CipherOutputStream(outputStream, cipher);
    }

    public static byte[] EncrypyByte(byte[] inputByteData, byte[] key) throws Exception {
        SecretKeySpec sKey = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sKey);
        return cipher.doFinal(inputByteData);
    }

    public static byte[] DecrypyByte(byte[] inputByteData, byte[] key) throws Exception {
        SecretKeySpec sKey = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sKey);
        return cipher.doFinal(inputByteData);
    }

    public static void main(String args[]) throws Exception {
        String tempkey = GetKeyString();
        byte[] key = Key2Byte(String2Key(tempkey));
        String data = "DES  123";
        Debug.Log(new String(DecrypyByte(EncrypyByte(data.getBytes(), key), key)));

        File file1 = new File("/Users/sherlock_xujianguo/XCloudProject/src/Core/temp.txt");
        File file2 = new File("/Users/sherlock_xujianguo/XCloudProject/src/Core/temp2.txt");
        File file3 = new File("/Users/sherlock_xujianguo/XCloudProject/src/Core/temp3.txt");

        int length;
        byte[] b = new byte[1024];

        FileInputStream fis = new FileInputStream(file1);
        FileOutputStream fos = new FileOutputStream(file2);

        InputStream is = DESInputStream(fis, key);


        while ((length = is.read(b, 0, b.length)) != -1) {
            fos.write(b, 0, length);
            fos.flush();
        }

        fis = new FileInputStream(file2);
        fos = new FileOutputStream(file3);
        OutputStream os = DESOutputStream(fos, key);
        while ((length = fis.read(b, 0, b.length)) != -1) {
            os.write(b, 0, length);
            os.flush();
        }


        fis.close();
        fos.close();
        System.out.println("over");
    }
}
