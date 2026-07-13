package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email должен быть указан и содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new IllegalArgumentException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            throw new IllegalArgumentException("Дата рождения обязательна");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }

        long id = generateNextId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Id должен быть указан");
        }
        Optional<User> existingOpt = findById(user.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Пользователь с указанным id = %d не найден", user.getId()));
        }
        User existingUser = existingOpt.get();

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new IllegalArgumentException("Email должен содержать символ @");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new IllegalArgumentException("Логин не может содержать пробелы и быть пустым");
            }
            existingUser.setLogin(user.getLogin());
        }

        if (user.getName() != null) {
            if (!user.getName().isBlank()) {
                existingUser.setName(user.getName());
            } else {
                // если явно передали пустую строку — ставим логин
                existingUser.setName(existingUser.getLogin());
            }
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(user.getBirthday());
        }

        return existingUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    private long generateNextId() {
        return users.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}
