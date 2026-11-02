# Modelagem — Sistema de Aluguel de Carros

Diagramas em Mermaid (o GitHub renderiza automaticamente). **Versão 1.0**.

## 1. Diagrama de Casos de Uso

```mermaid
flowchart LR
    Cliente((Cliente))
    Empresa((Agente<br/>Empresa))
    Banco((Agente<br/>Banco))

    subgraph Sistema[Sistema de Aluguel de Carros]
        UC1([Cadastrar-se])
        UC2([Fazer login])
        UC3([Gerenciar cadastro<br/>CRUD de cliente])
        UC4([Introduzir pedido])
        UC5([Modificar pedido])
        UC6([Consultar pedidos e status])
        UC7([Cancelar pedido])
        UC8([Avaliar pedido financeiramente])
        UC9([Executar contrato])
        UC10([Gerenciar automóveis])
        UC11([Conceder contrato de crédito])
    end

    Cliente --- UC1 & UC3 & UC4 & UC5 & UC6 & UC7
    Empresa --- UC5 & UC6 & UC8 & UC9 & UC10
    Banco --- UC5 & UC6 & UC8 & UC9 & UC10 & UC11

    UC3 -. include .-> UC2
    UC4 -. include .-> UC2
    UC8 -. include .-> UC2
    UC11 -. extend .-> UC8
    UC9 -. include .-> UC8
```

## 2. Histórias de Usuário

| ID | Como... | Quero... | Para... | Critérios de aceite |
|----|---------|----------|---------|---------------------|
| US01 | visitante | me cadastrar com usuário e senha, informando RG, CPF, nome, endereço, profissão e até 3 rendimentos | usar o sistema | login, CPF e RG únicos; máximo de 3 rendimentos |
| US02 | usuário cadastrado | fazer login com usuário e senha | acessar as funções do meu perfil | sem login o acesso é negado; senha inválida é rejeitada |
| US03 | cliente | consultar, alterar e excluir meu cadastro | manter meus dados atualizados | acesso só ao próprio cadastro |
| US04 | cliente | introduzir um pedido de aluguel | alugar um automóvel | datas válidas; pedido nasce `EM_ANALISE` |
| US05 | cliente | consultar o status dos meus pedidos | acompanhar a análise | lista apenas os meus pedidos |
| US06 | cliente ou agente | modificar um pedido | corrigir dados | somente em `EM_ANALISE` |
| US07 | cliente | cancelar um pedido | desistir do aluguel | somente em `EM_ANALISE` ou `APROVADO` |
| US08 | agente | avaliar financeiramente um pedido | aprová-lo ou reprová-lo | registra parecer; só em `EM_ANALISE` |
| US09 | banco | conceder contrato de crédito ao aprovar | financiar o aluguel | só perfil `BANCO`; informa valor e juros |
| US10 | agente | executar o contrato de um pedido aprovado | formalizar o aluguel | só em `APROVADO`; passa a `CONTRATADO` |
| US11 | agente | cadastrar e manter automóveis | disponibilizar a frota | matrícula e placa únicas; proprietário cliente, empresa ou banco |

## 3. Diagrama de Classes

```mermaid
classDiagram
    class Usuario {
        -Long id
        -String login
        -String senha
        -Perfil perfil
    }
    class Cliente {
        -Long id
        -String nome
        -String rg
        -String cpf
        -String endereco
        -String profissao
    }
    class Rendimento {
        -String entidadeEmpregadora
        -BigDecimal valor
    }
    class Agente {
        -Long id
        -String nome
        -String cnpj
    }
    class Automovel {
        -Long id
        -String matricula
        -int ano
        -String marca
        -String modelo
        -String placa
        -TipoProprietario tipoProprietario
    }
    class PedidoAluguel {
        -Long id
        -LocalDate dataInicio
        -LocalDate dataFim
        -BigDecimal valorTotal
        -StatusPedido status
        -String parecer
        -LocalDateTime criadoEm
    }
    class ContratoCredito {
        -Long id
        -BigDecimal valorFinanciado
        -BigDecimal taxaJurosMensal
    }
    class Perfil {
        <<enumeration>>
        CLIENTE
        EMPRESA
        BANCO
    }
    class StatusPedido {
        <<enumeration>>
        EM_ANALISE
        APROVADO
        REPROVADO
        CONTRATADO
        CANCELADO
    }
    class TipoProprietario {
        <<enumeration>>
        CLIENTE
        EMPRESA
        BANCO
    }

    Usuario "1" -- "0..1" Cliente : é
    Usuario "1" -- "0..1" Agente : é
    Cliente "1" *-- "0..3" Rendimento
    Cliente "1" -- "0..*" PedidoAluguel : solicita
    Agente "0..1" -- "0..*" PedidoAluguel : avalia
    PedidoAluguel "0..*" -- "1" Automovel : aluga
    PedidoAluguel "1" -- "0..1" ContratoCredito : possui
    Agente "1" -- "0..*" ContratoCredito : concede (banco)
    Cliente "0..1" -- "0..*" Automovel : proprietário
    Agente "0..1" -- "0..*" Automovel : proprietário
    Usuario ..> Perfil
    PedidoAluguel ..> StatusPedido
    Automovel ..> TipoProprietario
```

## 4. Estados do Pedido

```mermaid
stateDiagram-v2
    [*] --> EM_ANALISE : cliente introduz
    EM_ANALISE --> APROVADO : agente aprova
    EM_ANALISE --> REPROVADO : agente reprova
    EM_ANALISE --> CANCELADO : cancelar
    APROVADO --> CONTRATADO : agente executa contrato
    APROVADO --> CANCELADO : cancelar
    REPROVADO --> [*]
    CONTRATADO --> [*]
    CANCELADO --> [*]
```

## 5. Diagrama de Pacotes (Visão Lógica)

```mermaid
flowchart TB
    subgraph Frontend[Front-end React + Vite]
        pages[pages]
        components[components]
        api[api]
    end
    subgraph Backend[Back-end Spring Boot - MVC]
        controller[controller]
        service[service]
        repository[repository]
        model[model]
        dto[dto]
        security[security]
        exception[exception]
    end
    DB[(Supabase<br/>PostgreSQL)]

    pages --> components
    pages --> api
    api -- HTTP/JSON --> controller
    controller --> dto
    controller --> service
    service --> repository
    service --> model
    repository --> model
    security --> service
    controller --> exception
    repository -- JDBC --> DB
```

## Histórico de versões

| Versão | Data | Mudança |
|--------|------|---------|
| 1.0 | 2026-11-02 | Versão inicial |