package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null) {
            film.setDescription("");
        }
        if (film.getReleaseDate() == null) {
            throw new IllegalArgumentException("Дата релиза обязательна");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new IllegalArgumentException("Продолжительность должна быть больше 0");
        }

        long id = generateNextId();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        if (updatedFilm.getId() == null) {
            throw new IllegalArgumentException("ID должен быть указан");
        }
        Film existingFilm = films.get(updatedFilm.getId());
        if (existingFilm == null) {
            throw new IllegalArgumentException("Фильм с указанным ID не найден");
        }

        existingFilm.setName(updatedFilm.getName());
        existingFilm.setDescription(updatedFilm.getDescription());
        existingFilm.setReleaseDate(updatedFilm.getReleaseDate());
        existingFilm.setDuration(updatedFilm.getDuration());

        return  existingFilm;
    }

    private long generateNextId() {
        return films.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}

