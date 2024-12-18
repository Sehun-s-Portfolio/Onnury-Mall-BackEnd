package com.onnury.member.request;

import lombok.Getter;

@Getter
public class MemberLoginRequestDto {
    private String loginId;
    private String password;
}
