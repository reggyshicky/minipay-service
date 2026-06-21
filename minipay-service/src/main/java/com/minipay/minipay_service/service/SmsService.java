package com.minipay.minipay_service.service;


public interface SmsService {
    SmsResult sendSms(String phoneNumber, String message);
    record SmsResult(boolean success, String errorMessage) {}
}
