package com.example.drawingapp.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-wide beans.
 *
 * <p>The {@link ObjectMapper} is configured here rather than being created ad hoc so that reading
 * and writing always use the same rules:
 * <ul>
 *   <li>unknown properties are ignored, so a file written by a slightly different version still
 *       opens instead of failing;</li>
 *   <li>null values are omitted, keeping saved files small and readable;</li>
 *   <li>polymorphic typing stays restricted to the subtypes declared on the model. Jackson's
 *       default typing is deliberately never enabled, so a file cannot ask the application to
 *       instantiate arbitrary Java classes.</li>
 * </ul>
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }
}
