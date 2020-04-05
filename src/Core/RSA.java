package Core;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {
    static final String KEY_ALGORITHM = "RSA";
    static final int KEY_SIZE = 1024;
    static final String CHARSET = "UTF-8";

    public static KeyPair GetKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] GetPrivateKeyBytes(KeyPair keypair) {
        return keypair.getPrivate().getEncoded();
    }

    public static String GetPrivateKeyString(KeyPair keypair) {
        return Base64.getEncoder().encodeToString(GetPrivateKeyBytes(keypair));
    }

    public static byte[] GetPublicKeyBytes(KeyPair keyPair) {
        return keyPair.getPublic().getEncoded();
    }

    public static String GetPublicKeyString(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(GetPublicKeyBytes(keyPair));
    }

    public static byte[] EncryptByPrivateKeyByte(byte[] inputByteData, byte[] privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(inputByteData);
    }

    public static String EncryptByPrivateKeyString(String inputStringData, String privateKey) throws Exception {
        byte[] key = Base64.getDecoder().decode(privateKey);
        return Base64.getEncoder().encodeToString(EncryptByPrivateKeyByte(inputStringData.getBytes(CHARSET), key));
    }

    public static byte[] EncryptByPublicKeyByte(byte[] inputBytedata, byte[] publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(inputBytedata);
    }

    public static String EncryptByPublicKeyString(String inputStringData, String publicKey) throws Exception {
        byte[] key = Base64.getDecoder().decode(publicKey);
        return Base64.getEncoder().encodeToString(EncryptByPublicKeyByte(inputStringData.getBytes(CHARSET), key));
    }

    public static byte[] DecryptByPrivateKeyByte(byte[] inputByteData, byte[] privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(inputByteData);
    }

    public static String DecryptByPrivateKeyString(String inputStringData, String privateKey) throws Exception {
        byte[] key = Base64.getDecoder().decode(privateKey);
        return new String(DecryptByPrivateKeyByte(Base64.getDecoder().decode(inputStringData), key), CHARSET);
    }

    public static byte[] DecryptByPublicKeyByte(byte[] inputByteData, byte[] publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(inputByteData);
    }

    public static String DecryptByPublicKeyString(String inputStringData, String publicKey) throws Exception {
        byte[] key = publicKey.getBytes();
        return new String(DecryptByPublicKeyByte(inputStringData.getBytes(), key), CHARSET);
    }

    public static void main(String[] args) throws Exception{
        KeyPair keyPair = RSA.GetKeyPair();
        byte[] publicKeyByte = RSA.GetPublicKeyBytes(keyPair);
        byte[] privateKeyByte = RSA.GetPrivateKeyBytes(keyPair);
        String publicKeyString = RSA.GetPublicKeyString(keyPair);
        String privateKeyString = RSA.GetPrivateKeyString(keyPair);

        Debug.Log("Public Key :");
        Debug.Log(publicKeyByte);
        Debug.Log(publicKeyString);
        Debug.Log("Private Key :");
        Debug.Log(privateKeyByte);
        Debug.Log(privateKeyString);

        String data = "RSA 加解密测试";
        Debug.Log(new String(RSA.DecryptByPrivateKeyByte(RSA.EncryptByPublicKeyByte(data.getBytes(), publicKeyByte), privateKeyByte), CHARSET));
        Debug.Log(RSA.DecryptByPrivateKeyString(RSA.EncryptByPublicKeyString(data, publicKeyString), privateKeyString));
    }
}
