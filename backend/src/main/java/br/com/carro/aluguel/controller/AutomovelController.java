package br.com.carro.aluguel.controller;

import br.com.carro.aluguel.dto.AutomovelRequest;
import br.com.carro.aluguel.dto.AutomovelResponse;
import br.com.carro.aluguel.service.AutomovelService;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/automoveis")
public class AutomovelController {

    private final AutomovelService service;

    public AutomovelController(AutomovelService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomovelResponse cadastrar(@Valid @RequestBody AutomovelRequest request, Authentication auth) {
        return service.cadastrar(auth.getName(), request);
    }

    /** Automóveis cadastrados pelo usuário autenticado. */
    @GetMapping
    public List<AutomovelResponse> listar(Authentication auth) {
        return service.listarMeus(auth.getName());
    }

    /** Catálogo (clientes): automóveis sem aluguel em andamento. */
    @GetMapping("/disponiveis")
    public List<AutomovelResponse> listarDisponiveis(Authentication auth) {
        return service.listarDisponiveis(auth.getName());
    }

    @GetMapping("/{id}")
    public AutomovelResponse buscar(@PathVariable Long id, Authentication auth) {
        return service.buscar(auth.getName(), id);
    }

    @PutMapping("/{id}")
    public AutomovelResponse atualizar(@PathVariable Long id, @Valid @RequestBody AutomovelRequest request,
                                       Authentication auth) {
        return service.atualizar(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, Authentication auth) {
        service.excluir(auth.getName(), id);
    }

    // ---------- Fotos ----------

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AutomovelResponse enviarFoto(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo,
                                        Authentication auth) {
        return service.salvarFoto(auth.getName(), id, arquivo);
    }

    @DeleteMapping("/{id}/foto")
    public AutomovelResponse removerFoto(@PathVariable Long id, Authentication auth) {
        return service.removerFoto(auth.getName(), id);
    }

    /** Pública (liberada no SecurityConfig): o navegador carrega a imagem via tag img, sem token. */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        AutomovelService.Foto foto = service.obterFoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(foto.conteudo());
    }
}
