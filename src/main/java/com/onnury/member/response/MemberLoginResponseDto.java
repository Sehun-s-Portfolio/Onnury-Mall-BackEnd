package com.onnury.member.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberLoginResponseDto implements AutoCloseable{
    private String loginId;
    private String type;
    private String userName;

    @Override
    public void close() throws Exception {
        throw new Exception();
    }
}
