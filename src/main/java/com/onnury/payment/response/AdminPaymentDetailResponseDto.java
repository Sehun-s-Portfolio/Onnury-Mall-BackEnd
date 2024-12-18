package com.onnury.payment.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AdminPaymentDetailResponseDto {

    private AdminPaymentMemberPosterResponseDto paymentDetailList;
    private List<AdminPaymentProductResponseDto> paymentProductList;
    private List<AdminPaymentDeriveryResponseDto> paymentDeriveryList;

}
