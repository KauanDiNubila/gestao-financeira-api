package br.com.gestao.service;

import br.com.gestao.domain.entity.TipoTransacao;
import br.com.gestao.domain.entity.Transacao;
import br.com.gestao.domain.entity.TransacaoRepository;
import br.com.gestao.domain.usuario.Usuario;
import br.com.gestao.domain.usuario.UsuarioRepository;
import br.com.gestao.dto.CategoriaSumarizadaResponse;
import br.com.gestao.dto.ResumoMensalResponse;
import br.com.gestao.dto.TransacaoRequest;
import br.com.gestao.dto.TransacaoResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public TransacaoResponse criar(TransacaoRequest request, String email) {

        Usuario usuario = buscarUsuarioPorEmail(email);

        Transacao transacao = Transacao.builder()
                .descricao(request.descricao())
                .valor(request.valor())
                .tipo(request.tipo())
                .categoria(request.categoria())
                .data(request.data())
                .usuario(usuario)
                .build();

        return TransacaoResponse.from(transacaoRepository.save(transacao));
    }

    public List<TransacaoResponse> listar(String email) {

        Usuario usuario = buscarUsuarioPorEmail(email);
        return transacaoRepository.findByUsuario(usuario)
                .stream()
                .map(TransacaoResponse::from)
                .toList();
    }

    public TransacaoResponse buscarPorId(Long id, String email) {

        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        validarDono(transacao, email);
        return TransacaoResponse.from(transacao);
    }

    public TransacaoResponse editar(Long id, TransacaoRequest request, String email) {

        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        validarDono(transacao, email);

        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setTipo(request.tipo());
        transacao.setCategoria(request.categoria());
        transacao.setData(request.data());

        return TransacaoResponse.from(transacaoRepository.save(transacao));
    }

    public void deletar(Long id, String email) {

        Transacao transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        validarDono(transacao, email);
        transacaoRepository.delete(transacao);
    }

    private Usuario buscarUsuarioPorEmail(String email) {

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private void validarDono(Transacao transacao, String email) {

        String emailDono = transacao.getUsuario().getEmail();
        System.out.println(">>> Email do token: " + email);
        System.out.println(">>> Email do dono: " + emailDono);

        if (!emailDono.equals(email)) {
            throw new RuntimeException("Acesso negado");
        }
    }

    public ResumoMensalResponse resumoMensal(String email, String mes) {

        Usuario usuario = buscarUsuarioPorEmail(email);

        YearMonth yearMonth = YearMonth.parse(mes);
        int ano = yearMonth.getYear();
        int mesInt = yearMonth.getMonthValue();

        List<Transacao> transacoes = transacaoRepository.findByUsuarioAndMes(usuario, ano, mesInt);

        BigDecimal totalReceitas = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.RECEITA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesas = transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = totalReceitas.subtract(totalDespesas);

        return new ResumoMensalResponse(totalReceitas, totalDespesas, saldo);
    }

    public List<CategoriaSumarizadaResponse> resumoPorCategoria(String email) {

        Usuario usuario = buscarUsuarioPorEmail(email);

        List<Transacao> transacoes = transacaoRepository.findByUsuario(usuario);

        return transacoes.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
                .collect(Collectors.groupingBy(
                        Transacao::getCategoria,
                        Collectors.reducing(BigDecimal.ZERO, Transacao::getValor, BigDecimal::add)
                ))
                .entrySet().stream()
                .map(entry -> new CategoriaSumarizadaResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CategoriaSumarizadaResponse::total).reversed())
                .toList();
    }
}
