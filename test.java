package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.ws.rs.Path;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GetApiNameTests {

    // TODO: replace with the real class that has getAPIName(Method)
    private final YourClassUnderTest helper = new YourClassUnderTest();

    /** Test doubles to reflect against */
    static class DummyResource {
        @Path("/users/{id}")
        public void getUser() {}

        public void noAnnotation() {}

        @Path("") // empty value should be returned as-is
        public void emptyPath() {}
    }

    @Nested
    @DisplayName("getAPIName(Method)")
    class GetApiName {

        @Test
        @DisplayName("returns @Path value when present on the method")
        void returnsPathValueWhenPresent() throws Exception {
            Method m = DummyResource.class.getMethod("getUser");
            String apiName = helper.getAPIName(m);
            assertEquals("/users/{id}", apiName);
        }

        @Test
        @DisplayName("falls back to method name when @Path is absent")
        void fallsBackToMethodNameWhenNoAnnotation() throws Exception {
            Method m = DummyResource.class.getMethod("noAnnotation");
            String apiName = helper.getAPIName(m);
            assertEquals("noAnnotation", apiName);
        }

        @Test
        @DisplayName("returns \"Unknown\" when method is null")
        void returnsUnknownOnNullInput() {
            String apiName = helper.getAPIName(null);
            assertEquals("Unknown", apiName);
        }

        @Test
        @DisplayName("returns empty string if @Path value is empty")
        void returnsEmptyStringWhenPathEmpty() throws Exception {
            Method m = DummyResource.class.getMethod("emptyPath");
            String apiName = helper.getAPIName(m);
            assertEquals("", apiName);
        }
    }
}