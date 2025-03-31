package com.onnury.admin.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class AdminAccountRegisterRequestDto extends AbstractVO {
    private String loginId;
    private String password;
}
