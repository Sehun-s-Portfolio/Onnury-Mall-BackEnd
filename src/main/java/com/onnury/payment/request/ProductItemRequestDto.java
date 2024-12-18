package com.onnury.payment.request;

import lombok.Getter;

@Getter
public class ProductItemRequestDto {
    private int seq;
    private String biz_no;
    private String frc_cd;
    private String name;
    private int count;
    private int amount;
}
