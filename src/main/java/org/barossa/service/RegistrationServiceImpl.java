package org.barossa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.barossa.dto.Application;
import org.barossa.dto.Country;
import org.barossa.dto.PhoneNumber;
import org.barossa.dto.SmsCode;
import org.barossa.handler.AuthorizationState;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.barossa.handler.AuthorizationState.PHONE_NUMBER_BANNED;
import static org.barossa.handler.AuthorizationState.WAIT_FOR_AUTH_CODE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private static final long REQUESTS_COUNTDOWN_MS = 500;
    private static final long SMS_TIMEOUT_SECONDS = 60;

    private final SmsService smsService;
    private final TelegramService telegramService;

    @Override
    public void registerAccount(long serviceId, long countryId) {
        Application application = smsService.getApplicationById(serviceId);
        Country country = smsService.getCountryById(countryId);
        log.info("Getting number for {} app and {} country", application.title(), country.title());
        PhoneNumber phoneNumber = getNumber(countryId, serviceId, REQUESTS_COUNTDOWN_MS);
        AuthorizationState state = telegramService.login(phoneNumber.number());
        if (PHONE_NUMBER_BANNED.equals(state)) {
            smsService.releaseNumber(phoneNumber);
            telegramService.close(phoneNumber.number());
            smsService.releaseNumber(phoneNumber);
        }else
        if (WAIT_FOR_AUTH_CODE.equals(state)) {
            log.info("Waiting for sms from number {}...", phoneNumber.number());
            SmsCode sms = getSms(phoneNumber.requestId(), SMS_TIMEOUT_SECONDS, REQUESTS_COUNTDOWN_MS * 2);
            if (Objects.isNull(sms.smsCode())) {
                log.info("Sms code from {} not received for {} seconds", phoneNumber.number(), SMS_TIMEOUT_SECONDS);
                smsService.releaseNumber(phoneNumber);
            }else {
                log.info("Received sms code ({}) from number {}", sms.smsCode(), phoneNumber.number());
                AuthorizationState authorizationState = telegramService.enterAuthCode(phoneNumber.number(), sms.smsCode());
                log.info("Final state: {}", authorizationState);
            }
        }
    }

    private PhoneNumber getNumber(long countryId, long serviceId, long msCountdown) {
        PhoneNumber phoneNumber = smsService.getNumber(countryId, serviceId);
        while (Objects.isNull(phoneNumber.number())) {
            try {
                Thread.sleep(msCountdown);

            } catch (InterruptedException e) {
                log.info("Can't get number: {}", e.getMessage(), e);
            }
            phoneNumber = smsService.getNumber(countryId, serviceId);
        }

        return phoneNumber;
    }

    private SmsCode getSms(long requestId, long secondsTimeout, long msRequestCountdown) {
        long start = System.currentTimeMillis();
        SmsCode sms = smsService.getSms(requestId);
        while (Objects.isNull(sms.smsCode()) && (System.currentTimeMillis() - start) < secondsTimeout * 1000) {
            try {
                Thread.sleep(msRequestCountdown);

            } catch (InterruptedException e) {
                log.info("Can't get sms: {}", e.getMessage(), e);
            }
            sms = smsService.getSms(requestId);
        }

        return sms;
    }
}
