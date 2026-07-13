package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        log.debug("Запрос на получение всех фильмов");
        return new ArrayList<>(filmStorage.getAll());
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: name='{}'", film.getName());
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: id={}", film.getId());
        return filmStorage.update(film);
    }

    public void likeFilm(long filmId, long userId) {
        ensureFilmExists(filmId);
        ensureUserExists(userId);

        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void unlikeFilm(long filmId, long userId) {
        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes == null || !filmLikes.contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }
        filmLikes.remove(userId);
        if (filmLikes.isEmpty()) {
            likes.remove(filmId);
        }
        log.debug("Пользователь {} убрал лайк у фильма {}", userId, filmId);
    }

    public List<Long> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> {
                    int likes1 = getLikesCount(f1.getId());
                    int likes2 = getLikesCount(f2.getId());
                    if (likes1 != likes2) {
                        return Integer.compare(likes2, likes1); // по убыванию лайков
                    }
                    return Long.compare(f1.getId(), f2.getId()); // при равенстве — по ID
                })
                .limit(count)
                .map(Film::getId)
                .collect(Collectors.toList());
    }

    public int getLikesCount(Long filmId) {
        return likes.getOrDefault(filmId, Collections.emptySet()).size();
    }

    private void ensureFilmExists(long id) {
        if (!filmStorage.findById(id).isPresent()) {
            throw new NotFoundException("Фильм с ID = " + id + " не найден");
        }
    }

    private void ensureUserExists(long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }
    }
}
