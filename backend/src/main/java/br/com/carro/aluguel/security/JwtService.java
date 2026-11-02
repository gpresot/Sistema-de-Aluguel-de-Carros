package br.com.carro.aluguel.security;

import br.com.carro.aluguel.model.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiracao;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long minutos) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(minutos);
    }

    public String gerar(String login, Perfil perfil) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(login)
                .claim("perfil", perfil.name())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracao.toMillis()))
                .signWith(key)
                .compact();
    }

    public Claims ler(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
