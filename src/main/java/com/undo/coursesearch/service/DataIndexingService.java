package com.undo.coursesearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undo.coursesearch.document.CourseDocument;
import com.undo.coursesearch.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.annotation.Order;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIndexingService {

    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Order(2) // Run after index initialization
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
            log.info("Loaded {} courses from JSON file", courses.size());

            // Initialize suggest fields for each course and log a sample
            courses.forEach(course -> {
                course.initializeSuggestFields();
                if (course.getId().equals("1")) {
                    log.info("Sample course data: ID={}, Title={}, NextSessionDate={}",
                            course.getId(), course.getTitle(), course.getNextSessionDate());
                }
            });

            // Bulk index the data
            try {
                Iterable<CourseDocument> savedCourses = courseRepository.saveAll(courses);
                long savedCount = courseRepository.count();
                log.info("Successfully indexed {} courses", savedCount);
            } catch (Exception e) {
                log.error("Failed to bulk index courses. Error: {}", e.getMessage());

                // Try indexing one by one to identify problematic documents
                log.info("Attempting to index documents individually...");
                int successCount = 0;
                for (CourseDocument course : courses) {
                    try {
                        courseRepository.save(course);
                        successCount++;
                    } catch (Exception individualError) {
                        log.error("Failed to index course ID {}: {}", course.getId(), individualError.getMessage());
                        log.error("Problematic course data: {}", course);
                    }
                }
                log.info("Successfully indexed {} out of {} courses individually", successCount, courses.size());
            }

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