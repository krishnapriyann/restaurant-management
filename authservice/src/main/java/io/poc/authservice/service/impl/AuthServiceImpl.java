package io.poc.authservice.service.impl;

import io.poc.authservice.entity.UserCred;
import io.poc.authservice.model.AuthRequest;
import io.poc.authservice.model.AuthResponse;
import io.poc.authservice.repository.AuthRepository;
import io.poc.authservice.service.AuthService;
import io.poc.authservice.utils.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final AuthRepository authRepository;

    public AuthServiceImpl(JwtUtil jwtUtil, AuthRepository authRepository) {
        this.jwtUtil = jwtUtil;
        this.authRepository = authRepository;
    }

    @Override
    public AuthResponse login(AuthRequest userCred) {
        return null;
    }

    @Override
    public AuthResponse register(AuthRequest userCred) {
        UserCred user = UserCred.builder()
                .username(userCred.getUsername())
                .password(userCred.getPassword())
                .build();
        authRepository.save(user);
        return null;
    }
}
