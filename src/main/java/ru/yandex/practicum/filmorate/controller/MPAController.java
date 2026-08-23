package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.db.MPADbStorage;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MPAController {

    private final MPADbStorage mpaStorage;

    public MPAController(MPADbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @GetMapping
    public Collection<MPARating> getAll() {
        log.debug("Запрос всех рейтингов MPA");
        return mpaStorage.getAll();
    }

    @GetMapping("/{id}")
    public MPARating getById(@PathVariable Integer id) {
        log.info("Запрос рейтинга MPA по id={}", id);
        return mpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID = " + id + " не найден"));
    }
}
