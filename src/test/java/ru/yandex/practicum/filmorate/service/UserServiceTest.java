package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Test
    void addFriendCreatesBidirectionalFriendship() {
        User u1 = createUser("u1");
        User u2 = createUser("u2");

        userService.addFriend(u1.getId(), u2.getId());

        Collection<Long> friendsOfU1 = u1.getFriends();
        Collection<Long> friendsOfU2 = u2.getFriends();

        assertTrue(friendsOfU1.contains(u2.getId()));
        assertTrue(friendsOfU2.contains(u1.getId()));
    }

    @Test
    void removeFriendRemovesBidirectionalFriendship() {
        User u1 = createUser("u1");
        User u2 = createUser("u2");

        userService.addFriend(u1.getId(), u2.getId());
        userService.removeFriend(u1.getId(), u2.getId());

        assertFalse(u1.getFriends().contains(u2.getId()));
        assertFalse(u2.getFriends().contains(u1.getId()));
    }

    @Test
    void getFriendsReturnsFriends() {
        User u1 = createUser("u1");
        User u2 = createUser("u2");
        User u3 = createUser("u3");

        userService.addFriend(u1.getId(), u2.getId());
        userService.addFriend(u1.getId(), u3.getId());

        var friends = userService.getFriends(u1.getId());
        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getLogin().equals("u2")));
        assertTrue(friends.stream().anyMatch(u -> u.getLogin().equals("u3")));
    }

    @Test
    void getCommonFriendsReturnsIntersection() {
        User u1 = createUser("u1");
        User u2 = createUser("u2");
        User u3 = createUser("u3");
        User u4 = createUser("u4");

        userService.addFriend(u1.getId(), u2.getId());
        userService.addFriend(u1.getId(), u3.getId());

        userService.addFriend(u4.getId(), u2.getId());
        userService.addFriend(u4.getId(), u3.getId());

        var common = userService.getCommonFriends(u1.getId(), u4.getId());
        assertEquals(2, common.size());
        assertTrue(common.stream().anyMatch(u -> u.getLogin().equals("u2")));
        assertTrue(common.stream().anyMatch(u -> u.getLogin().equals("u3")));
    }

    @Test
    void addFriendSelfThrowsValidationException() {
        User u = createUser("u");
        assertThrows(ValidationException.class, () -> userService.addFriend(u.getId(), u.getId()));
    }

    private User createUser(String login) {
        User u = new User();
        u.setEmail(login + "@test.com");
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(u);
    }
}
