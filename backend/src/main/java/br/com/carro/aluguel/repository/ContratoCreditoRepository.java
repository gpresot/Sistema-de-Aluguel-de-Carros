package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.ContratoCredito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoCreditoRepository extends JpaRepository<ContratoCredito, Long> {
    Optional<ContratoCredito> findByAluguelId(Long aluguelId);
    boolean existsByAluguelId(Long aluguelId);
}
