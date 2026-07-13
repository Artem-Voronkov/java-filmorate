package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    User getUserById(Long id);

    Collection<User> getAllUsers();

    Set<Long> getFriends(Long userId);
}
