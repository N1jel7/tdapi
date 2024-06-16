package org.barossa.service;

import org.barossa.handler.AuthorizationState;

public interface TelegramService {
    AuthorizationState login(String phone);

    AuthorizationState enterAuthCode(String phone, String code);
    void close(String phone);
}
