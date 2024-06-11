package org.barossa.handler;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.util.StringUtils.hasText;

public class TelegramClientAuthorizationState {
    private final AtomicBoolean haveAuthorization = new AtomicBoolean();

    private final AtomicBoolean waitAuthenticationCode = new AtomicBoolean();

    private final AtomicBoolean waitAuthenticationPassword = new AtomicBoolean();

    private final AtomicBoolean waitEmailAddress = new AtomicBoolean();

    private final AtomicBoolean stateClosed = new AtomicBoolean();

    /*
     * code/password/emailAddress will be cleaned up after check
     */
    private volatile String code;

    private volatile String password;

    private volatile String emailAddress;

    public synchronized void checkAuthenticationCode(String code) {
        if (waitAuthenticationCode.get()) {
            if (hasText(code)) {
                this.code = code;
                waitAuthenticationCode.set(false);
            }
        }
    }

    public synchronized void checkAuthenticationPassword(String password) {
        if (waitAuthenticationPassword.get()) {
            if (hasText(password)) {
                this.password = password;
                waitAuthenticationPassword.set(false);
            }
        }
    }

    public synchronized void checkEmailAddress(String email) {
        if (waitEmailAddress.get()) {
            if (hasText(email)) {
                this.emailAddress = email;
                waitEmailAddress.set(false);
            }
        }
    }

    public boolean isWaitAuthenticationCode() {
        return waitAuthenticationCode.get();
    }

    public boolean isWaitAuthenticationPassword() {
        return waitAuthenticationPassword.get();
    }

    public boolean isWaitEmailAddress() {
        return waitEmailAddress.get();
    }

    public boolean haveAuthorization() {
        return haveAuthorization.get();
    }

    public boolean isStateClosed() {
        return stateClosed.get();
    }

    public void setStateClosed() {
        stateClosed.set(true);
    }

    String getCode() {
        return code;
    }

    String getPassword() {
        return password;
    }

    String getEmailAddress() {
        return emailAddress;
    }

    void setHaveAuthorization(boolean haveAuthorization) {
        this.haveAuthorization.set(haveAuthorization);
    }

    void setWaitAuthenticationCode() {
        waitAuthenticationCode.set(true);
    }

    void setWaitAuthenticationPassword() {
        waitAuthenticationPassword.set(true);
    }

    void setWaitEmailAddress() {
        waitEmailAddress.set(true);
    }

    void clearCode() {
        code = null;
    }

    void clearPassword() {
        password = null;
    }

    void clearEmailAddress() {
        emailAddress = null;
    }
}
