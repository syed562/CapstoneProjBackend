package com.example.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class DownstreamUserHeaderFilter implements WebFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    Object details = auth.getDetails();
                    if (details instanceof Claims) { // Java 8 compatible instanceof
                        Claims claims = (Claims) details;
                        ServerWebExchange mutated = exchange.mutate()
                                .request(builder -> builder
                                        .header("X-User-Id", auth.getName())
                                        .header("X-User-Role", claims.get("role", String.class))
                                        .header("X-User-Name", claims.get("username", String.class))
                                )
                                .build();
                        return chain.filter(mutated);
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // Run immediately after authorization so claims are available
        return SecurityWebFiltersOrder.AUTHORIZATION.getOrder() + 1;
    }
}
