package com.onnury.banner.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class BannerUpdateRequestDto extends AbstractVO {
    private String title;
    private String linkUrl;
    private int expressionOrder;
    private String startPostDate;
    private String endPostDate;
}
