package Core;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

public class RSA {
    static final String KEY_ALGORITHM = "RSA";
    static final int KEY_SIZE = 2048;
    static final String CHARSET = "UTF-8";

    public static KeyPair GetKeyPair(String password) {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG")
    }
}
