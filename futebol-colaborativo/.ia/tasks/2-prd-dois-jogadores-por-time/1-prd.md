# PRD: Dois Jogadores por Time

> **Escopo:** expansao da simulacao para 2x2

## Resumo

Esta mudanca expande a simulacao para 2 jogadores azuis e 2 jogadores vermelhos. Cada time deve ter 1 atacante e 1 zagueiro.

Esta entrega depende da refatoracao arquitetural do jogador com OODA estar pronta ou bem planejada, para evitar aumentar a complexidade do `JogadorAgent` atual.

## Problema

Hoje a simulacao funciona principalmente como 1 jogador azul contra 1 jogador vermelho. Ainda nao existe diferenciacao clara entre aliados e adversarios, nem papeis taticos consolidados.

Com 4 jogadores, o sistema precisa evitar disputa entre jogadores do mesmo time e permitir comportamento diferente para atacante e zagueiro.

## Objetivo

Permitir uma simulacao inicial com 2 jogadores azuis e 2 jogadores vermelhos, sendo 1 atacante e 1 zagueiro por time.

## Escopo

- Criacao dos 4 jogadores no bootstrap.
- Uso de `Time` para diferenciar aliados e adversarios.
- Uso de `PapelJogador` para atacante e zagueiro.
- Regra para impedir disputa entre jogadores do mesmo time.
- Posicionamento inicial adequado para atacante e zagueiro.

## Fora de Escopo

- Criar goleiro.
- Implementar passe completo.
- Implementar estrategia coletiva sofisticada ou probabilidades dinamicas complexas.
- Refatorar profundamente o frontend.

## Criterios de Aceite

- A simulacao possui 4 jogadores planejados.
- Cada jogador tem time e papel inicial definidos.
- Aliados nao sao tratados como oponentes em disputa.
