package com.onnury.payment.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class AdminSupllierPaymentListResponseDto {

    private Long total;
    private List<AdminOrderInProductResponseQDto> paymentList;

}
