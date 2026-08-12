package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void likeFilm(Long filmId, Long userId) {
        Optional<Film> film = filmStorage.getById(filmId);
        if (film.isEmpty()) return;
        film.get().getLikes().add(userId);
    }

    public void unlikeFilm(Long filmId, Long userId) {
        Optional<Film> film = filmStorage.getById(filmId);
        if (film.isEmpty()) return;
        film.get().getLikes().remove(userId);
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparing(f -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
