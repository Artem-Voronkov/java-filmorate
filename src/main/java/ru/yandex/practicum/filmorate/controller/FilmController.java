package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.db.MPADbStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final MPADbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmController(FilmStorage filmStorage,
                          FilmService filmService,
                          MPADbStorage mpaStorage,
                          GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Запрос всех фильмов");
        return new ArrayList<>(filmStorage.getAll());
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос фильма по id={}", id);
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID = " + id + " не найден"));
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Создание фильма: name='{}'", film.getName());

        validateFilm(film);

        MPARating mpa = mpaStorage.getById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID = " + film.getMpa().getId() + " не найден"));

        Set<Genre> genres = getValidatedGenres(film.getGenres());

        Film validatedFilm = film.toBuilder()
                .mpa(mpa)
                .genres(genres)
                .build();

        return filmStorage.create(validatedFilm);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        log.info("Обновление фильма: id={}", updatedFilm.getId());

        if (updatedFilm.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        Film existingFilm = filmStorage.getById(updatedFilm.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID = " + updatedFilm.getId() + " не найден"));

        validateFilmUpdate(updatedFilm);

        MPARating mpa = existingFilm.getMpa();
        if (updatedFilm.getMpa() != null) {
            mpa = mpaStorage.getById(updatedFilm.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID = " + updatedFilm.getMpa().getId() + " не найден"));
        }

        Set<Genre> genres = updatedFilm.getGenres() != null
                ? getValidatedGenres(updatedFilm.getGenres())
                : existingFilm.getGenres();

        Film validatedFilm = existingFilm.toBuilder()
                .name(updatedFilm.getName() != null ? updatedFilm.getName() : existingFilm.getName())
                .description(updatedFilm.getDescription() != null ? updatedFilm.getDescription() : existingFilm.getDescription())
                .releaseDate(updatedFilm.getReleaseDate() != null ? updatedFilm.getReleaseDate() : existingFilm.getReleaseDate())
                .duration(updatedFilm.getDuration() != null ? updatedFilm.getDuration() : existingFilm.getDuration())
                .mpa(mpa)
                .genres(genres)
                .build();

        return filmStorage.update(validatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Лайк: фильм={}, пользователь={}", id, userId);
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка: фильм={}, пользователь={}", id, userId);
        filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new ValidationException("Количество должно быть больше 0");
        }
        return filmService.getTopFilms(count);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.info("Удаление фильма: id={}", id);
        filmStorage.deleteById(id);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть больше 0");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("Поле mpa обязательно");
        }
    }

    private void validateFilmUpdate(Film film) {
        if (film.getName() != null && film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть больше 0");
        }
    }

    private Set<Genre> getValidatedGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return new HashSet<>();
        }

        return genres.stream()
                .map(genre -> {
                    if (genre.getId() == 0) {
                        throw new ValidationException("ID жанра не может быть 0");
                    }
                    return genreStorage.getById(genre.getId())
                            .orElseThrow(() -> new NotFoundException("Жанр с ID = " + genre.getId() + " не найден"));
                })
                .collect(Collectors.toSet());
    }
}
