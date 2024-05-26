package org.example;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Security;
import java.util.Base64;

public class Des {

    private static final Logger logger = LoggerFactory.getLogger(Des.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String getCipher(String plaintext, String key) {
        String cipher = null;
        try {
            cipher = cipherPassword(plaintext, key);
        } catch (Exception e) {
            logger.error("Error while ciphering password", e);
        }
        return cipher;
    }

    public String cipherPassword(String password, String key) throws Exception {
        byte[] desKey = key.getBytes();

        DESKeySpec desKeySpec = new DESKeySpec(desKey);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] plaintext = password.getBytes();
        byte[] output = cipher.doFinal(plaintext);

        return Base64.getEncoder().encodeToString(output);
    }

}
