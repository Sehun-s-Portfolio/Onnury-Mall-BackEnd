package com.onnury.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Service
public class BizPointCodeccService {
    public static byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    @Value("${onnury.biz.point.mchtKey}")
    private String mchtKey; // 암복호화 KEY

    public String biztotpayDecCode(String deviceCode) throws Exception {
        String decData = DecryptAesFromHexa(deviceCode, mchtKey);

        return decData;
    }
    public String biztotpayEncCode(String deviceCode) throws Exception {
        String encData = EncryptAesToHexa(deviceCode, mchtKey);

        return encData;
    }

    public String EncryptAesToHexa(String input, String key) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException {
        if (input == null || key == null || input.length() < 1 || key.length() < 1) {
            return null;
        }
        AlgorithmParameterSpec iv = new IvParameterSpec(ivBytes);
        SecretKeySpec k = new SecretKeySpec(key.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, k, iv);

        byte[] inBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] encBytes = c.doFinal(inBytes);

        return changeBytes2Hex(encBytes);
    }

    public String changeBytes2Hex(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public String DecryptAesFromHexa(String input, String key) throws Exception {
        if (input == null || key == null || input.length() < 1 || key.length() < 1) {
            return null;
        }

        AlgorithmParameterSpec iv = new IvParameterSpec(ivBytes);
        SecretKeySpec k = new SecretKeySpec(key.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, k, iv);

        String decString = new String(c.doFinal(changeHex2Byte(input)));
        return decString;
    }

    public byte[] changeHex2Byte(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < hex.length(); i += 2) {
            int b = Integer.parseInt(hex.substring(i, i + 2), 16);
            baos.write(b);
        }

        return baos.toByteArray();
    }

    public String getHmacSha256(String input) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {

        if(input == null || input.length() <1) {
            return null;
        }

        SecretKeySpec keySpec = new SecretKeySpec(mchtKey.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);

        byte[] inBytes = input.getBytes("UTF-8");
        byte[] encBytes = mac.doFinal(inBytes);
        return changeBytes2Hex(encBytes);
    }

}
