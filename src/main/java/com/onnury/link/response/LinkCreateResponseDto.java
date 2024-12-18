package com.onnury.link.response;

import lombok.Builder;
import lombok.Getter;
@Builder
@Getter
public class LinkCreateResponseDto {

    private String type; // 링크 type
    private String linkCompany; // 링크처
    private String link; // 풀링크

}
