package com.hardi.suggestions.dto;

public record Pagination(int page,
                         int size,
                         long totalElements,
                         int totalPages) {
}
