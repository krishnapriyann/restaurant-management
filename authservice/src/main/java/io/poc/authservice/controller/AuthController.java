package io.poc.authservice.controller;

import io.poc.authservice.model.AuthRequest;
import io.poc.authservice.model.AuthResponse;
import io.poc.authservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth-service")
public class AuthController {

    private final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest requestUserCred) {
        log.info("AuthController::login()");

        AuthResponse result = authService.login(requestUserCred);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@RequestBody AuthRequest userCred) {
        log.info("AuthController::signUp()");

        AuthResponse response = authService.register(userCred);

        return ResponseEntity.ok(response);
    }
}
