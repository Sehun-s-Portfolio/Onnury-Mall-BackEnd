package com.onnury.query.member;

import com.onnury.member.domain.Member;
import com.onnury.member.request.MemberFindPasswordRequestDto;
import com.onnury.member.response.AreaMemberCountInfoResponseDto;
import com.onnury.member.response.MemberDashboardResponseDto;
import com.onnury.member.response.MemberDataResponseDto;
import com.onnury.member.response.MemberListUpResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.onnury.inquiry.domain.QInquiry.inquiry;
import static com.onnury.member.domain.QMember.member;


@Slf4j
@RequiredArgsConstructor
@Component
public class MemberQueryData {

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;


    // 관리자 회원 리스트업
    public MemberListUpResponseDto listUpMember(int page, String searchtype, String search, String startDate, String endDate) {

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.memberId.goe(1L)
                        .and(membersearch(searchtype, search))
                        .and(memberSearchInDateRange(startDate, endDate))
                )
                .orderBy(member.memberId.desc())
                .fetch();

        Long totalCount = (long) result.size();

        if (result.size() >= 10) {
            if ((page * 10) <= result.size()) {
                result = result.subList((page * 10) - 10, page * 10);
            } else {
                result = result.subList((page * 10) - 10, result.size());
            }
        } else {
            if (!result.isEmpty()) {
                result = result.subList((page * 10) - 10, result.size());
            }
        }

        List<MemberDataResponseDto> memberList = new ArrayList<>();

        if (!result.isEmpty()) {
            memberList = result.stream()
                    .map(eachMember -> {

                        String createdAt = jpaQueryFactory
                                .select(member.createdAt.stringValue())
                                .from(member)
                                .where(member.memberId.eq(eachMember.getMemberId()))
                                .fetchOne();

                        return MemberDataResponseDto.builder()
                                .memberId(eachMember.getMemberId())
                                .loginId(eachMember.getLoginId())
                                .userName(eachMember.getUserName())
                                .birth(eachMember.getBirth())
                                .postNumber(eachMember.getPostNumber())
                                .address(eachMember.getAddress())
                                .detailAddress(eachMember.getDetailAddress())
                                .email(eachMember.getEmail())
                                .phone(eachMember.getPhone())
                                .type(eachMember.getType())
                                .businessNumber(eachMember.getBusinessNumber())
                                .manager(eachMember.getManager())
                                .createdAt(createdAt)
                                .build();

                    })
                    .collect(Collectors.toList());
        }

        return MemberListUpResponseDto.builder()
                .memberDataResponseDto(memberList)
                .total(totalCount)
                .build();
    }


    private BooleanExpression memberSearchInDateRange(String startDate, String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 날짜와 시간 포맷 형식

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            String searchStartDate = startDate + " 00:00:00";
            String searchEndDate = endDate + " 23:59:59";
            LocalDateTime startDateTime = LocalDateTime.parse(searchStartDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용
            LocalDateTime endDateTime = LocalDateTime.parse(searchEndDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

            return member.createdAt.after(startDateTime)
                    .and(member.createdAt.before(endDateTime));
        } else {
            if (!startDate.isEmpty()) {
                String searchStartDate = startDate + " 00:00:00";
                LocalDateTime startDateTime = LocalDateTime.parse(searchStartDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

                return member.createdAt.after(startDateTime);
            } else if (!endDate.isEmpty()) {
                String searchEndDate = endDate + " 23:59:59";
                LocalDateTime endDateTime = LocalDateTime.parse(searchEndDate, formatter); // 추출한 날짜 데이터에 포맷 형식 적용

                return member.createdAt.before(endDateTime);
            }
        }

        return null;
    }

    // page 계산
    private int paging(int page) {
        if (page > 0) {
            return (page - 1) * 10;
        }
        return 0;
    }


    // 회원 검색 조건
    private BooleanExpression membersearch(String searchtype, String search) {

        if (!searchtype.isEmpty()) {
            //'회원ID', '이름', '제목'
            if (searchtype.equals("회원ID")) {
                if (!search.isEmpty()) {
                    return member.loginId.like("%" + search.replace(" ", "%") + "%");
                }
            } else if (searchtype.equals("이름")) {
                if (!search.isEmpty()) {
                    return member.userName.like("%" + search.replace(" ", "%") + "%")
                            .or(member.manager.like("%" + search.replace(" ", "%") + "%"));
                }
            } else if (searchtype.equals("사업자번호")) {
                if (!search.isEmpty()) {
                    return member.businessNumber.like("%" + search.replace(" ", "%") + "%");
                }
            } else if (searchtype.equals("생년월일")) {
                if (!search.isEmpty()) {
                    return member.birth.like("%" + search.replace(" ", "%") + "%");
                }
            } else if (searchtype.equals("전체")) {
                if (!search.isEmpty()) {
                    return member.loginId.like("%" + search.replace(" ", "%") + "%")
                            .or(member.userName.like("%" + search.replace(" ", "%") + "%")
                                    .or(member.manager.like("%" + search.replace(" ", "%") + "%")))
                            .or(member.businessNumber.like("%" + search.replace(" ", "%") + "%"))
                            .or(member.birth.like("%" + search.replace(" ", "%") + "%"));
                } else {
                    return null;
                }
            }
        }

        return null;
    }


    // 로그인한 고객 계정 조회
    public Member getLoginAccount(String loginId) {
        String[] loginAccount = loginId.split("-");

        return jpaQueryFactory
                .selectFrom(member)
                .where(member.loginId.eq(loginAccount[1])
                        .and(member.type.eq(loginAccount[0])))
                .fetchOne();
    }


    // 로그인할 고객 객체 정보 조회
    public Member getMember(String loginId) {

        return jpaQueryFactory
                .selectFrom(member)
                .where(member.loginId.eq(loginId))
                .fetchOne();
    }


    // 유저 로그인 시 토큰 발급될 때 조회될 쿼리 함수
    public Member getLoginMemberAccount(String loginId) {

        return jpaQueryFactory
                .selectFrom(member)
                .where(member.loginId.eq(loginId))
                .fetchOne();
    }


    // 아이디 찾기 용 이메일 기준 계정 조회
    public Member getLoginAccountAboutEmail(String email, String phone) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.email.eq(email)
                        .and(member.phone.eq(phone)))
                .fetchOne();
    }


    // 비밀번호 찾기 용 로그인 id, 이메일, 연락처 기준 계정 조회
    public Member getLoginAccountAboutLoginIdEmailPhone(String loginId, String email, String phone) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.loginId.eq(loginId)
                        .and(member.email.eq(email))
                        .and(member.phone.eq(phone)))
                .fetchOne();
    }


    // 비밀번호 재발급을 위한 임시 비밀번호 업데이트
    @Transactional
    public String updateImmediatePassword(Long memberId, String loginId, String email, String phone) {

        // 로그인 아이디 + 이메일 + 폰 번호를 합쳐 임시 비밀번호 부여
        String immediatePassword = loginId + email + phone;
        String encodeImmediatePassword = passwordEncoder.encode(immediatePassword).substring(20, 30);

        // 발급한 임시 비밀번호로 기존 비밀번호 업데이트
        jpaQueryFactory
                .update(member)
                .set(member.password, passwordEncoder.encode(encodeImmediatePassword))
                .where(member.memberId.eq(memberId))
                .execute();

        entityManager.flush();
        entityManager.clear();

        return encodeImmediatePassword;
    }


    // 회원 대시보드 쿼리 함수
    public MemberDashboardResponseDto getDashboard(String startDate, String endDate) {

        List<Member> memberList = jpaQueryFactory
                .selectFrom(member)
                .where(member.memberId.goe(1L)
                        .and(memberSearchInDateRange(startDate, endDate))
                )
                .fetch();

        Long businessMemberCount = memberList.stream()
                .filter(businessMember -> businessMember.getType().equals("B"))
                .count();

        Long customerMemberCount = memberList.stream()
                .filter(businessMember -> businessMember.getType().equals("C"))
                .count();

        List<AreaMemberCountInfoResponseDto> areaMemberCountList = getAreaMemberCountList(memberList);

        return MemberDashboardResponseDto.builder()
                .businessMemberCount(businessMemberCount)
                .customerMemberCount(customerMemberCount)
                .areaMemberCountList(areaMemberCountList)
                .build();
    }

    private List<AreaMemberCountInfoResponseDto> getAreaMemberCountList(List<Member> memberList) {

        List<String> areaList = Arrays.asList("서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충청", "전라", "경상", "제주");
        List<AreaMemberCountInfoResponseDto> areaMemberCountList = new ArrayList<>();

        areaList.forEach(eachArea -> {
            String areaName = eachArea;

            if (eachArea.equals("충청") || eachArea.equals("전라") || eachArea.equals("경상")) {

                String secondSouthAreaName = areaName + "남도";
                String secondNorthAreaName = areaName + "북도";

                Long southBusinessMemberCount = memberList.stream()
                        .filter(areaBusinessMember -> areaBusinessMember.getAddress().contains(eachArea) && areaBusinessMember.getAddress().contains("남도") && areaBusinessMember.getType().equals("B"))
                        .count();

                Long southCustomerMemberCount = memberList.stream()
                        .filter(areaCustomerMember -> areaCustomerMember.getAddress().contains(eachArea) && areaCustomerMember.getAddress().contains("남도") && areaCustomerMember.getType().equals("C"))
                        .count();

                Long southTotalMemberCount = memberList.stream()
                        .filter(area -> area.getAddress().contains(eachArea) && area.getAddress().contains("남도"))
                        .count();

                areaMemberCountList.add(
                        AreaMemberCountInfoResponseDto.builder()
                                .area(secondSouthAreaName)
                                .businessMemberCount(southBusinessMemberCount)
                                .customerMemberCount(southCustomerMemberCount)
                                .totalMemberCount(southTotalMemberCount)
                                .build()
                );


                Long northBusinessMemberCount = memberList.stream()
                        .filter(areaBusinessMember -> areaBusinessMember.getAddress().contains(eachArea) && areaBusinessMember.getAddress().contains("북도") && areaBusinessMember.getType().equals("B"))
                        .count();

                Long northCustomerMemberCount = memberList.stream()
                        .filter(areaCustomerMember -> areaCustomerMember.getAddress().contains(eachArea) && areaCustomerMember.getAddress().contains("북도") && areaCustomerMember.getType().equals("C"))
                        .count();

                Long northTotalMemberCount = memberList.stream()
                        .filter(area -> area.getAddress().contains(eachArea) && area.getAddress().contains("북도"))
                        .count();

                areaMemberCountList.add(
                        AreaMemberCountInfoResponseDto.builder()
                                .area(secondNorthAreaName)
                                .businessMemberCount(northBusinessMemberCount)
                                .customerMemberCount(northCustomerMemberCount)
                                .totalMemberCount(northTotalMemberCount)
                                .build()
                );

            } else {
                if (eachArea.equals("서울")) {
                    areaName += "특별시";
                } else if (eachArea.equals("부산") || eachArea.equals("대구") || eachArea.equals("인천") || eachArea.equals("광주") || eachArea.equals("대전") || eachArea.equals("울산")) {
                    areaName += "광역시";
                } else if (eachArea.equals("세종")) {
                    areaName += "특별자치시";
                } else if (eachArea.equals("경기") || eachArea.equals("강원")) {
                    areaName += "도";
                } else if (eachArea.equals("제주")) {
                    areaName += "특별자치도";
                }

                Long businessMemberCount = memberList.stream()
                        .filter(areaBusinessMember -> areaBusinessMember.getAddress().contains(eachArea) && areaBusinessMember.getType().equals("B"))
                        .count();

                Long customerMemberCount = memberList.stream()
                        .filter(areaCustomerMember -> areaCustomerMember.getAddress().contains(eachArea) && areaCustomerMember.getType().equals("C"))
                        .count();

                Long totalMemberCount = memberList.stream()
                        .filter(area -> area.getAddress().contains(eachArea))
                        .count();

                areaMemberCountList.add(
                        AreaMemberCountInfoResponseDto.builder()
                                .area(areaName)
                                .businessMemberCount(businessMemberCount)
                                .customerMemberCount(customerMemberCount)
                                .totalMemberCount(totalMemberCount)
                                .build()
                );
            }

        });

        return areaMemberCountList;
    }

}