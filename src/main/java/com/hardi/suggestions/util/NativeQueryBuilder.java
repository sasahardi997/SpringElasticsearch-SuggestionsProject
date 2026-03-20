package com.hardi.suggestions.util;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.hardi.suggestions.dto.SearchRequestParameters;
import com.hardi.suggestions.dto.SuggestionRequestParameters;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;

import java.util.List;
import java.util.Optional;

public class NativeQueryBuilder {

    private static final List<QueryRule> FILTER_QUERY_RULES = List.of(
            QueryRules.STATE_QUERY,
            QueryRules.RATING_QUERY,
            QueryRules.DISTANCE_QUERY,
            QueryRules.OFFERINGS_QUERY
    );

    private static final List<QueryRule> MUST_QUERY_RULES = List.of(
            QueryRules.STATE_QUERY
    );

    private static final List<QueryRule> SHOULD_QUERY_RULES = List.of(
            QueryRules.CATEGORY_QUERY
    );

    public static NativeQuery toSuggestQuery(SuggestionRequestParameters parameters) {
        var suggester = ElasticsearchUtil.buildCompletionSuggester(Constants.Suggestion.SUGGEST_NAME,
                Constants.Suggestion.SEARCH_TERM, parameters.prefix(), parameters.limit());

        return NativeQuery.builder()
                .withSuggester(suggester)
                .withMaxResults(0)
                .withSourceFilter(FetchSourceFilter.of(b -> b.withExcludes("*")))
                .build();
    }

    public static NativeQuery toSearchQuery(SearchRequestParameters parameters) {
        var filterQueries = buildQueries(FILTER_QUERY_RULES, parameters);
        var mustQueries = buildQueries(MUST_QUERY_RULES, parameters);
        var shouldQueries = buildQueries(SHOULD_QUERY_RULES, parameters);

        var boolQuery = BoolQuery.of(builder -> builder
                .filter(filterQueries)
                .must(mustQueries)
                .should(shouldQueries));

        return NativeQuery.builder()
                .withQuery(Query.of(builder -> builder.bool(boolQuery)))
                .withAggregation(Constants.Business.OFFERINGS_AGGREGATE_NAME, ElasticsearchUtil.buildTermsAggregations(Constants.Business.OFFERINGS_RAW))
                .withPageable(PageRequest.of(parameters.page(), parameters.size()))
                .withTrackTotalHits(true)
                .build();
    }

    private static List<Query> buildQueries(List<QueryRule> queryRules, SearchRequestParameters parameters) {
        return queryRules.stream()
                .map(qr -> qr.build(parameters))
                .flatMap(Optional::stream)
                .toList();
    }
}
