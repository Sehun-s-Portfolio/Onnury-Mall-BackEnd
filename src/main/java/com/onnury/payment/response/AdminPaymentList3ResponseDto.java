package com.onnury.payment.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AdminPaymentList3ResponseDto {

    private Long total;
    private List<AdminPaymentResponse3Dto> paymentList;

}
