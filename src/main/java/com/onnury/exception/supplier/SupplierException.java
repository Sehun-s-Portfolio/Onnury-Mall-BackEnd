package com.onnury.exception.supplier;

import com.onnury.admin.domain.AdminAccount;
import com.onnury.query.supplier.SupplierQueryData;
import com.onnury.supplier.domain.Supplier;
import com.onnury.supplier.request.SupplierCreateRequestDto;
import com.onnury.supplier.request.SupplierUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SupplierException implements SupplierExceptioInterface {

    private final SupplierQueryData supplierQueryData;

    // 생성하고자 하는 공급사의 정보가 옳바른지 확인
    @Override
    public boolean checkCreateSupplierInfo(SupplierCreateRequestDto supplierCreateRequestDto) {

        if (supplierCreateRequestDto.getSupplierCompany().isEmpty()
                || supplierCreateRequestDto.getBusinessNumber().isEmpty() || supplierCreateRequestDto.getRepresent().isEmpty()
                || supplierCreateRequestDto.getAddress().isEmpty() || supplierCreateRequestDto.getTel().isEmpty() || supplierCreateRequestDto.getPersonInCharge().isEmpty()
                || supplierCreateRequestDto.getContactCall().isEmpty() || supplierCreateRequestDto.getEmail().isEmpty()) {
            return true;
        }

        return false;
    }

    // 수정하고자 하는 공급사의 정보가 옳바른지 확인
    @Override
    public boolean checkUpdateSupplierInfo(SupplierUpdateRequestDto supplierCreateRequestDto) {

        if (supplierCreateRequestDto.getSupplierCompany().isEmpty()
                || supplierCreateRequestDto.getBusinessNumber().isEmpty() || supplierCreateRequestDto.getRepresent().isEmpty()
                || supplierCreateRequestDto.getAddress().isEmpty() || supplierCreateRequestDto.getTel().isEmpty()) {
            return true;
        }

        return false;
    }

    // 공급사 로그인 아이디 기존에 이미 존재하는지 확인
    @Override
    public boolean checkExistSupplierLoginId(String loginId) {
        Supplier loginSupplier = supplierQueryData.getSupplier(loginId);

        if(loginSupplier != null){
            return true;
        }

        return false;
    }
}
