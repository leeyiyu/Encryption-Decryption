package com.lee.uti;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * 功能描述:
 *
 * @param:
 * @return:
 * @auther: Zywoo Lee
 * @date: 2022/11/3 19:00
 */
public class SM2Util {

    /**
     * 创建公私钥
     *
     * @return
     */
    public static SM2Key createKeys() {
        SM2 sm2 = SmUtil.sm2();
        return SM2Key.builder()
                .privateKey(HexUtil.encodeHexStr(BCUtil.encodeECPrivateKey(sm2.getPrivateKey())))
                .publicKey(HexUtil.encodeHexStr(((BCECPublicKey) sm2.getPublicKey()).getQ().getEncoded(false)))
                .build();
    }

    public static String sign(String data,
                              String privateKey) {
        return sign(data, StandardCharsets.UTF_8, privateKey);
    }

    public static String sign(String data,
                              Charset charset,
                              String privateKey) {
        ECPrivateKeyParameters privateKeyParameters = BCUtil.toSm2Params(privateKey);
        SM2 sm2 = new SM2(privateKeyParameters, null);
        sm2.usePlainEncoding();
        return HexUtil.encodeHexStr(sm2.sign(data.getBytes(charset), "SZL".getBytes(charset)));
    }

    public static boolean verify(String data,
                                 String sign,
                                 String publicKey) {
        return verify(data, sign, StandardCharsets.UTF_8, publicKey);
    }

    public static boolean verify(String data,
                                 String sign,
                                 Charset charset,
                                 String publicKey) {
        SM2 sm2 = new SM2(null, getECPublicKeyParameters(publicKey));
//        sm2.setMode(SM2Engine.Mode.C1C2C3);
        sm2.usePlainEncoding();
        boolean verify = sm2.verify(data.getBytes(charset), HexUtil.decodeHex(sign), "SZL".getBytes(charset));
//        boolean verify = sm2.verifyHex(HexUtil.encodeHexStr(data, charset), sign);
        return verify;
    }

    private static ECPublicKeyParameters getECPublicKeyParameters(String publicKey) {
        String publicKeyHex = publicKey;
        //这里需要根据公钥的长度进行加工
        if (publicKeyHex.length() == 130) {
            //这里需要去掉开始第一个字节 第一个字节表示标记
            publicKeyHex = publicKeyHex.substring(2);
        }
        String xhex = publicKeyHex.substring(0, 64);
        String yhex = publicKeyHex.substring(64, 128);
        return BCUtil.toSm2Params(xhex, yhex);
    }

    /**
     * 公钥加密
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static String encrypt(String data,
                                 String publicKey) {
//        SM2 sm2 = new SM2();
//        PublicKey publicKey = KeyUtil.generatePublicKey("SM2", HexUtil.decodeHex(key.getPublicKey()));
//        sm2.setPublicKey(publicKey);
//        sm2.setMode(SM2Engine.Mode.C1C2C3);


        SM2 sm2 = new SM2(null, getECPublicKeyParameters(publicKey));
        sm2.setMode(SM2Engine.Mode.C1C3C2);
        sm2.usePlainEncoding();
        String encryptStr = sm2.encryptBcd(data, KeyType.PublicKey);
        return encryptStr;
    }

    /**
     * 私钥解密
     *
     * @param encryptStr
     * @param privateKey
     * @return
     */
    public static String decryp(String encryptStr,
                                String privateKey) {
//        SM2 sm2 = new SM2();
//        PrivateKey privateKey = KeyUtil.generatePrivateKey("SM2", HexUtil.decodeHex(key.getPrivateKey()));
//        sm2.setPrivateKey(privateKey);
//        sm2.setMode(SM2Engine.Mode.C1C2C3);
        ECPrivateKeyParameters privateKeyParameters = BCUtil.toSm2Params(privateKey);
        SM2 sm2 = new SM2(privateKeyParameters, null);
        sm2.setMode(SM2Engine.Mode.C1C3C2);

        String decryptStr = StrUtil.utf8Str(sm2.decryptFromBcd(encryptStr, KeyType.PrivateKey));
        return decryptStr;
    }


    public static void main(String[] args) {
        //需要加密的明文
        String data = "测试";
        //SM2Key key = createKeys();
        SM2Key key = SM2Key.builder()
                .privateKey("00e6d2f474137b02c502c63615695461b7d49220941ee0f50e3acfc1c2d989d8a3")
                .publicKey("0460635a37995021d74d42f4333ca006e084180f3a6dc6671c82ac4969ece10369597a2ab17881d9c1e20e45ada93d30850299629387e31210b5c6e6a584769708")
                .build();
        System.out.println(key);
//        String sign = sign(data, key.getPrivateKey());
//        String sign = "32A6200C17801FA127601AC6FD7308A824B3033B67423FD45A4D9C83AA319D70BE779A4544AAFB7078AEC3D008E382C9AB00523FBE178CD2E1836E8B49FCC179";
//        System.out.println("签名: " + sign);
//        System.out.println("verify: " + verify(data, sign, key.getPublicKey()));
        //String encryptStr = encrypt(data, key.getPublicKey());
        String encryptStr = "042d0419eec070a20bb03763b7af8dc27dca92e663105d0c094bb5309ecd205fea54feb185ffed54af7d744dbf3ae6c979254d791de31a5ca7fbad4b980a81f3f5b0192a5e4e529b09f738e47b731ed3171cf6370c488ecef634c7a89bfa86f444592e239b0fec596adf4ce40bb787e4ea06855ec73a5ce24e";
        System.out.println("encryptStr " + encryptStr);
        System.out.println("decryp " + decryp(encryptStr, key.getPrivateKey()));
    }
}
