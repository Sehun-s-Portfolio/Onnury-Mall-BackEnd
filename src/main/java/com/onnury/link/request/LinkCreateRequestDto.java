package com.onnury.link.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

import javax.persistence.Column;

@Getter
public class LinkCreateRequestDto extends AbstractVO {
    private String type; // 링크 type
    private String linkCompany; // 링크처
    private String link; // 풀링크
}
