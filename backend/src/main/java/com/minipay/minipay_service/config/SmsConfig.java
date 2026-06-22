package com.minipay.minipay_service.config;

import com.minipay.minipay_service.service.SmsService;
import com.minipay.minipay_service.service.impl.AfricasTalkingSmsService;
import com.minipay.minipay_service.service.impl.ConsoleSmsService;
import com.minipay.minipay_service.service.impl.TwilioSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class SmsConfig {

    private final ConsoleSmsService consoleSmsService;
    private final TwilioSmsService twilioSmsService;
    private final AfricasTalkingSmsService africasTalkingSmsService;

    @Value("${sms.provider}")
    private String provider;

    @Bean
    @Primary
    public SmsService smsService() {
        return switch (provider.toLowerCase()) {
            case "twilio" -> twilioSmsService;
            case "africastalking" -> africasTalkingSmsService;
            default -> consoleSmsService;
        };
    }
}