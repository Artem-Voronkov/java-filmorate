package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Film create(Film film) {
        long id = nextId.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        log.debug("Создан фильм: id={}, name='{}'", id, film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new IllegalArgumentException("ID должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            throw new IllegalArgumentException("Фильм с ID = " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.debug("Обновлён фильм: id={}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAll() {
        return Collections.unmodifiableCollection(films.values());
    }

    @Override
    public void deleteById(Long id) {
        films.remove(id);
        log.debug("Удалён фильм: id={}", id);
    }
}
