package io.poc.authservice.service;

import io.poc.authservice.model.AuthRequest;
import io.poc.authservice.model.AuthResponse;

public interface AuthService {

    AuthResponse login(AuthRequest userCred);

    AuthResponse register(AuthRequest userCred);
}
