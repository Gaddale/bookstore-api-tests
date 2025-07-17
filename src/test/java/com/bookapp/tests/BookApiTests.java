package com.bookapp.tests;

import com.bookapp.api.factory.BookApiFactory;
import com.bookapp.api.utility.RequestHelper;
import com.bookapp.model.book.Book;
import com.bookapp.model.user.User;
import org.junit.jupiter.api.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Book Application API Testing")
@Feature("Book Management Operations")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookApiTests extends TestBaseSimplified {

    private static BookApiFactory bookApiFactory;
    private static User testUser;
    private static List<Integer> createdBookIds;

    @BeforeAll
    static void setupAuthenticationAndFactory() {
        bookApiFactory = new BookApiFactory();
        createdBookIds = new ArrayList<>();

        System.out.println("Setting up global test user and obtaining access token...");
        testUser = bookApiFactory.signupUniqueUser();
        bookApiFactory.loginAndGetToken(testUser);
        System.out.println("Global test user setup complete. Token obtained.");
    }

    @Test
    @Order(1)
    @DisplayName("Verify Health endpoint is accessible")
    @Story("Health check verifies API availability")
    @Tag("smoke")
    void testHealthEndpoint() {
        bookApiFactory.verifyHealthEndpointIsAccessible();
    }

    @Test
    @Order(2)
    @DisplayName("Verify that a unique book can be created successfully by authenticated user")
    @Story("As an authenticated user, I can add new books to the system")
    @Tag("sanity")
    void testCreateBookSuccessfully() {
        Book book = bookApiFactory.createAndVerifyUniqueBook();
        createdBookIds.add(book.getId()); // Store ID for cleanup
        System.out.println("Created Book ID: " + book.getId());
    }

    @Test
    @Order(3)
    @DisplayName("Verify all books can be retrieved by an authenticated user")
    @Story("As an authenticated user, I can view all books")
    @Tag("sanity")
    void testGetAllBooksAuthenticated() {
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
    @Tag("sanity")
    void testUpdateBook() {
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
    @Tag("regression")
    void testCreateBookWithInvalidDataFails() {
        bookApiFactory.attemptCreateBookWithInvalidDataAndVerifyError();
    }

    @Test
    @Order(6)
    @DisplayName("Verify an existing book can be deleted by an authenticated user")
    @Story("As an authenticated user, I can remove books from the system")
    @Tag("sanity")
    void testDeleteBook() {
        Book bookToDelete = bookApiFactory.createAndVerifyUniqueBook();
        bookApiFactory.deleteAndVerifyBook(bookToDelete.getId());
    }

    @Test
    @Order(7)
    @DisplayName("Verify accessing authenticated endpoints without a token fails")
    @Story("As an unauthenticated user, I cannot access secured endpoints")
    @Tag("regression")
    void testAccessAuthEndpointUnauthenticated() {
        String originalToken = RequestHelper.getAccessToken();
        RequestHelper.setAccessToken(null); // Simulate no token
        try {
            bookApiFactory.attemptAccessAuthEndpointUnauthenticated();
        } finally {
            RequestHelper.setAccessToken(originalToken);
        }
    }


    @AfterAll
    static void cleanup() {
        System.out.println("Cleaning up " + createdBookIds.size() + " books...");
        for (Integer bookId : createdBookIds) {
            try {
                bookApiFactory.deleteAndVerifyBook(bookId);
                System.out.println("Cleaned up book: " + bookId);
            } catch (AssertionError e) {
                System.err.println("Failed to clean up book " + bookId + ": " + e.getMessage());
            }
        }
        bookApiFactory.cleanupUser(testUser);
        System.out.println("Cleanup complete.");
    }
}