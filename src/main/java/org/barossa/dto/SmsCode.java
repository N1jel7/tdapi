package org.barossa.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SmsCode(
        @JsonAlias("request_id")
        long requestId,
        @JsonAlias("country_id")
        long countryId,
        String number,
        @JsonAlias("sms_code")
        String smsCode,
        @JsonAlias("error_code")
        String errorCode,
        @JsonAlias("error_msg")
        String errorMsg
) {
}
