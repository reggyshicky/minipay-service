package com.minipay.minipay_service.service.impl;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.minipay.minipay_service.service.SmsService.SmsResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("africasTalkingSmsService")
public class AfricasTalkingSmsService implements com.minipay.minipay_service.service.SmsService {
    @Value("${sms.africastalking.username}")
    private String username;

    @Value("${sms.africastalking.api-key}")
    private String apiKey;

    private SmsService smsService;

    @PostConstruct
    public void init() {
        AfricasTalking.initialize(username, apiKey);
        smsService = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String message) {
        try {
            List<Recipient> response = smsService.send(message, new String[]{phoneNumber}, false);

            Recipient recipient = response.get(0);
            log.info("Africa's Talking SMS sent, status: {}, messageId: {}", recipient.status, recipient.messageId);

            return new SmsResult(true, null);

        } catch (Exception e) {
            log.error("Africa's Talking SMS error for {}: {}", phoneNumber, e.getMessage());
            return new SmsResult(false, "Africa's Talking error: " + e.getMessage());
        }
    }
}