package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Запрос на получение списка пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Попытка создания пользователя: login='{}'", user.getLogin());
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Попытка обновления пользователя: id={}", user.getId());
        return userService.updateUser(user);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public List<Long> addFriend(@PathVariable long userId, @PathVariable long friendId) {
        log.info("Добавление дружбы: {} <-> {}", userId, friendId);
        userService.addFriend(userId, friendId);
        return userService.getFriends(userId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public List<Long> removeFriend(@PathVariable long userId, @PathVariable long friendId) {
        log.info("Удаление дружбы: {} <-> {}", userId, friendId);
        userService.removeFriend(userId, friendId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends")
    public List<Long> getFriends(@PathVariable long userId) {
        log.debug("Получение списка друзей для пользователя {}", userId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId1}/friends/common/{userId2}")
    public List<Long> getCommonFriends(@PathVariable long userId1, @PathVariable long userId2) {
        log.debug("Поиск общих друзей между {} и {}", userId1, userId2);
        return userService.getCommonFriends(userId1, userId2);
    }
}
