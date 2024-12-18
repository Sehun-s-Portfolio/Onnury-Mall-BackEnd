package com.onnury.mypage.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TotalMyPaymentInfoResponseDto {
    private Long totalCount;
    private List<MyPagePaymentResponseDto> paymentList;
}
