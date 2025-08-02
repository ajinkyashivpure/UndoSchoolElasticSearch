package com.undo.coursesearch.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
@JsonIgnoreProperties(ignoreUnknown = true) // This will ignore the _class field
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

    // Updated date field configuration
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS||strict_date_optional_time||epoch_millis")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
    @JsonIgnoreProperties(ignoreUnknown = true) // Also add to nested class
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