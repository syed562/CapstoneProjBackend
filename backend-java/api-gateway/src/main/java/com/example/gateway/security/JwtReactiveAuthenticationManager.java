package com.example.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenService jwtTokenService;

    public JwtReactiveAuthenticationManager(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials() != null ? authentication.getCredentials().toString() : null;
        if (token == null) {
            return Mono.empty();
        }

        if (!jwtTokenService.validate(token)) {
            return Mono.empty();
        }

        Claims claims = jwtTokenService.parse(token);
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId,
                token,
                Collections.singletonList(authority)
        );
        auth.setDetails(claims);
        return Mono.just(auth);
    }
}
