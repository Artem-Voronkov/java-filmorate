package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException; // <-- добавь импорт
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userStorage.getAll());
    }

    public User createUser(User user) {
        log.info("Создание пользователя: login='{}'", user.getLogin());
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя: id={}", user.getId());
        if (user.getId() == null) {
            throw new ValidationException("Для обновления пользователя ID обязателен");
        }
        Optional<User> existingOpt = userStorage.findById(user.getId());
        if (existingOpt.isEmpty()) {
            throw new NotFoundException("Пользователь с указанным id = " + user.getId() + " не найден");
        }
        User existingUser = existingOpt.get();

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Email должен содержать символ @");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может содержать пробелы и быть пустым");
            }
            existingUser.setLogin(user.getLogin());
        }

        if (user.getName() != null) {
            if (!user.getName().isBlank()) {
                existingUser.setName(user.getName());
            } else {
                existingUser.setName(existingUser.getLogin());
            }
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(user.getBirthday());
        }

        return existingUser;
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        ensureUserExists(userId);
        ensureUserExists(friendId);

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
        log.debug("Добавлена дружба: {} <-> {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        ensureUserExists(userId);
        ensureUserExists(friendId);

        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> friendFriends = friends.getOrDefault(friendId, Collections.emptySet());

        userFriends.remove(friendId);
        friendFriends.remove(userId);

        cleanEmptySets(userId, friendId);
        log.debug("Дружба удалена: {} <-> {}", userId, friendId);
    }

    public List<Long> getFriends(long userId) {
        ensureUserExists(userId);
        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        log.debug("Список друзей для пользователя {}: {}", userId, userFriends);
        return new ArrayList<>(userFriends);
    }

    public List<Long> getCommonFriends(long userId1, long userId2) {
        ensureUserExists(userId1);
        ensureUserExists(userId2);

        Set<Long> friends1 = friends.getOrDefault(userId1, Collections.emptySet());
        Set<Long> friends2 = friends.getOrDefault(userId2, Collections.emptySet());

        List<Long> common = friends1.stream()
                .filter(friends2::contains)
                .sorted()
                .collect(Collectors.toList());

        log.debug("Общие друзья для {} и {}: {}", userId1, userId2, common);
        return common;
    }

    private void ensureUserExists(long id) {
        if (!userStorage.findById(id).isPresent()) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }
    }

    private void cleanEmptySets(long... ids) {
        for (long id : ids) {
            Set<Long> set = friends.get(id);
            if (set != null && set.isEmpty()) {
                friends.remove(id);
            }
        }
    }
}
