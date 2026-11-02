package br.com.carro.aluguel.service;

import br.com.carro.aluguel.exception.BadRequestException;
import br.com.carro.aluguel.model.PasswordResetToken;
import br.com.carro.aluguel.model.Usuario;
import br.com.carro.aluguel.repository.PasswordResetTokenRepository;
import br.com.carro.aluguel.repository.UsuarioRepository;
import br.com.carro.aluguel.util.Entradas;
import br.com.carro.aluguel.util.SenhaPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordRecoveryService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LINK_INVALIDO = "Link inválido ou expirado. Solicite outro.";

    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final String frontendUrl;
    private final long minutos;

    public PasswordRecoveryService(UsuarioRepository usuarios, PasswordResetTokenRepository tokens,
                                   PasswordEncoder encoder, EmailService emailService,
                                   @Value("${app.frontend-url}") String frontendUrl,
                                   @Value("${app.recovery.expiration-minutes:15}") long minutos) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.encoder = encoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.minutos = minutos;
    }

    /** Se o e-mail existir, gera o token e envia o link. O chamador responde igual nos dois casos. */
    @Transactional
    public void solicitar(String emailBruto) {
        String email = Entradas.normalizarEmail(emailBruto);
        usuarios.findByEmail(email).ifPresent(u -> {
            tokens.deleteByUsuarioId(u.getId());
            String token = gerarToken();
            tokens.save(new PasswordResetToken(hash(token), u, Instant.now().plus(Duration.ofMinutes(minutos))));
            String link = frontendUrl + "/redefinir-senha?token=" + token;
            // Se o envio falhar, a exceção desfaz a transação e o token não fica salvo.
            emailService.enviar(u.getEmail(), "Aluguel de Carros - Redefinição de senha",
                    "Para definir uma nova senha, acesse:\n" + link
                            + "\n\nEste link é válido por " + minutos + " minutos."
                            + "\nSe você não fez esta solicitação, ignore este e-mail.");
        });
    }

    @Transactional(readOnly = true)
    public void validar(String token) {
        buscarValido(token);
    }

    @Transactional
    public void redefinir(String token, String senha, String confirmacao) {
        PasswordResetToken t = buscarValido(token);
        // Valida a senha antes de consumir o token, para uma senha fraca não "queimar" o link.
        SenhaPolicy.validar(senha, confirmacao);
        // Remoção atômica: dois envios simultâneos não reutilizam o mesmo token.
        if (tokens.deleteByTokenHash(hash(token)) == 0) {
            throw new BadRequestException(LINK_INVALIDO);
        }
        Usuario u = t.getUsuario();
        u.alterarSenha(encoder.encode(senha));
        usuarios.save(u);
    }

    private PasswordResetToken buscarValido(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException(LINK_INVALIDO);
        }
        return tokens.findByTokenHash(hash(token))
                .filter(t -> t.getExpiraEm().isAfter(Instant.now()))
                .orElseThrow(() -> new BadRequestException(LINK_INVALIDO));
    }

    private static String gerarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
