package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private FilmController filmController;
    private Map<Long, Film> testStorage;

    @BeforeEach
    void setUp() {
        testStorage = new HashMap<>();
        filmController = new FilmController(testStorage);
    }

    @Test
    void createFilmSuccess() {
        var film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Когда засуха и вымирание приводят к продовольственному кризису");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film result = filmController.createFilm(film);

        assertNotNull(result.getId());
        assertEquals("Интерстеллар", result.getName());
        assertTrue(testStorage.containsKey(result.getId()));
    }

    @Test
    void createFilmNameBlankThrowsValidation() {
        var film = new Film();
        film.setName("");
        film.setDescription("Какое-то описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(90);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.createFilm(film)
        );
        assertTrue(ex.getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void createFilmDescriptionTooLongThrowsValidation() {
        var film = new Film();
        film.setName("Название фильма");
        film.setDescription("х".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(90);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.createFilm(film)
        );
        assertTrue(ex.getMessage().contains("Описание не может быть длиннее 200 символов"));
    }

    @Test
    void createFilmReleaseDateBeforeMinThrowsValidation() {
        var film = new Film();
        film.setName("Старый фильм");
        film.setDescription("Очень старый");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(60);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.createFilm(film)
        );
        assertTrue(ex.getMessage().contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilmDurationNegativeThrowsValidation() {
        var film = new Film();
        film.setName("Какое-то название");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.createFilm(film)
        );
        assertTrue(ex.getMessage().contains("Продолжительность должна быть больше 0"));
    }

    @Test
    void updateFilmSuccess() {
        var existingFilm = new Film();
        existingFilm.setId(1L);
        existingFilm.setName("Название фильма");
        existingFilm.setDescription("Описание фильма");
        existingFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        existingFilm.setDuration(100);
        testStorage.put(1L, existingFilm);

        var updateFilm = new Film();
        updateFilm.setId(1L);
        updateFilm.setName("Обновленное название");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(2001, 2, 2));
        updateFilm.setDuration(120);

        Film result = filmController.updateFilm(updateFilm);

        assertEquals(1L, result.getId());
        assertEquals("Обновленное название", result.getName());
        assertEquals("Обновленное описание", result.getDescription());
        assertEquals(120, result.getDuration());
    }

    @Test
    void updateFilmIdMissingThrowsValidation() {
        var film = new Film();
        film.setName("Новое название");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.updateFilm(film)
        );
        assertTrue(ex.getMessage().contains("ID должен быть указан"));
    }

    @Test
    void updateFilmNonExistingIdThrowsValidation() {
        var film = new Film();
        film.setId(9999L);
        film.setName("Другое название");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.updateFilm(film)
        );
        assertTrue(ex.getMessage().contains("Фильм с указанным ID = 9999 не найден"));
    }

    @Test
    void updateFilmNameBlankThrowsValidation() {
        var existingFilm = new Film();
        existingFilm.setId(2L);
        existingFilm.setName("Название");
        existingFilm.setDescription("Описание");
        existingFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        existingFilm.setDuration(90);
        testStorage.put(2L, existingFilm);

        var updateFilm = new Film();
        updateFilm.setId(2L);
        updateFilm.setName("");

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmController.updateFilm(updateFilm)
        );
        assertTrue(ex.getMessage().contains("Название фильма не может быть пустым"));
    }
}

