package Core;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class RSA {
    static final String KEY_ALGORITHM = "RSA";
    static final int KEY_SIZE = 2048;
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

    public static void main(String[] args) throws Exception{
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];

        random.nextBytes(bytes);
        System.out.println(bytes);
        bytes = new byte[20];
        random.nextBytes(bytes);
        System.out.println(bytes);

    }
}
