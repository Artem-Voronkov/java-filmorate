package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new IllegalArgumentException("Описание не может быть длиннее 200 символов");
        }
        if (film.getReleaseDate() == null) {
            throw new IllegalArgumentException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new IllegalArgumentException("Продолжительность должна быть больше 0");
        }

        long id = generateNextId();
        film.setId(id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new IllegalArgumentException("ID должен быть указан");
        }
        Optional<Film> existing = findById(film.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException(String.format("Фильм с указанным ID = %d не найден", film.getId()));
        }
        Film existingFilm = existing.get();

        if (film.getName() != null) {
            if (film.getName().isBlank()) {
                throw new IllegalArgumentException("Название фильма не может быть пустым");
            }
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                throw new IllegalArgumentException("Описание не может быть длиннее 200 символов");
            }
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
                throw new IllegalArgumentException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                throw new IllegalArgumentException("Продолжительность должна быть положительным числом");
            }
            existingFilm.setDuration(film.getDuration());
        }

        return existingFilm;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAll() {
        return Collections.unmodifiableCollection(films.values());
    }

    @Override
    public void deleteById(Long id) {
        films.remove(id);
    }

    private long generateNextId() {
        return films.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}
