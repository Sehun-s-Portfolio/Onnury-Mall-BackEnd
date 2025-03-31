package com.onnury.common.base;

import com.onnury.exception.admin.AdminExecptionInterface;
import com.onnury.exception.banner.BannerExceptioInterface;
import com.onnury.exception.brand.BrandExceptioInterface;
import com.onnury.exception.category.CategoryExceptioInterface;
import com.onnury.exception.inquiry.FaqExceptioInterface;
import com.onnury.exception.inquiry.InquiryExceptioInterface;
import com.onnury.exception.label.LabelExceptioInterface;
import com.onnury.exception.member.MemberExceptionInterface;
import com.onnury.exception.mypage.MyPageExceptionInterface;
import com.onnury.exception.notice.NoticeExceptionInterface;
import com.onnury.exception.product.ProductExceptionInterface;
import com.onnury.exception.supplier.SupplierExceptioInterface;
import com.onnury.exception.token.JwtTokenExceptionInterface;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractExceptionHandler
        implements JwtTokenExceptionInterface, MemberExceptionInterface, AdminExecptionInterface,
        BannerExceptioInterface, BrandExceptioInterface, CategoryExceptioInterface, FaqExceptioInterface,
        InquiryExceptioInterface, LabelExceptioInterface, MyPageExceptionInterface, NoticeExceptionInterface,
        ProductExceptionInterface, SupplierExceptioInterface {

}
