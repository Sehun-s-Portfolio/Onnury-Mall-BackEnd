package com.onnury.excel.response;

import com.onnury.media.response.MediaResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Builder
@Getter
public class InquiryExcelResponseDto {
    private Long inquiryId;
    private String type; // 문의타입
    private String inquiryTitle; // 문의 제목
    private String inquiryContent; // 문의 내용
    private String answer; // 문의 답변
    private String answerAt; // 문의 답변 시간
    private String createdAt; //
    private String customerLoginId;
    private String customerName;
    private List<MediaResponseDto> relateImages; // 연관된 이미지 파일들
}
