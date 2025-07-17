package com.bookapp.api.factory;

import com.bookapp.api.helper.BookApiServiceHelper;
import com.bookapp.model.book.Book;
import com.bookapp.model.common.ErrorResponse; // Corrected import
import com.bookapp.model.user.AuthResponse;
import com.bookapp.model.user.User;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.awaitility.Awaitility; // Assuming Awaitility is configured in TestBase

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class BookApiFactory {

    private final BookApiServiceHelper bookApiServiceHelper;
    private final Faker faker;

    public BookApiFactory() {
        this.bookApiServiceHelper = new BookApiServiceHelper();
        this.faker = new Faker();
    }

    // --- User Management Workflows ---

    @Step("Sign up a new unique user")
    public User signupUniqueUser() {
        Integer id = faker.number().numberBetween(1, 1000000);
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 12, true, true, true);
        User newUser = new User(id, email, password);

        Response signupResponse = bookApiServiceHelper.signupUser(newUser);
        assertThat(signupResponse.statusCode())
                .as("User signup should be successful")
                .isEqualTo(200);

        return newUser; // Return the user object
    }

    @Step("Login user {0} and obtain access token")
    public String loginAndGetToken(User user) {
        // Explicitly declare the type returned by loginUser
        AuthResponse authResponse = bookApiServiceHelper.loginUser(user); // Now returns AuthResponse
        String token = authResponse.getAccessToken(); // And AuthResponse has getAccessToken()

        assertThat(token).as("Access token should not be null or empty").isNotNull().isNotEmpty();
        return token;
    }

    // --- Book Management Workflows (now dependent on authenticated calls) ---

    @Step("Create a unique book and verify it is created successfully")
    public Book createAndVerifyUniqueBook() {
        Book newBookPayload = generateUniqueBookPayload();
        Response response = bookApiServiceHelper.createBook(newBookPayload);

        assertThat(response.statusCode())
                .as("Book creation should be successful")
                .isEqualTo(200);

        Book createdBook = response.as(Book.class);
        assertThat(createdBook.getId())
                .as("Created book should have an ID")
                .isNotNull();
        assertThat(createdBook.getName()).isEqualTo(newBookPayload.getName());
        assertThat(createdBook.getAuthor()).isEqualTo(newBookPayload.getAuthor());
        assertThat(createdBook.getPublishedYear()).isEqualTo(newBookPayload.getPublishedYear());
        assertThat(createdBook.getBookSummary()).isEqualTo(newBookPayload.getBookSummary());

        // Verify it can be retrieved using its new ID
        Response getResponse = bookApiServiceHelper.getBookById(createdBook.getId());
        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.as(Book.class)).isEqualTo(createdBook);

        return createdBook;
    }

    @Step("Get all books and assert expected count")
    public List<Book> getAllBooksAndAssertCount(int expectedMinCount) {
        Response response = bookApiServiceHelper.getAllBooks();
        assertThat(response.statusCode()).isEqualTo(200);

        List<Book> books = Arrays.asList(response.as(Book[].class));
        assertThat(books.size()).as("Number of books should be at least " + expectedMinCount).isGreaterThanOrEqualTo(expectedMinCount);
        return books;
    }

    @Step("Verify Health endpoint accessibility")
    public void verifyHealthEndpointIsAccessible() {
        bookApiServiceHelper.getHealth();
    }

    @Step("Update a book and verify changes")
    public Book updateAndVerifyBook(Integer bookId, String newName, Integer newPublishedYear, String newSummary) {
        Response getResponse = bookApiServiceHelper.getBookById(bookId);
        assertThat(getResponse.statusCode()).isEqualTo(200);
        Book bookToUpdate = getResponse.as(Book.class);
        String originalAuthor = bookToUpdate.getAuthor(); // Keep original author

        bookToUpdate.setName(newName); // Modify the name
        bookToUpdate.setPublishedYear(newPublishedYear); // Modify the year
        bookToUpdate.setBookSummary(newSummary); // Modify the summary
        // Ensure ID is explicitly set in payload for PUT if API expects it
        bookToUpdate.setId(bookId);

        Response updateResponse = bookApiServiceHelper.updateBook(bookId, bookToUpdate);
        assertThat(updateResponse.statusCode()).isEqualTo(200);
        Book updatedBook = updateResponse.as(Book.class);

        assertThat(updatedBook.getName()).isEqualTo(newName);
        assertThat(updatedBook.getPublishedYear()).isEqualTo(newPublishedYear);
        assertThat(updatedBook.getBookSummary()).isEqualTo(newSummary);
        assertThat(updatedBook.getAuthor()).isEqualTo(originalAuthor); // Author should be unchanged

        // Verify retrieval confirms update
        Response getUpdatedResponse = bookApiServiceHelper.getBookById(bookId);
        assertThat(getUpdatedResponse.statusCode()).isEqualTo(200);
        Book retrievedBook = getUpdatedResponse.as(Book.class);
        assertThat(retrievedBook.getName()).isEqualTo(newName);
        assertThat(retrievedBook.getPublishedYear()).isEqualTo(newPublishedYear);
        assertThat(retrievedBook.getBookSummary()).isEqualTo(newSummary);

        return updatedBook;
    }

    @Step("Delete a book and verify it's no longer retrievable")
    public void deleteAndVerifyBook(Integer bookId) {
        Response deleteResponse = bookApiServiceHelper.deleteBook(bookId);
        assertThat(deleteResponse.statusCode()).isEqualTo(200); // Assuming 200 OK for successful deletion

        // Verify it's truly deleted (expect 404)
        Response getDeletedResponse = bookApiServiceHelper.getNonExistentBook(bookId);
        assertThat(getDeletedResponse.statusCode()).isEqualTo(404);
    }

    // --- Helper for generating unique Book payloads ---
    public Book generateUniqueBookPayload() {
        return new Book(
                null, // Explicitly pass null for the 'id' as it's server-generated for new books
                faker.book().title() + " " + faker.number().digits(4), // Make name unique
                faker.book().author(),
                faker.number().numberBetween(1900, 2024),
                faker.lorem().sentence()
        );
    }

    // --- Example of Awaitility for asynchronous processes (if your book app had one) ---
    @Step("Wait for book status to be {1}")
    public Book waitForBookStatus(Integer bookId, String expectedStatus, int timeoutSeconds) {
        AtomicReference<Book> bookRef = new AtomicReference<>();
        Awaitility.await()
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    Response response = bookApiServiceHelper.getBookById(bookId);
                    if (response.statusCode() == 200) {
                        Book currentBook = response.as(Book.class);
                        bookRef.set(currentBook);
                        // Placeholder: Assuming Book has a 'status' field to check
                        // For this app, we'll check name match for demonstration
                        return currentBook.getName().contains(expectedStatus);
                    }
                    return false;
                });
        return bookRef.get();
    }

    // --- Example of Negative Test Orchestration ---
//    @Step("Attempt to create a book with invalid data and verify error")
//    public void attemptCreateBookWithInvalidDataAndVerifyError() {
//        Book invalidBook = new Book(
//                null, // Pass null for the 'id' field
//                null, // Name is null (as per your example logic for invalid data)
//                faker.book().author(),
//                2020,
//                "summary"
//        );
//        Response response = bookApiServiceHelper.createBookWithInvalidData(invalidBook);
//        assertThat(response.statusCode()).isEqualTo(422); // Assuming 422 Unprocessable Entity
//    }

    @Step("Attempt to create a book with invalid data (null ID, null name, invalid year) and verify error")
    public void attemptCreateBookWithInvalidDataAndVerifyError() {
        // We'll build a Map and convert it to JSON string to allow 'test' for published_year
        // Alternatively, manually build the JSON string
        String invalidJsonPayload = String.format(
                "{\"id\": null, \"name\": null, \"author\": \"%s\", \"published_year\": test, \"book_summary\": \"%s\"}",
                faker.book().author(),
                faker.lorem().sentence()
        );

        Response response = bookApiServiceHelper.createBookWithRawJson(invalidJsonPayload, 422);
        assertThat(response.statusCode())
                .as("Request with invalid published_year should return a validation error")
                .isEqualTo(422);
    }

    @Step("Attempt to access authenticated endpoint without login")
    public void attemptAccessAuthEndpointUnauthenticated() {
        bookApiServiceHelper.getAllBooksUnauthenticated();
    }

    // --- Cleanup Helper ---
    public void cleanupUser(User user) {
        // Implement DELETE /users endpoint if available for proper cleanup
        System.out.println("Cleanup: User " + user.getEmail() + " would be deleted here if API exists.");
    }
}