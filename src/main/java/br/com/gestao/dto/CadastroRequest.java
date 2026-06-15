package br.com.gestao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(@NotBlank(message = "Nome é obrigatório") String nome,
                              @NotBlank(message = "Email é obrigatório") @Email String email,
                              @NotBlank(message = "Senha é obrigatória") @Size(min = 6) String senha) {
}
