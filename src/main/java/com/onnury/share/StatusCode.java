package com.onnury.share;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusCode {

    // success response
    OK("정상 수행", "O-200"),
    CANT_APPROVAL_PAYMENT_INFO_GET("결제 정보가 존재하지 않습니다.", "O-201"),
    CANT_APPROVAL_EASY_PAYMENT_INFO_GET("결제 정보가 존재하지 않습니다.", "O-201"),

    // bad response
    NOT_EXIST_ADMIN_ACCOUNT("관리자 계정이 존재하지 않습니다.", "O-401"),
    CANT_REGISTER_ADMIN_ACCOUNT("관리자 계정을 생성하실 수 없습니다.", "O-402"),
    CANT_ADMIN_LOGIN("로그인하실 수 없습니다.", "O-403"),
    CANT_CREATE_BANNER("배너를 생성할 수 없습니다.", "O-404"),
    CANT_UPDATE_BANNER("배너를 수정할 수 없습니다.", "O-405"),
    CANT_DELETE_BANNER("배너를 삭제할 수 없습니다.", "O-406"),
    CANT_GET_BANNER_LISTUP("배너 리스트 업에 실패하였습니다.", "O-407"),
    CANT_CREATE_PRODUCT("제품을 생성하지 못하였습니다.", "O-408"),
    CANT_UPDATE_PRODUCT("제품을 수정하지 못하였습니다.", "O-408"),
    NOT_EXIST_PRODUCTS("제품이 존재하지 않습니다.", "O-409"),
    NOT_EXIST_READY_INFO("제품 등록을 위한 사전 데이터들이 존재하지 않습니다.", "O-410"),
    CANT_DELETE_PRODUCT("제품을 삭제할 수 없습니다.", "O-411"),
    CANT_REGIST_MEMBER("회원가입 하실 수 없습니다.", "O-412"),
    CANT_LOGIN_MEMBER("로그인 하실 수 없습니다.", "O-413"),
    CANT_FIND_LOGINID("찾고자 하는 계정이 존재하지 않습니다.", "O-414"),
    CANT_FIND_PASSWORD("비밀번호를 찾을 수 없습니다.", "O-415"),
    NOT_EXIST_NAVIGATION_CATEGORIES("네비게이션 카테고리가 존재하지 않습니다.", "O-416"),
    NOT_EXIST_BANNER("배너가 존재하지 않습니다.", "O-417"),
    NOT_EXIST_QUICK_UP_CATEGORIES("메인 페이지에 노출될 대분류 카테고리가 존재하지 않습니다.", "O-418"),
    NOT_EXIST_MIDDLE_CATEGORIES_RELATED_UP_CATEGORY("선택한 대분류 카테고리에 해당되는 중분류 카테고리가 존재하지 않습니다.", "O-419"),
    NOT_EXIST_LABEL("해당 라벨은 현재 존재하지 않습니다.", "O-420"),
    NOT_SAVE_DETAIL_INFO_IMAGES("제품 상세 정보 이미지를 생성하지 못하였습니다.", "O-421"),
    CANT_ADD_CART("장바구니에 담지 못하였습니다.", "O-422"),
    CANT_DELETE_CART("장바구니 덜어내기에 실패하셨습니다.", "O-423"),
    CANT_GET_CART_LIST("장바구니 정보를 조회할 수 없습니다.", "O-424"),
    CANT_GET_MY_INFO("마이페이지 정보를 조회할 수 없습니다.", "O-425"),
    CANT_CHANGE_MY_PASSWORD("비밀번호를 변경할 수 없습니다.", "O-426"),
    CANT_WITHDRAWAL_ACCOUNT("회원 탈퇴를 진행하실 수 없습니다.", "O-427"),
    CANT_UPDATE_ACCOUNT_INFO("회원 정보를 수정할 수 없습니다.", "O-428"),
    CANT_WRITE_INQUIRY("문의하실 수 없습니다.", "O-429"),
    CANT_GET_INQUIRY("작성하신 문의 내역을 확인할 수 없습니다. 다시 로그인해주십시오.", "O-429"),
    NOT_EXIST_FAQ("FAQ가 존재하지 않습니다.", "O-430"),
    NOT_ADMIN_ACCOUNT_FOR_NOTICE("공지사항 작성 가능한 계정 정보가 아닙니다.", "O-431"),
    CANT_GET_NOTICES("공지사항 데이터를 불러올 수 없습니다.", "O-432"),
    CANT_GET_APPROVAL_INFO("결제 정보를 저장 및 조회할 수 없습니다.", "O-433"),
    CANT_ENCODE_DATA("데이터를 암호화할 수 없습니다.", "O-434"),
    CANT_GET_CATEGORY_BEST_PRODUCTS("카테고리 베스트 제품들을 조회할 수 없습니다.", "O-435"),
    CANT_GET_WEEKLY_BEST_PRODUCTS("WEEKLY 베스트 제품들을 조회할 수 없습니다.", "O-436"),
    CANT_GET_MY_PAYMENTS("구매 이력을 조회할 수 없습니다.", "O-437"),
    CANT_GET_DASHBOARD_DATA("대시 보드 정보를 활성화할 수 없습니다.", "O-438"),
    CANT_CREATE_LINK("링크를 생성할 수 없습니다.", "O-439"),
    CANT_UPDATE_LINK("링크를 수정할 수 없습니다.", "O-440"),
    CANT_DELETE_LINK("링크를 삭제할 수 없습니다.", "O-441"),
    CANT_GET_LINK_LIST("링크 리스트를 조회할 수 없습니다.", "O-441"),
    CANT_REGIST_SUPPLIER("공급사 계정을 생성할 수 없습니다.", "O-442"),
    CANT_CONFIRM_PAYMENT("주문 확정할 수 없습니다.", "O-443"),
    EXIST_CANCEL_INFO_THAN_CANT_CONFIRM_PAYMENT("취소 상태인 제품이기 때문에 주문 확정할 수 없습니다.", "O-444"),
    EXPIRED_ACCOUNT("계정 정보가 만료되었습니다. 다시 로그인 해주십시오.", "O-445"),
    CANT_GET_MEMBER_DASHBOARD_DATA("회원 대시보드 정보를 조회할 수 없습니다.", "O-446");


    private final String message;
    private final String code;
}
