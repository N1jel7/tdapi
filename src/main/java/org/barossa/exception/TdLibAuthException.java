package org.barossa.exception;

public class TdLibAuthException extends RuntimeException{
    private final ErrorAuthState authState;

    public TdLibAuthException(ErrorAuthState authState) {
        this.authState = authState;
    }

    public TdLibAuthException(String message, ErrorAuthState authState) {
        super(message);
        this.authState = authState;
    }

    public TdLibAuthException(String message, Throwable cause, ErrorAuthState authState) {
        super(message, cause);
        this.authState = authState;
    }

    public TdLibAuthException(Throwable cause, ErrorAuthState authState) {
        super(cause);
        this.authState = authState;
    }

    public TdLibAuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ErrorAuthState authState) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.authState = authState;
    }
}
