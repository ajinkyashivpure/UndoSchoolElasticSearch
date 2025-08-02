package com.undo.coursesearch.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "courses")
public class CourseDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private CourseType type;

    @Field(type = FieldType.Keyword)
    private String gradeRange;

    @Field(type = FieldType.Integer)
    private Integer minAge;

    @Field(type = FieldType.Integer)
    private Integer maxAge;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime nextSessionDate;

    // For autocomplete suggestions
    @Field(type = FieldType.Search_As_You_Type)
    private String titleSuggest;

    // For completion suggester
    @CompletionField
    private CompletionSuggest suggest;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionSuggest {
        private List<String> input;
        private String output;
        private Integer weight;
    }

    public enum CourseType {
        ONE_TIME,
        COURSE,
        CLUB
    }

    // Helper method to create suggest field from title
    public void initializeSuggestFields() {
        this.titleSuggest = this.title;
        if (this.title != null) {
            this.suggest = CompletionSuggest.builder()
                    .input(List.of(this.title.toLowerCase().split("\\s+")))
                    .output(this.title)
                    .weight(1)
                    .build();
        }
    }
}