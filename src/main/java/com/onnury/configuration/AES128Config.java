package com.onnury.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

@Slf4j
@Component
public class AES128Config {

    private static final Charset ENCODING_TYPE = StandardCharsets.UTF_8;
    private static final String INSTANCE_TYPE = "AES/CBC/PKCS5Padding";

    //@Value("${supplier.pwd.secret.key}")
    private final String secretKey = "T19iZAzYtZAE1sSTNrVlBCQS1pNzk0ek";
    private IvParameterSpec ivParameterSpec;
    private SecretKeySpec secretKeySpec;
    private Cipher cipher;

    AES128Config() throws NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] iv = secretKey.substring(0, 16).getBytes();   // 16bytes = 128bits
        this.ivParameterSpec = new IvParameterSpec(iv);
        this.secretKeySpec = new SecretKeySpec(this.secretKey.getBytes(ENCODING_TYPE), "AES");
        this.cipher = Cipher.getInstance(INSTANCE_TYPE);
    }

    // AES 암호화
    public String encryptAes(String plaintext) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encryted = cipher.doFinal(plaintext.getBytes(ENCODING_TYPE));
        return new String(Base64.getEncoder().encode(encryted), ENCODING_TYPE);
    }

    // AES 복호화
    public String decryptAes(String plaintext) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decoded = Base64.getDecoder().decode(plaintext.getBytes(ENCODING_TYPE));
        return new String(cipher.doFinal(decoded), ENCODING_TYPE);
    }

}
