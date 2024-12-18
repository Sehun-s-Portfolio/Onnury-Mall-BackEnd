package com.onnury.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RequiredArgsConstructor
@Service
public class EasyPayCodeccService {

        @Value("${easy.codec.secret}")
        private String codecSecretKey;

        public String easypayDeccode(String args) throws NoSuchAlgorithmException, InvalidKeyException {

                String secretKey = codecSecretKey; // 암복호화키
                String message = args; // PG 거래번호|결제금액|거래일시
                Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
                sha256_HMAC.init(secret_key);

                byte[] hash = sha256_HMAC.doFinal(message.getBytes()); // hash 값을 HexString 으로 변환하세요.

                //byte array to hexString
                StringBuilder sb = new StringBuilder();
                for (final byte b : hash)
                    sb.append(String.format("%02x", b&0xff));

                return sb.toString();
        }

}
