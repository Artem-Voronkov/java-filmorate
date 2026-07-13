package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // Создаём тестовых пользователей
        user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("login1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userService.createUser(user1);

        user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("login2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        user2 = userService.createUser(user2);

        user3 = new User();
        user3.setEmail("user3@test.com");
        user3.setLogin("login3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1992, 3, 3));
        user3 = userService.createUser(user3);
    }

    @Test
    void createUserValidDataSuccess() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setLogin("newlogin");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(2000, 5, 5));

        User created = userService.createUser(newUser);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getLogin()).isEqualTo("newlogin");
        assertThat(created.getName()).isEqualTo("New User");
    }

    @Test
    void createUserInvalidEmailThrowsValidation() {
        User invalid = new User();
        invalid.setEmail("no-at-sign");
        invalid.setLogin("badlogin");
        invalid.setName("Bad User");
        invalid.setBirthday(LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> userService.createUser(invalid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email должен содержать символ @");
    }

    @Test
    void updateUserValidDataSuccess() {
        String newName = "Updated Name";
        user1.setName(newName);
        User updated = userService.updateUser(user1);

        assertThat(updated.getName()).isEqualTo(newName);
        assertThat(updated.getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    void addFriendSuccess() {
        userService.addFriend(user1.getId(), user2.getId());

        List<Long> friendsOfUser1 = userService.getFriends(user1.getId());
        List<Long> friendsOfUser2 = userService.getFriends(user2.getId());

        assertThat(friendsOfUser1).containsExactly(user2.getId());
        assertThat(friendsOfUser2).containsExactly(user1.getId());
    }

    @Test
    void addFriendSelfThrowsValidation() {
        assertThatThrownBy(() -> userService.addFriend(user1.getId(), user1.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя добавить самого себя в друзья");
    }

    @Test
    void removeFriendSuccess() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());

        List<Long> friendsOfUser1 = userService.getFriends(user1.getId());
        List<Long> friendsOfUser2 = userService.getFriends(user2.getId());

        assertThat(friendsOfUser1).isEmpty();
        assertThat(friendsOfUser2).isEmpty();
    }

    @Test
    void getCommonFriendsSuccess() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<Long> common = userService.getCommonFriends(user2.getId(), user3.getId());

        assertThat(common).containsExactly(user1.getId());
    }

    @Test
    void getCommonFriendsNoCommonEmptyList() {
        userService.addFriend(user1.getId(), user2.getId());

        List<Long> common = userService.getCommonFriends(user2.getId(), user3.getId());

        assertThat(common).isEmpty();
    }
}
