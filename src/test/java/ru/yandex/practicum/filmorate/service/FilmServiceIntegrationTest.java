package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    @Autowired
    private InMemoryFilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage.clear();
    }

    @Test
    void createFilmValidSuccess() {
        var film = createValidFilm();
        Film result = filmService.createFilm(film);
        assertNotNull(result.getId());
        assertEquals("Test Film", result.getName());
    }

    @Test
    void createFilmInvalidNameThrows() {
        var film = new Film();
        film.setName("");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(90);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmService.createFilm(film));
        assertTrue(ex.getMessage().contains("Название фильма не может быть пустым"));
    }

    @Test
    void likeAndGetPopularWorks() {
        var film = createValidFilm();
        Film createdFilm = filmService.createFilm(film);

        var u1 = new User();
        u1.setEmail("u1@test.com");
        u1.setLogin("user1");
        u1.setName("User 1");
        u1.setBirthday(LocalDate.of(1990, 1, 1));
        User user1 = userService.createUser(u1);

        var u2 = new User();
        u2.setEmail("u2@test.com");
        u2.setLogin("user2");
        u2.setName("User 2");
        u2.setBirthday(LocalDate.of(1991, 2, 2));
        User user2 = userService.createUser(u2);

        filmService.likeFilm(createdFilm.getId(), user1.getId());
        filmService.likeFilm(createdFilm.getId(), user2.getId());

        List<Long> popular = filmService.getPopularFilms(10);
        assertEquals(1, popular.size());
        assertEquals(createdFilm.getId(), popular.get(0));
    }

    private Film createValidFilm() {
        var f = new Film();
        f.setName("Test Film");
        f.setDescription("Description");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(120);
        return f;
    }
}
