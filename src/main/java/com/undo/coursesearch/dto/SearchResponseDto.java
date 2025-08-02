package com.undo.coursesearch.dto;

import com.undo.coursesearch.document.CourseDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {

    private long total;
    private int page;
    private int size;
    private int totalPages;
    private List<CourseDto> courses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseDto {
        private String id;
        private String title;
        private String description;
        private String category;
        private CourseDocument.CourseType type;
        private String gradeRange;
        private Integer minAge;
        private Integer maxAge;
        private BigDecimal price;

        private LocalDateTime nextSessionDate;

        public static CourseDto fromDocument(CourseDocument document) {
            return CourseDto.builder()
                    .id(document.getId())
                    .title(document.getTitle())
                    .description(document.getDescription())
                    .category(document.getCategory())
                    .type(document.getType())
                    .gradeRange(document.getGradeRange())
                    .minAge(document.getMinAge())
                    .maxAge(document.getMaxAge())
                    .price(document.getPrice())
                    .nextSessionDate(document.getNextSessionDate())
                    .build();
        }
    }
}