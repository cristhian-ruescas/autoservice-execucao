package com.autoservice.execucao.api;

import com.autoservice.execucao.config.SecurityConfig;
import com.autoservice.execucao.domain.NaoEncontradoException;
import com.autoservice.execucao.domain.NegocioException;
import com.autoservice.execucao.security.JwtService;
import com.autoservice.execucao.service.ExecucaoService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExecucaoController.class)
@Import({SecurityConfig.class, JwtService.class, ApiExceptionHandler.class, com.autoservice.execucao.observability.ObservabilityWebMvcTestConfig.class})
@TestPropertySource(properties = "autoservice.jwt.secret=test-secret-test-secret-test-secret")
class ExecucaoApiTest {

    private static final String SEGREDO = "test-secret-test-secret-test-secret";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExecucaoService execucaoService;

    @Test
    void operaAFilaComToken() throws Exception {
        final UUID ordemId = UUID.randomUUID();
        final ExecucaoResponse resposta = new ExecucaoResponse(
                ordemId, UUID.randomUUID(), "relato", "EM_REPARO", null, Instant.now()
        );
        when(this.execucaoService.consultar(ordemId)).thenReturn(resposta);
        when(this.execucaoService.concluirDiagnostico(ordemId)).thenReturn(resposta);
        when(this.execucaoService.concluirReparo(ordemId)).thenReturn(resposta);
        when(this.execucaoService.falhar(eq(ordemId), eq("peca"))).thenReturn(resposta);
        final String auth = "Bearer " + token("admin");

        this.mvc.perform(get("/execucoes/" + ordemId).header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etapa").value("EM_REPARO"));
        this.mvc.perform(post("/execucoes/" + ordemId + "/diagnostico/concluir").header("Authorization", auth))
                .andExpect(status().isOk());
        this.mvc.perform(post("/execucoes/" + ordemId + "/reparo/concluir").header("Authorization", auth))
                .andExpect(status().isOk());
        this.mvc.perform(post("/execucoes/" + ordemId + "/falhar").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"peca\"}"))
                .andExpect(status().isOk());
        this.mvc.perform(post("/execucoes/" + ordemId + "/falhar").header("Authorization", auth))
                .andExpect(status().isOk());
        this.mvc.perform(get("/execucoes/" + ordemId)).andExpect(status().isUnauthorized());
    }

    @Test
    void traduzExcecoesETokenSemSubject() throws Exception {
        when(this.execucaoService.consultar(any())).thenThrow(new NaoEncontradoException("ausente"));
        when(this.execucaoService.falhar(any(), any())).thenThrow(new NegocioException("encerrada"));
        final String auth = "Bearer " + token("");

        this.mvc.perform(get("/execucoes/" + UUID.randomUUID()).header("Authorization", auth))
                .andExpect(status().isNotFound());
        this.mvc.perform(post("/execucoes/" + UUID.randomUUID() + "/falhar").header("Authorization", "Bearer " + token("admin")))
                .andExpect(status().isBadRequest());
    }

    private static String token(final String subject) {
        final var builder = JWT.create().withExpiresAt(Date.from(Instant.now().plusSeconds(600)));
        if (!subject.isBlank()) {
            builder.withSubject(subject);
        }
        return builder.sign(Algorithm.HMAC256(SEGREDO));
    }
}
