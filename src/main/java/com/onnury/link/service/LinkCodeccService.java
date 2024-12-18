package com.onnury.link.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkCodeccService {
    public static byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public String DecCode(String vv) throws Exception {

        final String mchtKey = "L0qEgypylYXbR8MjLYoEfUiTMYO4zkiW"; // 암복호화 KEY
        String decData = DecryptAesFromHexa(vv, mchtKey);

        return decData;
    }

    public String EncCode(String vv) throws Exception {

        final String mchtKey = "L0qEgypylYXbR8MjLYoEfUiTMYO4zkiW"; // 암복호화 KEY
        String encData = EncryptAesToHexa(vv, mchtKey);

        return encData;
    }

    public static String EncryptAesToHexa(String input, String key) throws NoSuchAlgorithmException,
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

    public static String changeBytes2Hex(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public static String DecryptAesFromHexa(String input, String key) throws Exception {
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

    public static byte[] changeHex2Byte(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < hex.length(); i += 2) {
            int b = Integer.parseInt(hex.substring(i, i + 2), 16);
            baos.write(b);
        }

        return baos.toByteArray();
    }
}