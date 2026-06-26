# TechSpec: Refatoracao do Jogador com Ciclo OODA

> **Status:** rascunho tecnico
> **PRD:** `futebol-colaborativo/.ia/tasks/1-prd-refatoracao-arquitetura-jogador-ooda/1-prd.md`
> **Escopo:** refatoracao interna do `JogadorAgent`

---

## 1. Resumo Executivo

Esta TechSpec define uma refatoracao incremental do `JogadorAgent` para organizar seu ciclo de decisao em quatro etapas inspiradas em OODA:

```text
Observar -> Orientar -> Decidir -> Agir
```

A mudanca deve preservar a simulacao atual com 1 jogador azul e 1 jogador vermelho, mas ja deve introduzir a nocao de `Time` no backend. O jogador azul deve nascer como `Time.AZUL` e o vermelho como `Time.VERMELHO`. Nao faz parte desta TechSpec criar 2 jogadores por time, passe completo, marcacao coletiva, goleiro ou reescrever o protocolo de disputa.

---

## 2. Estado Atual

O `JogadorAgent` hoje concentra:

- ciclo JADE (`setup`, `TickerBehaviour`, `CyclicBehaviour`);
- leitura de mensagens ACL de disputa;
- decisao principal (`decidirAcaoPrincipal`);
- acoes concretas (`perseguirBola`, `agirComBola`, `agirSemBola`);
- chute;
- interceptacao;
- penalidade;
- posse;
- atualizacao de estado no `SistemaFutebol`.

O fluxo atual do tick deve continuar funcional:

```text
penalidade -> disputa -> tentativa de disputa -> decisao principal -> atualizacao de estado
```

---

## 3. Objetivo Tecnico

Substituir a decisao direta dentro de `JogadorAgent` por um fluxo mais explicito:

```text
JogadorAgent
  observa estado atual
  monta ContextoDecisao
  chama ControladorDecisaoJogador
  recebe TipoDecisao
  executa acao concreta existente
```

A primeira implementacao deve preservar comportamento. A mudanca e arquitetural, nao uma mudanca de estrategia de jogo.

---

## 4. Arquitetura Proposta

### 4.1. Pacotes novos

```text
com.futebol.colaborativo.jogo
  ContextoDecisao
  ConfiguracaoJogador
  TipoDecisao
  PapelJogador
  Time

com.futebol.colaborativo.estrategia
  PerfilTatico
  ControladorDecisaoJogador
  SeletorDecisaoPonderada
```

### 4.2. Responsabilidades

| Classe | Responsabilidade |
| --- | --- |
| `ContextoDecisao` | Representar a foto do momento usada para decidir |
| `ConfiguracaoJogador` | Concentrar configuracao inicial do jogador, incluindo time e papel |
| `TipoDecisao` | Enumerar a intencao escolhida pelo controlador |
| `PapelJogador` | Preparar papeis futuros sem criar subclasses ainda |
| `Time` | Identificar se o jogador pertence ao time azul ou vermelho |
| `PerfilTatico` | Guardar perfil base do jogador para uso futuro |
| `ControladorDecisaoJogador` | Escolher `TipoDecisao` a partir do contexto |
| `SeletorDecisaoPonderada` | Sortear decisoes simples por peso |

---

## 5. Contratos Tecnicos

### 5.1. `TipoDecisao`

Nesta primeira etapa, o enum deve refletir os ramos atuais do `JogadorAgent`:

```java
public enum TipoDecisao {
    PERSEGUIR_BOLA,
    AGIR_COM_BOLA,
    INTERCEPTAR,
    MANTER_POSICAO_DEFENSIVA,
    MANTER_POSICAO
}
```

`AGIR_COM_BOLA` preserva a logica atual de `agirComBola`, incluindo o modo de chute aleatorio de teste.

### 5.2. `PapelJogador`

```java
public enum PapelJogador {
    JOGADOR,
    ATACANTE,
    ZAGUEIRO
}
```

Nesta TechSpec, `ATACANTE` e `ZAGUEIRO` sao preparacao arquitetural. A simulacao continua 1x1.

### 5.3. `Time`

```java
public enum Time {
    AZUL,
    VERMELHO
}
```

Nesta etapa, `Time` deve ser implementado e conhecido pelo `JogadorAgent`, mas ainda nao deve alterar a regra de disputa. A logica de impedir disputa entre aliados pertence ao PRD de dois jogadores por time.

### 5.4. `ConfiguracaoJogador`

Criar uma configuracao publica de dominio para substituir gradualmente a configuracao privada atual de `SistemaFutebol`.

Campos recomendados:

```text
nome
xInicial
yInicial
golX
time
papel
```

Uso esperado nesta etapa:

```text
azul     -> Time.AZUL, PapelJogador.JOGADOR
vermelho -> Time.VERMELHO, PapelJogador.JOGADOR
```

### 5.5. `PerfilTatico`

Primeira versao simples, com probabilidade minima e controlada:

```java
public class PerfilTatico {
    private final PapelJogador papel;
    private final int pesoPerseguirBolaLivre;
    private final int pesoManterPosicaoDefensiva;
}
```

Deve existir uma fabrica simples, por exemplo:

```java
PerfilTatico.equilibrado()
```

Pesos iniciais do perfil equilibrado:

```text
Bola livre:
  PERSEGUIR_BOLA = 85
  MANTER_POSICAO_DEFENSIVA = 15

Outro jogador com bola:
  INTERCEPTAR = 100

Eu com bola:
  AGIR_COM_BOLA = 100
```

A probabilidade deve ser usada apenas no caso de bola livre. Nos outros casos, a decisao continua deterministica para preservar a simulacao.

### 5.6. `ContextoDecisao`

Campos recomendados:

```text
nomeJogador
estadoAtual
jogadorComBola
papel
time
perfilTatico
```

Metodos utilitarios esperados:

```text
existeJogadorComBola()
jogadorAtualEstaComBola()
bolaEstaLivre()
```

O contexto nao deve executar regra de jogo nem alterar estado. Ele apenas orienta a decisao.

### 5.7. `ControladorDecisaoJogador`

Contrato:

```java
public TipoDecisao decidir(ContextoDecisao contexto)
```

Regra inicial para preservar comportamento:

```text
se nao existe jogador com bola -> sorteia entre PERSEGUIR_BOLA e MANTER_POSICAO_DEFENSIVA
se jogador atual esta com bola -> AGIR_COM_BOLA
se outro jogador esta com bola -> INTERCEPTAR
caso contrario -> MANTER_POSICAO
```

Protecoes para evitar travar a simulacao:

```text
se jogador esta muito perto da bola -> forcar PERSEGUIR_BOLA
se a bola ficou livre por muitos ticks -> forcar PERSEGUIR_BOLA
```

O contador de bola livre pode ser mantido no `JogadorAgent` nesta primeira etapa, sem alterar `SistemaFutebol`.

### 5.8. `SeletorDecisaoPonderada`

Contrato sugerido:

```java
public TipoDecisao sortear(Map<TipoDecisao, Integer> pesos)
```

Regras:

- ignorar pesos menores ou iguais a zero;
- se existir apenas uma opcao valida, retornar essa opcao;
- se nao existir opcao valida, retornar `MANTER_POSICAO`;
- usar `Random` injetado ou criado internamente.

---

## 6. Mudancas no `JogadorAgent`

### 6.1. Campos novos

```text
ControladorDecisaoJogador controladorDecisao
PerfilTatico perfilTatico
PapelJogador papel
Time time
int ticksBolaLivre
```

Valores iniciais nesta etapa:

```text
perfilTatico = PerfilTatico.equilibrado()
papel = PapelJogador.JOGADOR
time = recebido via configuracao do jogador
```

O agente deve saber seu time desde o `setup`, mesmo que essa informacao ainda nao mude sua decisao.

### 6.2. Novo fluxo interno

`decidirAcaoPrincipal()` deve passar a ser algo equivalente a:

```java
ContextoDecisao contexto = montarContextoDecisao();
TipoDecisao decisao = controladorDecisao.decidir(contexto);
executarDecisao(decisao, contexto);
```

### 6.3. `montarContextoDecisao`

Responsavel por observar/orientar:

- obter `sistema.getJogadorComBola()`;
- incluir `estado`;
- incluir nome local do agente;
- incluir papel, time e perfil;
- nao alterar estado.

### 6.4. `executarDecisao`

Responsavel por agir:

```text
PERSEGUIR_BOLA -> agirSemJogadorComBola()
AGIR_COM_BOLA  -> agirComBola()
INTERCEPTAR    -> agirSemBola(contexto.getJogadorComBola())
MANTER_POSICAO_DEFENSIVA -> mover para uma posicao defensiva simples
MANTER_POSICAO -> nao move
```

Nesta primeira versao, a posicao defensiva simples pode ser o proprio gol do jogador: `getProprioGolX(), Ambiente.golY`. Os metodos de acao existentes podem continuar privados nesta etapa. A ideia e trocar o ponto de decisao, nao reescrever todas as acoes.

---

## 7. O Que Nao Deve Mudar Nesta TechSpec

- `App.java` deve continuar criando apenas `azul` e `vermelho`, mas agora informando `Time.AZUL` e `Time.VERMELHO`.
- `SistemaFutebol.criarJogador` deve aceitar ou montar uma `ConfiguracaoJogador` com `time` e `papel`.
- `JogadorEstado` nao precisa ganhar `time` ou `papel` ainda, para evitar mudanca no payload do frontend.
- O payload do WebSocket nao deve mudar.
- O frontend nao deve precisar de ajuste.
- A disputa ACL/FIPA deve continuar dentro do `JogadorAgent`.
- O chute aleatorio de teste deve manter o comportamento atual.

---

## 8. Sequencia Recomendada de Implementacao

1. Criar enums `TipoDecisao`, `PapelJogador` e `Time`.
2. Criar `ConfiguracaoJogador`.
3. Ajustar criacao de `azul` e `vermelho` para informar time.
4. Criar `PerfilTatico`.
5. Criar `ContextoDecisao`.
6. Criar `ControladorDecisaoJogador`.
7. Criar `SeletorDecisaoPonderada`.
8. Adicionar campos e inicializacao no `JogadorAgent`.
9. Substituir o corpo de `decidirAcaoPrincipal` pelo fluxo contexto/controlador/execucao.
10. Manter metodos de acao existentes como execucao concreta.
11. Rodar validacoes.

Esta lista orienta a futura quebra em tasks, mas nao cria tasks ainda.

---

## 9. Estrategia de Testes

### 9.1. Testes unitarios recomendados

Criar testes para `ControladorDecisaoJogador`:

- sem jogador com bola pode retornar `PERSEGUIR_BOLA` ou `MANTER_POSICAO_DEFENSIVA` conforme pesos;
- jogador atual com bola retorna `AGIR_COM_BOLA`;
- outro jogador com bola retorna `INTERCEPTAR`;
- jogador perto da bola livre retorna `PERSEGUIR_BOLA`;
- bola livre por muitos ticks retorna `PERSEGUIR_BOLA`;
- contexto inconsistente retorna `MANTER_POSICAO` quando aplicavel.

Criar testes para `SeletorDecisaoPonderada`:

- retorna opcao unica quando so ha um peso valido;
- ignora pesos zero/negativos;
- retorna `MANTER_POSICAO` quando nao ha opcoes validas.

### 9.2. Validacao automatizada

```powershell
cd futebol-colaborativo
mvn test
```

### 9.3. Validacao manual

- iniciar backend com `mvn exec:java`;
- confirmar criacao dos agentes `azul`, `vermelho` e `bola`;
- confirmar que jogadores continuam se movendo;
- confirmar que disputa de bola continua ocorrendo;
- confirmar que WebSocket/frontend continuam recebendo estado.

---

## 10. Riscos e Mitigacoes

| Risco | Mitigacao |
| --- | --- |
| Alterar comportamento ao refatorar decisao | Probabilidade so deve atuar na bola livre; demais ramos seguem determinísticos |
| Quebrar disputa ACL | Nao extrair disputa nesta etapa |
| Quebrar frontend | Nao alterar `JogadorEstado` serializado nem payload WebSocket |
| Criar abstração grande demais cedo | Manter classes pequenas e sem logica futura complexa |
| Confundir com PRD de 2 jogadores por time | `Time` e implementado, mas ainda nao muda disputa nem quantidade de jogadores |

---

## 11. Decisoes e Trade-offs

- `TipoDecisao` inicialmente espelha os ramos atuais, com adicao de `MANTER_POSICAO_DEFENSIVA`.
- `PerfilTatico` ja influencia minimamente a decisao quando a bola esta livre.
- `Time` deve ser implementado ja nesta etapa para `azul` e `vermelho`, mas sem impacto na regra de disputa.
- `PapelJogador` pode ser preparado com `JOGADOR` como padrao, sem criar atacante/zagueiro ainda.
- Disputa e interceptacao permanecem no `JogadorAgent` para reduzir risco.
- A arquitetura fica pronta para evoluir, mas a unica variacao comportamental planejada e a chance pequena de manter posicao defensiva quando a bola esta livre.

---

## 12. Fora de Escopo Tecnico

- Criar `JogadorAtacanteAgent` ou `JogadorZagueiroAgent`.
- Criar 2 jogadores por time.
- Impedir disputa entre aliados.
- Criar mensagens taticas de passe/cobertura.
- Implementar probabilidades dinamicas.
- Extrair `ProtocoloDisputaBola`.
- Extrair `CalculadoraInterceptacao`.
