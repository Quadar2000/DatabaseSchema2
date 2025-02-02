package com.test.demo.controllers.userController;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.test.demo.entities.user.UserDTO;
import com.test.demo.requests.userRegistrationRequest.UserRegistrationRequest;
import com.test.demo.services.userService.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegistrationRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok().body(Map.of("message", "User created successfully"));
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/get-users")
    public ResponseEntity<?> getUsers() {
        try {
            List <UserDTO> users = userService.getUsers();
            return ResponseEntity.ok().body(Map.of("users", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/get-profile")
    public ResponseEntity<?> getProfile(@RequestParam String id) {
        try {
            UserDTO user = userService.getUser(id);
            return ResponseEntity.ok().body(Map.of("user", user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                     .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestParam String id) {
        try {
            System.out.print("id: " + id + "\n");
            userService.deleteUserById(id);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            // Pobierz aktualnie zalogowanego użytkownika
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = authentication.getName();

            // Pobierz nowe hasło z żądania
            String newPassword = request.get("newPassword");

            // Zmień hasło
            userService.updatePassword(currentUserEmail, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred"));
        }
    }
}

