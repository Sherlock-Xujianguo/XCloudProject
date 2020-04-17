package Core;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.util.Base64;

public class AES {
    static final int KEY_SIZE = 256;
    static final String KEY_ALGORITHM = "AES";

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

    public static byte[] StringKey2Byte(String key) throws Exception {
        return Key2Byte(String2Key(key));
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

    public static byte[] EncrypyByte(byte[] inputByteData, String key) throws Exception {
        byte[] keyByte = StringKey2Byte(key);
        return EncrypyByte(inputByteData, keyByte);
    }

    public static byte[] DecrypyByte(byte[] inputByteData, String key) throws Exception {
        byte[] keyByte = StringKey2Byte(key);
        return DecrypyByte(inputByteData, keyByte);
    }

    public static byte[] EncrypyByte(byte[] inputByteData, byte[] key) throws Exception {
        SecretKey sKey = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, sKey);
        return cipher.doFinal(inputByteData);
    }

    public static byte[] DecrypyByte(byte[] inputByteData, byte[] key) throws Exception {
        SecretKey sKey = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, sKey);
        return cipher.doFinal(inputByteData);
    }

    public static void main(String args[]) throws Exception {
        String tempkey = GetKeyString();
        byte[] key = StringKey2Byte(tempkey);
        String data = "this is a aes test\n" +
                "i hope it has no bugs\n" +
                "fuck\n" +
                "?\n" +
                "??\n" +
                "but i dont know why it has bugs\n" +
                "please please please please";
        Debug.Log(new String(DecrypyByte(EncrypyByte(data.getBytes(), tempkey), tempkey)));

        File file1 = new File(Setting.Client._defaultDirectoryPath + Setting._envSep + "temp.txt");
        File file2 = new File(Setting.Client._defaultDirectoryPath + Setting._envSep + "temp2.txt");
        File file3 = new File(Setting.Client._defaultDirectoryPath + Setting._envSep + "temp3.txt");

        int b;

        FileInputStream fis = new FileInputStream(file1);
        FileOutputStream fos = new FileOutputStream(file2);

        InputStream is = DESInputStream(fis, key);


        while ((b = is.read()) != -1) {
            fos.write((byte) b);
        }

        fis.close();
        fos.close();
        is.close();

        fis = new FileInputStream(file2);
        fos = new FileOutputStream(file3);
        OutputStream os = DESOutputStream(fos, key);
        while ((b = fis.read()) != -1) {
            os.write((byte) b);
        }


        fis.close();
        fos.close();
        System.out.println("over");
    }
}
