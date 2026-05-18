package br.com.gestao.dto;

import br.com.gestao.domain.entity.TipoTransacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        @NotBlank(message = "Descrição é obrigatória") String descricao,
        @NotNull(message = "Valor é obrigatório") @Positive(message = "Valor deve ser positivo") BigDecimal valor,
        @NotNull(message = "Tipo é obrigatório") TipoTransacao tipo,
        @NotBlank(message = "Categoria é obrigatória") String categoria,
        @NotNull(message = "Data é obrigatória") LocalDate data
) {}
