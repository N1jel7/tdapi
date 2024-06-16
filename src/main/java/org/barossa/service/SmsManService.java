package org.barossa.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.barossa.dto.*;
import org.barossa.exception.ApplicationNotFoundException;
import org.barossa.exception.CountryNotFoundException;
import org.barossa.properties.SmsServiceProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsManService implements SmsService {
    private static final String RELEASE_NUMBER_TARGET_STATUS = "reject";

    private final SmsServiceProperties properties;
    private final List<Country> countries = new ArrayList<>();
    private final List<Application> applications = new ArrayList<>();
    private RestTemplate restTemplate;

    @PostConstruct
    private void initialize() {
        restTemplate = new RestTemplateBuilder()
                .rootUri(properties.baseUrl())
                .build();
        getApplications();
        getCountries();
    }

    @Override
    public BigDecimal getBalance() {
        ResponseEntity<Balance> response = restTemplate.getForEntity(properties.getBalance(), Balance.class, properties.token());
        return new BigDecimal(Objects.requireNonNull(response.getBody()).value());
    }

    @Override
    public List<Country> getCountries() {
        if(countries.isEmpty()){
            countries.addAll(getList(properties.getCountries(), new ParameterizedTypeReference<Map<String, Country>>() {}));
        }
        return countries;
    }

    @Override
    public List<Application> getApplications() {
        if(applications.isEmpty()){
            applications.addAll(getList(properties.getApplications(), new ParameterizedTypeReference<Map<String, Application>>() {}));
        }
        return applications;
    }

    @Override
    public PhoneNumber getNumber(long countryId, long applicationId) {
        PhoneNumber response;
        try{
            response =  restTemplate.getForEntity(properties.getNumber(), PhoneNumber.class, properties.token(), countryId, applicationId).getBody();

        }catch (RestClientException e){
            log.info("Can't receive number for country={}, app={}: {}", countryId, applicationId, e.getMessage(), e);
            response = PhoneNumber.builder()
                    .countryId(countryId)
                    .applicationId(applicationId)
                    .build();
        }
        return response;
    }

    @Override
    public Country getCountryById(long countryId) {
        return getCountries().stream()
                .filter(country -> country.id() == countryId)
                .findFirst()
                .orElseThrow(() -> new CountryNotFoundException("Country with id " + countryId + " not found"));
    }

    @Override
    public Application getApplicationById(long applicationId) {
        return getApplications().stream()
                .filter(application -> application.id() == applicationId)
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFoundException("Application with id " + applicationId + " not found"));
    }

    @Override
    public SmsCode getSms(long requestId) {
        ResponseEntity<SmsCode> response = restTemplate.getForEntity(properties.getSms(), SmsCode.class, properties.token(), requestId);
        return response.getBody();
    }

    @Override
    public void releaseNumber(PhoneNumber number) {
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                properties.setStatus(),
                GET,
                null,
                new ParameterizedTypeReference<>() {},
                properties.token(), number.requestId(),
                RELEASE_NUMBER_TARGET_STATUS
        );
        log.info("Number {} release status: {}", number.number(), Objects.requireNonNull(response.getBody()).get("success"));
    }

    private <T> List<T> getList(String url, ParameterizedTypeReference<Map<String, T>> typeReference) {
        //TODO Retry
        ResponseEntity<Map<String, T>> response = restTemplate.exchange(
                url,
                GET,
                null,
                typeReference,
                properties.token()
        );
        return new ArrayList<>(Objects.requireNonNull(response.getBody()).values());
    }
}
