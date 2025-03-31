package com.onnury.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.onnury.common.base.AbstractVO;
import com.onnury.common.constant.AppErrorCode;
import com.onnury.common.exception.AppException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class ResultVO extends AbstractVO {
//	private String trscId = null;
//	private String srvNm = null;

    private String rsltYn = "Y";
    private String rsltErrCode = AppErrorCode.SUCCESS.getErrCode();
    private String rsltMsge = null;

    public ResultVO() {
//		this.setSrvNm(Core.getServerName());

//		TrscVO trscVO = Core.getTrsc();
//		this.setTrscId(trscVO.getTrscId());
    }

    @JsonIgnore
    public void setException(final Exception e) {
        this.rsltYn = "N";

        if( e instanceof AppException) {
            AppException ex = (AppException)e;
            this.setRsltErrCode(ex.getEnumError().getErrCode());
        }

        this.setRsltMsge("[" + e.getClass().getSimpleName() + "]\n" + e.getMessage());
    }

    @JsonIgnore
    public void setErrorMsg(final String msg) {
        this.rsltYn = "N";
        this.setRsltMsge(msg);
    }

    @JsonIgnore
    public void setErrorCode(final AppErrorCode errCode) {
        if(errCode == AppErrorCode.SUCCESS) {
            this.rsltYn = "Y";
        } else {
            this.rsltYn = "N";
        }
        this.setRsltErrCode(errCode.getErrCode());
        this.setRsltMsge(errCode.getErrMsg());
    }
}
