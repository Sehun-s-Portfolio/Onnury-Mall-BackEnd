package com.onnury.link.service;

import com.onnury.exception.token.JwtTokenExceptionInterface;
import com.onnury.link.domain.Link;
import com.onnury.link.repository.LinkRepository;
import com.onnury.link.request.LinkCreateRequestDto;
import com.onnury.link.request.LinkUpdateRequestDto;
import com.onnury.link.response.LinkCreateResponseDto;
import com.onnury.link.response.LinkListResponseDto;
import com.onnury.link.response.LinkResponseDto;
import com.onnury.query.link.LinkQueryData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkService {

    private final JwtTokenExceptionInterface jwtTokenExceptionInterface;
    private final LinkRepository linkRepository;
    private final LinkQueryData linkQueryData;

    // 링크 생성 service
    public LinkCreateResponseDto createLink(HttpServletRequest request, LinkCreateRequestDto linkInfo) {
        log.info("링크 생성 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 링크 정보 저장
        Link link = Link.builder()
                .type(linkInfo.getType())
                .linkCompany(linkInfo.getLinkCompany())
                .link(linkInfo.getLink())
                .build();

        linkRepository.save(link);

        return LinkCreateResponseDto.builder()
                .type(link.getType())
                .linkCompany(link.getLinkCompany())
                .link(link.getLink())
                .build();
    }


    // 링크 수정
    public LinkResponseDto updateLink(HttpServletRequest request, LinkUpdateRequestDto linkInfo) throws IOException {
        log.info("링크 수정 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        // 수정한 링크 정보 추출
        Link link = linkQueryData.updateLink(linkInfo);

        return LinkResponseDto.builder()
                .type(link.getType())
                .linkCompany(link.getLinkCompany())
                .link(link.getLink())
                .build();
    }


    // 링크 삭제
    @Transactional
    public boolean deleteLink(HttpServletRequest request, Long linkId) {
        log.info("링크 삭제 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return true;
        }

        return linkQueryData.deleteLink(linkId);
    }


    // 관리자 링크 리스트업 service
    public LinkListResponseDto listUpLink(HttpServletRequest request, int page) {
        log.info("관리자 공급사 리스트업 페이지 service");

        // 정합성이 검증된 토큰인지 확인
        if (jwtTokenExceptionInterface.checkAccessToken(request)) {
            log.info("토큰 정합성 검증 실패");
            return null;
        }

        return linkQueryData.listUpLink(page);
    }
}

