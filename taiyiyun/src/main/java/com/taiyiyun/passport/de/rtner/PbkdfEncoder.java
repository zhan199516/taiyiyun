package com.taiyiyun.passport.de.rtner;

import com.taiyiyun.passport.de.rtner.security.auth.spi.PBKDF2Engine;
import com.taiyiyun.passport.de.rtner.security.auth.spi.PBKDF2Parameters;
import sun.misc.BASE64Encoder;

import java.security.SecureRandom;

/**
 * Created by okdos on 2017/7/8.
 */
public class PbkdfEncoder {
    private static PbkdfEncoder ourInstance = new PbkdfEncoder();

    public static PbkdfEncoder getInstance() {
        return ourInstance;
    }

    private PbkdfEncoder() {
    }

    public String newRandomString() throws Exception{
        byte[] src = new byte[12];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(src);
        String ss = new BASE64Encoder().encode(src);
        return ss;
    }

    private final int circleCount = 901;

    public String encodePassword(String password) throws Exception {

        String saltStr = newRandomString();

        byte[] salt = saltStr.getBytes();

        PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA256", "UTF-8", salt, circleCount);
        byte[] dk = new PBKDF2Engine(p).deriveKey(password);
        byte[] dk24 = new byte[24];
        System.arraycopy(dk, 0, dk24, 0, 24);

        String hashedPassword = new BASE64Encoder().encode(dk24);

        return "PBKDF2$sha256$" + circleCount + "$" + saltStr + "$" + hashedPassword;
    }

}
