package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.db.FriendshipDbStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipDbStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendshipDbStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public Collection<User> getFriends(Long userId) {
        return friendshipStorage.getFriendIds(userId).stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void addFriend(Long userId, Long friendId) {
        friendshipStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        friendshipStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        return friendshipStorage.getCommonFriendIds(userId, otherId).stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
