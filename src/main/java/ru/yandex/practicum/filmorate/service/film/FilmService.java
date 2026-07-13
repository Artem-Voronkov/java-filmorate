package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void likeFilm(Long filmId, Long userId) {
        Film film = getFilmOrFail(filmId);
        film.getLikes().add(userId); // Set гарантирует уникальность
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        Film film = getFilmOrFail(filmId);
        boolean removed = film.getLikes().remove(userId);
        if (!removed) {
            log.warn("Попытка убрать лайк: пользователь {} не ставил лайк фильму {}", userId, filmId);
        } else {
            log.info("Пользователь {} убрал лайк у фильма {}", userId, filmId);
        }
    }

    public List<Film> getTop10Films() {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private Film getFilmOrFail(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new ValidationException(
                        "Фильм с ID = " + id + " не найден"));
    }
}
