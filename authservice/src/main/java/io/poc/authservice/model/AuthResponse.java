package io.poc.authservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthResponse {
    private String username;
    private String message;
    private String token;
}
