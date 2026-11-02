package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.StatusAluguel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    boolean existsByMatricula(String matricula);

    boolean existsByPlaca(String placa);

    Optional<Automovel> findByMatricula(String matricula);

    Optional<Automovel> findByPlaca(String placa);

    List<Automovel> findByClienteId(Long clienteId);

    List<Automovel> findByAgenteId(Long agenteId);

    @Query("""
        select a from Automovel a 
        where a.id not in (
        select al.automovel.id from Aluguel al
        where al.status in :statusAtivos
        )
        order by a.marca, a.modelo       
    """)
    List<Automovel> findDisponiveis(@Param("statusAtivos") List<StatusAluguel> statusAtivos);
}