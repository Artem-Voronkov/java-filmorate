package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    private Film film1;
    private Film film2;

    private long userId1;
    private long userId2;
    private long userId3;

    @BeforeEach
    void setUp() {
        // Фильмы
        film1 = createTestFilm("Фильм 1", LocalDate.of(1999, 1, 1), 90);
        film2 = createTestFilm("Фильм 2", LocalDate.of(2000, 2, 2), 120);

        // Пользователи — получаем их реальные ID
        userId1 = createTestUser("u1@test.com", "login1", "User One").getId();
        userId2 = createTestUser("u2@test.com", "login2", "User Two").getId();
        userId3 = createTestUser("u3@test.com", "login3", "User Three").getId();
    }

    private Film createTestFilm(String name, LocalDate releaseDate, int duration) {
        Film f = new Film();
        f.setName(name);
        f.setDescription(name);
        f.setReleaseDate(releaseDate);
        f.setDuration(duration);
        return filmService.createFilm(f);
    }

    private User createTestUser(String email, String login, String name) {
        User u = new User();
        u.setEmail(email);
        u.setLogin(login);
        u.setName(name);
        u.setBirthday(LocalDate.of(1980, 1, 1));
        return userService.createUser(u);
    }

    @Test
    void likeAndUnlikeSuccess() {
        long filmId = film1.getId();
        long userId = userId1;

        filmService.likeFilm(filmId, userId);
        assertThat(filmService.getLikesCount(filmId)).isEqualTo(1);

        filmService.unlikeFilm(filmId, userId);
        assertThat(filmService.getLikesCount(filmId)).isEqualTo(0);
    }

    @Test
    void unlikeWithoutLikeThrowsValidation() {
        long filmId = film1.getId();
        long userId = userId1;

        assertThatThrownBy(() -> filmService.unlikeFilm(filmId, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не ставил лайк этому фильму");
    }

    @Test
    void popularFilmsSortByLikes() {
        long f1 = film1.getId();
        long f2 = film2.getId();

        filmService.likeFilm(f1, userId1);
        filmService.likeFilm(f2, userId2);
        filmService.likeFilm(f2, userId3);

        List<Long> popular = filmService.getPopularFilms(2);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0)).isEqualTo(f2);
        assertThat(popular.get(1)).isEqualTo(f1);
    }

    @Test
    void updateFilmValidSuccess() {
        String newName = "Обновляем имя фильма";
        film1.setName(newName);
        Film updated = filmService.updateFilm(film1);
        assertThat(updated.getName()).isEqualTo(newName);
    }

    @Test
    void updateFilmNonExistingThrowsNotFound() {
        Film fake = new Film();
        fake.setId(9999L);
        fake.setName("Неправильное");

        assertThatThrownBy(() -> filmService.updateFilm(fake))
                .isInstanceOf(NotFoundException.class);
    }
}
