package com.onnury.payment.request;

import lombok.Getter;

@Getter
public class PosterInfoRequestDto {
    private String address;
    private String detailAddress;
    private String phone;
    private String postNumber;
    private String userName;
    private String memo;
}
