package com.onnury.inquiry.service;

import com.onnury.common.util.LogUtil;
import com.onnury.exception.inquiry.InquiryException;
import com.onnury.exception.token.JwtTokenException;
import com.onnury.inquiry.domain.Inquiry;
import com.onnury.inquiry.request.InquiryAnswerRequestDto;
import com.onnury.inquiry.request.InquiryRequestDto;
import com.onnury.inquiry.response.InquiryDataResponseDto;
import com.onnury.inquiry.response.InquiryListUpResponseDto;
import com.onnury.inquiry.response.InquiryUpdateResponseDto;
import com.onnury.jwt.JwtTokenProvider;
import com.onnury.member.domain.Member;
import com.onnury.query.inquiry.InquiryQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class InquiryService {

    private final JwtTokenException jwtTokenException;
    private final InquiryException inquiryException;
    private final InquiryQueryData inquiryQueryData;
    private final JwtTokenProvider jwtTokenProvider;

    // 문의 답변 수정
    @Transactional
    public InquiryUpdateResponseDto updateInquiry(HttpServletRequest request, InquiryAnswerRequestDto inquiryAnswerRequestDto) throws IOException {
        log.info("배너 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 수정하고자 하는 정보가 옳바른지 확인
        if (inquiryException.checkUpdateInquiryInfo(inquiryAnswerRequestDto.getAnswer())) {
            log.info("문의 답변 요청 정보가 옳바르지 않음");
            LogUtil.logError("문의 답변 요청 정보가 옳바르지 않음", request, inquiryAnswerRequestDto);
            return null;
        }

        // 수정한 배너 정보 추출
        Inquiry newInquiry = inquiryQueryData.updateInquiry(inquiryAnswerRequestDto);

        return InquiryUpdateResponseDto.builder()
                .inquiryId(newInquiry.getInquiryId())
                .inquiryTitle(newInquiry.getInquiryTitle())
                .inquiryContent(newInquiry.getInquiryContent())
                .type(newInquiry.getType())
                .answer(newInquiry.getAnswer())
                .answerAt(newInquiry.getAnswerAt())
                .build();
    }



    // 관리자 문의 페이지 리스트업
    public InquiryListUpResponseDto listUpInquiry(HttpServletRequest request, int page, String searchType, String searchType2, String searchKeyword) {
        log.info("관리자 문의 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        return inquiryQueryData.listUpInquiry(page, searchType, searchType2, searchKeyword);
    }


    // 고객 문의 작성 service
    public InquiryDataResponseDto writeInquiry(HttpServletRequest request, InquiryRequestDto inquiryRequestDto, List<MultipartFile> inquiryFile) throws IOException {
        log.info("고객 문의 작성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenException.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            LogUtil.logError("토큰 정합성 검증 실패", request);
            return null;
        }

        // 문의 회원
        Member inquiryMember = jwtTokenProvider.getMemberFromAuthentication();

        return inquiryQueryData.writeInquiry(inquiryMember, inquiryRequestDto, inquiryFile);
    }
}
