package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        return userStorage.update(user);
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        ensureUserExists(userId);
        ensureUserExists(friendId);

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId); // двусторонняя дружба
        log.debug("Добавлена дружба: {} <-> {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
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
            throw new ValidationException(String.format("Пользователь с ID = %d не найден", id));
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
