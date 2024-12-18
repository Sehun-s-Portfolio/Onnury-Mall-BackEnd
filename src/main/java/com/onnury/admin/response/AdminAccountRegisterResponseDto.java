package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdminAccountRegisterResponseDto {
    private String loginId;
    private String passwword;
}
