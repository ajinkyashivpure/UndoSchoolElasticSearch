package com.undo.coursesearch.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import co.elastic.clients.json.JsonData;
import com.undo.coursesearch.document.CourseDocument;
import com.undo.coursesearch.dto.SearchRequestDto;
import com.undo.coursesearch.dto.SearchResponseDto;
import com.undo.coursesearch.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSearchService {

    private final CourseRepository courseRepository;
    private final ElasticsearchClient elasticsearchClient;

    private static final String COURSES_INDEX = "courses";

    public SearchResponseDto searchCourses(SearchRequestDto request) {
        try {
            Query query = buildQuery(request);

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(COURSES_INDEX)
                    .query(query)
                    .sort(buildSort(request))
                    .from(request.getPage() * request.getSize())
                    .size(request.getSize())
            );

            SearchResponse<CourseDocument> response = elasticsearchClient.search(searchRequest, CourseDocument.class);

            List<SearchResponseDto.CourseDto> courses = response.hits().hits().stream()
                    .map(Hit::source)
                    .map(SearchResponseDto.CourseDto::fromDocument)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            int totalPages = (int) Math.ceil((double) total / request.getSize());

            return SearchResponseDto.builder()
                    .total(total)
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalPages(totalPages)
                    .courses(courses)
                    .build();

        } catch (Exception e) {
            log.error("Error searching courses", e);
            throw new RuntimeException("Failed to search courses", e);
        }
    }

    public List<String> getSuggestions(String query) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(COURSES_INDEX)
                    .size(0)
                    .suggest(suggest -> suggest
                            .suggesters("course_suggest", cs -> cs
                                    .prefix(query)
                                    .completion(c -> c
                                            .field("suggest")
                                            .size(10)
                                            .skipDuplicates(true)
                                    )
                            )
                    )
            );

            SearchResponse<CourseDocument> response = elasticsearchClient.search(searchRequest, CourseDocument.class);

            List<String> suggestions = new ArrayList<>();
            if (response.suggest() != null && response.suggest().get("course_suggest") != null) {
                response.suggest().get("course_suggest").forEach(suggestItem -> {
                    if (suggestItem.completion() != null) {
                        suggestItem.completion().options().forEach(option -> {
                            if (option.source() != null && option.source().getTitle() != null) {
                                suggestions.add(option.source().getTitle());
                            }
                        });
                    }
                });
            }

            return suggestions.stream().distinct().limit(10).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting suggestions for query: {}", query, e);
            return new ArrayList<>();
        }
    }

    private Query buildQuery(SearchRequestDto request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // Text search with fuzzy matching
        if (request.hasTextSearch()) {
            MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                    .query(request.getQ())
                    .fields("title^2", "description")
                    .fuzziness("AUTO")
                    .operator(Operator.Or)
                    .type(TextQueryType.BestFields)
            );
            boolQueryBuilder.must(multiMatchQuery._toQuery());
        }

        // Category filter
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            TermQuery categoryQuery = TermQuery.of(t -> t
                    .field("category")
                    .value(request.getCategory())
            );
            boolQueryBuilder.filter(categoryQuery._toQuery());
        }

        // Type filter
        if (request.getType() != null) {
            TermQuery typeQuery = TermQuery.of(t -> t
                    .field("type")
                    .value(request.getType().name())
            );
            boolQueryBuilder.filter(typeQuery._toQuery());
        }

        // Age range filter
        if (request.hasAgeFilter()) {
            BoolQuery.Builder ageQueryBuilder = new BoolQuery.Builder();

            if (request.getMinAge() != null) {
                RangeQuery minAgeQuery = RangeQuery.of(r -> r
                        .field("maxAge")
                        .gte(JsonData.of(request.getMinAge()))  // Direct JsonData.of() conversion
                );
                ageQueryBuilder.must(minAgeQuery._toQuery());
            }

            if (request.getMaxAge() != null) {
                RangeQuery maxAgeQuery = RangeQuery.of(r -> r
                        .field("minAge")
                        .lte(JsonData.of(request.getMaxAge()))  // Direct JsonData.of() conversion
                );
                ageQueryBuilder.must(maxAgeQuery._toQuery());
            }

            boolQueryBuilder.filter(ageQueryBuilder.build()._toQuery());
        }

        // Price range filter
        if (request.hasPriceFilter()) {
            RangeQuery.Builder priceQueryBuilder = new RangeQuery.Builder().field("price");

            if (request.getMinPrice() != null) {
                priceQueryBuilder.gte(JsonData.of(request.getMinPrice()));
            }

            if (request.getMaxPrice() != null) {
                priceQueryBuilder.lte(JsonData.of(request.getMaxPrice()));
            }

            boolQueryBuilder.filter(priceQueryBuilder.build()._toQuery());
        }

        // Date filter
        if (request.hasDateFilter()) {
            RangeQuery dateQuery = RangeQuery.of(r -> r
                    .field("nextSessionDate")
                    .gte(JsonData.of( FieldValue.of(request.getStartDate().toString())))
            );
            boolQueryBuilder.filter(dateQuery._toQuery());
        }

        BoolQuery boolQuery = boolQueryBuilder.build();

        // If no conditions are set, return match_all query
        if (boolQuery.must().isEmpty() && boolQuery.filter().isEmpty() && boolQuery.should().isEmpty()) {
            return MatchAllQuery.of(m -> m)._toQuery();
        }

        return boolQuery._toQuery();
    }

    private co.elastic.clients.elasticsearch._types.SortOptions buildSort(SearchRequestDto request) {
        return switch (request.getSort()) {
            case PRICE_ASC -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .field(f -> f.field("price").order(SortOrder.Asc)));
            case PRICE_DESC -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .field(f -> f.field("price").order(SortOrder.Desc)));
            default -> co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
                    .field(f -> f.field("nextSessionDate").order(SortOrder.Asc)));
        };
    }
}
