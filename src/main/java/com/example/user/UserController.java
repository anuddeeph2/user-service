package com.example.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User request) {
        String id = UUID.randomUUID().toString();
        User user = new User(id, request.getUsername(), request.getEmail());
        users.put(id, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable String id) {
        User user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
