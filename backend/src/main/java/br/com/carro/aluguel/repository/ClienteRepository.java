package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCpf(String cpf);
    boolean existsByRg(String rg);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}
