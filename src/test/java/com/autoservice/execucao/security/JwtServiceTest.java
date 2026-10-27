package com.autoservice.execucao.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void rejeitaTokenInvalidoESegredoVazio() {
        final JwtService jwtService = new JwtService("test-secret-test-secret-test-secret");
        final String expirado = JWT.create()
                .withSubject("admin")
                .withExpiresAt(Date.from(Instant.now().minusSeconds(30)))
                .sign(Algorithm.HMAC256("test-secret-test-secret-test-secret"));

        assertThat(jwtService.isValid(expirado)).isFalse();
        assertThat(jwtService.isValid("lixo")).isFalse();
        assertThatThrownBy(() -> new JwtService(" ")).isInstanceOf(IllegalStateException.class);
    }
}
