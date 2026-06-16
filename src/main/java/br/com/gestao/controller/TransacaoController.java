package br.com.gestao.controller;


import br.com.gestao.dto.CategoriaSumarizadaResponse;
import br.com.gestao.dto.ResumoMensalResponse;
import br.com.gestao.dto.TransacaoRequest;
import br.com.gestao.dto.TransacaoResponse;
import br.com.gestao.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<TransacaoResponse> criar(@RequestBody @Valid TransacaoRequest request,
                                                   Authentication authentication) {

        TransacaoResponse response = transacaoService.criar(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransacaoResponse>> listar(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(transacaoService.listar(authentication.getName(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponse> buscarPorId(@PathVariable Long id,
                                                         Authentication authentication) {

        TransacaoResponse transacao = transacaoService.buscarPorId(id, authentication.getName());
        return ResponseEntity.ok(transacao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoResponse> editar(@PathVariable Long id,
                                                    @RequestBody @Valid TransacaoRequest request,
                                                    Authentication authentication) {

        TransacaoResponse transacao = transacaoService.editar(id, request, authentication.getName());
        return ResponseEntity.ok(transacao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id,
                                        Authentication authentication) {

        transacaoService.deletar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoMensalResponse> resumoMensal(
            @RequestParam String mes,
            Authentication authentication) {

        ResumoMensalResponse resumo = transacaoService.resumoMensal(authentication.getName(), mes);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/por-categoria")
    public ResponseEntity<List<CategoriaSumarizadaResponse>> resumoPorCategoria(
            Authentication authentication) {

        List<CategoriaSumarizadaResponse> resumo = transacaoService.resumoPorCategoria(authentication.getName());
        return ResponseEntity.ok(resumo);
    }
}
