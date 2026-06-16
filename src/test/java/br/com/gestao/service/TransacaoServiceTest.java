package br.com.gestao.service;

import br.com.gestao.domain.entity.TipoTransacao;
import br.com.gestao.domain.entity.Transacao;
import br.com.gestao.domain.entity.TransacaoRepository;
import br.com.gestao.domain.usuario.Usuario;
import br.com.gestao.domain.usuario.UsuarioRepository;
import br.com.gestao.dto.CategoriaSumarizadaResponse;
import br.com.gestao.dto.ResumoMensalResponse;
import br.com.gestao.exception.AcessoNegadoException;
import br.com.gestao.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do TransacaoService")
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nome("Kauan")
                .email("kauan@email.com")
                .senha("senha-codificada")
                .build();
    }

    @Test
    @DisplayName("Deve calcular o resumo mensal somando receitas, despesas e saldo corretamente")
    void deveCalcularResumoMensalCorretamente() {
        // Arrange
        List<Transacao> transacoes = List.of(
                criarTransacao(BigDecimal.valueOf(5000), TipoTransacao.RECEITA, "Salário"),
                criarTransacao(BigDecimal.valueOf(1500), TipoTransacao.DESPESA, "Aluguel"),
                criarTransacao(BigDecimal.valueOf(300), TipoTransacao.DESPESA, "Mercado")
        );

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(transacaoRepository.findByUsuarioAndMes(usuario, 2026, 6)).thenReturn(transacoes);

        // Act
        ResumoMensalResponse resumo = transacaoService.resumoMensal(usuario.getEmail(), "2026-06");

        // Assert
        assertThat(resumo.totalReceitas()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(resumo.totalDespesas()).isEqualByComparingTo(BigDecimal.valueOf(1800));
        assertThat(resumo.saldo()).isEqualByComparingTo(BigDecimal.valueOf(3200));
    }

    @Test
    @DisplayName("Deve retornar zero quando não há transações no mês")
    void deveRetornarZeroQuandoNaoHaTransacoes() {
        // Arrange
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(transacaoRepository.findByUsuarioAndMes(usuario, 2026, 6)).thenReturn(List.of());

        // Act
        ResumoMensalResponse resumo = transacaoService.resumoMensal(usuario.getEmail(), "2026-06");

        // Assert
        assertThat(resumo.totalReceitas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.totalDespesas()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resumo.saldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve sumarizar despesas por categoria, ordenadas da maior para a menor")
    void deveSumarizarDespesasPorCategoria() {
        // Arrange
        List<Transacao> transacoes = List.of(
                criarTransacao(BigDecimal.valueOf(1500), TipoTransacao.DESPESA, "Aluguel"),
                criarTransacao(BigDecimal.valueOf(300), TipoTransacao.DESPESA, "Mercado"),
                criarTransacao(BigDecimal.valueOf(200), TipoTransacao.DESPESA, "Mercado"),
                criarTransacao(BigDecimal.valueOf(5000), TipoTransacao.RECEITA, "Salário")
        );

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(transacaoRepository.findByUsuario(usuario)).thenReturn(transacoes);

        // Act
        List<CategoriaSumarizadaResponse> resumo = transacaoService.resumoPorCategoria(usuario.getEmail());

        // Assert
        // Receita não entra; Aluguel (1500) vem antes de Mercado (500)
        assertThat(resumo).hasSize(2);
        assertThat(resumo.get(0).categoria()).isEqualTo("Aluguel");
        assertThat(resumo.get(0).total()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(resumo.get(1).categoria()).isEqualTo("Mercado");
        assertThat(resumo.get(1).total()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar transação de outro usuário")
    void deveLancarExcecaoAoAcessarTransacaoDeOutroUsuario() {
        // Arrange
        Usuario outroUsuario = Usuario.builder()
                .id(2L)
                .email("outro@email.com")
                .build();

        Transacao transacaoDeOutro = criarTransacao(BigDecimal.valueOf(100), TipoTransacao.DESPESA, "Livro");
        transacaoDeOutro.setUsuario(outroUsuario);

        when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacaoDeOutro));

        // Act + Assert
        assertThatThrownBy(() -> transacaoService.buscarPorId(1L, usuario.getEmail()))
                .isInstanceOf(AcessoNegadoException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar transação inexistente")
    void deveLancarExcecaoAoBuscarTransacaoInexistente() {
        // Arrange
        when(transacaoRepository.findById(9999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> transacaoService.buscarPorId(9999L, usuario.getEmail()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Transação não encontrada");
    }

    // Método auxiliar para montar transações de teste sem repetir código
    private Transacao criarTransacao(BigDecimal valor, TipoTransacao tipo, String categoria) {
        return Transacao.builder()
                .descricao(categoria)
                .valor(valor)
                .tipo(tipo)
                .categoria(categoria)
                .data(LocalDate.of(2026, 6, 1))
                .usuario(usuario)
                .build();
    }
}