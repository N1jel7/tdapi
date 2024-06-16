package org.barossa.service;

import org.barossa.dto.Application;
import org.barossa.dto.Country;
import org.barossa.dto.PhoneNumber;
import org.barossa.dto.SmsCode;

import java.math.BigDecimal;
import java.util.List;

public interface SmsService {
    BigDecimal getBalance();
    List<Country> getCountries();
    List<Application> getApplications();
    PhoneNumber getNumber(long countryId, long applicationId);
    Country getCountryById(long countryId);
    Application getApplicationById(long applicationId);
    SmsCode getSms(long requestId);
    void releaseNumber(PhoneNumber number);
}
