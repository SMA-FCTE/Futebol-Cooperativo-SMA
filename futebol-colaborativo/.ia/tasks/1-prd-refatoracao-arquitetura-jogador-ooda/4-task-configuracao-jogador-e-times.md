# Task 4: Integrar ConfiguracaoJogador e Time no Bootstrap

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Fazer `azul` e `vermelho` nascerem com `Time` e `PapelJogador`, ainda mantendo apenas 1 jogador por time.

## Escopo

- Ajustar `SistemaFutebol.criarJogador` para aceitar apenas `ConfiguracaoJogador`.
- Passar a configuracao completa para `JogadorAgent`.
- Ajustar `App.java` para criar:
  - `azul` com `Time.AZUL` e `PapelJogador.JOGADOR`;
  - `vermelho` com `Time.VERMELHO` e `PapelJogador.JOGADOR`.
- Manter a configuracao usada para reset apos gol.

## Requisitos

- `JogadorAgent` deve conhecer seu `time` e `papel` no `setup`.
- `JogadorEstado` nao deve ganhar campos novos nesta task.
- O payload WebSocket deve continuar igual.

## Fora de Escopo

- Impedir disputa entre aliados.
- Criar 2 jogadores por time.
- Criar subclasses de jogador.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
mvn exec:java
```

## Criterios de Sucesso

- Backend compila.
- Agentes `azul`, `vermelho` e `bola` continuam sendo criados.
- Simulacao 1x1 continua rodando.

## Resultado

- `App.java` agora cria `azul` com `Time.AZUL` e `vermelho` com `Time.VERMELHO`.
- `SistemaFutebol` possui apenas o caminho novo `criarJogador(ConfiguracaoJogador)`.
- O caminho antigo com `nome`, `xInicial`, `yInicial` e `golX` separados foi removido.
- `SistemaFutebol` passa a configuracao completa ao `JogadorAgent`.
- `JogadorAgent` passa a conhecer `time` e `papel` no `setup`.
- `JogadorAgent` aceita apenas argumentos com `ConfiguracaoJogador`.
- `JogadorEstado` e payload WebSocket nao foram alterados.

## Validacao Executada

```powershell
mvn test
mvn clean test
```

Ambos executaram com `BUILD SUCCESS`. A validacao manual com `mvn exec:java` fica para a task de regressao, porque inicia o processo JADE/API/WebSocket e permanece rodando.
