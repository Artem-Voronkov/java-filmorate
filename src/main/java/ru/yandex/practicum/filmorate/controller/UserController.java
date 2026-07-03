package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.debug("Запрос на получение списка пользователей. Всего пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Попытка создания пользователя: login='{}'", user.getLogin());

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Неудачная регистрация: некорректный email");
            throw new ValidationException("Email должен быть указан и содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Неудачная регистрация: логин пустой или содержит пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя не указано, установлено равным логину: '{}'", user.getName());
        }

        if (user.getBirthday() == null) {
            log.warn("Неудачная регистрация: дата рождения отсутствует");
            throw new ValidationException("Дата рождения обязательна");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Неудачная регистрация: дата рождения в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        long id = generateNextId();
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь успешно создан: id={}, login='{}'", id, user.getLogin());

        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        log.info("Попытка обновления пользователя: id={}", updatedUser.getId());

        if (updatedUser.getId() == null) {
            log.warn("Неудачное обновление: отсутствует id");
            throw new ValidationException("Id должен быть указан");
        }

        User existingUser = users.get(updatedUser.getId());
        if (existingUser == null) {
            log.warn("Неудачное обновление: пользователь с id={} не найден", updatedUser.getId());
            throw new ValidationException("Пользователь с указанным id не найден");
        }

        if (updatedUser.getEmail() != null) {
            if (updatedUser.getEmail().isBlank() || !updatedUser.getEmail().contains("@")) {
                log.warn("Неудачное обновление: некорректный email");
                throw new ValidationException("Email должен содержать символ @");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getLogin() != null) {
            if (updatedUser.getLogin().isBlank() || updatedUser.getLogin().contains(" ")) {
                log.warn("Неудачное обновление: логин содержит пробелы или пустой");
                throw new ValidationException("Логин не может содержать пробелы и быть пустым");
            }
            existingUser.setLogin(updatedUser.getLogin());
        }

        if (updatedUser.getName() != null) {
            if (!updatedUser.getName().isBlank()) {
                existingUser.setName(updatedUser.getName());
            } else {
                existingUser.setName(existingUser.getLogin()); // Если явно передали пустую строку
                log.debug("Имя очищено, установлено равным логину");
            }
        }

        if (updatedUser.getBirthday() != null) {
            if (updatedUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Неудачное обновление: дата рождения в будущем");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(updatedUser.getBirthday());
        }

        log.info("Пользователь успешно обновлён: id='{}'", existingUser.getId());
        return existingUser;
    }

    private long generateNextId() {
        return users.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

}
