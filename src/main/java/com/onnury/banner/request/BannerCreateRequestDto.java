package com.onnury.banner.request;

import lombok.Getter;

@Getter
public class BannerCreateRequestDto {
    private String title;
    private String linkUrl;
    private int expressionOrder;
    private String startPostDate;
    private String endPostDate;
}
