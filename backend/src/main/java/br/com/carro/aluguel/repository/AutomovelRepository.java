package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.Automovel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    boolean existsByMatricula(String matricula);

    boolean existsByPlaca(String placa);

    Optional<Automovel> findByMatricula(String matricula);

    Optional<Automovel> findByPlaca(String placa);

    List<Automovel> findByClienteId(Long clienteId);

    List<Automovel> findByAgenteId(Long agenteId);
}