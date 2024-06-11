package org.barossa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.barossa.handler.TelegramClientAuthorizationHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {
    private final TelegramClientAuthorizationHandler telegramClientAuthorizationHandler;

    @Override
    public void login(String phone) {
        telegramClientAuthorizationHandler.login(phone);
    }
}
