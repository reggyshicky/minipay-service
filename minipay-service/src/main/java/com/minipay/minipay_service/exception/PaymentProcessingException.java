package com.minipay.minipay_service.exception;


public class PaymentProcessingException extends RuntimeException{
    public PaymentProcessingException (String message){
        super(message);
    }
}
