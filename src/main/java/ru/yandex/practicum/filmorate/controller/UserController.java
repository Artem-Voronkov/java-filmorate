package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;
    private static final String PATH_LINE = "/{id}/friends/{friendId}";

    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.debug("Запрос всех пользователей");
        return userStorage.getAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Запрос пользователя по id={}", id);
        var user = userStorage.getById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }
        return user.get();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Попытка создания пользователя: login='{}'", user.getLogin());

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен быть указан и содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя не указано, установлено равным логину: '{}'", user.getName());
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения обязательна");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        User created = userStorage.create(user);
        log.info("Пользователь успешно создан: id={}, login='{}'", created.getId(), created.getLogin());
        return created;
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        log.info("Попытка обновления пользователя: id={}", updatedUser.getId());

        if (updatedUser.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        var existing = userStorage.getById(updatedUser.getId());
        if (existing.isEmpty()) {
            throw new NotFoundException("Пользователь с указанным ID = " + updatedUser.getId() + " не найден");
        }

        if (updatedUser.getEmail() != null) {
            if (updatedUser.getEmail().isBlank() || !updatedUser.getEmail().contains("@")) {
                throw new ValidationException("Email должен содержать символ @");
            }
        }
        if (updatedUser.getLogin() != null) {
            if (updatedUser.getLogin().isBlank() || updatedUser.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может содержать пробелы и быть пустым");
            }
        }
        if (updatedUser.getName() != null && updatedUser.getName().isBlank()) {
            updatedUser.setName(existing.get().getLogin());
            log.debug("Имя очищено, установлено равным логину");
        }
        if (updatedUser.getBirthday() != null && updatedUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        User updated = userStorage.update(updatedUser);
        log.info("Пользователь успешно обновлён: id='{}'", updated.getId());
        return updated;
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        log.info("Запрос списка друзей для пользователя id={}", id);

        var user = userStorage.getById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }

        return userService.getFriends(id);
    }

    @PutMapping(PATH_LINE)
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Попытка добавить друга: {} -> {}", id, friendId);

        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }

        var user = userStorage.getById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }

        var friend = userStorage.getById(friendId);
        if (friend.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + friendId + " не найден");
        }

        userService.addFriend(id, friendId);
    }

    @DeleteMapping(PATH_LINE)
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Попытка удалить друга: {} -> {}", id, friendId);

        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя удалить себя из списка друзей");
        }

        var user = userStorage.getById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }

        var friend = userStorage.getById(friendId);
        if (friend.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + friendId + " не найден");
        }

        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id1}/friends/common/{id2}")
    public Collection<User> getCommonFriends(@PathVariable Long id1, @PathVariable Long id2) {
        log.info("Запрос общих друзей между {} и {}", id1, id2);

        var u1 = userStorage.getById(id1);
        var u2 = userStorage.getById(id2);

        if (u1.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + id1 + " не найден");
        }
        if (u2.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + id2 + " не найден");
        }

        return userService.getCommonFriends(id1, id2);
    }
}
