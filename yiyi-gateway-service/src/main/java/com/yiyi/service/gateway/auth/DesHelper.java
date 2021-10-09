package com.yiyi.service.gateway.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;


public class DesHelper {
    private static final Logger log = LoggerFactory.getLogger(DesHelper.class);

    public static String decrypt(String data, String key) {
        return (data == null) ? null : new String(decrypt(Base64Utils.decodeFromUrlSafeString(data), key.getBytes()));
    }


    private static byte[] decrypt(byte[] data, byte[] key) {
        SecureRandom sr = new SecureRandom();


        try {
            DESKeySpec dks = new DESKeySpec(key);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(dks);


            Cipher cipher = Cipher.getInstance("DES");


            cipher.init(2, securekey, sr);

            return cipher.doFinal(data);
        } catch (Exception e) {
            log.error("decrypt error：", e);
            throw new FallbackException(e);
        }
    }
}
