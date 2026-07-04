package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films;

    public FilmController() {
        this(new HashMap<>());
    }

    public FilmController(Map<Long, Film> films) {
        this.films = films;
    }

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.debug("Запрос на получение всех фильмов. Всего фильмов: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Попытка создания фильма: name='{}'", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Неудачная попытка создания фильма: название пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Неудачная попытка создания фильма: описание длиннее 200 символов");
            throw new ValidationException("Описание не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Неудачная попытка создания фильма: дата релиза отсутствует");
            throw new ValidationException("Дата релиза обязательна");
        }

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Неудачная попытка создания фильма: дата релиза раньше 1895-12-28");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Неудачная попытка создания фильма: продолжительность не положительная");
            throw new ValidationException("Продолжительность должна быть больше 0");
        }

        long id = generateNextId();
        film.setId(id);
        films.put(id, film);
        log.info("Фильм успешно создан: id='{}', name='{}'", id, film.getName());

        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        log.info("Попытка обновления фильма: id={}", updatedFilm.getId());

        if (updatedFilm.getId() == null) {
            log.warn("Неудачное обновление: отсутствует id");
            throw new ValidationException("ID должен быть указан");
        }

        Film existingFilm = films.get(updatedFilm.getId());
        if (existingFilm == null) {
            log.warn("Неудачное обновление: фильм с id={} не найден", updatedFilm.getId());
            throw new ValidationException(String.format("Фильм с указанным ID = %d не найден", updatedFilm.getId()));
        }

        if (updatedFilm.getName() != null) {
            if (updatedFilm.getName().isBlank()) {
                log.warn("Неудачное обновление: название пустое");
                throw new ValidationException("Название фильма не может быть пустым");
            }
            existingFilm.setName(updatedFilm.getName());
        }

        if (updatedFilm.getDuration() != null) {
            if (updatedFilm.getDescription().length() > 200) {
                log.warn("Неудачное обновление: описание длиннее 200 символов");
                throw new ValidationException("Описание не может быть длиннее 200 символов");
            }
            existingFilm.setDescription(updatedFilm.getDescription());
        }

        if (updatedFilm.getReleaseDate() != null) {
            if (updatedFilm.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                log.warn("Неудачное обновление: дата релиза слишком ранняя");
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
            existingFilm.setReleaseDate(updatedFilm.getReleaseDate());
        }

        if (updatedFilm.getDuration() != null) {
            if (updatedFilm.getDuration() <= 0) {
                log.warn("Неудачное обновление: продолжительность не положительная");
                throw new ValidationException("Продолжительность должна быть положительным числом");
            }
            existingFilm.setDuration(updatedFilm.getDuration());
        }

        return  existingFilm;
    }

    private long generateNextId() {
        return films.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}

