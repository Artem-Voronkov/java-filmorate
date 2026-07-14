package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User create(User user) {
        long id = nextId.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        log.debug("Создан пользователь: id={}, login='{}'", id, user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("ID должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new IllegalArgumentException("Пользователь с ID = " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.debug("Обновлён пользователь: id={}", user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
        log.debug("Удалён пользователь: id={}", id);
    }
}
