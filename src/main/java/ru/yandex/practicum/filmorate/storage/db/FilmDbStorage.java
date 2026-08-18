package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.db.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("filmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {

    private static final String SELECT_FILM_SQL =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                    "m.name AS mpa_name, m.description AS mpa_description " +
                    "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id ";

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
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
        String sql = SELECT_FILM_SQL + "ORDER BY f.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(film -> film.setGenres(loadGenres(film.getId())));
        return films;
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = SELECT_FILM_SQL + "WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            film.setGenres(loadGenres(id));
            return Optional.of(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() <= 0) {
            throw new IllegalArgumentException("mpa is required and must be positive");
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );

        Long generatedId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM films", Long.class);
        film.setId(generatedId);
        saveGenres(film);

        return getById(generatedId)
                .orElseThrow(() -> new IllegalStateException("Не удалось создать фильм"));
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
                film.getMpa().getId(),
                film.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с ID = " + film.getId() + " не найден");
        }

        saveGenres(film);

        return getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID = " + film.getId() + " не найден"));
    }

    private Set<Genre> loadGenres(Long filmId) {
        String sql = "SELECT g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, genreRowMapper, filmId));
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(
                        "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }
    }
}
