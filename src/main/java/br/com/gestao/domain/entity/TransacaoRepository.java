package br.com.gestao.domain.entity;

import br.com.gestao.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuario(Usuario usuario);

    List<Transacao> findByUsuarioAndTipo(Usuario usuario, TipoTransacao tipo);

    List<Transacao> findByUsuarioAndDataBetween(Usuario usuario, LocalDate inicio, LocalDate fim);

    @Query("SELECT t FROM Transacao t WHERE t.usuario = :usuario " +
            "AND YEAR(t.data) = :ano AND MONTH(t.data) = :mes")
    List<Transacao> findByUsuarioAndMes(@Param("usuario") Usuario usuario,
                                        @Param("ano") int ano,
                                        @Param("mes") int mes);
}
