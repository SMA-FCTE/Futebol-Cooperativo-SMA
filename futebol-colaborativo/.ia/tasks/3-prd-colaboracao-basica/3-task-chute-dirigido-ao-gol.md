# Task 3: Chute Dirigido ao Gol com Ruído Angular

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

---

## Problema

`chutarAleatorioParaTeste()` gera força completamente aleatória em X e Y, sem referência
ao `golX` do agente. A bola vai para qualquer direção, inclusive de volta para o campo próprio.
O usuário quer que a bola vá *aproximadamente* para o lado do gol adversário, mas com desvios
naturais — inclusive desvios grandes ocasionais para parecer comportamento humano.

---

## Objetivo

Substituir o chute aleatório por um chute que usa o ângulo em direção ao gol como base,
com ruído angular de até ±120°. A maioria dos chutes vai para o lado certo; ocasionalmente
erra feio ou vai para o campo próprio.

---

## Escopo

Alterar apenas `JogadorAgent.java`.

---

## Requisitos

### Constantes a adicionar

```java
private static final double MAX_DESVIO_ANGULO = Math.PI * 2.0 / 3.0; // 120 graus
private static final double FORCA_CHUTE_DIRIGIDO = 1.35;
```

### Constante a remover

```java
// remover:
private static final double FORCA_CHUTE_ALEATORIO_TESTE = 1.35;
```

### Método a criar: `chutarDirigidoAoGol()`

```java
private void chutarDirigidoAoGol() {
    double anguloBase = Math.atan2(Ambiente.golY - estado.y, golX - estado.x);
    double ruido = (random.nextDouble() * 2.0 - 1.0) * MAX_DESVIO_ANGULO;
    double anguloFinal = anguloBase + ruido;

    double forcaX = Math.cos(anguloFinal) * FORCA_CHUTE_DIRIGIDO;
    double forcaY = Math.sin(anguloFinal) * FORCA_CHUTE_DIRIGIDO;

    estado.comBola = false;

    ACLMessage chute = new ACLMessage(ACLMessage.INFORM);
    chute.addReceiver(new AID("bola", AID.ISLOCALNAME));
    chute.setContent(forcaX + "," + forcaY);
    send(chute);

    if (sistema != null) {
        sistema.atualizarEstado(getLocalName(), estado);
    }

    System.out.printf("[%s] chute dirigido | angulo_base=%.0f° | desvio=%.0f° | forca=(%.2f, %.2f)%n",
            getLocalName(),
            Math.toDegrees(anguloBase),
            Math.toDegrees(ruido),
            forcaX, forcaY);
}
```

### Alteração em `agirComBola()`

Substituir:

```java
if (USAR_CHUTE_ALEATORIO_TESTE) {
    chutarAleatorioParaTeste();
} else {
    conduzirAteOGol();
}
```

Por:

```java
if (USAR_CHUTE_ALEATORIO_TESTE) {
    chutarDirigidoAoGol();
} else {
    conduzirAteOGol();
}
```

### Método a remover

`chutarAleatorioParaTeste()` pode ser removido inteiramente.

---

## Fora de Escopo

- Alterar `conduzirAteOGol()` ou `chutarParaOGol()`.
- Alterar qualquer outro arquivo.
- Implementar divisão de papéis (task 4).
- Implementar passe (task 5).

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

- [ ] Log mostra `chute dirigido | angulo_base=...° | desvio=...°` em vez de `chute aleatorio de teste`
- [ ] A bola vai para o lado do gol adversário na maioria dos chutes
- [ ] Ocasionalmente a bola vai para o lado errado ou para trás (comportamento esperado)
- [ ] Zagueiros continuam mantendo posição defensiva (nenhuma regressão)
- [ ] Gols continuam sendo registrados

---

## Critérios de Sucesso

- Código compila.
- Testes passam.
- Visualmente: a bola vai para o lado correto na maioria dos chutes.
- `chutarAleatorioParaTeste()` e `FORCA_CHUTE_ALEATORIO_TESTE` não existem mais no arquivo.

---

## Resultado

- `chutarDirigidoAoGol()` criado com fórmula `atan2 + ruído angular uniforme em [-120°, +120°]`.
- `chutarAleatorioParaTeste()` e `FORCA_CHUTE_ALEATORIO_TESTE` removidos.
- `agirComBola()` atualizado para chamar `chutarDirigidoAoGol()` no modo teste.
- 15 testes passaram (`mvn clean test`).
- Validação visual pendente — executar `mvn exec:java` + frontend para confirmar o checklist visual.
