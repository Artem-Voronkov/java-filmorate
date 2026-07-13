package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        validateUser(user);
        User created = userStorage.createUser(user);
        log.info("Создание пользователя: login='{}'", created.getLogin());
        return created;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new NotFoundException("ID пользователя не может быть null при обновлении");
        }
        validateUser(user);
        User updated = userStorage.updateUser(user);
        if (updated == null) {
            throw new NotFoundException("Пользователь с ID = " + user.getId() + " не найден");
        }
        log.info("Обновление пользователя: id={}, login='{}'", updated.getId(), updated.getLogin());
        return updated;
    }

    public User getUserById(long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }
        return user;
    }

    public List<User> getAllUsers() {
        var all = userStorage.getAllUsers();
        if (all == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(all);
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        getUserById(userId);
        getUserById(friendId);

        Set<Long> userFriends = userStorage.getFriends(userId);
        Set<Long> friendFriends = userStorage.getFriends(friendId);

        boolean addedToUser = userFriends.add(friendId);
        boolean addedToFriend = friendFriends.add(userId);

        if (!addedToUser && !addedToFriend) {
            log.debug("Пользователи {} и {} уже являются друзьями", userId, friendId);
            return;
        }

        log.info("Добавлена дружба: пользователь {} и пользователь {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        getUserById(userId);
        getUserById(friendId);

        Set<Long> userFriends = userStorage.getFriends(userId);
        Set<Long> friendFriends = userStorage.getFriends(friendId);

        boolean removedFromUser = userFriends.remove(friendId);
        boolean removedFromFriend = friendFriends.remove(userId);

        if (!removedFromUser && !removedFromFriend) {
            log.debug("У пользователей {} и {} нет дружбы для удаления", userId, friendId);
            return;
        }

        log.info("Удалена дружба: пользователь {} и пользователь {}", userId, friendId);
    }

    public List<Long> getFriends(long userId) {
        getUserById(userId);
        return new ArrayList<>(userStorage.getFriends(userId));
    }

    public List<Long> getCommonFriends(long userId1, long userId2) {
        getUserById(userId1);
        getUserById(userId2);

        Set<Long> friends1 = userStorage.getFriends(userId1);
        Set<Long> friends2 = userStorage.getFriends(userId2);

        return friends1.stream()
                .filter(friends2::contains)
                .sorted()
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new NotFoundException("Email не может быть пустым");
        } else if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new NotFoundException("Login не может быть пустым");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null) {
            throw new NotFoundException("Дата рождения не может быть пустой");
        }
    }
}
