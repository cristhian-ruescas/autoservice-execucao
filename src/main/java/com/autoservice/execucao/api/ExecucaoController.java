package com.autoservice.execucao.api;

import com.autoservice.execucao.service.ExecucaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/execucoes")
public class ExecucaoController {

    private final ExecucaoService execucaoService;

    public ExecucaoController(final ExecucaoService execucaoService) {
        this.execucaoService = execucaoService;
    }

    @GetMapping("/{ordemServicoId}")
    public ExecucaoResponse consultar(@PathVariable final UUID ordemServicoId) {
        return this.execucaoService.consultar(ordemServicoId);
    }

    @PostMapping("/{ordemServicoId}/diagnostico/concluir")
    public ExecucaoResponse concluirDiagnostico(@PathVariable final UUID ordemServicoId) {
        return this.execucaoService.concluirDiagnostico(ordemServicoId);
    }

    @PostMapping("/{ordemServicoId}/reparo/concluir")
    public ExecucaoResponse concluirReparo(@PathVariable final UUID ordemServicoId) {
        return this.execucaoService.concluirReparo(ordemServicoId);
    }

    @PostMapping("/{ordemServicoId}/falhar")
    public ExecucaoResponse falhar(
            @PathVariable final UUID ordemServicoId,
            @RequestBody(required = false) final FalharRequest request
    ) {
        return this.execucaoService.falhar(ordemServicoId, request == null ? null : request.motivo());
    }
}
