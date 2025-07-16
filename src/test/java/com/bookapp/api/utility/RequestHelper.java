package com.bookapp.api.utility;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail; // For more granular logging
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response; // Not always needed here, but kept for consistency

public class RequestHelper {

    // Base URI of your Book Application API
    // In a real framework, this would come from an environment config (like a 'secrets' file)
    public static final String BASE_URI = "http://localhost:8000";

    // --- Authentication Token Management ---
    private static String accessToken; // Stores the bearer token

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    // --- Request Specification Builders ---

    // Prepares common specs for unauthenticated endpoints (signup, login, health)
    public static RequestSpecification getUnauthenticatedRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON) // Request body content type
                .setAccept(ContentType.JSON)     // Preferred response content type
                .log(LogDetail.ALL) // Log all request details for debugging
                .build();
    }

    // Prepares common specs for authenticated endpoints (books)
    public static RequestSpecification getAuthenticatedRequestSpec() {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("Access Token is not set. Please log in first.");
        }
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + accessToken) // Add the bearer token
                .log(LogDetail.ALL) // Log all request details
                .build();
    }

    // --- Response Logging and Extraction Helper (Similar to your updateCookies logic) ---
    // This helper method encapsulates logging and returning the response.
    public static Response logAndExtractResponse(Response response, int expectedStatusCode, boolean logAll) {
        if (logAll) {
            response.then().log().all(); // Log both request and response for this call
        } else {
            response.then().log().ifError(); // Log response only if an error status code (4xx, 5xx)
        }
        // Assert the status code directly here
        response.then().statusCode(expectedStatusCode);
        return response;
    }

    // Overloaded method for cases where multiple status codes are acceptable (e.g., 202 or 409)
    public static Response logAndExtractResponse(Response response, org.hamcrest.Matcher<Integer> statusCodeMatcher, boolean logAll) {
        if (logAll) {
            response.then().log().all();
        } else {
            response.then().log().ifError();
        }
        response.then().statusCode(statusCodeMatcher);
        return response;
    }
}