package com.minipay.minipay_service.service.impl;

import com.minipay.minipay_service.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("consoleSmsService")
@RequiredArgsConstructor
public class ConsoleSmsService implements SmsService {

    private final InMemorySmsQueue smsQueue;

    @Override
    public SmsResult sendSms(String phoneNumber, String message) {
        smsQueue.enqueue(new SmsJob(phoneNumber, message));
        return new SmsResult(true, null);
    }
}