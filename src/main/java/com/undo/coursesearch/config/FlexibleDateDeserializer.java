//package com.undo.coursesearch.config;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonDeserializer;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//
//public class FlexibleDateDeserializer extends JsonDeserializer<LocalDateTime> {
//
//    private static final DateTimeFormatter[] FORMATTERS = {
//            DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    };
//
//    @Override
//    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//        String dateString = p.getValueAsString().trim();
//
//        // Log the actual value being parsed
//        System.out.println("Parsing date string: '" + dateString + "'");
//
//        for (DateTimeFormatter formatter : FORMATTERS) {
//            try {
//                LocalDateTime result = LocalDateTime.parse(dateString, formatter);
//                System.out.println("Successfully parsed with formatter: " + formatter + " -> " + result);
//                return result;
//            } catch (DateTimeParseException e) {
//                // Continue to next formatter
//                System.out.println("Failed with formatter: " + formatter + " - " + e.getMessage());
//            }
//        }
//
//        throw new IOException("Unable to parse date: '" + dateString + "'. Expected formats: yyyy-MM-dd'T'HH:mm:ss'Z' or yyyy-MM-dd'T'HH:mm:ss");
//    }
//}