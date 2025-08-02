package com.undo.coursesearch.repository;

import com.undo.coursesearch.document.CourseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {

    // Custom query methods can be added here if needed
    Page<CourseDocument> findByCategory(String category, Pageable pageable);
    Page<CourseDocument> findByType(CourseDocument.CourseType type, Pageable pageable);
    Page<CourseDocument> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<CourseDocument> findByNextSessionDateAfter(LocalDateTime date, Pageable pageable);
}