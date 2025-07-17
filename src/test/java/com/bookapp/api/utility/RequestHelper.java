package com.bookapp.api.utility;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail; // For more granular logging
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response; // Not always needed here, but kept for consistency
import lombok.Getter;
import lombok.Setter;

public class RequestHelper {

    public static final String BASE_URI = ConfigLoader.getProperty("base.url");

    @Getter
    @Setter
    private static String accessToken;

    public static RequestSpecification getUnauthenticatedRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    public static RequestSpecification getAuthenticatedRequestSpec() {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalStateException("Access Token is not set. Please log in first.");
        }
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + accessToken)
                .log(LogDetail.ALL)
                .build();
    }

    public static Response logAndExtractResponse(Response response, int expectedStatusCode, boolean logAll) {
        if (logAll) {
            response.then().log().all();
        } else {
            response.then().log().ifError();
        }
        response.then().statusCode(expectedStatusCode);
        return response;
    }

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