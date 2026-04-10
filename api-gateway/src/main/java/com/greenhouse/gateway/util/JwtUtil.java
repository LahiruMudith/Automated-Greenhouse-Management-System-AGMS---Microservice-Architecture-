package com.greenhouse.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Claims parseWithKey(String token, SecretKey key, String keyName) {
        try {
            System.out.println("[GATEWAY][JwtUtil] trying key mode: " + keyName);
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            System.out.println("[GATEWAY][JwtUtil] key mode failed (" + keyName + "): " + e.getMessage());
            return null;
        }
    }

    public Claims extractAllClaims(String token) {
        token = sanitizeToken(token);

        if (token.split("\\.").length != 3) {
            throw new IllegalArgumentException("Invalid JWT format: expected 3 parts (JWS)");
        }

        // Mode 1: raw UTF-8 bytes (your current method)
        Claims c1 = parseWithKey(token,
                Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),
                "UTF8(secret)");
        if (c1 != null) return c1;

        // Mode 2: Base64-decoded secret (common in some setups)
        try {
            byte[] b64 = Decoders.BASE64.decode(secret);
            Claims c2 = parseWithKey(token, Keys.hmacShaKeyFor(b64), "BASE64(secret)");
            if (c2 != null) return c2;
        } catch (Exception e) {
            System.out.println("[GATEWAY][JwtUtil] BASE64 decode not applicable: " + e.getMessage());
        }

        // Mode 3: Base64URL-decoded secret
        try {
            byte[] b64url = Decoders.BASE64URL.decode(secret);
            Claims c3 = parseWithKey(token, Keys.hmacShaKeyFor(b64url), "BASE64URL(secret)");
            if (c3 != null) return c3;
        } catch (Exception e) {
            System.out.println("[GATEWAY][JwtUtil] BASE64URL decode not applicable: " + e.getMessage());
        }

        throw new JwtException("JWT signature does not match for any supported key interpretation");
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            System.out.println("[GATEWAY][JwtUtil] tokenValid sub=" + claims.getSubject());
            return true;
        } catch (Exception e) {
            System.out.println("[GATEWAY][JwtUtil] Token validation failed: " + e.getMessage());
            return false;
        }
    }

    private String sanitizeToken(String token) {
        if (token == null) return null;
        token = token.trim();
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
            token = token.substring(1, token.length() - 1).trim();
        }
        return token;
    }
}