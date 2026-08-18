package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.db.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikeDbStorage likeStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, LikeDbStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;
    }

    public void likeFilm(Long filmId, Long userId) {
        filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID = " + filmId + " не найден"));
        likeStorage.addLike(filmId, userId);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID = " + filmId + " не найден"));
        likeStorage.removeLike(filmId, userId);
    }

    public List<Film> getTopFilms(int count) {
        List<Long> topIds = likeStorage.getTopFilmIds(count);

        List<Film> result = topIds.stream()
                .map(filmStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (result.size() < count) {
            Set<Long> already = result.stream().map(Film::getId).collect(Collectors.toSet());
            filmStorage.getAll().stream()
                    .filter(f -> !already.contains(f.getId()))
                    .limit(count - result.size())
                    .forEach(result::add);
        }

        return result;
    }
}
