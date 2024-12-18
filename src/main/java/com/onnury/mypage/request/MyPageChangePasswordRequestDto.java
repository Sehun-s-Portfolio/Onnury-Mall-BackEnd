package com.onnury.mypage.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MyPageChangePasswordRequestDto {
    private String presentPassword; // 현재 비밀번호
    private String newPassword; // 변경할 비밀번호
    private String newPasswordCheck; // 변경 비밀번호 확인용 비밀번호
}
