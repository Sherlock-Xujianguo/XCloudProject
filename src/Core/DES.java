package Core;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;

public class DES {
    static final int KEY_SIZE = 56;
    static final String KEY_ALGORITHM = "DES";

    public static byte[] GetKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey generateKey = keyGenerator.generateKey();
        return generateKey.getEncoded();
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

    public static void main(String args[]) throws Exception {
        Debug.Log(GetKey());
    }
}
