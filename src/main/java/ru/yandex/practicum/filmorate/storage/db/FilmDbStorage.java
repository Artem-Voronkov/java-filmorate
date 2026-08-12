package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.db.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
@Qualifier("filmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public void deleteById(Long id) {
        if (!getById(id).isPresent()) {
            throw new NotFoundException("Фильм с ID = " + id + " не найден");
        }

        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT id, name, description, release_date, duration, mpa_id FROM films ORDER BY id";
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = "SELECT id, name, description, release_date, duration, mpa_id FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            return Optional.of(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        if (film.getMpaId() == null || film.getMpaId() <= 0) {
            throw new IllegalArgumentException("mpaId is required and must be positive");
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId()
        );

        Long generatedId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM films", Long.class);
        film.setId(generatedId);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaId(),
                film.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с ID = " + film.getId() + " не найден");
        }

        return film;
    }
}
