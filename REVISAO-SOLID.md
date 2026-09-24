# Revisão final da atividade

Nome: Vinicios
Data: 24/09/2026

## Como validei a solução

Comandos executados (PowerShell, na raiz do projeto):

```powershell
javac -d out src/exercicio10/*.java
javac -d out src/solidexercicio10/model/*.java
javac -d out src/solidexercicio10/*.java            # pendente: depende do Lote B
java -cp out exercicio10.Main
java -cp out solidexercicio10.Main
```

- [x] compilação do código inicial;
- [ ] compilação da versão refatorada (aguardando Lote B);
- [ ] início de uma missão;
- [ ] movimentação, embarque e conclusão da missão;
- [ ] consulta e reset do ranking;
- [x] outro teste: programa temporário exercitando o modelo (pontuação, símbolos, movimento com limites e embarque) — todos os resultados esperados.

## Achados da revisão

Copie este bloco para cada ponto analisado:

```text
Local: src/exercicio10/Main.java (loadRanking, saveRanking, parseRankingJson)
Princípio relacionado: DIP
Observação: a regra de negócio lê e grava o ranking usando Path, Files e um "JSON" montado à mão, tudo dentro da classe principal.
Impacto: trocar a persistência (arquivo por banco ou memória) exige reescrever a classe; o fluxo fica difícil de testar sem tocar no disco.
Proposta: criar a interface RankingRepository e a implementação RankingService no Lote B (JogoService recebe a abstração).
Prioridade: alta
```

```text
Local: src/exercicio10/Main.java (desenharMapa e criarPassageiroPolimorfico)
Princípio relacionado: OCP / LSP
Observação: o desenho do mapa decide o símbolo com instanceof (Engenheiro/Astronauta) e strings getTipo().
Impacto: cada novo tipo de passageiro exigiria editar o desenho e a criação; o código "fecha" a modificação e dificulta a extensão.
Proposta: EntidadeMapa.getSimbolo() polimórfico (implementado no Lote A) — o renderer pergunta ao objeto, sem instanceof.
Prioridade: alta
```

```text
Local: src/exercicio10/Main.java (classe inteira)
Princípio relacionado: SRP
Observação: Main concentra menu, loop da partida, estatísticas, desenho do mapa e persistência do ranking.
Impacto: múltiplos motivos para mudar; qualquer alteração visual, de regra ou de arquivo passa pela mesma classe.
Proposta: camadas model / repository / presentation / service + Main só como ponto de entrada (Lote A feito, Lote B pendente).
Prioridade: alta
```

```text
Local: src/exercicio10/Passageiro.java
Princípio relacionado: LSP
Observação: a classe base é concreta e instanciável, com getPontuacao() padrão 10.
Impacto: um "Passageiro genérico" pode existir com comportamento surpreendente; o contrato de pontuação é fraco.
Proposta: tornar Passageiro abstrata e obrigar cada subtipo a declarar a pontuação (feito no Lote A).
Prioridade: média
```

```text
Local: src/exercicio10/Asteroide.java e Inimigo.java
Princípio relacionado: ISP
Observação: ambas duplicam o estado (x, y) e colideCom(Nave).
Impacto: código repetido e sem contrato comum de posição.
Proposta: interfaces Posicionavel (posição) e Movel (movimento) — Asteroide implementa só posição, Inimigo/Nave implementam as duas (feito no Lote A).
Prioridade: média
```

```text
Local: referência do tutorial (src/README.md, passo 5) — pontuação dos passageiros
Princípio relacionado: LSP
Observação: a referência usa Professor=15, Engenheiro=20 e Astronauta=10; o jogo original usa Professor=10, Engenheiro=15 e Astronauta=20.
Impacto: seguir a referência mudaria o resultado das partidas em relação ao jogo original que precisa ser preservado.
Proposta: manter a pontuação do jogo original (decisão da equipe).
Prioridade: alta
```

```text
Local: tutorialSolid.md (seção 12, Etapa 4) — JogoService de referência
Princípio relacionado: OCP
Observação: o código de referência do tutorialSolid.md nunca adiciona asteroides ao mapa (o método colocar(false, ...) não insere nada).
Impacto: o jogo ficaria sem obstáculos fixos, mudando o comportamento.
Proposta: usar como base o código do src/README.md e conferir comportamento por teste (Lote B).
Prioridade: média
```

```text
Local: src/README.md (Missao.moverInimigos) vs jogo original
Princípio relacionado: ISP
Observação: a referência move inimigos com Math.random()*3-1 sem verificar bordas; o original restringe movimento nos limites do mapa.
Impacto: inimigos poderiam sair do mapa na versão refatorada.
Proposta: manter movimento com limites (implementado no Lote A: moverInimigos(Random, minX, maxX, minY, maxY)).
Prioridade: média
```

```text
Local: service/JogoService.java (planejado)
Princípio relacionado: SRP
Observação: o serviço ainda concentra criação da missão, pontuação e fluxo — intencional nesta versão de ensino.
Impacto: se a criação de missões crescer, o serviço ganha um segundo motivo para mudar.
Proposta: futura extração para MissaoFactory ou serviço de pontuação; registrar prioridade média/baixa.
Prioridade: média
```

## Decisões com as quais concordo

Descreva uma decisão do tutorial e explique qual benefício ela traz.

1. **DIP com RankingRepository:** fazer o JogoService receber a abstração no construtor (em vez de conhecer o arquivo) permite trocar a persistência por memória/banco e facilita testes — decisão central do tutorial com a qual concordo.
2. **ISP com interfaces pequenas:** separar Posicionavel e Movel impede que Asteroide seja obrigado a ter movimento. É exatamente o "cliente não depende de métodos que não usa".
3. **Model separado do console/arquivo:** o domínio não importa Scanner, Path ou Files; ficou testável de forma isolada (comprovado no teste do Lote A).
4. **Símbolo polimórfico (getSimbolo):** substitui o instanceof do original e mantém o desenho aberto para novos tipos.

## Decisões com as quais não concordo

Descreva uma decisão que você alteraria ou manteria diferente. Considere complexidade, facilidade de teste e custo de manutenção.

1. **Pontuação dos passageiros:** a referência muda os valores (Professor 15, Engenheiro 20, Astronauta 10). Mantive os valores do jogo original (10/15/20) para preservar o comportamento exigido pela atividade.
2. **Dificuldade sem acentos:** a referência simplifica o enum e perde o toString/acentos do original. Mantive o original ("Fácil"/"Difícil" e aceitação de "fácil"/"difícil" com acento).
3. **Movimento dos inimigos:** a referência do src/README.md não limita o movimento às bordas; mantive os limites do original.
4. **Bug no tutorialSolid.md:** o código de lá não coloca asteroides no mapa; ignoramos essa referência para o comportamento.
5. **Formato do ranking:** o tutorial troca o JSON (ranking.json) por linhas com "|" (ranking-solid-exercicio10.json). Como é arquivo separado do original, aceito como decisão de design do tutorial, mas registro que essa validação precisa ser testada no Lote B.

## Melhoria implementada (opcional)

Descreva a alteração feita e compare o código antes e depois.

**Pendente — será escolhida depois do Lote B.** Candidatas:
- Implementar um `RankingRepository` em memória (lista) para testes, sem criar/apagar arquivos — comparação de `RankingService` (disco) vs `RankingEmMemoria` (memória).
- Extrair a criação da missão para uma `MissaoFactory` e mostrar antes/depois.