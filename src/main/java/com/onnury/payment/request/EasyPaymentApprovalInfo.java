package com.onnury.payment.request;

import com.onnury.common.base.AbstractVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class EasyPaymentApprovalInfo extends AbstractVO {
    private String resCd;
    private String resMsg;
    private String shopOrderNo;
    private String authorizationId;
    private String shopValue1;
    private String shopValue2;
    private String shopValue3;
    private String shopValue4;
    private String shopValue5;
    private String shopValue6;
    private String shopValue7;
}
