package com.onnury.mypage.request;

import lombok.Getter;

@Getter
public class UserCancleRequestDto {

    private String orderNumber;
    private String seq;
    private int quantity;
    private String linkCompany; // 출처
}
