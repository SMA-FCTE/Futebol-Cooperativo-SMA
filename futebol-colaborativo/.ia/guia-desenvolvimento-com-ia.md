# Guia: Desenvolvimento Assistido por IA com Processo

Esse guia descreve uma metodologia para usar IA como apoio de engenharia em projetos de software reais, e não apenas como “autocomplete melhorado”. A ideia é simples: antes de pedir código, você reduz ambiguidade. Você usa a IA para pensar, estruturar, documentar, decompor e revisar — não apenas para digitar.

O objetivo deste documento é te ensinar a montar esse fluxo no seu próprio projeto, com os mesmos commands, templates e mentalidade.

---

## 1. A mentalidade antes da ferramenta

A primeira coisa que precisa entrar na sua cabeça é: **não comece pelo código**.

A maior parte dos problemas que aparecem em projetos com IA não vem da IA escrever código ruim. Vem do desenvolvedor entregar um pedido vago, aceitar a primeira saída, dar merge e descobrir o problema duas semanas depois. Isso tem nome: **vibe coding**. Você produz mudança sem controle real sobre escopo, regra, impacto e corretude.

O modelo tradicional usa IA só na parte mecânica do trabalho: gerar snippet, completar função, sugerir refactor. Isso ajuda a digitar código, mas ajuda pouco a estruturar pensamento, reduzir ambiguidade, registrar decisões, quebrar escopo e revisar coerência entre produto e implementação. Em features reais, esse tipo de clareza costuma valer mais do que o ganho de velocidade na digitação.

A proposta aqui é inverter a ordem: **a IA entra antes da implementação**. Ela ajuda a transformar uma ideia em PRD, depois em TechSpec, depois em tasks pequenas, depois em execução, review e Pull Request. Assim, a IA deixa de atuar apenas como autocomplete e passa a apoiar também a modelagem do problema, a qualidade da decisão técnica e a organização do trabalho.

Isso **não** reduz a responsabilidade do desenvolvedor. Pelo contrário: exige mais critério. Você continua sendo a autoridade final sobre escopo, arquitetura, validação e qualidade. A diferença é que a IA deixa de ser só um apoio local de código e passa a estruturar raciocínio, documentação e decomposição da entrega.

Resumindo: o ponto não é que usar IA como suporte pontual seja “errado”. É que esse uso é insuficiente em projetos com regra de negócio relevante. O objetivo é capturar mais valor da IA sem abrir mão de rigor técnico.

---

## 2. O pipeline em uma linha

```
PRD → TechSpec → Tasks → Execução → Review → Pull Request
```

Cada etapa existe para **obrigar clareza antes da próxima**. Você não passa pra TechSpec sem ter PRD travado. Não quebra em tasks sem TechSpec. Não executa sem task. Não dá merge sem review. E não fecha PR sem cenário de validação.

Vamos olhar cada etapa.

### 2.1. PRD — o quê e por quê

O PRD (Product Requirements Document) descreve a feature do ponto de vista de produto e negócio.

Ele responde:
- Qual problema estamos resolvendo?
- Qual é o objetivo da entrega?
- Quem é o usuário e qual o fluxo principal?
- Quais endpoints, telas, regras de negócio importam?
- O que está dentro e o que está fora de escopo?
- Como saber se deu certo? (métricas)

O PRD **não** discute detalhes de implementação. Ele trava o “o que” e o “porquê”. A tentação de já cair em decisão técnica aqui é forte — resista.

### 2.2. TechSpec — o como

A TechSpec traduz o PRD em decisões técnicas implementáveis.

Ela responde:
- Quais módulos, tabelas, DTOs, serviços, guards e fluxos entram?
- Como ficam os contratos de request e response?
- Quais invariantes precisam ser protegidos (unicidade, integridade, idempotência)?
- Quais edge cases e riscos técnicos existem?
- Qual a estratégia de testes?

A TechSpec é o **contrato técnico entre produto e engenharia**. Ela precisa ser completa o suficiente para que outra pessoa (ou um agente de IA) implemente a solução sem precisar adivinhar decisões arquiteturais.

Detalhe importante: a TechSpec **não é o código final**. Ela inclui apenas snippets curtos (5–10 linhas) para ilustrar padrões. O código real vem na execução das tasks.

### 2.3. Tasks — quebra de implementação

Depois da TechSpec, você quebra a feature em tasks pequenas, com dependência clara e critério de aceite objetivo.

Uma boa task:
- tem objetivo único;
- tem escopo limitado (idealmente até 1–2 dias);
- pode ser validada;
- pode ser revisada sem depender de adivinhação;
- é commitável isoladamente.

Isso reduz a chance da IA tentar resolver tudo de uma vez num patch enorme e opaco. E te dá pontos de checkpoint para corrigir rumo.

Granularidade típica:
- **Tarefa principal (X.0)** — entregável de 1–2 dias. Se passar disso, divida.
- **Subtarefas (X.Y)** — passos pequenos, com execução clara.

### 2.4. Execução — IA implementa uma task por vez

Aqui a IA finalmente coloca a mão no código. O ideal é que cada task:
- gere mudanças coerentes;
- rode validações locais (lint, typecheck, testes);
- tenha impacto limitado;
- possa ser commitada separadamente.

Regra forte: **a IA nunca executa `git commit` sozinha**. Ela faz `git add` (stage) e sugere a mensagem de commit. O controle do commit fica com você. Isso evita que mudanças sem revisão entrem no histórico.

### 2.5. Review — três lentes

A review **não é formalidade**. Ela tem três lentes complementares, e se você só aplicar uma, revisou raso.

- **Micro (dentro do diff)** — bugs, off-by-one, null check, tratamento de erro, edge case nas linhas alteradas, testes da própria mudança.
- **Macro (fora do diff)** — quem consome o que mudou? Algum contrato quebrou? A mudança segue o padrão de outros módulos? Há regressão em features adjacentes? Migrations reversíveis?
- **Spec (intenção × implementação)** — cada critério de aceite do PRD/TechSpec foi implementado? Há scope creep ou lacuna? Alguma regra crítica foi alterada sem documentação?

Se todos os findings forem de estilo no diff, sua review está superficial. Volte para Macro e Spec.

### 2.6. Pull Request — fechamento narrativo

A PR não substitui o código nem a TechSpec. Ela conta a história da mudança e, principalmente, **permite que outro dev rode o setup e valide os cenários sem depender de você**.

Toda PR deve ter:
- contexto e motivação;
- o que mudou (alto nível, sem inventário de arquivos);
- impactos (rotas, contratos, migrations, variáveis);
- **setup autossuficiente** — comandos reais, `.env`, seeds, como obter token;
- **cenários de validação** — chamadas HTTP completas, payloads, status esperado;
- notas de deploy (migrations, backfills, riscos);
- validação automatizada (lint, testes, build).

Se o revisor não consegue reproduzir os cenários só lendo a PR, ela está incompleta.

---

## 3. O pré-requisito: documento de contexto

Antes de qualquer feature, você precisa de um documento que descreva o **contexto do projeto**. Sem isso, a IA trata cada task como um problema isolado e perde consistência entre features.

O contexto deve registrar:
- **Domínio e princípios** — qual é o produto, qual o público, quais decisões valem para tudo.
- **Escopo do MVP** — o que está dentro e o que está fora.
- **Regras de negócio** — invariantes, fórmulas, estados, transições, permissões.
- **Entidades principais** — modelos do domínio e relações.
- **Stack** — linguagens, frameworks, banco, integrações.
- **Padrões esperados** — estrutura de pastas, naming, convenções de teste, timezone, formatação de datas.

Esse documento é a **fonte de verdade** que todos os commands leem antes de produzir qualquer artefato. Ele evita que a IA invente convenções diferentes a cada conversa.

Dica prática: deixe esse documento num lugar fácil de referenciar (por exemplo, `contexto.md` na raiz). Atualize-o sempre que uma decisão estrutural mudar.

---

## 4. Os commands (prompts reutilizáveis)

Cada etapa do pipeline tem um **command** correspondente — um prompt longo, versionado, que descreve o papel da IA, as regras críticas, o fluxo de trabalho e os checklists para aquela etapa.

A intenção não é automatizar cegamente. É **padronizar o processo** para que a IA opere com contexto, disciplina e checkpoints toda vez que você invoca aquela etapa.

Os seis commands recomendados são:

### 4.1. `criar-prd.md`
Especialista em PRDs. Antes de gerar qualquer coisa, ele faz perguntas de esclarecimento (problema, usuário, escopo, métricas, riscos). Só depois das respostas é que ele produz o PRD usando o template padrão. Termina salvando em `tasks/prd-[nome-da-feature]/prd.md`.

Regra crítica do command: **nunca gere o PRD sem antes esclarecer**. Isso protege você de PRDs genéricos que “preenchem o template” mas não capturam a feature.

### 4.2. `criar-techspec.md`
Arquiteto de software sênior. Lê o PRD, analisa profundamente a base de código existente (módulos relacionados, padrões em uso, contratos atuais, schema do banco), formula esclarecimentos técnicos e só então redige a TechSpec.

Esse é o command mais denso, porque é onde a IA precisa olhar para fora do PRD e entender o sistema real. Ele inclui checklists para arquitetura, banco, segurança, observabilidade, testes e impacto.

### 4.3. `criar-tasks.md`
Lê PRD e TechSpec e quebra a feature em tasks pequenas. Antes de gerar qualquer arquivo, ele apresenta a **lista high-level para aprovação**. Só depois cria `tasks.md` (resumo) e um arquivo `[num]_task.md` para cada tarefa principal.

Cada task individual deve ser auto-contida: incluir inline os trechos relevantes da TechSpec (schema, endpoint, contrato) para que o executor não precise navegar entre arquivos.

### 4.4. `executar-task.md`
Implementa uma task por vez. Lê a task, o PRD, a TechSpec, marca status como `in-progress`, executa, roda lint/testes/typecheck, marca como `completed` e faz `git add` dos arquivos. **Não comita**.

Esse command tem regras fortes sobre padrões de código: nomes que comunicam intenção, comentários só onde a decisão não for óbvia, comentários que explicam o *porquê* e não o *o quê*.

### 4.5. `fazer-review.md`
Lê o diff e aplica as três lentes (Micro, Macro, Spec). Roda lint e testes. Produz um relatório com findings agrupados por lente e por severidade (Crítico, Importante, Menor). **Não corrige nada** — só revisa.

Tem um checklist anti-superficial: “se todos os findings forem só estilo no diff, você revisou raso, volte para Macro e Spec”.

### 4.6. `criar-pull-request.md`
Gera o **texto** da PR (título + corpo) a partir do diff e dos artefatos da feature. Não faz commit, não cria branch, não executa `gh pr create`. Você decide o que fazer com o texto.

O foco é gerar uma PR **autossuficiente para validação**: comandos reais, payloads, status esperados, SQL idempotente quando necessário.

---

## 5. Os templates

Cada command produz um artefato em formato padronizado. Os templates ficam em `templates/` e são referenciados pelos commands. Padronizar a saída tem três vantagens:

1. **Previsibilidade** — você sempre sabe onde está cada coisa em um PRD, em uma TechSpec, em uma task.
2. **Comparabilidade** — duas features escritas pela mesma equipe seguem o mesmo formato.
3. **Reusabilidade** — quando alguém entra no time, o padrão já está documentado.

Templates recomendados:

- **`prd-template.md`** — visão geral, objetivos, métricas do MVP, histórias de usuário, funcionalidades principais com requisitos funcionais numerados, estados e transições, UX, restrições, fora de escopo, riscos.
- **`techspec-template.md`** — resumo executivo, arquitetura, interfaces e contratos, modelos de dados, endpoints, navegação (se houver frontend), regras de negócio, integrações externas, segurança, observabilidade, estratégia de testes, sequenciamento, decisões e trade-offs.
- **`tasks-template.md`** — tabela resumo com ID, título, tamanho (S/M/L), área, dependências e critério de aceite.
- **`task-template.md`** — status, contexto da task, visão geral, requisitos obrigatórios, subtarefas, detalhes de implementação, padrões, decisões, como validar manualmente, critérios de sucesso, arquivos relevantes.
- **`PULL_REQUEST_TEMPLATE.md`** — descrição, impactos, setup, cenários, deploy, validação automatizada.

Os templates não precisam ser idênticos aos do meu projeto — adapte ao seu domínio. Mas mantenha o esqueleto: cada artefato tem campos obrigatórios e checkpoints de qualidade.

---

## 6. Estrutura de pastas recomendada

Uma forma simples de organizar tudo:

```text
seu-projeto/
├── .ia/
│   ├── commands/
│   │   ├── criar-prd.md
│   │   ├── criar-techspec.md
│   │   ├── criar-tasks.md
│   │   ├── executar-task.md
│   │   ├── fazer-review.md
│   │   └── criar-pull-request.md
│   ├── templates/
│   │   ├── prd-template.md
│   │   ├── techspec-template.md
│   │   ├── tasks-template.md
│   │   ├── task-template.md
│   │   └── PULL_REQUEST_TEMPLATE.md
│   └── contexto.md          # documento de contexto do projeto
│   ├── tasks/
│   │   └── prd-[nome-da-feature]/
│   │       ├── prd.md
│   │       ├── techspec.md
│   │       ├── tasks.md
│   │       ├── 1_task.md
│   │       ├── 2_task.md
│   │       └── pull_request.md
│   └── rules/                   # regras locais opcionais
│       └── *.md
└── (resto do projeto)
```

---

## 7. Boas práticas

### 7.1. Não pule etapas
Se a feature tem regra de negócio relevante, não comece pela implementação. Primeiro trave PRD, depois TechSpec, depois tasks. Pular direto pro código quase sempre custa caro depois.

### 7.2. Peça clareza, não “mágica”
Prompts ruins:
- “faz essa feature completa”
- “implementa isso aí”

Prompts melhores:
- escopo claro
- regra de negócio explícita
- endpoint definido
- expectativa de validação
- restrições conhecidas

Quanto melhor o contexto, melhor o resultado.

### 7.3. Revise o que a IA produziu
Nunca assuma que “parece bom” significa “está correto”. Leia com mais atenção:
- regras de negócio
- autenticação e autorização
- concorrência
- persistência
- serialização
- erros e status HTTP
- testes

Boilerplate de framework costuma ser o menos perigoso. O risco real normalmente está na lógica.

### 7.4. Quebre o trabalho em partes pequenas
Features grandes viram tasks pequenas. Isso melhora qualidade, revisão, testabilidade, rollback e entendimento.

### 7.5. Atualize a documentação quando a implementação mudar
Se a implementação final exigir decisão diferente da planejada, reflita isso na TechSpec e, se necessário, no PRD. **Documento desatualizado atrapalha mais do que ajuda**.

### 7.6. Não trate a IA como autoridade final
A IA ajuda a pensar, estruturar e acelerar. A responsabilidade de engenharia continua humana. O padrão esperado é: questionar, validar, pedir justificativa, testar, revisar.

### 7.7. Use validação objetiva
Sempre que possível, feche cada entrega com:
- lint
- build
- testes unitários
- testes de integração ou e2e
- cenários manuais descritos com clareza

Se não foi validado, deixe explícito.

---

## 8. Como não virar “vibe coder”

“Vibe coding” é produzir mudança sem controle real sobre escopo, regra, impacto e corretude. Para evitar isso:

- não aceite código sem entender o fluxo principal;
- não misture várias decisões grandes no mesmo passo;
- não esconda regra de negócio dentro de detalhes de framework;
- não deixe a documentação para depois;
- não faça merge de algo que só “parece funcionar”;
- não substitua engenharia por entusiasmo com ferramenta.

O papel da IA é acelerar engenharia, não substituir critério técnico.

---

## 9. Como começar do zero no seu projeto

Se você quer aplicar esse fluxo num projeto novo (ou existente), siga essa ordem:

1. **Crie o documento de contexto.** Anote domínio, escopo do MVP, regras críticas, stack, padrões. Não precisa ser perfeito — você vai melhorando.
2. **Crie a estrutura `.ia`** com `commands/` e `templates/`. Copie/adapte os seis commands e os cinco templates.
3. **Escolha uma feature pequena** para o primeiro ciclo. Não comece com a feature mais complexa do roadmap.
4. **Rode o ciclo completo**: invoque o command `criar-prd`, responda às perguntas, gere PRD. Depois `criar-techspec`. Depois `criar-tasks`. Depois `executar-task` (uma de cada vez). Depois `fazer-review`. Depois `criar-pull-request`.
5. **Ajuste os commands** com base no que funcionou ou não. Esses prompts são vivos. Quando você notar que a IA pula uma verificação importante, adicione no command.
6. **Mantenha o contexto atualizado.** Toda decisão estrutural nova vai para lá.

---

## 10. Resumo

A sequência `PRD → TechSpec → Tasks → Execução → Review → Pull Request` existe para transformar velocidade em **qualidade sustentável**.

Ela te ajuda a:
- pensar melhor antes de codar;
- documentar decisões enquanto elas estão frescas;
- reduzir retrabalho;
- revisar com mais qualidade;
- evoluir o produto sem perder coerência.

A IA entra como **multiplicador de produtividade**. O processo existe para garantir que esse ganho não vire desorganização.

Não é um dogma. É um conjunto de checkpoints que evita os erros mais comuns de quem desenvolve com IA. Use, adapte e melhore.
