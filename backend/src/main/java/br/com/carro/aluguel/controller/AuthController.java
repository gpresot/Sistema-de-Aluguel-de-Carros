package br.com.carro.aluguel.controller;

import br.com.carro.aluguel.dto.AuthResponse;
import br.com.carro.aluguel.dto.LoginRequest;
import br.com.carro.aluguel.dto.MensagemResponse;
import br.com.carro.aluguel.dto.RecuperarSenhaRequest;
import br.com.carro.aluguel.dto.RedefinirSenhaRequest;
import br.com.carro.aluguel.dto.RegistroClienteRequest;
import br.com.carro.aluguel.service.AuthService;
import br.com.carro.aluguel.service.PasswordRecoveryService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService recoveryService;

    public AuthController(AuthService authService, PasswordRecoveryService recoveryService) {
        this.authService = authService;
        this.recoveryService = recoveryService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroClienteRequest req) {
        return authService.registrarCliente(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/recuperar-senha")
    public MensagemResponse recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest req) {
        recoveryService.solicitar(req.email());
        // Resposta igual para e-mails cadastrados e não cadastrados.
        return new MensagemResponse("Se o email estiver cadastrado, você receberá as instruções de recuperação.");
    }

    @GetMapping("/redefinir-senha/validar")
    public ResponseEntity<Void> validarToken(@RequestParam(defaultValue = "") String token) {
        recoveryService.validar(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/redefinir-senha")
    public MensagemResponse redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest req) {
        recoveryService.redefinir(req.token(), req.senha(), req.confirmacaoSenha());
        return new MensagemResponse("Senha redefinida! Entre com sua nova senha.");
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication auth) {
        String perfil = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .findFirst().orElse("").replace("ROLE_", "");
        return Map.of("email", auth.getName(), "perfil", perfil);
    }
}
