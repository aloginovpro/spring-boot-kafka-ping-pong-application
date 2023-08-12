package com.aloginovpro.kafka.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StringMessage(
        @JsonProperty("value") String value
) {
}