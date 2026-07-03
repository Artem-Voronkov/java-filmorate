package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private UserController userController;
    private Map<Long, User> testStorage;

    @BeforeEach
    void setUp() {
        testStorage = new HashMap<>();
        userController = new UserController(testStorage);
    }

    @Test
    void createUserSuccess() {
        var user = new User();
        user.setEmail("artem@yandex.ru");
        user.setLogin("artem99");
        user.setName("Артем Воронков");
        user.setBirthday(LocalDate.of(1999, 3, 29));

        User result = userController.createUser(user);

        assertNotNull(result.getId());
        assertEquals("Артем Воронков", result.getName());
        assertEquals("artem99", result.getLogin());
        assertTrue(testStorage.containsKey(result.getId()));
    }

    @Test
    void createUserNameNullUsesLogin() {
        var user = new User();
        user.setEmail("ivan@yandex.ru");
        user.setLogin("ivan123");
        user.setBirthday(LocalDate.of(1990, 5, 20));

        User result = userController.createUser(user);

        assertEquals("ivan123", result.getName());
    }

    @Test
    void createUserNameBlankUsesLogin() {
        var user = new User();
        user.setEmail("anna@yandex.ru");
        user.setLogin("anna99");
        user.setName("");
        user.setBirthday(LocalDate.of(1980, 1, 1));

        User result = userController.createUser(user);

        assertEquals("anna99", result.getName());
    }

    @Test
    void createUserEmailMissingThrowsValidation() {
        var user = new User();
        user.setLogin("badlogin");
        user.setBirthday(LocalDate.now().minusYears(20));

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(ex.getMessage().contains("Email должен быть указан"));
    }

    @Test
    void createUserEmailNoAtThrowsValidation() {
        var user = new User();
        user.setEmail("no-at-sign.com");
        user.setLogin("goodlogin");
        user.setBirthday(LocalDate.now().minusYears(20));

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(ex.getMessage().contains("символ @"));
    }

    @Test
    void createUserLoginWithSpaceThrowsValidation() {
        var user = new User();
        user.setEmail("test@yandex.ru");
        user.setLogin("with space");
        user.setBirthday(LocalDate.now().minusYears(20));

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(ex.getMessage().contains("пробелы"));
    }

    @Test
    void createUserBirthdayInFutureThrowsValidation() {
        var user = new User();
        user.setEmail("future@yandex.ru");
        user.setLogin("future123");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(ex.getMessage().contains("в будущем"));
    }

    @Test
    void updateUserNameBlankResetsToLogin() {
        var existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("temp@yandex.ru");
        existingUser.setLogin("tempuser");
        existingUser.setName("Было имя");
        existingUser.setBirthday(LocalDate.now().minusYears(30));
        testStorage.put(1L, existingUser);

        var updateUser = new User();
        updateUser.setId(1L);
        updateUser.setName("");

        User result = userController.updateUser(updateUser);

        assertEquals("tempuser", result.getName());
    }

    @Test
    void updateUserIdMissingThrowsValidation() {
        var user = new User();
        user.setName("Новое имя");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.updateUser(user);
        });

        assertTrue(ex.getMessage().contains("Id должен быть указан"));
    }

    @Test
    void updateUserNonExistingIdThrowsValidation() {
        var user = new User();
        user.setId(9999L);
        user.setName("Другое имя");

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.updateUser(user);
        });

        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    void createUserEmptyRequestThrowsValidation() {
        var user = new User();

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(ex.getMessage().contains("Email должен быть указан"));
    }
}

