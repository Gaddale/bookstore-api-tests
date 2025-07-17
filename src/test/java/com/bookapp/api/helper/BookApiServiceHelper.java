package com.bookapp.api.helper;

import com.bookapp.model.book.Book;
import com.bookapp.model.user.AuthResponse;
import com.bookapp.model.user.User;
import com.bookapp.api.utility.RequestHelper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static com.bookapp.api.utility.RequestHelper.*; // Import static methods from RequestHelper

public class BookApiServiceHelper {

    // --- Unauthenticated Endpoints ---

    // Health Check
    public Response getHealth() {
        return logAndExtractResponse(given()
                .spec(getUnauthenticatedRequestSpec())
                .when()
                .get("/health").then()
                .extract().response(), 200, true);
    }

    // User Signup
    public Response signupUser(User userPayload) {
        return logAndExtractResponse(given()
                .spec(getUnauthenticatedRequestSpec())
                .body(userPayload)
                .when()
                .post("/signup").then()
                .extract().response(), 200, true);
    }

    // User Login and Token Retrieval
    public AuthResponse loginUser(User userPayload) {
        // Step 1: Execute the request and get the raw Response object
        Response rawResponse = given()
                .spec(getUnauthenticatedRequestSpec())
                .body(userPayload)
                .when()
                .post("/login")
                .then() // Ensure .then() is present for proper chain continuation before extraction
                .extract().response();

        Response response = logAndExtractResponse(rawResponse, 200, true);
        AuthResponse authResponse = response.as(AuthResponse.class);
        RequestHelper.setAccessToken(authResponse.getAccessToken());
        return authResponse;
    }

    // --- Authenticated Endpoints (Books) ---

    // GET all books
    public Response getAllBooks() {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .when()
                .get("/books/").then()
                .extract().response(), 200, false);
    }

    // GET a book by ID
    public Response getBookById(Integer bookId) {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .when()
                .get("/books/"+bookId).then()
                .extract().response(), 200, false);
    }

    // POST a new book
    public Response createBook(Book bookPayload) {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .body(bookPayload)
                .when()
                .post("/books/").then()
                .extract().response(), 200, false); // API returns 200 OK for creation
    }

    // PUT an existing book
    public Response updateBook(Integer bookId, Book bookPayload) {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .body(bookPayload)
                .when()
                .put("/books/"+bookId).then()
                .extract().response(), 200, false);
    }

    // DELETE a book
    public Response deleteBook(Integer bookId) {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .when()
                .delete("/books/"+bookId).then()
                .extract().response(), 200, false); // API returns 200 OK for deletion
    }

    // --- Negative Test Helpers ---

    // GET a non-existent book (expect 404)
    public Response getNonExistentBook(Integer bookId) {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .when()
                .get("/books/"+bookId).then()
                .extract().response(), 404, false);
    }

    public Response createBookWithRawJson(String jsonPayload, int expectedStatusCode) {
        return logAndExtractResponse(given()
                .spec(getAuthenticatedRequestSpec())
                .contentType(ContentType.JSON) // Ensure Content-Type is JSON for raw body
                .body(jsonPayload) // Send the raw JSON string
                .when()
                .post("/books/")
                .then()
                .extract().response(), expectedStatusCode, false);
    }

    public Response getAllBooksUnauthenticated() {
        return logAndExtractResponse(given()
                .spec(getUnauthenticatedRequestSpec()) // Deliberately use unauthenticated spec
                .when()
                .get("/books/").then()
                .extract().response(), 403, false);
    }
}