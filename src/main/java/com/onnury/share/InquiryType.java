package com.onnury.share;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryType{
    TYPE_1("이용안내"),
    TYPE_2("회원관련"),
    TYPE_3("주문/결제"),
    TYPE_4("배송"),
    TYPE_5("취소/반품"),
    TYPE_6("교환/AS"),
    TYPE_7("혜택/이벤트"),
    TYPE_8("대량주문/제휴");

    String inqury;

    public static String getInquiry(int typeNumber){
        switch (typeNumber){
            case 1:
                return TYPE_1.inqury;
            case 2:
                return TYPE_2.inqury;
            case 3:
                return TYPE_3.inqury;
            case 4:
                return TYPE_4.inqury;
            case 5:
                return TYPE_5.inqury;
            case 6:
                return TYPE_6.inqury;
            case 7:
                return TYPE_7.inqury;
            case 8:
                return TYPE_8.inqury;
        }

        return null;
    }
}
