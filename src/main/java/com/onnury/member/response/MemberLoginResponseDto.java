package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberLoginResponseDto {
    private String loginId;
    private String type;
    private String userName;
}
