package com.minipay.minipay_service.service.impl;

import com.minipay.minipay_service.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("twilioSmsService")
public class TwilioSmsService implements SmsService {

    @Value("${sms.twilio.account-sid}")
    private String accountSid;

    @Value("${sms.twilio.auth-token}")
    private String authToken;

    @Value("${sms.twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String message) {
        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("Twilio SMS sent, SID: {}, status: {}", twilioMessage.getSid(), twilioMessage.getStatus());
            return new SmsResult(true, null);

        } catch (Exception e) {
            log.error("Twilio SMS error for {}: {}", phoneNumber, e.getMessage());
            return new SmsResult(false, "Twilio error: " + e.getMessage());
        }
    }
}