package com.onnury.link.request;

import lombok.Getter;

@Getter
public class LinkUpdateRequestDto {
    private Long linkId;
    private String type; // 링크 type
    private String linkCompany; // 링크처
    private String link; // 풀링크
}
