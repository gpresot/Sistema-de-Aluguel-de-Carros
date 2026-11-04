# Sistema de Aluguel de Carros — Modelo Inicial (v1.0)

> Primeira versão da modelagem, feita a partir do enunciado do Laboratório 02, antes da implementação. A evolução até o protótipo final está na seção 6 de [`modelagem.md`](./modelagem.md#6-comparação-modelo-inicial--protótipo-final).

## 1. Casos de Uso

```mermaid
flowchart LR
    Cliente[Cliente]
    Agente[Agente<br/>Empresa ou Banco]

    UC1((Cadastrar-se))
    UC2((Fazer login))
    UC3((Introduzir pedido de aluguel))
    UC4((Consultar pedido))
    UC5((Modificar pedido))
    UC6((Cancelar pedido))
    UC7((Avaliar pedido financeiramente))
    UC8((Executar contrato))

    Cliente --- UC1
    Cliente --- UC2
    Cliente --- UC3
    Cliente --- UC4
    Cliente --- UC5
    Cliente --- UC6

    Agente --- UC2
    Agente --- UC5
    Agente --- UC7
    Agente --- UC8
```

## 2. Histórias de Usuário

| ID | Como... | Quero... | Para... |
|----|---------|----------|---------|
| US01 | cliente | me cadastrar com usuário e senha | usar o sistema |
| US02 | cliente ou agente | fazer login | acessar o sistema |
| US03 | cliente | introduzir um pedido de aluguel | alugar um automóvel |
| US04 | cliente | consultar meus pedidos | acompanhar o andamento |
| US05 | cliente ou agente | modificar um pedido | corrigir dados |
| US06 | cliente | cancelar um pedido | desistir do aluguel |
| US07 | agente | avaliar o pedido financeiramente | decidir se segue para contrato |
| US08 | agente | executar o contrato de um pedido positivo | formalizar o aluguel |

## 3. Diagrama de Classes

```mermaid
classDiagram
    class Usuario {
        -String login
        -String senha
    }
    class Cliente {
        -String nome
        -String rg
        -String cpf
        -String endereco
        -String profissao
    }
    class Rendimento {
        -String entidadeEmpregadora
        -double valor
    }
    class Agente {
        -String nome
        -String tipo
    }
    class Automovel {
        -String matricula
        -int ano
        -String marca
        -String modelo
        -String placa
    }
    class PedidoAluguel {
        -Date dataInicio
        -Date dataFim
        -String status
    }
    class ContratoCredito {
        -double valor
    }

    Usuario <|-- Cliente
    Usuario <|-- Agente
    Cliente "1" *-- "0..3" Rendimento
    Cliente "1" -- "0..*" PedidoAluguel : solicita
    PedidoAluguel "0..*" -- "1" Automovel : aluga
    Agente "1" -- "0..*" PedidoAluguel : avalia
    PedidoAluguel "1" -- "0..1" ContratoCredito : possui
    ContratoCredito "0..*" -- "1" Agente : concedido por banco
    Automovel "0..*" -- "1" Cliente : proprietário
    Automovel "0..*" -- "1" Agente : proprietário
```

## 4. Diagrama de Pacotes (Visão Lógica)

```mermaid
flowchart TB
    View[Camada de apresentação<br/>React + Vite]
    Controller[controller]
    Service[service]
    Repository[repository]
    Model[model]
    DB[(Supabase PostgreSQL)]

    View --> Controller
    Controller --> Service
    Service --> Repository
    Service --> Model
    Repository --> Model
    Repository --> DB
```
