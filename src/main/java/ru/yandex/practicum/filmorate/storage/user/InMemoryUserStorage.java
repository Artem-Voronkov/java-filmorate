package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
//Импорты все

@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    private final Map<Long, Set<Long>> friends = new HashMap<>();

    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        long id = nextId.getAndIncrement();
        user.setId(id);

        users.put(id, user);

        friends.putIfAbsent(id, new HashSet<>());

        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }

        Long id = user.getId();
        if (!users.containsKey(id)) {
            return null;
        }
        return user;
    }

    @Override
    public User getUserById(Long id) {
        return users.get(id);
    }

    @Override
    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        if (!friends.containsKey(userId)) {
            friends.put(userId, new HashSet<>());
        }
        return friends.get(userId);
    }
}
