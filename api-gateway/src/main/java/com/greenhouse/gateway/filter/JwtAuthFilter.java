package com.greenhouse.gateway.filter;

import com.greenhouse.gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator",
            "/actuator/health"
            // add more public routes if you want:
            // "/api/auth",
            // "/api/zones"
    );

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        System.out.println("[GATEWAY][JwtAuthFilter] called path=" + path);

        // public path bypass
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        System.out.println("[GATEWAY][JwtAuthFilter] isPublic=" + isPublic);
        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("[GATEWAY][JwtAuthFilter] AUTH HEADER RAW=[" + authHeader + "]");

        String token = extractJwtFromBearerHeader(authHeader);

        if (token == null) {
            System.out.println("[GATEWAY][JwtAuthFilter] No usable bearer token -> 401");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        long dotCount = token.chars().filter(ch -> ch == '.').count();
        int parts = token.split("\\.").length;

        System.out.println("[GATEWAY][JwtAuthFilter] tokenPreview=" + preview(token));
        System.out.println("[GATEWAY][JwtAuthFilter] dotCount=" + dotCount + " parts=" + parts);

        if (parts != 3) {
            System.out.println("[GATEWAY][JwtAuthFilter] Invalid JWT format (expected 3 parts) -> 401");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        boolean ok = jwtUtil.isTokenValid(token);
        System.out.println("[GATEWAY][JwtAuthFilter] isTokenValid=" + ok);

        if (!ok) {
            System.out.println("[GATEWAY][JwtAuthFilter] Token validation failed -> 401");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        System.out.println("[GATEWAY][JwtAuthFilter] Token ok -> proceeding");
        return chain.filter(exchange);
    }

    /**
     * Extracts a usable JWT (JWS) from Authorization: Bearer <token>.
     *
     * Handles common mistakes:
     *  - token wrapped in quotes: "a.b.c"
     *  - multiple tokens pasted with spaces/newlines
     *  - tokens accidentally concatenated (a.b.c.d.e...) -> take the last 3 segments
     */
    private String extractJwtFromBearerHeader(String authHeader) {
        if (authHeader == null) return null;
        if (!authHeader.startsWith("Bearer ")) return null;

        String raw = authHeader.substring(7).trim();

        // strip surrounding quotes
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() > 1) {
            raw = raw.substring(1, raw.length() - 1).trim();
        }

        // if multiple tokens separated by whitespace, use the last piece
        String[] whitespacePieces = raw.split("\\s+");
        raw = whitespacePieces[whitespacePieces.length - 1];

        // if tokens were concatenated, keep last 3 segments
        String[] parts = raw.split("\\.");
        if (parts.length == 3) {
            return raw;
        }
        if (parts.length > 3) {
            String recovered = parts[parts.length - 3] + "." + parts[parts.length - 2] + "." + parts[parts.length - 1];
            System.out.println("[GATEWAY][JwtAuthFilter] recoveredTokenPreview=" + preview(recovered));
            return recovered;
        }

        return null;
    }

    private String preview(String token) {
        if (token == null) return "null";
        int keep = Math.min(token.length(), 30);
        return token.substring(0, keep) + "...(len=" + token.length() + ")";
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}