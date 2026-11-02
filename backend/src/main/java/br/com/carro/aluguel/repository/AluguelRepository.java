package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.Aluguel;
import br.com.carro.aluguel.model.StatusAluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    boolean existsByAutomovelIdAndStatusIn(Long automovelId, Collection<StatusAluguel> status);

    List<Aluguel> findByClienteUsuarioIdOrderByIdDesc(Long usuarioId);

    List<Aluguel> findByAutomovelAgenteUsuarioIdOrderByIdDesc(Long usuarioId);

    boolean existsByAutomovelId(Long automovelId);

    /** Pedidos que o banco enxerga: todos em análise, os que ele avaliou e os de seus próprios carros. */
    @Query("""
            select a from Aluguel a
            left join a.avaliador av
            left join a.automovel au
            left join au.agente ag
            where a.status = :analise or av.usuario.id = :usuarioId or ag.usuario.id = :usuarioId
            order by a.id desc
            """)
    List<Aluguel> findParaBanco(@Param("usuarioId") Long usuarioId,
                                @Param("analise") StatusAluguel analise);
}
