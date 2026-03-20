package com.hardi.suggestions.controller;

import com.hardi.suggestions.dto.SearchRequestParameters;
import com.hardi.suggestions.dto.SearchResponse;
import com.hardi.suggestions.dto.SuggestionRequestParameters;
import com.hardi.suggestions.service.SearchService;
import com.hardi.suggestions.service.SuggestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BusinessSearchController {

    private final SuggestionService suggestionService;
    private final SearchService searchService;

    public BusinessSearchController(SuggestionService suggestionService, SearchService searchService) {
        this.suggestionService = suggestionService;
        this.searchService = searchService;
    }

    @GetMapping("/api/suggestions")
    public List<String> suggest(SuggestionRequestParameters parameters) {
        return suggestionService.fetchSuggestions(parameters);
    }

    @GetMapping("/api/search")
    public SearchResponse suggest(SearchRequestParameters parameters) {
        return searchService.search(parameters);
    }
}
