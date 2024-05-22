package org.example;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Security;

public class Des {

    private static final Logger logger = LoggerFactory.getLogger(Des.class);

    public static byte[] getCipher(String plaintext, String key) {
        byte[] cipher = new byte[0];
        try {
            cipher = cipherPassword(plaintext, key);
        } catch (Exception e) {
            logger.error("Error while ciphering password", e);
        }
        return cipher;
    }

    public static byte[] cipherPassword(String password, String key) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        byte[] desKey = key.getBytes();

        DESKeySpec desKeySpec = new DESKeySpec(desKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] plaintext = password.getBytes();

        return cipher.doFinal(plaintext);
    }
}
