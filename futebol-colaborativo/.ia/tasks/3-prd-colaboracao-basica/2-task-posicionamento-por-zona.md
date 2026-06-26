# Task 4: Posicionamento por Zona

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** Task 3 (chute dirigido)

---

## Problema

Quando a bola está no campo adversário, o zagueiro pode sortear `PERSEGUIR_BOLA` e
avançar até o campo ofensivo junto com o atacante. Isso deixa o campo defensivo vazio
e não corresponde ao papel tático esperado de um zagueiro.

O comportamento deve ser não-rígido: não é uma barreira física, mas uma penalidade
forte nos pesos do controlador quando a bola está do lado errado do campo.

---

## Objetivo

Fazer o controlador de decisão considerar a posição da bola relativa ao meio-campo ao
calcular os pesos do zagueiro para bola livre.

Quando a bola está no campo adversário, o zagueiro quase sempre mantém posição
defensiva em vez de ir buscar a bola.

---

## Escopo

Alterar `ContextoDecisao.java`, `JogadorAgent.java` e `ControladorDecisaoJogador.java`.

---

## Requisitos

### ContextoDecisao.java

Adicionar campo calculado no construtor:

```java
private final boolean bolaNoFieldAdversario;
```

Getter:

```java
public boolean isBolaNoFieldAdversario() {
    return bolaNoFieldAdversario;
}
```

Cálculo no construtor (a partir de dados já disponíveis):

```java
double meioX = Ambiente.largura / 2.0;
double bolaDist = Math.abs(Ambiente.bola.x - estado.golX);
double propriaDist = Math.abs(Ambiente.bola.x - (estado.golX == 0 ? Ambiente.largura : 0));
this.bolaNoFieldAdversario = bolaDist < propriaDist;
```

Interpretação: a bola está mais perto do gol que o jogador ataca do que do gol próprio.

### ControladorDecisaoJogador.java

Na decisão de `bola livre` quando `papel == ZAGUEIRO`:

Substituir o uso direto de `perfilTatico.getPesosBolaLivre()` por uma verificação:

```java
if (papel == PapelJogador.ZAGUEIRO && contexto.isBolaNoFieldAdversario()) {
    // pesos fortemente defensivos quando bola está no campo adversário
    // PERSEGUIR_BOLA = 5, MANTER_POSICAO_DEFENSIVA = 95
    return selecionarComPesos(Map.of(
        TipoDecisao.PERSEGUIR_BOLA, 5,
        TipoDecisao.MANTER_POSICAO_DEFENSIVA, 95
    ));
}
// caso normal: usa os pesos do perfil tático
return selecionarComPesos(contexto.getPerfilTatico().getPesosBolaLivre());
```

### JogadorAgent.java

No método `montarContextoDecisao()`, o `ContextoDecisao` já vai receber o novo
campo automaticamente se o construtor for atualizado — nenhuma outra mudança é
necessária aqui, além de verificar que o construtor de `ContextoDecisao` é chamado
com todos os parâmetros corretos.

---

## Fora de Escopo

- Alterar comportamento do atacante (ele já tende a perseguir pelo perfil).
- Criar barreira física que impeça o zagueiro de cruzar o meio-campo.
- Alterar qualquer outro arquivo além dos três acima.
- Implementar passe (Task 5).

---

## Validação

```powershell
cd futebol-colaborativo
mvn test
mvn exec:java
```

Em outro terminal:

```powershell
cd frontend
npm run dev
```

### Checklist visual

- [ ] Quando a bola está no campo adversário, o zagueiro raramente vai buscá-la
- [ ] Quando a bola está no campo próprio, o zagueiro vai buscá-la normalmente (pesos 25/75)
- [ ] O atacante continua perseguindo a bola independente do lado do campo
- [ ] Nenhuma regressão: disputa entre adversários continua funcionando
- [ ] Nenhuma regressão: gols continuam sendo registrados

---

## Critérios de Sucesso

- Código compila.
- Testes passam.
- Visualmente: zagueiro fica no campo defensivo quando a bola está longe (campo adversário).
- Zagueiro ainda vai buscar a bola ocasionalmente quando ela está no campo próprio.

---

## Resultado

- `ContextoDecisao.java`: adicionado campo `bolaNoFieldAdversario` (calculado no
  construtor a partir de `Ambiente.bola.x` e `estadoAtual.golX`) + getter
  `isBolaNoFieldAdversario()`. Importado `Ambiente`.
- `ControladorDecisaoJogador.java`: em `decidirComBolaLivre`, antes do sorteio normal,
  adicionada a regra: se `papel == ZAGUEIRO` e bola no campo adversario, sorteia com
  pesos `PERSEGUIR_BOLA=5 / MANTER_POSICAO_DEFENSIVA=95`. Imports `PapelJogador` e `Map`.
- `JogadorAgent.java`: nenhuma mudanca necessaria (campo calculado dentro do construtor;
  assinatura inalterada).
- Adicionado teste `zagueiroMantemPosicaoQuandoBolaEstaNoCampoAdversario`
  (deterministico via `RandomFixo(50)`, robusto a ordem do `Map.of`).
- 16 testes passaram (`mvn clean test`), antes eram 15.
- Nota de comportamento: as regras de override existentes (jogador perto da bola e
  bola livre por muitos ticks) ainda tem prioridade sobre a regra de zona, conforme
  o escopo da task (que pedia para alterar apenas o sorteio final).
- Validacao visual pendente — executar `mvn exec:java` + frontend.
