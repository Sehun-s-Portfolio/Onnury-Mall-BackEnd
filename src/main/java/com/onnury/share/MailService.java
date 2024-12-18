package com.onnury.share;

import com.onnury.member.domain.Member;
import com.onnury.member.request.MemberFindLoginIdRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    // 계정 id 확인 메일 보내기
    public void sendLoginIdEmail(Member loginMember) {

        try {
            // 단순 문자 메일을 보낼 수 있는 객체 생성
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(loginMember.getEmail()); // 메일을 받을 목적지 이메일
            //message.setTo("wlstpgns51@naver.com"); // 메일을 받을 목적지 이메일

            message.setSubject("[확인] 온누리 몰 계정 아이디 확인"); // 메일 제목

            StringBuilder expressId = new StringBuilder(loginMember.getLoginId().substring(0, 3));
            String notExpressId = loginMember.getLoginId().substring(4);

            for (int i = 0; i < notExpressId.length(); i++) {
                String wildCard = "*";
                expressId.append(wildCard);
            }

            if (loginMember.getType().equals("C")) {
                // 메일 내용
                message.setText(
                        loginMember.getUserName() + " 님의 아이디는 " + expressId + " 입니다."
                );
            } else if (loginMember.getType().equals("B")) {
                // 메일 내용
                message.setText(
                        loginMember.getManager() + " 님의 아이디는 " + expressId + " 입니다."
                );
            }

            // 메일 전송
            javaMailSender.send(message);
        } catch (MailSendException m) {
            m.printStackTrace();
        }
    }


    // 임시 비밀번호 재발급 메일 보내기
    public void sendPasswordEmail(Member loginMember, String immediatePassword) {

        try {
            // 단순 문자 메일을 보낼 수 있는 객체 생성
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(loginMember.getEmail()); // 메일을 받을 목적지 이메일
            //message.setTo("wlstpgns51@naver.com"); // 메일을 받을 목적지 이메일

            message.setSubject("[확인] 온누리 몰 계정 임시 비밀번호 발급"); // 메일 제목

            if (loginMember.getType().equals("C")) {
                // 메일 내용
                message.setText(
                        loginMember.getUserName() + " 님의 비밀번호는 " + immediatePassword + " 입니다."
                );
            } else if (loginMember.getType().equals("B")) {
                // 메일 내용
                message.setText(
                        loginMember.getManager() + " 님의 비밀번호는 " + immediatePassword + " 입니다."
                );
            }

            // 메일 전송
            javaMailSender.send(message);
        } catch (MailSendException m) {
            m.printStackTrace();
        }
    }


    // HTML 메일 보내기
    public void sendHTMLEmail() {

    }

    // 6자리 랜덤 비밀번호 생성
    public void createRandomPw() {

    }
}
