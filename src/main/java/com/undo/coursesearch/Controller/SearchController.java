package com.undo.coursesearch.Controller;


import com.undo.coursesearch.dto.SearchRequestDto;
import com.undo.coursesearch.dto.SearchResponseDto;
import com.undo.coursesearch.service.CourseSearchService;
import com.undo.coursesearch.service.DataIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final CourseSearchService courseSearchService;
    private final DataIndexingService dataIndexingService;

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        SearchRequestDto request = new SearchRequestDto();
        request.setQ(q);
        request.setMinAge(minAge);
        request.setMaxAge(maxAge);
        request.setCategory(category);

        // Handle type conversion
        if (type != null && !type.isEmpty()) {
            try {
                request.setType(com.undo.coursesearch.document.CourseDocument.CourseType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid course type provided: {}", type);
            }
        }

        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setStartDate(startDate);
        request.setSort(SearchRequestDto.SortType.fromString(sort));
        request.setPage(page);
        request.setSize(size);

        log.debug("Search request: {}", request);

        SearchResponseDto response = courseSearchService.searchCourses(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/suggest")
    public ResponseEntity<Map<String, List<String>>> getSuggestions(
            @RequestParam String q) {

        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.badRequest().body(Map.of("suggestions", List.of()));
        }

        List<String> suggestions = courseSearchService.getSuggestions(q.trim());
        return ResponseEntity.ok(Map.of("suggestions", suggestions));
    }

    @PostMapping("/admin/reindex")
    public ResponseEntity<Map<String, String>> reindexData() {
        try {
            dataIndexingService.reindexData();
            return ResponseEntity.ok(Map.of("message", "Data reindexed successfully"));
        } catch (Exception e) {
            log.error("Failed to reindex data", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to reindex data: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Course Search Engine",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}