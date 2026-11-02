package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.Agente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenteRepository extends JpaRepository<Agente, Long> {
    boolean existsByCnpj(String cnpj);
    Optional<Agente> findByUsuarioId(Long usuarioId);
}
