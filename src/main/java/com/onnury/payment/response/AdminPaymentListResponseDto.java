package com.onnury.payment.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AdminPaymentListResponseDto {

    private Long total;
    private List<AdminPaymentResponse2Dto> paymentList;

}
