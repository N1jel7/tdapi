package org.barossa.exception;

public class SmsServiceException extends RuntimeException{
    public SmsServiceException() {
        super();
    }

    public SmsServiceException(String message) {
        super(message);
    }

    public SmsServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmsServiceException(Throwable cause) {
        super(cause);
    }

    protected SmsServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
