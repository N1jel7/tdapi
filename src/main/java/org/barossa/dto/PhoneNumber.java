package org.barossa.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;

@Builder
public record PhoneNumber(
        @JsonAlias("request_id")
        long requestId,
        @JsonAlias("country_id")
        long countryId,
        @JsonAlias("application_id")
        long applicationId,
        String number
) {
}
