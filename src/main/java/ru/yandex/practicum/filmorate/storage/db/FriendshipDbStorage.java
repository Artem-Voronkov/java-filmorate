package ru.yandex.practicum.filmorate.storage.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.db.mapper.UserRowMapper;

import java.util.Collection;

@Repository
@Qualifier("friendshipDbStorage")
public class FriendshipDbStorage {

    private static final Logger log = LoggerFactory.getLogger(FriendshipDbStorage.class);
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
        try {
            jdbcTemplate.update(sql, userId, friendId);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.debug("Дружба между пользователем {} и {} уже существует", userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public Collection<Long> getFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    public Collection<Long> getCommonFriendIds(Long userId, Long otherId) {
        String sql = """
            SELECT f1.friend_id FROM friendships f1
            INNER JOIN friendships f2 ON f1.friend_id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;
        return jdbcTemplate.queryForList(sql, Long.class, userId, otherId);
    }
}
