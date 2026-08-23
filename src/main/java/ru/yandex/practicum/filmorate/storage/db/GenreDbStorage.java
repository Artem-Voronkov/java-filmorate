package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.mapper.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@Qualifier("genreDbStorage")
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    public Collection<Genre> getAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    public Optional<Genre> getById(Integer id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, genreRowMapper, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
