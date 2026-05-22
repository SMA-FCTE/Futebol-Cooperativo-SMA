# Task 6: Criar ContextoDecisao e ControladorDecisaoJogador

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Separar a orientacao e a decisao tatica do `JogadorAgent`.

## Escopo

Criar:

- `ContextoDecisao` em `com.futebol.colaborativo.jogo`;
- `ControladorDecisaoJogador` em `com.futebol.colaborativo.estrategia`.

## Requisitos

`ContextoDecisao` deve conter:

```text
nomeJogador
estadoAtual
jogadorComBola
papel
time
perfilTatico
ticksBolaLivre
```

`ControladorDecisaoJogador.decidir(contexto)` deve:

- retornar `AGIR_COM_BOLA` se o jogador atual esta com bola;
- retornar `INTERCEPTAR` se outro jogador esta com bola;
- sortear entre `PERSEGUIR_BOLA` e `MANTER_POSICAO_DEFENSIVA` se a bola esta livre;
- forcar `PERSEGUIR_BOLA` se o jogador esta perto da bola livre;
- forcar `PERSEGUIR_BOLA` se a bola ficou livre por muitos ticks.

## Fora de Escopo

- Alterar o tick do `JogadorAgent`.
- Criar passe, marcacao ou cobertura.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

## Criterios de Sucesso

- Controlador possui testes unitarios para os ramos principais.
- Nenhuma mudanca funcional ocorre antes da integracao no agente.
- Excecao: o chute aleatorio de teste foi desativado nesta task por decisao explicita.

## Resultado

- Criado `ContextoDecisao` com nome, estado atual, jogador com bola, papel, time, perfil tatico e ticks de bola livre.
- Criado `ControladorDecisaoJogador` com decisao para jogador com bola, outro jogador com bola e bola livre.
- O controlador forca `PERSEGUIR_BOLA` quando o jogador esta perto da bola livre ou quando a bola ficou livre por muitos ticks.
- Adicionado metodo `manterPosicaoDefensiva()` no `JogadorAgent`, movendo o jogador para frente do proprio gol.
- Desativado o chute aleatorio de teste em `JogadorAgent`.
- A integracao do controlador no tick do `JogadorAgent` continua fora desta task.

## Validacao Executada

```powershell
mvn test
```

Resultado: `BUILD SUCCESS`, com 10 testes executados.
