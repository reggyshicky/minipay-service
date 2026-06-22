package com.minipay.minipay_service.util;

public final class PhoneNumberUtil {

    private PhoneNumberUtil() {}

    public static String toE164(String phoneNumber) {
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        if (phoneNumber.startsWith("0")) {
            return "+254" + phoneNumber.substring(1);
        }
        return "+254" + phoneNumber;
    }
}