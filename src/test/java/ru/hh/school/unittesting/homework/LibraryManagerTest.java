package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// PR
@ExtendWith(MockitoExtension.class)
public class LibraryManagerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LibraryManager libraryManager;

    @Test
    public void getAvailableCopiesNegativeTest() {
        assertEquals(0, libraryManager.getAvailableCopies("book"));
    }

    @Test
    public void addBookPositiveTest() {
        libraryManager.addBook("book1", 3);

        assertEquals(3, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    public void duplicateAddBookPositiveTest() {
        libraryManager.addBook("book1", 3);
        libraryManager.addBook("book1", 2);

        assertEquals(5, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    public void borrowBookUserInactiveTest() {
        when(userService.isUserActive("user1")).thenReturn(false);

        assertFalse(libraryManager.borrowBook("book1", "user1"));
        verify(notificationService).notifyUser("user1", "Your account is not active.");
    }

    @Test
    public void borrowBookPositiveTest() {
        when(userService.isUserActive("user1")).thenReturn(true);

        libraryManager.addBook("book1", 3);

        assertTrue(libraryManager.borrowBook("book1", "user1"));
        assertEquals(2, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have borrowed the book: book1");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void borrowBookNegativeOrDefaultBooks(int quantity) {
        when(userService.isUserActive("user1")).thenReturn(true);

        libraryManager.addBook("book1", quantity);

        assertFalse(libraryManager.borrowBook("book1", "user1"));
    }

    @Test
    public void returnBookPositiveTest() {
        when(userService.isUserActive("user1")).thenReturn(true);

        libraryManager.addBook("book1", 1);
        libraryManager.borrowBook("book1", "user1");

        assertTrue(libraryManager.returnBook("book1", "user1"));
        assertEquals(1, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have returned the book: book1");
    }

    @ParameterizedTest
    @CsvSource({
        "book1, user1",
        "book, user"
    })
    public void returnBookNegativeTest(String book, String user) {
        libraryManager.addBook("book1", 1);

        assertFalse(libraryManager.returnBook(book, user));
    }

    @ParameterizedTest
    @CsvSource({
        "4, false, false, 2.0",
        "4, true, false, 3.0",
        "4, false, true, 1.6",
        "4, true, true, 2.4",
        "0, false, false, 0.0",
        "1, true, true, 0.6"
    })
    public void calculateLateFeePositiveTest(int days, boolean bestseller, boolean premium, double expected) {
        assertEquals(expected, libraryManager.calculateDynamicLateFee(days, bestseller, premium));
    }

    @Test
    public void calculateLateFeePositiveTest() {
        assertThrows(IllegalArgumentException.class, () ->
            libraryManager.calculateDynamicLateFee(-1, false, false)
        );
    }

}
