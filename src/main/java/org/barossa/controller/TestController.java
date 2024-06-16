package org.barossa.controller;

import lombok.RequiredArgsConstructor;
import org.barossa.dto.Application;
import org.barossa.handler.AuthorizationState;
import org.barossa.service.RegistrationService;
import org.barossa.service.SmsService;
import org.barossa.service.TelegramService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final TelegramService telegramService;
    private final SmsService smsService;
    private final RegistrationService registrationService;

    @GetMapping
    BigDecimal getBalance(){
        return smsService.getBalance();
    }

    @GetMapping("/login")
    void test(String phone) {
        telegramService.login(phone);
    }

    @GetMapping("/auth")
    void testAuth(String phone, String code) {
        telegramService.enterAuthCode(phone, code);
    }

    @GetMapping("/applications")
    List<Application> getApplications(){
        return smsService.getApplications();
    }

    @GetMapping("/register")
    void register(long serviceId, long countryId){
        registrationService.registerAccount(serviceId, countryId);
    }
}
