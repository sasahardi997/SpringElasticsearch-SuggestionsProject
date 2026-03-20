package com.hardi.suggestions.service;

import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import com.hardi.suggestions.dto.*;
import com.hardi.suggestions.util.Constants;
import com.hardi.suggestions.util.NativeQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public SearchResponse search(SearchRequestParameters parameters) {
        log.info("search request: {}", parameters);

        var query = NativeQueryBuilder.toSuggestQuery(parameters);
        log.info("bool query: {}", query.getQuery());

        var searchHits = elasticsearchOperations.search(query, Business.class, Constants.Index.BUSINESS);

        return buildResponse(parameters, searchHits);
    }

    private SearchResponse buildResponse(SearchRequestParameters parameters, SearchHits<Business> searchHits) {
        var results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        var searchPage = SearchHitSupport.searchPageFor(searchHits, PageRequest.of(parameters.page(), parameters.size()));
        var pagination = new Pagination(
                searchPage.getNumber(),
                searchPage.getNumberOfElements(),
                searchPage.getTotalElements(),
                searchPage.getTotalPages()
        );

        var facets = buildFacets((List<ElasticsearchAggregation) searchHits.getAggregations().aggregations());

        return new SearchResponse(
                results,
                facets,
                pagination,
                searchHits.getExecutionDuration().toMillis()
        );
    }

    private List<Facet> buildFacets(List<ElasticsearchAggregation> aggs) {
        var map = aggs.stream()
                .map(ElasticsearchAggregation::aggregation)
                .collect(Collectors.toMap(
                        a -> a.getName(),
                        a -> a.getAggregate()
                ));

        return List.of(
                buildFacet(Constants.Business.OFFERINGS_AGGREGATE_NAME, map.get(Constants.Business.OFFERINGS_AGGREGATE_NAME).sterms())
        );
    }

    private Facet buildFacet(String name, StringTermsAggregate stringTermsAggregate) {
        var facetItems = stringTermsAggregate.buckets()
                .array()
                .stream()
                .map(b -> new FacetItem(b.key().stringValue(), b.docCount()))
                .toList();

        return new Facet(name, facetItems);
    }
}
