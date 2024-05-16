package org.example;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Security;

public class Des {

    byte[] cipherPassword(String password, String key) throws Exception {
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
