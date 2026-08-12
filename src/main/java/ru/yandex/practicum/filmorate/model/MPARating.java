package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MPARating {
    private int id;
    private String name;
    private String description;
}
