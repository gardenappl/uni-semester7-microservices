package ua.knu.carrental.users.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.knu.carrental.users.model.User;
import ua.knu.carrental.users.service.UserService;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Data
    public static class LoginRequest {
        public String username;
        public String password;
    }

    @Data
    @AllArgsConstructor
    private static class LoginResponse {
        public String token;
        public boolean shouldUseAdminFrontend;
    }

    @PostMapping("users/login")
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        String token = userService.logIn(request.username, request.password);
        return ResponseEntity.ok(new LoginResponse(
                token,
                userService.shouldEnableAdminFrontend(request.username)
        ));
    }

    @Data
    public static class RegisterRequest {
        public long passportId;
        public String username;
        public String password;
    }

    @PostMapping("users/register")
    public ResponseEntity<LoginResponse> registerAndLogin(@Validated @RequestBody RegisterRequest request) {
        String token = userService.registerAndLogIn(request.passportId, request.username, request.password);
        return ResponseEntity.ok(new LoginResponse(
                token,
                userService.shouldEnableAdminFrontend(request.username)
        ));
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<User> get(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("users/id/{keycloakId}")
    public ResponseEntity<Long> getByKeycloakId(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.getUserByKeycloakId(keycloakId).getPassportId());
    }
}
