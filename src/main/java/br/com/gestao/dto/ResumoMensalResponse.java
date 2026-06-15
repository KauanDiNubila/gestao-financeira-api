package br.com.gestao.dto;

import java.math.BigDecimal;

public record ResumoMensalResponse(BigDecimal totalReceitas,
                                   BigDecimal totalDespesas,
                                   BigDecimal saldo) {
}
