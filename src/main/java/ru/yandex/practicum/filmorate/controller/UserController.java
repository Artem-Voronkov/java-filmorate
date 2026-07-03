package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email должен быть указан");
        }
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new IllegalArgumentException("Логин должен быть указан");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin()); // fallback: имя = логин
        }
        if (user.getBirthday() == null) {
            throw new IllegalArgumentException("Дата рождения обязательна");
        }

        long id = generateNextId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new IllegalArgumentException("Id должен быть указан");
        }
        User existingUser = users.get(updatedUser.getId());
        if (existingUser == null) {
            throw new IllegalArgumentException("Пользователь с указанным id не найден");
        }

        if (updatedUser.getEmail() != null) {
            if (updatedUser.getEmail().isBlank() || !updatedUser.getEmail().contains("@")) {
                throw new IllegalArgumentException("Некорректный email");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getLogin() != null && !updatedUser.getLogin().isBlank()) {
            existingUser.setLogin(updatedUser.getLogin());
        }
        if (updatedUser.getName() != null) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getBirthday() != null) {
            existingUser.setBirthday(updatedUser.getBirthday());
        }

        return existingUser;
    }

    private long generateNextId() {
        return users.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

}
