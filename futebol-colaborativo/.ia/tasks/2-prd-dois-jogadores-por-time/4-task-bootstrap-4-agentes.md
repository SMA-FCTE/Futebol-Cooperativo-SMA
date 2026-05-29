# Task 2: Bootstrap dos 4 Agentes e Selecao de Perfil por Papel

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Substituir os agentes `azul` e `vermelho` pelos 4 agentes do 2x2, cada um com
posicao inicial, time e papel corretos. Fazer o `JogadorAgent` selecionar o
`PerfilTatico` de acordo com o papel recebido.

## Escopo

Alterar `App.java` e `JogadorAgent.java`.

## Requisitos

### App.java

Substituir a criacao dos 2 agentes atuais pelos 4 agentes abaixo:

| Agente | x | y | golX | Time | Papel |
|---|---|---|---|---|---|
| azul-atacante | 45 | 30 | 100 (Ambiente.largura) | AZUL | ATACANTE |
| azul-zagueiro | 20 | 30 | 100 (Ambiente.largura) | AZUL | ZAGUEIRO |
| vermelho-atacante | 55 | 30 | 0 | VERMELHO | ATACANTE |
| vermelho-zagueiro | 80 | 30 | 0 | VERMELHO | ZAGUEIRO |

As constantes `AZUL_X_INICIAL`, `AZUL_Y_INICIAL`, `VERMELHO_X_INICIAL`, `VERMELHO_Y_INICIAL`
podem ser removidas e substituidas pelos valores diretos ou por novas constantes nomeadas por papel.

### JogadorAgent.java

No `setup()`, substituir a selecao fixa de `PerfilTatico.equilibrado()` por selecao baseada no papel:

```java
perfilTatico = switch (papel) {
    case ATACANTE -> PerfilTatico.atacante();
    case ZAGUEIRO -> PerfilTatico.zagueiro();
    default -> PerfilTatico.equilibrado();
};
```

## Fora de Escopo

- Alterar filtro de disputa por time (Task 3).
- Alterar frontend (Task 4).
- Criar subclasses de `JogadorAgent`.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

Verificar nos logs do `mvn exec:java` que os 4 agentes sao criados:

```
Jogador criado: azul-atacante
Jogador criado: azul-zagueiro
Jogador criado: vermelho-atacante
Jogador criado: vermelho-zagueiro
```

## Criterios de Sucesso

- Backend compila e sobe com 4 agentes.
- Cada agente usa o perfil correspondente ao seu papel.
- Testes existentes continuam passando.
