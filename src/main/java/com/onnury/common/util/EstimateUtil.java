package com.onnury.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EstimateUtil {

	private Long stPnt = null;
	private Map<String, Long> chkPntMap = null;
	
	public EstimateUtil() {
		super();
		
		this.setStPnt(System.nanoTime());
		this.setChkPntMap(new HashMap<String, Long>());
	}
	
	private Long getStPnt() {
		return this.stPnt;
	}
	
	private void setStPnt(final Long stPnt) {
		this.stPnt = stPnt;
	}
	
	private Map<String, Long> getChkPntMap() {
		return this.chkPntMap;
	}
	
	private void setChkPntMap(final Map<String, Long> chkPntMap) {
			this.chkPntMap = chkPntMap;
	}
	
	// 측정 포인트 설정
	public void setCheckPoint(final String chkPnt) {
		Map<String, Long> chkPntMap = this.getChkPntMap();	
		chkPntMap.put(chkPnt,  System.nanoTime());
	}
	
	// 두 측정 포인트간의 측정시간 반환
	public long estimate(final String stPnt, final String edPnt) {
		Map<String, Long> chkPntMap = this.getChkPntMap();
		long stPntTm = 0L;
		long edPntTm = 0L;
		
		if(chkPntMap.containsKey(stPnt)) {
			stPntTm = chkPntMap.get(stPnt);
		} else {
			stPntTm = this.getStPnt();
		}
		
		if(chkPntMap.containsKey(edPnt)) {
			edPntTm = chkPntMap.get(edPnt);
		} else {
			edPntTm = this.getStPnt();
		}
		
		long estimateTm = TimeUnit.NANOSECONDS.toMillis(edPntTm - stPntTm);
		return estimateTm;
	}
}
