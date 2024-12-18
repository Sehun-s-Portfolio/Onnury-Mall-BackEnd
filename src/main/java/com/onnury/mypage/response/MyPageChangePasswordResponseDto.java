package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MyPageChangePasswordResponseDto {
    private String prevPassword; // 이전 비밀번호
    private String newPresentPassword; // 새롭게 변경된 현재 비밀번호
}
