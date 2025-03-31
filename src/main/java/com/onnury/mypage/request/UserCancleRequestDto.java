package com.onnury.mypage.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class UserCancleRequestDto extends AbstractVO {
    private String orderNumber;
    private String seq;
    private int quantity;
    private String linkCompany; // 출처
}
