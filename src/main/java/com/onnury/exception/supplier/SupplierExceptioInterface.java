package com.onnury.exception.supplier;

import com.onnury.supplier.request.SupplierCreateRequestDto;
import com.onnury.supplier.request.SupplierUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public interface SupplierExceptioInterface {

    // 생성하고자 하는 공급사의 정보가 옳바른지 확인
    boolean checkCreateSupplierInfo(SupplierCreateRequestDto supplierCreateRequestDto);

    // 수정하고자 하는 배너의 정보가 옳바른지 확인
    boolean checkUpdateSupplierInfo(SupplierUpdateRequestDto supplierCreateRequestDto);

    // 공급사 로그인 아이디 기존에 이미 존재하는지 확인
    boolean checkExistSupplierLoginId(String supplierLoginId);

}
