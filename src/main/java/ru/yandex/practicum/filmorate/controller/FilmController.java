package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;

    public FilmController(FilmStorage filmStorage, FilmService filmService, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.userStorage = userStorage;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Запрос всех фильмов");
        return List.copyOf(filmStorage.getAll());
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрос фильма по id={}", id);
        var film = filmStorage.getById(id);
        if (film.isEmpty()) {
            throw new NotFoundException("Фильм с ID = " + id + " не найден");
        }
        return film.get();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Попытка создания фильма: name='{}'", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
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

        Film created = filmStorage.create(film);
        log.info("Фильм успешно создан: id={}, name='{}'", created.getId(), created.getName());
        return created;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        log.info("Попытка обновления фильма: id={}", updatedFilm.getId());

        if (updatedFilm.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        var existing = filmStorage.getById(updatedFilm.getId());
        if (existing.isEmpty()) {
            throw new NotFoundException("Фильм с указанным ID = " + updatedFilm.getId() + " не найден");
        }

        if (updatedFilm.getName() != null) {
            if (updatedFilm.getName().isBlank()) {
                throw new ValidationException("Название фильма не может быть пустым");
            }
        }
        if (updatedFilm.getDescription() != null && updatedFilm.getDescription().length() > 200) {
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }
        if (updatedFilm.getReleaseDate() != null && updatedFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (updatedFilm.getDuration() != null && updatedFilm.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        Film updated = filmStorage.update(updatedFilm);
        log.info("Фильм успешно обновлён: id='{}'", updated.getId());
        return updated;
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Лайк: фильм={}, пользователь={}", id, userId);

        // 1. Проверяем существование фильма
        var film = filmStorage.getById(id);
        if (film.isEmpty()) {
            throw new NotFoundException("Фильм с ID = " + id + " не найден");
        }

        var user = userStorage.getById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден");
        }

        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Убрать лайк: фильм={}, пользователь={}", id, userId);

        var film = filmStorage.getById(id);
        if (film.isEmpty()) {
            throw new NotFoundException("Фильм с ID = " + id + " не найден");
        }

        var user = userStorage.getById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден");
        }

        filmService.unlikeFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }
        log.debug("Запрос топ-{} фильмов по лайкам", count);
        List<Film> top = filmService.getTop10Films();
        return top.size() > count ? top.subList(0, count) : top;
    }
}
