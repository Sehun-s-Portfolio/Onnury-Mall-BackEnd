package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AreaMemberCountInfoResponseDto {
    private String area;
    private Long businessMemberCount;
    private Long customerMemberCount;
    private Long totalMemberCount;
}
