package com.hardi.suggestions.dto;

import com.hardi.suggestions.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Objects;

public record SuggestionRequestParameters(
        String prefix,
        Integer limit
) {

    public SuggestionRequestParameters {
        if(!StringUtils.hasText(prefix)) {
            throw new BadRequestException("Prefix cannot be empty");
        }
        limit = Objects.requireNonNullElse(limit, 10);
    }
}
