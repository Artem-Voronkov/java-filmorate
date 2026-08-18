package ru.yandex.practicum.filmorate.storagae.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPARating;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.db.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class})
class FilmDbStorageTest {

    @Autowired
    private FilmStorage filmStorage;

    @Test
    void shouldCreateFilmWithMpaId() {
        Film film = Film.builder()
                .name("Inception")
                .description("A mind-bending thriller")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(MPARating.builder().id(3).build())
                .build();

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("Inception");
        assertThat(created.getMpa().getId()).isEqualTo(3);
    }

    @Test
    void shouldUpdateFilm() {
        // Создаём
        Film film = Film.builder()
                .name("Old Name")
                .description("Old desc")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(MPARating.builder().id(1).build())
                .build();

        Film created = filmStorage.create(film);

        // Обновляем
        created.setName("Updated Name");
        created.setDescription("Updated description");
        created.setMpa(MPARating.builder().id(2).build());

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getMpa().getId()).isEqualTo(2);
    }

    @Test
    void shouldFindFilmById() {
        Film film = Film.builder()
                .name("Finding Nemo")
                .description("A fish adventure")
                .releaseDate(LocalDate.of(2003, 5, 30))
                .duration(100)
                .mpa(MPARating.builder().id(2).build())
                .build();

        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.getById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Finding Nemo");
        assertThat(found.get().getMpa().getId()).isEqualTo(2);
    }
}
