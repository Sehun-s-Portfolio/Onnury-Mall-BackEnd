package com.onnury.admin.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AdminAccountLoginResponseDto {
    private Long adminAccountId;
    private String loginId;
    private String type;
    private Long supplierId;
}
