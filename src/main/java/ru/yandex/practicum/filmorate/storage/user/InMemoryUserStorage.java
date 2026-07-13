package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserStorage implements UserStorage {

    // Хранилище пользователей: ID -> User
    private final Map<Long, User> users = new HashMap<>();

    // Хранилище дружбы: ID пользователя -> Set ID его друзей
    // Используем HashSet для быстрого поиска и отсутствия дублей
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    // Генератор ID (начинается с 1 и увеличивается атомарно)
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }

        long id = nextId.getAndIncrement();
        user.setId(id);

        users.put(id, user);

        // Инициализируем пустой список друзей для нового пользователя
        friends.putIfAbsent(id, new HashSet<>());

        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            return null; // Пусть сервис обработает это как ошибку
        }

        Long id = user.getId();
        if (!users.containsKey(id)) {
            return null;
        }

        // Обновляем данные пользователя, но НЕ трогаем список друзей здесь
        // (дружба управляется отдельно через add/remove)
        users.put(id, user);
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

    /**
     * Возвращает изменяемый Set друзей.
     * ВАЖНО: UserService напрямую модифицирует этот Set (add/remove).
     * Это допустимо в рамках in-memory реализации для упрощения логики двусторонности.
     */
    @Override
    public Set<Long> getFriends(Long userId) {
        // Гарантируем, что у любого существующего пользователя есть набор друзей
        if (!friends.containsKey(userId)) {
            friends.put(userId, new HashSet<>());
        }
        return friends.get(userId);
    }
}
