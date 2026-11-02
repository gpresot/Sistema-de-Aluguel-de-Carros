package br.com.carro.aluguel.repository;

import br.com.carro.aluguel.model.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    long deleteByTokenHash(String tokenHash);
    void deleteByUsuarioId(Long usuarioId);
}
