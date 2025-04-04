package com.onnury;

import com.onnury.configuration.AES128Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class OnNuryApplicationTests {

//	@Autowired
//	private AES128Config aes128Config;
//
//	@Test
//	@DisplayName("AES128 양반향 암호화 테스트")
//	void contextLoads() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
//
//		String text = "My Name Is Jin Se Hun";
//
//		String enc = aes128Config.encryptAes(text);
//		String dec = aes128Config.decryptAes(enc);
//
//		System.out.println("enc = " + enc);
//		System.out.println("dec = " + dec);
//
//		assertThat(dec).isEqualTo(text);
//	}

}
