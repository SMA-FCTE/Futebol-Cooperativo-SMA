# PRD: Refatoracao da Arquitetura do Jogador com OODA

> **Escopo:** arquitetura interna do `JogadorAgent`

## Resumo

Esta mudanca prepara o `JogadorAgent` para uma arquitetura mais organizada de decisao, usando o ciclo OODA como modelo mental: Observar, Orientar, Decidir e Agir.

O escopo inicial deve manter a simulacao atual com 1 jogador azul e 1 jogador vermelho. A expansao para 2 jogadores por time fica fora deste PRD.

## Problema

Hoje o `JogadorAgent` mistura ciclo JADE, mensagens ACL, disputa, movimento, chute, interceptacao, posse, penalidade e decisao tatica. Isso dificulta evoluir o comportamento sem aumentar muito a complexidade da classe.

## Objetivo

Reorganizar o fluxo de decisao para:

- separar percepcao, contexto, decisao e execucao;
- manter o agente autonomo dentro do JADE;
- preservar comunicacao por mensagens ACL;
- preparar papeis taticos sem mudar a quantidade de jogadores.

## Escopo

Planejar `ContextoDecisao`, `ControladorDecisaoJogador`, `TipoDecisao`, `PerfilTatico`, `PapelJogador`, `Time` e o ciclo OODA no tick do jogador.

## Fora de Escopo

- Criar 2 jogadores por time.
- Implementar passe completo.
- Implementar marcacao coletiva.
- Implementar goleiro ou reescrever toda a disputa de bola.

## Criterios de Aceite

- O fluxo OODA do jogador esta claro.
- Esta claro o que permanece no `JogadorAgent`.
- Esta claro o que sai para classes de decisao/contexto.
