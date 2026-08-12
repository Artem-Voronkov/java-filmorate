package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)  // ← ключевое: toBuilder = true
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Integer mpaId;
    private Set<Genre> genres = new HashSet<>();
    private Set<Long> likes = new HashSet<>();
}
