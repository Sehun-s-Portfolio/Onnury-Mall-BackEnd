package com.onnury.member.request;

import com.onnury.common.base.AbstractVO;
import lombok.Getter;

@Getter
public class MemberLoginRequestDto extends AbstractVO {
    private String loginId;
    private String password;
}
