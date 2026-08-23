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
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID = " + id + " не найден"));
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Создание пользователя: login='{}'", user.getLogin());

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения обязательна");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        return userStorage.create(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Удаление пользователя: id={}", id);
        userStorage.deleteById(id);
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        log.info("Обновление пользователя: id={}", updatedUser.getId());

        if (updatedUser.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        //Исправил
        userStorage.getById(updatedUser.getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с ID = %d не найден", updatedUser.getId())));

        if (updatedUser.getEmail() != null && (!updatedUser.getEmail().contains("@") || updatedUser.getEmail().isBlank())) {
            throw new ValidationException("Email должен содержать @");
        }
        if (updatedUser.getLogin() != null && (updatedUser.getLogin().isBlank() || updatedUser.getLogin().contains(" "))) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (updatedUser.getName() != null && updatedUser.getName().isBlank()) {
            updatedUser.setName(updatedUser.getLogin());
        }
        if (updatedUser.getBirthday() != null && updatedUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        return userStorage.update(updatedUser);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        log.info("Запрос друзей для пользователя id={}", id);

        userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID = " + id + " не найден"));

        return userService.getFriends(id);
    }

    @PutMapping(PATH_LINE)
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Добавление друга: {} -> {}", id, friendId);
        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id1}/friends/common/{id2}")
    public Collection<User> getCommonFriends(@PathVariable Long id1, @PathVariable Long id2) {
        log.info("Запрос общих друзей: {} и {}", id1, id2);
        return userService.getCommonFriends(id1, id2);
    }
}
