
package br.com.carro.aluguel.service;

import br.com.carro.aluguel.model.Agente;
import br.com.carro.aluguel.model.Automovel;
import br.com.carro.aluguel.model.Cliente;
import br.com.carro.aluguel.model.TipoProprietario;
import br.com.carro.aluguel.repository.AgenteRepository;
import br.com.carro.aluguel.repository.AutomovelRepository;
import br.com.carro.aluguel.repository.ClienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutomovelService {

    private final AutomovelRepository automoveis;
    private final ClienteRepository clientes;
    private final AgenteRepository agentes;

    public AutomovelService(
            AutomovelRepository automoveis,
            ClienteRepository clientes,
            AgenteRepository agentes
    ) {
        this.automoveis = automoveis;
        this.clientes = clientes;
        this.agentes = agentes;
    }

    @Transactional
    public Automovel cadastrarParaCliente(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            Long clienteId
    ) {
        validarUnicidade(matricula, placa);

        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Cliente não encontrado.")
                );

        return automoveis.save(
            new Automovel(matricula, ano, marca, modelo, placa, cliente)
        );
    }

    @Transactional
    public Automovel cadastrarParaAgente(
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa,
            Long agenteId
    ) {
        validarUnicidade(matricula, placa);

        Agente agente = agentes.findById(agenteId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Agente não encontrado.")
                );

        return automoveis.save(
            new Automovel(matricula, ano, marca, modelo, placa, agente)
        );
    }

    @Transactional(readOnly = true)
    public List<Automovel> listarTodos() {
        return automoveis.findAll();
    }

    @Transactional(readOnly = true)
    public Automovel buscarPorId(Long id) {
        return automoveis.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Automóvel não encontrado.")
                );
    }

    @Transactional(readOnly = true)
    public List<Automovel> listarPorCliente(Long clienteId) {
        return automoveis.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Automovel> listarPorAgente(Long agenteId) {
        return automoveis.findByAgenteId(agenteId);
    }

    @Transactional
    public Automovel atualizar(
            Long id,
            String matricula,
            int ano,
            String marca,
            String modelo,
            String placa
    ) {
        Automovel automovel = buscarPorId(id);

        validarUnicidadeNaAtualizacao(matricula, placa, id);

        automovel.atualizarDados(
            matricula, ano, marca, modelo, placa
        );

        return automovel;
    }

    @Transactional
    public void excluir(Long id) {
        Automovel automovel = buscarPorId(id);
        automoveis.delete(automovel);
    }

    private void validarUnicidade(String matricula, String placa) {
        if (automoveis.existsByMatricula(matricula.trim())) {
            throw new IllegalArgumentException(
                "Já existe um automóvel com essa matrícula."
            );
        }

        if (automoveis.existsByPlaca(placa.trim().toUpperCase())) {
            throw new IllegalArgumentException(
                "Já existe um automóvel com essa placa."
            );
        }
    }

    private void validarUnicidadeNaAtualizacao(
            String matricula,
            String placa,
            Long id
    ) {
        automoveis.findByMatricula(matricula.trim())
                .filter(a -> !a.getId().equals(id))
                .ifPresent(a -> {
                    throw new IllegalArgumentException(
                        "Já existe um automóvel com essa matrícula."
                    );
                });

        automoveis.findByPlaca(placa.trim().toUpperCase())
                .filter(a -> !a.getId().equals(id))
                .ifPresent(a -> {
                    throw new IllegalArgumentException(
                        "Já existe um automóvel com essa placa."
                    );
                });
    }
}
