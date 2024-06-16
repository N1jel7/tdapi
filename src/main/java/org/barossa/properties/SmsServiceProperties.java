package org.barossa.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.sms-services.sms-man")
public record SmsServiceProperties(
        String token,
        String baseUrl,
        String getBalance,
        String getLimits,
        String getNumber,
        String getSms,
        String getCountries,
        String getPrices,
        String getApplications,
        String setStatus
) {

}
