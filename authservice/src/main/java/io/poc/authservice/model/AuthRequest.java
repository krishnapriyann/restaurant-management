package io.poc.authservice.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuthRequest {
    private String username;
    private String password;
}
