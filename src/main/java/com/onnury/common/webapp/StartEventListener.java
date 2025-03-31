package com.onnury.common.webapp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StartEventListener implements ApplicationListener<ApplicationReadyEvent> {
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		String sysProp = System.getProperty("batch.issueCoupon");
		if(!"ON".equalsIgnoreCase(sysProp) ) {
			log.info("ApplicationReadyEvent");

			return;
		}

		log.info("ApplicationReadyEvent: batch.issueCoupon=ON");
	}
	
}
