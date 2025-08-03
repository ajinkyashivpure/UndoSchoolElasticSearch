package com.undo.coursesearch.dto;

import com.undo.coursesearch.document.CourseDocument;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SearchRequestDto {

    private String q; // Search query
    private BigDecimal minAge;
    private BigDecimal maxAge;
    private String category;
    private CourseDocument.CourseType type;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    private SortType sort = SortType.UPCOMING;
    private Integer page = 0;
    private Integer size = 10;

    public enum SortType {
        UPCOMING("upcoming"),
        PRICE_ASC("priceAsc"),
        PRICE_DESC("priceDesc");

        private final String value;

        SortType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static SortType fromString(String value) {
            if (value == null) return UPCOMING;

            for (SortType sortType : SortType.values()) {
                if (sortType.value.equalsIgnoreCase(value)) {
                    return sortType;
                }
            }
            return UPCOMING;
        }
    }

    // Validation helpers
    public boolean hasTextSearch() {
        return q != null && !q.trim().isEmpty();
    }

    public boolean hasAgeFilter() {
        return minAge != null || maxAge != null;
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }

    public boolean hasDateFilter() {
        return startDate != null;
    }
}