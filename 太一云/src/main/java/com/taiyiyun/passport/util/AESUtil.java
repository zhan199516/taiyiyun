package com.taiyiyun.passport.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class AESUtil {
	private static final String VIPARA = "tPqMUvpBP4Y3LU2l";
    private static final String pwd="8925800tPqMUvpBP4Y3LU2l163e063c40";
    public static final String KEY="123456789";
    
	public static String encrypt(String content, String password) {
		try {
			IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());

			byte[] pwdBytes = password.getBytes("UTF-8");
			byte[] keyBytes = new byte[16];
			int len = pwdBytes.length;
			if (len > keyBytes.length) {
				len = keyBytes.length;
			}

			keyBytes = Arrays.copyOf(pwdBytes, len);
			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
			
			byte[] encryptedData = cipher.doFinal(content.getBytes("UTF-8"));
			return new BASE64Encoder().encode(encryptedData);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String encrypted, String password) {
		try {
			byte[] byteMi = new BASE64Decoder().decodeBuffer(encrypted);
			IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());

			byte[] pasBytes = password.getBytes("UTF-8");
			byte[] keyBytes = new byte[16];
			int len = pasBytes.length;
			if (len > keyBytes.length) {
				len = keyBytes.length;
			}

			keyBytes = Arrays.copyOf(pasBytes, len);

			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
			byte[] decryptedData = cipher.doFinal(byteMi);
			return new String(decryptedData, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static final String encryptStr(String InvitationId,String InvitationUserId) {
	   String key = "]".concat(InvitationUserId).concat("=").concat(String.valueOf(System.nanoTime()-10000));
	  return encrypt(key,pwd);
	}
	
	public static final String dncryptStr(String InvitationUserId) {
		String val = AESUtil.decrypt(InvitationUserId, pwd);
		String value= val.substring(val.indexOf("]")+1, val.lastIndexOf("="));
		return value;
	}
	
	
	
	static public void main(String[] args) {
		String value = AESUtil.encryptStr("b87c30690a4311e8925800163e063c8989", "b87c30690a4311e8925800163e063c40");
		System.out.println("....."+value);
		System.out.println("....."+AESUtil.dncryptStr(value));
		
	}

}
