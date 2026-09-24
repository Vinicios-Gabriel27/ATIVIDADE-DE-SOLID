# Revisão final da atividade

Nome: Vinicios
Data: 24/09/2026

## Como validei a solução

Comandos executados na raiz de `ATIVIDADE-DE-SOLID`:

```bash
javac -encoding UTF-8 -d out src/exercicio10/*.java
javac -encoding UTF-8 -d out $(find src/solidexercicio10 -name '*.java')
java -cp out solidexercicio10.Main
```

- [x] compilação do código inicial;
- [x] compilação da versão refatorada (model + Lote B);
- [x] início de uma missão nos três níveis;
- [x] movimentação, embarque e conclusão da missão;
- [x] consulta e reset do ranking;
- [x] outro teste: modelo do Lote A (pontuação, símbolos, movimento com limites e embarque) e `RankingService` isolado (top 5, nome com `|`, linha corrompida e `limpar`).

Resultados conferidos na versão refatorada:

- fácil: 4 passageiros, 1 asteroide, 1 inimigo, 30 pontos, capacidade 5, ninguém na origem;
- médio: 5 / 2 / 2, 20 pontos, com astronauta;
- difícil, digitando `difícil`: 5 / 3 / 3, 15 pontos, rótulo "Difícil";
- `w` tira a nave de `(0,0)` e leva a `(0,1)`;
- partida fácil concluída: embarque de professor (+10) e engenheiro (+15), retorno à plataforma, estatísticas e entrada no top 5;
- o arquivo gravado ficou no formato `nome|pontos|FACIL|4|data|tempo` (o enum entra como `name()`, a tela mostra "Fácil");
- reset com `n` cancela; com `s` apaga o arquivo e a consulta seguinte volta vazia;
- opção inválida no menu continua no laço.

## Achados da revisão

Copie este bloco para cada ponto analisado:

```text
Local: src/exercicio10/Main.java (loadRanking, saveRanking, parseRankingJson)
Princípio relacionado: DIP
Observação: a regra de negócio lê e grava o ranking usando Path, Files e um "JSON" montado à mão, tudo dentro da classe principal.
Impacto: trocar a persistência (arquivo por banco ou memória) exige reescrever a classe; o fluxo fica difícil de testar sem tocar no disco.
Proposta: criar a interface RankingRepository e a implementação RankingService no Lote B (JogoService recebe a abstração). Feito: só o Main instancia RankingService.
Prioridade: alta
```

```text
Local: src/exercicio10/Main.java (desenharMapa e criarPassageiroPolimorfico)
Princípio relacionado: OCP / LSP
Observação: o desenho do mapa decide o símbolo com instanceof (Engenheiro/Astronauta) e strings getTipo().
Impacto: cada novo tipo de passageiro exigiria editar o desenho e a criação; o código "fecha" a modificação e dificulta a extensão.
Proposta: EntidadeMapa.getSimbolo() polimórfico (Lote A). No Lote B, MapaRenderer usa esse método; não compara getTipo() nem usa instanceof.
Prioridade: alta
```

```text
Local: src/exercicio10/Main.java (classe inteira)
Princípio relacionado: SRP
Observação: Main concentra menu, loop da partida, estatísticas, desenho do mapa e persistência do ranking.
Impacto: múltiplos motivos para mudar; qualquer alteração visual, de regra ou de arquivo passa pela mesma classe.
Proposta: camadas model / repository / presentation / service + Main só como ponto de entrada. Lote A e Lote B feitos. src/exercicio10 não foi alterado.
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
Proposta: no Lote B o JogoService coloca os asteroides e usa as quantidades do jogo original (fácil 4/1/1, médio 5/2/2, difícil 5/3/3). Conferido no mapa.
Prioridade: média
```

```text
Local: src/README.md (Missao.moverInimigos) vs jogo original
Princípio relacionado: ISP
Observação: a referência move inimigos com Math.random()*3-1 sem verificar bordas; o original restringe movimento nos limites do mapa.
Impacto: inimigos poderiam sair do mapa na versão refatorada.
Proposta: manter movimento com limites. O Lote A expõe moverInimigos(Random, minX, maxX, minY, maxY); o JogoService do Lote B chama esse método. Na partida jogada, o inimigo não saiu do mapa.
Prioridade: média
```

```text
Local: src/solidexercicio10/service/JogoService.java
Princípio relacionado: SRP
Observação: o serviço ainda concentra criação da missão, pontuação e fluxo — intencional nesta versão de ensino. A criação dos tipos continua num switch (índice % 5, como no original).
Impacto: se a criação de missões crescer, o serviço ganha um segundo motivo para mudar. Um passageiro novo ainda exige editar esse ponto de criação; não exige editar o renderer nem a soma da pontuação.
Proposta: futura extração para MissaoFactory ou serviço de pontuação, só se a geração passar a mudar por outro motivo.
Prioridade: média
```

```text
Local: src/solidexercicio10/service/JogoService.java (definirPontuacaoInicial, criarNovaMissao, criarPassageiro)
Princípio relacionado: OCP
Observação: um repositório novo de ranking não exige editar o serviço. Uma dificuldade nova, ou um passageiro novo, ainda cai num switch deste arquivo (pontuação inicial, quantidades e o ciclo Dr. Silva / Eng. Rosa / Dr. Lima / Eng. Carlos / Ast. Maria).
Impacto: a persistência está aberta para extensão; o catálogo da missão ainda se estende editando código estável. Com três níveis e três tipos, uma tabela à parte custaria mais do que evita.
Proposta: manter os switches iguais ao jogo original. Se aparecer um quarto nível, mover quantidades e pontuação inicial para o enum Dificuldade.
Prioridade: média
```

```text
Local: presentation/MapaRenderer.java (classe concreta) e repository/RankingRepository.java (salvar com dois argumentos)
Princípio relacionado: DIP
Observação: RankingRepository cobre uma variação real (arquivo, memória ou banco). Não há interface de renderer: existe uma tela só. O atalho salvar(nome, pontos) é default da interface e o laço da partida não o usa; o serviço chama o salvar completo.
Impacto: criar MapaRenderer como interface agora só aumentaria arquivo. O atalho não obriga o JogoService a depender de um método que ele não chama.
Proposta: manter o renderer concreto e o método default. Não acrescentar abstração só para repetir o desenho do tutorial.
Prioridade: baixa
```

```text
Local: src/solidexercicio10/service/JogoService.java (posicionarAsteroides e posicionarInimigos) e a ausência de testes no repositório
Princípio relacionado: SRP
Observação: se o mapa acaba as casas livres, só os passageiros geram aviso. Asteroide ou inimigo que não couber deixa de ser colocado sem mensagem. O repositório não traz teste automatizado; a checagem foi rodar o jogo e exercitar o RankingService fora do projeto.
Impacto: no tamanho 1, o difícil pode vir com menos perigos e o jogador não vê o motivo. Quem mudar o formato do arquivo não tem um teste versionado que falhe junto.
Proposta: avisar também quando asteroide ou inimigo não couber. A suíte pode ficar de fora desta entrega; o roteiro da seção "Como validei" cobre o fluxo pedido.
Prioridade: baixa
```

## Modificações do Lote B

O model do Lote A ficou como estava, com um acréscimo: `model/Dificuldade.java` não existia e o ranking e o serviço dependem dele. O enum repete o do jogo original (aceita "fácil"/"difícil" e o `toString` devolve "Fácil"/"Médio"/"Difícil"). `src/exercicio10` não mudou.

```text
Local: src/solidexercicio10/repository/ (RankingEntry, RankingRepository, RankingService) e Main.java
Princípio relacionado: DIP
Observação: RankingEntry só guarda os campos de uma pontuação. RankingRepository declara salvar, listar e limpar. RankingService é quem usa Path e Files, no arquivo ranking-solid-exercicio10.json, com campos separados por "|". O Main cria o RankingService e passa a interface para o JogoService.
Impacto: trocar arquivo por memória ou banco mexe na borda (o Main e uma classe nova), não na regra de embarque, colisão ou pontuação.
Proposta: manter o concreto só no Main. O atalho salvar(nome, pontos) ficou como método default da interface, para uma implementação nova não reescrever o mesmo encaminhamento.
Prioridade: alta
```

```text
Local: src/solidexercicio10/service/JogoService.java, presentation/MapaRenderer.java e Main.java
Princípio relacionado: SRP
Observação: o fluxo da partida (menu, comandos, pontuação, quando salvar) está no serviço. O desenho do grid, da legenda e da lista de passageiros está no MapaRenderer. A gravação está no RankingService. O Main só imprime a abertura e monta as dependências.
Impacto: mudar o texto do mapa não altera a regra; mudar o formato do arquivo não altera o desenho. O serviço continua grande de propósito: criação da missão, pontuação e laço ainda moram juntos.
Proposta: a separação mapa / fluxo / arquivo fica como está. MissaoFactory só se a geração do mapa passar a ter outro motivo para mudar.
Prioridade: alta
```

```text
Local: src/solidexercicio10/presentation/MapaRenderer.java
Princípio relacionado: OCP
Observação: a referência do tutorial escolhe P, E ou T comparando getTipo(). O renderer do Lote B pede getSimbolo() à EntidadeMapa (nave, passageiro, asteroide e inimigo). A plataforma L e o vazio "." continuam na apresentação, porque não são entidades do domínio.
Impacto: um tipo novo de passageiro, com o próprio símbolo, aparece no mapa sem editar o renderer. A legenda ainda cita P/E/T à mão; isso é texto fixo, não a decisão de qual célula desenhar.
Proposta: manter sem uma interface de renderer. Só existe uma apresentação; criar interface agora seria antecipação.
Prioridade: alta
```

```text
Local: src/solidexercicio10/service/JogoService.java (embarque e criarPassageiro)
Princípio relacionado: LSP
Observação: o embarque soma passageiro.getPontuacao() e o mapa usa getSimbolo(), sem instanceof. A criação segue o ciclo do original: Dr. Silva, Eng. Rosa, Dr. Lima, Eng. Carlos e Ast. Maria. Pontuação dos tipos continua a do Lote A (professor 10, engenheiro 15, astronauta 20). Pontuação inicial: fácil 30, médio 20, difícil 15.
Impacto: qualquer subtipo que cumpra nome, tipo, símbolo e pontuação entra na lista da missão. O switch de criação ainda conhece as classes concretas; isso é o ponto em que um tipo novo precisaria ser incluído.
Proposta: manter o ciclo do jogo original. Na partida fácil testada, o embarque creditou +10 e +15; no médio, o astronauta apareceu no mapa.
Prioridade: alta
```

```text
Local: src/solidexercicio10/repository/RankingRepository.java
Princípio relacionado: ISP
Observação: o contrato tem as operações que o jogo usa (salvar completo, listar, limpar). O salvar só com nome e pontos é default e chama o método completo com Médio, zero passageiros e zero tempo. O serviço não é obrigado a usar esse atalho. Posicionavel e Movel, do Lote A, continuam separados: o renderer não move ninguém.
Impacto: uma implementação nova escreve um salvar, não dois. O JogoService não recebe método de arquivo, Scanner ou desenho dentro do repositório.
Proposta: não fatiar a interface em duas enquanto listar, salvar e limpar forem o mesmo cliente (o fluxo da partida). O reset com confirmação (s/n) ficou no serviço, porque é diálogo, não persistência.
Prioridade: média
```

```text
Local: src/solidexercicio10/service/JogoService.java (criarNovaMissao e moverInimigos)
Princípio relacionado: OCP / comportamento preservado
Observação: a referência do tutorialSolid não adiciona asteroides; a do src/README.md põe 6 passageiros no difícil e iguala a capacidade da nave a essa quantidade. O serviço usa as regras do jogo original: fácil 4/1/1, médio 5/2/2, difícil 5/3/3, capacidade fixa 5. Inimigos andam com moverInimigos(random, minX, maxX, minY, maxY), então não saem do mapa. A origem (0,0) não recebe passageiro nem perigo.
Impacto: a partida refatorada continua comparável à original. O mapa imprime o y maior em cima, para a tecla w (que soma 1 em y no model) subir na tela.
Proposta: manter essas quantidades. Conferido nos três níveis; na partida jogada até o fim, os asteroides não trocaram de casa e o inimigo ficou dentro dos limites.
Prioridade: alta
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
5. **Formato do ranking:** o tutorial troca o JSON (`ranking.json`) por linhas com `|` no arquivo `ranking-solid-exercicio10.json`. O arquivo é outro, então o jogo original não é afetado. Testei o ciclo: gravar, listar na ordem da pontuação, ignorar linha corrompida e apagar no reset. A extensão continua `.json` e o conteúdo não é JSON; mantenho o nome do tutorial e registro a diferença. O arquivo guarda o `name()` do enum (`FACIL`); a tela usa o `toString` ("Fácil").

## Melhoria implementada (opcional)

**O mapa pergunta o símbolo ao objeto, em vez de decidir o tipo na apresentação.**

Antes, no jogo original, `desenharMapa` escolhia `E`, `T` ou `P` com `instanceof`. A referência do tutorial em `src/README.md` troca isso por `getTipo().equals("Engenheiro")` e `equals("Astronauta")`. Os dois fecham o desenho: um passageiro novo obriga editar o renderer.

Depois, `MapaRenderer.simboloDe` lê `EntidadeMapa.getSimbolo()`. Nave, passageiro, asteroide e inimigo entregam o próprio caractere. A plataforma `L` e o vazio `.` ficam no renderer porque não são entidades. Incluir um subtipo de `Passageiro` com símbolo próprio não altera essa classe.

No mesmo lote, duas diferenças da referência foram corrigidas para o jogo não mudar de regra: os asteroides entram no mapa (o bloco do `tutorialSolid.md` não os coloca) e as quantidades continuam as do original, com capacidade 5. O `RankingService` também regrava só o top 5 e pula linha inválida, para um registro quebrado não apagar o resto nem estourar a leitura.

## Prioridade das melhorias

1. Alta, já feita no Lote B: tirar arquivo e desenho do `Main`; `JogoService` depende de `RankingRepository`; o mapa lê `getSimbolo()`.
2. Média, não feita: extrair `MissaoFactory` se a criação da missão crescer. Hoje o switch ainda cabe no serviço.
3. Média, não feita: se entrar um quarto nível, mover quantidades e pontuação inicial para `Dificuldade`.
4. Baixa, não feita: avisar quando asteroide ou inimigo não couber no mapa; encapsular os campos públicos de `RankingEntry`.