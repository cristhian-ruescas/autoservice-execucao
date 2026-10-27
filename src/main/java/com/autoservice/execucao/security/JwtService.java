package com.autoservice.execucao.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtService {

    private final Algorithm algorithm;

    public JwtService(@Value("${autoservice.jwt.secret}") final String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Defina autoservice.jwt.secret");
        }
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public boolean isValid(final String token) {
        try {
            final DecodedJWT jwt = JWT.require(this.algorithm).build().verify(token);
            final Date expiresAt = jwt.getExpiresAt();
            return expiresAt == null || expiresAt.after(new Date());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String subject(final String token) {
        return JWT.require(this.algorithm).build().verify(token).getSubject();
    }
}
