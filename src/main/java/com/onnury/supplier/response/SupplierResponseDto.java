package com.onnury.supplier.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SupplierResponseDto {
    private Long supplierId; // 공급사 id
    private String supplierCompany; // 공급사 명
}
