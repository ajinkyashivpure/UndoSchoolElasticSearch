package com.undo.coursesearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import com.undo.coursesearch.dto.SearchRequestDto;
import com.undo.coursesearch.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CourseSearchServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private CourseSearchService courseSearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuildQueryWithPriceFilter() throws Exception {
        // Create a SearchRequestDto with price filter
        SearchRequestDto request = new SearchRequestDto();
        request.setMinPrice(new BigDecimal("10.00"));
        request.setMaxPrice(new BigDecimal("100.00"));

        // Get the private buildQuery method using reflection
        Method buildQueryMethod = CourseSearchService.class.getDeclaredMethod("buildQuery", SearchRequestDto.class);
        buildQueryMethod.setAccessible(true);

        // Call the buildQuery method
        Query query = (Query) buildQueryMethod.invoke(courseSearchService, request);

        // Print debug information
        System.out.println("[DEBUG_LOG] Query: " + query);

        // Verify the query contains a price range filter
        assertNotNull(query);
        assertTrue(query.toString().contains("\"price\""));
        assertTrue(query.toString().contains("\"gte\""));
        assertTrue(query.toString().contains("\"lte\""));
        assertTrue(query.toString().contains("10.00"));
        assertTrue(query.toString().contains("100.00"));
    }
}