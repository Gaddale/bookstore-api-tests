package com.bookapp.tests;

import com.bookapp.api.factory.BookApiFactory;
import com.bookapp.api.utility.RequestHelper;
import com.bookapp.model.book.Book;
import com.bookapp.model.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Book Application API Testing")
@Feature("Book Management Operations")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // For ordered tests (optional for independence)
public class BookApiTests extends TestBaseSimplified {

    private static BookApiFactory bookApiFactory;
    private static User testUser; // User created for testing
    private static List<Integer> createdBookIds; // To keep track of books created for cleanup

    @BeforeAll
    static void setupAuthenticationAndFactory() {
        // Initialize the factory
        bookApiFactory = new BookApiFactory();
        createdBookIds = new ArrayList<>();

        // --- Global Precondition: User Signup and Login ---
        // This makes sure we have a valid authenticated session for all subsequent tests
        System.out.println("Setting up global test user and obtaining access token...");
        testUser = bookApiFactory.signupUniqueUser();
        bookApiFactory.loginAndGetToken(testUser);
        System.out.println("Global test user setup complete. Token obtained.");
    }

    @Test
    @Order(1)
    @DisplayName("Verify Health endpoint is accessible")
    @Story("Health check verifies API availability")
    void testHealthEndpoint() {
        bookApiFactory.verifyHealthEndpointIsAccessible(); // Direct call as it's simple and no factory method exists
    }

    @Test
    @Order(2)
    @DisplayName("Verify that a unique book can be created successfully by authenticated user")
    @Story("As an authenticated user, I can add new books to the system")
    void testCreateBookSuccessfully() {
        Book book = bookApiFactory.createAndVerifyUniqueBook();
        createdBookIds.add(book.getId()); // Store ID for cleanup
        System.out.println("Created Book ID: " + book.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Verify all books can be retrieved by an authenticated user")
    @Story("As an authenticated user, I can view all books")
    void testGetAllBooksAuthenticated() {
        // Ensure at least one book exists from previous test or create one if this test runs standalone
        if (createdBookIds.isEmpty()) {
            Book book = bookApiFactory.createAndVerifyUniqueBook();
            createdBookIds.add(book.getId());
        }
        bookApiFactory.getAllBooksAndAssertCount(1);
    }

    @Test
    @Order(4)
    @DisplayName("Verify an existing book can be updated by an authenticated user")
    @Story("As an authenticated user, I can update existing book details")
    void testUpdateBook() {
        // Ensure a book exists to update
        if (createdBookIds.isEmpty()) {
            Book book = bookApiFactory.createAndVerifyUniqueBook();
            createdBookIds.add(book.getId());
        }
        Integer bookIdToUpdate = createdBookIds.get(0);
        String newName = "Updated Name " + System.currentTimeMillis();
        Integer newPublishedYear = 2025;
        String newSummary = "Updated summary about the amazing book.";

        Book updatedBook = bookApiFactory.updateAndVerifyBook(bookIdToUpdate, newName, newPublishedYear, newSummary);
        assertThat(updatedBook.getName()).isEqualTo(newName);
        assertThat(updatedBook.getPublishedYear()).isEqualTo(newPublishedYear);
        assertThat(updatedBook.getBookSummary()).isEqualTo(newSummary);
    }

    @Test
    @Order(5)
    @DisplayName("Verify creating a book with invalid data (e.g., missing name) fails for authenticated user")
    @Story("As an authenticated user, I cannot add a book without mandatory fields")
    void testCreateBookWithInvalidDataFails() {
        bookApiFactory.attemptCreateBookWithInvalidDataAndVerifyError();
    }

    @Test
    @Order(6)
    @DisplayName("Verify an existing book can be deleted by an authenticated user")
    @Story("As an authenticated user, I can remove books from the system")
    void testDeleteBook() {
        // Ensure a book exists to delete
        Book bookToDelete = bookApiFactory.createAndVerifyUniqueBook();
        bookApiFactory.deleteAndVerifyBook(bookToDelete.getId());
    }

    @Test
    @Order(7) // This test should run after others that rely on authentication, or clear token before
    @DisplayName("Verify accessing authenticated endpoints without a token fails")
    @Story("As an unauthenticated user, I cannot access secured endpoints")
    void testAccessAuthEndpointUnauthenticated() {
        // Temporarily clear the token to simulate unauthenticated state for this specific test
        String originalToken = RequestHelper.getAccessToken();
        RequestHelper.setAccessToken(null); // Simulate no token
        try {
            bookApiFactory.attemptAccessAuthEndpointUnauthenticated();
        } finally {
            // Restore the token for any subsequent tests/cleanup that might run in the same JVM session
            RequestHelper.setAccessToken(originalToken);
        }
    }


    @AfterAll
    static void cleanup() {
        // Clean up any remaining books created during tests
        System.out.println("Cleaning up " + createdBookIds.size() + " books...");
        for (Integer bookId : createdBookIds) {
            try {
                // The delete method itself will verify 404, so we just call it.
                // We wrap in try-catch to ensure all books are attempted to be deleted even if one fails.
                bookApiFactory.deleteAndVerifyBook(bookId);
                System.out.println("Cleaned up book: " + bookId);
            } catch (AssertionError e) {
                System.err.println("Failed to clean up book " + bookId + ": " + e.getMessage());
                // Log the failure but continue cleanup for other items.
            }
        }
        // Optional: Cleanup the test user if there's a DELETE /users endpoint
        bookApiFactory.cleanupUser(testUser);
        System.out.println("Cleanup complete.");
    }
}