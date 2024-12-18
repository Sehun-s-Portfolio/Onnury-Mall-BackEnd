package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MemberDashboardResponseDto {
    private Long businessMemberCount;
    private Long customerMemberCount;
    private List<AreaMemberCountInfoResponseDto> areaMemberCountList;
}
