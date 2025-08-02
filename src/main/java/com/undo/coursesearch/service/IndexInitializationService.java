//package com.undo.coursesearch.service;
//
//
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
//import co.elastic.clients.elasticsearch.indices.ExistsRequest;
//import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
//import co.elastic.clients.json.JsonData;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Service;
//
//import java.io.StringReader;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class IndexInitializationService {
//
//    private final ElasticsearchClient elasticsearchClient;
//    private static final String INDEX_NAME = "courses";
//
//    @EventListener(ApplicationReadyEvent.class)
//    @Order(1) // Run before data indexing
//    public void initializeIndex() {
//        try {
//            // Check if index exists
//            ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(INDEX_NAME));
//            boolean exists = elasticsearchClient.indices().exists(existsRequest).value();
//
//            if (!exists) {
//                log.info("Creating index: {}", INDEX_NAME);
//                createIndex();
//            } else {
//                log.info("Index {} already exists, updating mapping", INDEX_NAME);
//                updateMapping();
//            }
//
//        } catch (Exception e) {
//            log.error("Failed to initialize index", e);
//            throw new RuntimeException("Failed to initialize index", e);
//        }
//    }
//
//    private void createIndex() throws Exception {
//        String mapping = """
//            {
//              "mappings": {
//                "properties": {
//                  "id": { "type": "keyword" },
//                  "title": {
//                    "type": "text",
//                    "analyzer": "standard",
//                    "fields": {
//                      "keyword": { "type": "keyword" }
//                    }
//                  },
//                  "description": {
//                    "type": "text",
//                    "analyzer": "standard"
//                  },
//                  "category": { "type": "keyword" },
//                  "type": { "type": "keyword" },
//                  "gradeRange": { "type": "keyword" },
//                  "minAge": { "type": "integer" },
//                  "maxAge": { "type": "integer" },
//                  "price": { "type": "double" },
//                  "nextSessionDate": {
//                    "type": "date",
//                    "format": "yyyy-MM-dd'T'HH:mm:ss'Z'||yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd'T'HH:mm:ss.SSS||strict_date_optional_time||epoch_millis"
//                  },
//                  "titleSuggest": {
//                    "type": "search_as_you_type"
//                  },
//                  "suggest": {
//                    "type": "completion",
//                    "analyzer": "simple",
//                    "preserve_separators": true,
//                    "preserve_position_increments": true,
//                    "max_input_length": 50
//                  }
//                }
//              }
//            }
//            """;
//
//        CreateIndexRequest createRequest = CreateIndexRequest.of(c -> c
//                .index(INDEX_NAME)
//                .withJson(new StringReader(mapping))
//        );
//
//        elasticsearchClient.indices().create(createRequest);
//        log.info("Successfully created index: {}", INDEX_NAME);
//    }
//
//    private void updateMapping() throws Exception {
//        String mapping = """
//            {
//              "properties": {
//                "nextSessionDate": {
//                  "type": "date",
//                  "format": "yyyy-MM-dd'T'HH:mm:ss'Z'||yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd'T'HH:mm:ss.SSS||strict_date_optional_time||epoch_millis"
//                }
//              }
//            }
//            """;
//
//        PutMappingRequest mappingRequest = PutMappingRequest.of(m -> m
//                .index(INDEX_NAME)
//                .withJson(new StringReader(mapping))
//        );
//
//        elasticsearchClient.indices().putMapping(mappingRequest);
//        log.info("Successfully updated mapping for index: {}", INDEX_NAME);
//    }
//}