package com.autoservice.execucao.messaging;

public final class TiposEvento {

    public static final String DIAGNOSTICO_SOLICITADO = "diagnostico.solicitado";
    public static final String DIAGNOSTICO_CONCLUIDO = "diagnostico.concluido";
    public static final String DIAGNOSTICO_FALHOU = "diagnostico.falhou";
    public static final String EXECUCAO_SOLICITADA = "execucao.solicitada";
    public static final String EXECUCAO_CONCLUIDA = "execucao.concluida";
    public static final String EXECUCAO_FALHOU = "execucao.falhou";
    public static final String COMPENSAR_FILA = "compensacao.fila.liberar";
    public static final String EXCHANGE = "autoservice.saga";

    private TiposEvento() {
    }
}
