package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalInquiryListResponseDto {
    private Long totalInquiryCount;
    private List<MyPageInquiryResponseDto> inquiryList;
}
