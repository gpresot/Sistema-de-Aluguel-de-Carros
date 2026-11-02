package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.Aluguel;
import br.com.carro.aluguel.model.StatusAluguel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    boolean existsByAutomovelIdAndStatusIn(Long automovelId, Collection<StatusAluguel> status);

    List<Aluguel> findByClienteUsuarioId(Long usuarioId);

    List<Aluguel> findByAutomovelAgenteUsuarioId(Long usuarioId);
}