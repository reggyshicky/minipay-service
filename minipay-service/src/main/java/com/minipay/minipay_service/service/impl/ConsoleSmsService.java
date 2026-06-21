package com.minipay.minipay_service.service.impl;

import com.minipay.minipay_service.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("consoleSmsService")
public class ConsoleSmsService implements SmsService {
    @Override
    public SmsResult sendSms(String phoneNumber, String message) {
        log.info("==== [MOCK SMS] ====");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("====================");
        return new SmsResult(true, null);
    }

}
