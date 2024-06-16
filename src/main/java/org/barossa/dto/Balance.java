package org.barossa.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record Balance(
        @JsonAlias("balance")
        String value
) {
}
