package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {

    private final GenreDbStorage genreStorage;

    public GenreController(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public Collection<Genre> getAll() {
        log.debug("Запрос всех жанров");
        return genreStorage.getAll();
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable Integer id) {
        log.info("Запрос жанра по id={}", id);
        return genreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID = " + id + " не найден"));
    }
}
