package br.com.gestao.dto;

import java.math.BigDecimal;

public record CategoriaSumarizadaResponse(
        String categoria,
        BigDecimal total
) {}
