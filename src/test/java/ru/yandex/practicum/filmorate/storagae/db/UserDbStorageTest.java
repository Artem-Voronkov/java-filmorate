package ru.yandex.practicum.filmorate.storagae.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.db.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserStorage userStorage;

    @Test
    void shouldFindUserById() {
        // Создаём тестового пользователя
        User user = User.builder()
                .email("test@example.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User saved = userStorage.create(user);

        Optional<User> found = userStorage.getById(saved.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(saved.getId());
                    assertThat(u.getEmail()).isEqualTo("test@example.com");
                    assertThat(u.getLogin()).isEqualTo("testuser");
                });
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> found = userStorage.getById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldCreateUserWithGeneratedId() {
        User user = User.builder()
                .email("new@user.com")
                .login("newuser")
                .birthday(LocalDate.now().minusYears(20))
                .build(); // name = null

        User created = userStorage.create(user);

        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("newuser");
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .email("old@user.com")
                .login("oldlogin")
                .name("Old Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userStorage.create(user);

        created.setName("Updated Name");
        created.setEmail("updated@user.com");

        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@user.com");
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .email("del@user.com")
                .login("deluser")
                .birthday(LocalDate.now().minusYears(25))
                .build();

        User created = userStorage.create(user);
        userStorage.deleteById(created.getId());

        Optional<User> found = userStorage.getById(created.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = User.builder().email("u1@u.com").login("u1").birthday(LocalDate.now().minusYears(20)).build();
        User user2 = User.builder().email("u2@u.com").login("u2").birthday(LocalDate.now().minusYears(22)).build();

        userStorage.create(user1);
        userStorage.create(user2);

        var all = userStorage.getAll();

        assertThat(all).hasSize(2);
    }
}
