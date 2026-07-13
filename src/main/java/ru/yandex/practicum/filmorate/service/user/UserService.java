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

    // Внедрение зависимости через конструктор
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        User user = getUserOrFail(userId);
        User friend = getUserOrFail(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserOrFail(userId);
        User friend = getUserOrFail(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = getUserOrFail(userId);
        return user.getFriends().stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new ValidationException(
                                "Друг с ID = " + id + " не найден")))
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId1, Long userId2) {
        User u1 = getUserOrFail(userId1);
        User u2 = getUserOrFail(userId2);

        Set<Long> commonIds = new HashSet<>(u1.getFriends());
        commonIds.retainAll(u2.getFriends());

        return commonIds.stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new ValidationException(
                                "Общий друг с ID = " + id + " не найден")))
                .collect(Collectors.toList());
    }

    private User getUserOrFail(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new ValidationException(
                        "Пользователь с ID = " + id + " не найден"));
    }
}
