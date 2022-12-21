package com.lee.uti;



import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * @Auther: Zywoo Lee
 * @Date: 2022/11/4 20:28
 * @Description:国密SM4对称加解密算法
 */
public class SM4 {

    private static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private static final String BOUNCY_CASTLE_PROVIDER_NME = BouncyCastleProvider.PROVIDER_NAME;
    private static final String SM4_ALGORITHM = "SM4";
    private static final int DEFAULT_KEY_SIZE = 128;
    private static final String DEFAULT_ENCODING = "UTF-8";

    static {
        Security.removeProvider(BOUNCY_CASTLE_PROVIDER_NME);
        Security.addProvider(BOUNCY_CASTLE_PROVIDER);
    }

    /**
     * SM4 加密
     *
     * @param plainText      明文数据
     * @param key            密钥
     * @param modeAndPadding 加密模式和padding模式
     * @param iv             初始向量(ECB模式下传NULL)
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] plainText, byte[] key, SM4ModeAndPaddingEnum modeAndPadding, byte[] iv) {
        return sm4(plainText, key, modeAndPadding, iv, Cipher.ENCRYPT_MODE);
    }

    /**
     * SM4 解密
     *
     * @param cipherText            密文数据
     * @param key                   密钥
     * @param sm4ModeAndPaddingEnum 加密模式和padding模式
     * @param iv                    初始向量(ECB模式下传NULL)
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] cipherText, byte[] key, SM4ModeAndPaddingEnum sm4ModeAndPaddingEnum, byte[] iv) {
        return sm4(cipherText, key, sm4ModeAndPaddingEnum, iv, Cipher.DECRYPT_MODE);
    }

    /**
     * SM4算法
     *
     * @param input                 输入数据
     * @param key                   密钥
     * @param sm4ModeAndPaddingEnum SM4模式
     * @param iv                    初始向量ECB模式下为null
     * @param mode                  加密或解密模式
     * @return
     * @throws Exception
     */
    private static byte[] sm4(byte[] input, byte[] key, SM4ModeAndPaddingEnum sm4ModeAndPaddingEnum, byte[] iv, int mode) {
        SecretKeySpec sm4Key = new SecretKeySpec(key, SM4_ALGORITHM);
        byte[] output = null;
        try {
            Cipher cipher = Cipher.getInstance(sm4ModeAndPaddingEnum.getName(), BOUNCY_CASTLE_PROVIDER_NME);
            if (iv == null) {
                cipher.init(mode, sm4Key);
            } else {
                cipher.init(mode, sm4Key, new IvParameterSpec(iv));
            }
            output = cipher.doFinal(input);
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
        } catch (NoSuchPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        }

        return output;
    }

    public enum SM4ModeAndPaddingEnum {
        SM4_ECB_NoPadding("SM4/ECB/NoPadding"),
        SM4_ECB_PKCS5Padding("SM4/ECB/PKCS5Padding"),
        SM4_ECB_PKCS7Padding("SM4/ECB/PKCS7Padding"),
        SM4_CBC_NoPadding("SM4/CBC/NoPadding"),
        SM4_CBC_PKCS5Padding("SM4/CBC/PKCS5Padding"),
        SM4_CBC_PKCS7Padding("SM4/CBC/PKCS7Padding");

        private String name;

        SM4ModeAndPaddingEnum(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 生成密钥
     *
     * @param sm4ModeAndPaddingEnum
     * @return
     * @throws Exception
     */
    public static byte[] generateKey(SM4ModeAndPaddingEnum sm4ModeAndPaddingEnum) throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(sm4ModeAndPaddingEnum.getName(), BOUNCY_CASTLE_PROVIDER_NME);
        kg.init(DEFAULT_KEY_SIZE, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    /**
     * 生成密钥
     *
     * @return
     */
    public static String generateKey() {
        /**
         * 默认使用SM4_ECB_NoPadding
         */
        String key = "";
        try {
            key = Base64.encodeBase64URLSafeString(generateKey(SM4ModeAndPaddingEnum.SM4_ECB_NoPadding));
        } catch (NoSuchProviderException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        return key;
    }


    /**
     * 加密
     *
     * @param plainText
     * @param base64Key
     * @return
     */
    public static String encrypt(String plainText, String base64Key) {
        /**
         * 默认使用SM4_ECB_NoPadding
         */
        String base64Cipher = "";
        try {
            byte[] plain = plainText.getBytes(DEFAULT_ENCODING);
            byte[] key = Base64.decodeBase64(base64Key);
            base64Cipher = Base64.encodeBase64String(encrypt(plain, key, SM4ModeAndPaddingEnum.SM4_ECB_PKCS5Padding, null));
        } catch (UnsupportedEncodingException e) {
        }
        return base64Cipher;
    }


    /**
     * 解密
     *
     * @param base64Cipher
     * @return
     */
    public static String decrypt(String base64Cipher, String base64Key) {
        /**
         * 默认使用SM4_ECB_NoPadding
         */
        String plain = "";
        try {
            byte[] cipher = Base64.decodeBase64(base64Cipher);
            byte[] key = Base64.decodeBase64(base64Key);
            plain = new String(decrypt(cipher, key, SM4ModeAndPaddingEnum.SM4_ECB_PKCS5Padding, null),DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return plain;
    }
}
