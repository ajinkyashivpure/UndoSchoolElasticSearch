package com.undo.coursesearch.service;

import com.undo.coursesearch.document.CourseDocument;
import com.undo.coursesearch.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIndexingService {

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void indexSampleData() {
        try {
            log.info("Starting to index sample course data...");

            // Check if data already exists
            long count = courseRepository.count();
            if (count > 0) {
                log.info("Data already exists in index. Count: {}. Skipping initial data load.", count);
                return;
            }

            List<CourseDocument> courses = loadSampleData();

            // Initialize suggest fields for each course
            courses.forEach(CourseDocument::initializeSuggestFields);

            // Bulk index the data
            Iterable<CourseDocument> savedCourses = courseRepository.saveAll(courses);

            long savedCount = courseRepository.count();
            log.info("Successfully indexed {} courses", savedCount);

        } catch (Exception e) {
            log.error("Failed to index sample data", e);
            throw new RuntimeException("Failed to index sample data", e);
        }
    }

    private List<CourseDocument> loadSampleData() throws IOException {
        ClassPathResource resource = new ClassPathResource("sample-courses.json");

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<CourseDocument>>() {});
        }
    }

    public void reindexData() {
        try {
            log.info("Reindexing all course data...");

            // Delete all existing data
            courseRepository.deleteAll();

            // Reload and index fresh data
            indexSampleData();

        } catch (Exception e) {
            log.error("Failed to reindex data", e);
            throw new RuntimeException("Failed to reindex data", e);
        }
    }
}