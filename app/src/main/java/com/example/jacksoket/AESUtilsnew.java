package com.example.jacksoket;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtilsnew {
    //IV长度应该为16，请跟服务端保持一致
    private static final String iv = "1234567887654321";

    //AES/CBC/PKCS5Padding默认对应PHP则为：AES-128-CBC
    private static final String CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    private static final String AES = "AES";//AES 加密

    /**
     *
     * @param key 这个key长度应该为16位，另外不要用KeyGenerator进行强化，否则无法跨平台
     * @param cleartext
     * @return
     */
    public static String encrypt(String key, String cleartext){
        try {

            Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), AES);
            IvParameterSpec ivspec = new IvParameterSpec(AesUtils.generateKey2().getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(cleartext.getBytes());

            //base64编码一下
            return Base64Encoder.encode(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String key, String encrypted){
        try
        {

            byte[] encrypted1 = Base64Decoder.decodeToBytes(encrypted);
            Cipher cipher = Cipher.getInstance(CBC_PKCS5_PADDING);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), AES);
            IvParameterSpec ivspec = new IvParameterSpec(AesUtils.generateKey2().getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] original = cipher.doFinal(encrypted1);

            //转换为字符串
            return new String(original);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
