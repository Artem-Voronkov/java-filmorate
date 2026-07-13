package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage.clear();
    }

    @Test
    void createUserBlankLoginThrowsValidation() {
        var user = new User();
        user.setEmail("a@b.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                userService.createUser(user)
        );
        assertTrue(ex.getMessage().contains("Логин не может быть пустым"));
    }

    @Test
    void createUserFutureBirthdayThrowsValidation() {
        var user = new User();
        user.setEmail("a@b.com");
        user.setLogin("goodlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                userService.createUser(user)
        );
        assertTrue(ex.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void updateUserValidSuccess() {
        var original = new User();
        original.setEmail("orig@example.com");
        original.setLogin("origlogin");
        original.setName("Original Name");
        original.setBirthday(LocalDate.of(1985, 3, 10));
        User created = userService.createUser(original);

        var update = new User();
        update.setId(created.getId());
        update.setName("New Name");
        update.setEmail("new@example.com");

        User updated = userService.updateUser(update);

        assertEquals(created.getId(), updated.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("origlogin", updated.getLogin());
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void addFriendSuccessCreates() {
        var u1 = createValidUserWith("u1@test.com", "user1", LocalDate.of(1995, 1, 1));
        var u2 = createValidUserWith("u2@test.com", "user2", LocalDate.of(1996, 2, 2));

        userService.addFriend(u1.getId(), u2.getId());

        List<Long> friendsOf1 = userService.getFriends(u1.getId());
        List<Long> friendsOf2 = userService.getFriends(u2.getId());

        assertTrue(friendsOf1.contains(u2.getId()));
        assertTrue(friendsOf2.contains(u1.getId()));
    }

    @Test
    void addFriendSelfThrowsValidation() {
        var u = createValidUserWith("self@test.com", "selflogin", LocalDate.of(2000, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                userService.addFriend(u.getId(), u.getId())
        );
        assertTrue(ex.getMessage().contains("Нельзя добавить самого себя в друзья"));
    }

    @Test
    void removeFriendSuccessRemoves() {
        var u1 = createValidUserWith("r1@test.com", "ruser1", LocalDate.of(1990, 1, 1));
        var u2 = createValidUserWith("r2@test.com", "ruser2", LocalDate.of(1991, 2, 2));

        userService.addFriend(u1.getId(), u2.getId());

        userService.removeFriend(u1.getId(), u2.getId());

        List<Long> friends1 = userService.getFriends(u1.getId());
        List<Long> friends2 = userService.getFriends(u2.getId());

        assertFalse(friends1.contains(u2.getId()));
        assertFalse(friends2.contains(u1.getId()));
    }

    @Test
    void getCommonFriendsSuccess() {
        var a = createValidUserWith("a@test.com", "a", LocalDate.of(1990, 1, 1));
        var b = createValidUserWith("b@test.com", "b", LocalDate.of(1990, 1, 1));
        var c = createValidUserWith("c@test.com", "c", LocalDate.of(1990, 1, 1));
        var d = createValidUserWith("d@test.com", "d", LocalDate.of(1990, 1, 1));

        userService.addFriend(a.getId(), b.getId());
        userService.addFriend(a.getId(), c.getId());

        userService.addFriend(b.getId(), a.getId());
        userService.addFriend(b.getId(), c.getId());

        userService.addFriend(c.getId(), a.getId());
        userService.addFriend(c.getId(), b.getId());
        userService.addFriend(c.getId(), d.getId());


        List<Long> common = userService.getCommonFriends(a.getId(), b.getId());

        assertEquals(1, common.size());
        assertTrue(common.contains(c.getId()));
    }

    private User createValidUserWith(String email, String login, LocalDate birthday) {
        var u = new User();
        u.setEmail(email);
        u.setLogin(login);
        u.setName(login + " Name");
        u.setBirthday(birthday);
        return userService.createUser(u);
    }
}
