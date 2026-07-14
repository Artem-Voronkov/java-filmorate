package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(filmStorage);
    }

    @Test
    void likeFilmAddsLike() {
        Film film = createTestFilm();
        long userId = 1L;

        filmService.likeFilm(film.getId(), userId);

        assertEquals(1, film.getLikes().size());
        assertTrue(film.getLikes().contains(userId));
    }

    @Test
    void unlikeFilmRemovesLike() {
        Film film = createTestFilm();
        long userId = 1L;

        filmService.likeFilm(film.getId(), userId);
        filmService.unlikeFilm(film.getId(), userId);

        assertEquals(0, film.getLikes().size());
        assertFalse(film.getLikes().contains(userId));
    }

    @Test
    void unlikeFilmNonExistingLikeDoesNotThrow() {
        Film film = createTestFilm();
        long userId = 1L;
        filmService.unlikeFilm(film.getId(), userId);
        assertEquals(0, film.getLikes().size());
    }

    @Test
    void getTop10FilmsSortsByLikesDescending() {
        Film f1 = createTestFilmWithName("A");
        Film f2 = createTestFilmWithName("B");
        Film f3 = createTestFilmWithName("C");

        filmService.likeFilm(f1.getId(), 1L);
        filmService.likeFilm(f1.getId(), 2L);
        filmService.likeFilm(f1.getId(), 3L);

        filmService.likeFilm(f2.getId(), 1L);
        filmService.likeFilm(f2.getId(), 2L);

        filmService.likeFilm(f3.getId(), 1L);

        List<Film> top = filmService.getTop10Films();
        assertEquals(3, top.size());
        assertEquals("A", top.get(0).getName());
        assertEquals("B", top.get(1).getName());
        assertEquals("C", top.get(2).getName());
    }

    @Test
    void getTop10FilmsEmptyListWhenNoFilms() {
        List<Film> top = filmService.getTop10Films();
        assertTrue(top.isEmpty());
    }

    private Film createTestFilm() {
        Film f = new Film();
        f.setName("Test");
        f.setDescription("Desc");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(90);
        return filmStorage.create(f);
    }

    private Film createTestFilmWithName(String name) {
        Film f = new Film();
        f.setName(name);
        f.setDescription(name + " desc");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(90);
        return filmStorage.create(f);
    }
}
