package com.audition.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.audition.web.interceptor.LoggingRequestInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

class WebServiceConfigurationTest {

    private transient WebServiceConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new WebServiceConfiguration();
    }

    @Test
    void objectMapper_shouldBeConfiguredCorrectly() {
        ObjectMapper mapper = configuration.objectMapper();

        assertNotNull(mapper);
        // Check date format
        SimpleDateFormat sdf = (SimpleDateFormat) mapper.getDateFormat();
        assertEquals("yyyy-MM-dd", sdf.toPattern());

        // Check serialization features
        assertFalse(mapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertFalse(mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // Check property naming strategy
        assertEquals(PropertyNamingStrategies.LOWER_CAMEL_CASE, mapper.getPropertyNamingStrategy());

        // Check inclusion
        assertEquals(JsonInclude.Include.NON_EMPTY, mapper.getSerializationConfig().getSerializationInclusion());
    }

    @Test
    void restTemplate_shouldBeConfiguredCorrectly() {
        ObjectMapper mapper = configuration.objectMapper();
        RestTemplate restTemplate = configuration.restTemplate(mapper);

        assertNotNull(restTemplate);
        assertTrue(restTemplate.getRequestFactory() instanceof InterceptingClientHttpRequestFactory);

        // Check message converters
        assertEquals(1, restTemplate.getMessageConverters().size());
        assertTrue(restTemplate.getMessageConverters().get(0) instanceof MappingJackson2HttpMessageConverter);
        MappingJackson2HttpMessageConverter converter =
            (MappingJackson2HttpMessageConverter) restTemplate.getMessageConverters().get(0);
        assertEquals(mapper, converter.getObjectMapper());

        // Check interceptors
        assertEquals(1, restTemplate.getInterceptors().size());
        assertTrue(restTemplate.getInterceptors().get(0) instanceof LoggingRequestInterceptor);
    }

    @Test
    void responseHeaderInjector_shouldBeCreated() {
        assertNotNull(configuration.responseHeaderInjector());
    }
}
