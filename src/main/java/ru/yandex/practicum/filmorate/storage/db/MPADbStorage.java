package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.db.mapper.MPARatingRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@Qualifier("mpaDbStorage")
public class MPADbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MPARatingRowMapper mpaRowMapper;

    public MPADbStorage(JdbcTemplate jdbcTemplate, MPARatingRowMapper mpaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRowMapper = mpaRowMapper;
    }

    public Collection<MPARating> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    public Optional<MPARating> getById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, mpaRowMapper, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
