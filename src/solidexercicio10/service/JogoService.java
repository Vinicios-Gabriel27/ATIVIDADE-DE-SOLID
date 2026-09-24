package solidexercicio10.service;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Professor;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;

/**
 * Coordena o caso de uso da partida: menu, missão, pontuação e chamadas ao
 * mapa e ao ranking. Não conhece o arquivo de ranking nem desenha o grid.
 */
public class JogoService {
    private static final int CAPACIDADE_NAVE = 5;
    private static final int LIMITE_RANKING = 5;

    private final RankingRepository rankingRepository;
    private final MapaRenderer mapaRenderer;
    private final Random random;

    public JogoService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
        this.mapaRenderer = new MapaRenderer();
        this.random = new Random();
    }

    public void executarLoop(Scanner scanner) {
        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            String opcao = lerLinha(scanner, "Escolha uma opção: ", "1");
            if (opcao == null) {
                break;
            }
            switch (opcao) {
                case "1":
                    jogarPartida(scanner);
                    break;
                case "2":
                    exibirRanking();
                    break;
                case "3":
                    resetarRanking(scanner);
                    break;
                case "4":
                    rodando = false;
                    System.out.println("\nObrigado por jogar a Missão Marte Unifor!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Histórico de Ranking");
        System.out.println("4. Sair do Jogo");
        System.out.println("----------------------");
    }

    private void jogarPartida(Scanner scanner) {
        String pilotoNome = lerLinha(scanner, "\nDigite o nome do piloto: ", "Piloto Anônimo");
        if (pilotoNome == null) {
            return;
        }
        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        Dificuldade dificuldade = lerDificuldade(scanner);
        if (dificuldade == null) {
            return;
        }
        int tamanhoMapa = lerTamanhoMapa(scanner);
        int minX = -tamanhoMapa;
        int maxX = tamanhoMapa;
        int minY = -tamanhoMapa;
        int maxY = tamanhoMapa;

        System.out.println("\nIniciando missão na dificuldade " + dificuldade + "...");
        if (lerLinha(scanner, "Pressione Enter para decolar!", "") == null) {
            return;
        }

        Missao missao = criarNovaMissao(dificuldade, minX, maxX, minY, maxY);
        Nave nave = missao.getNave();
        int score = definirPontuacaoInicial(dificuldade);
        int movimentos = 0;
        boolean partidaAtiva = true;
        long tempoInicio = System.currentTimeMillis();

        while (partidaAtiva) {
            mapaRenderer.desenhar(missao, score, pilotoNome, minX, maxX, minY, maxY);
            int aBordo = nave.getPassageiros().size();
            int restantes = missao.getPassageiros().size();
            System.out.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes no mapa: %d | Total: %d%n",
                    nave.getX(), nave.getY(), score, nave.getVidas(), aBordo, nave.getCapacidade(), restantes, aBordo + restantes);

            String entrada = lerLinha(scanner, "Comando (w/s/a/d/c/q): ", "");
            if (entrada == null) {
                System.out.println("Missão abortada pelo piloto.");
                break;
            }
            if (entrada.isEmpty()) {
                continue;
            }

            char cmd = entrada.toLowerCase().charAt(0);
            if (cmd == 'q') {
                System.out.println("Missão abortada pelo piloto.");
                partidaAtiva = false;
                break;
            } else if (cmd == 'c') {
                Passageiro passageiro = missao.passagemNaPosicao();
                if (passageiro == null) {
                    System.out.println("Nenhum passageiro nesta posição.");
                } else if (missao.embarcarPassageiroNaPosicao()) {
                    int bonus = passageiro.getPontuacao();
                    score += bonus;
                    System.out.printf("Passageiro %s embarcado com sucesso! +%d pontos!%n", passageiro.getNome(), bonus);
                } else {
                    System.out.println("Nave cheia! Não há espaço para mais passageiros.");
                }
            } else if (cmd == 'w' || cmd == 's' || cmd == 'a' || cmd == 'd') {
                nave.moverComLimites(cmd, minX, maxX, minY, maxY);
                score--;
                movimentos++;
            } else {
                System.out.println("Comando inválido.");
                continue;
            }

            missao.moverInimigos(random, minX, maxX, minY, maxY);

            if (missao.verificaColisao()) {
                nave.perderVida();
                if (nave.getVidas() > 0) {
                    System.out.printf("Alerta! Colisão detectada! Vidas restantes: %d%n", nave.getVidas());
                } else {
                    System.out.println("GAME OVER! A nave foi destruída.");
                    partidaAtiva = false;
                }
            }

            if (score <= 0) {
                System.out.println("Combustível/Pontuação zerada! Missão perdida.");
                partidaAtiva = false;
            }

            if (missao.todosEmbarcados() && partidaAtiva) {
                if (nave.getX() == 0 && nave.getY() == 0) {
                    long tempoJogoSegundos = (System.currentTimeMillis() - tempoInicio) / 1000;
                    System.out.println("\n================================================================");
                    System.out.println("DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0).");
                    System.out.println("Retornando à órbita marciana com todos os passageiros. Missão cumprida!");
                    System.out.println("================================================================");
                    exibirEstatisticas(score, movimentos, tempoJogoSegundos, nave.getPassageiros().size());
                    salvarSeEntrouNoRanking(pilotoNome, score, dificuldade, nave.getPassageiros().size(), tempoJogoSegundos);
                    partidaAtiva = false;
                } else {
                    System.out.println("ALERTA: Todos os passageiros resgatados! Retorne para a Plataforma de Pouso 'L' em (0,0) para completar a missão.");
                }
            }
        }
    }

    private Dificuldade lerDificuldade(Scanner scanner) {
        String valor = lerLinha(scanner, "Escolha a Dificuldade (facil/medio/dificil): ", "medio");
        if (valor == null) {
            return null;
        }
        return Dificuldade.deString(valor);
    }

    private int lerTamanhoMapa(Scanner scanner) {
        String valor = lerLinha(scanner, "Tamanho do mapa (ex: 5 para mapa de -5 a +5): ", "5");
        if (valor == null) {
            return 5;
        }
        try {
            int tamanho = Integer.parseInt(valor);
            return tamanho > 0 ? tamanho : 5;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida, usando tamanho padrão (5).");
            return 5;
        }
    }

    private int definirPontuacaoInicial(Dificuldade dificuldade) {
        return switch (dificuldade) {
            case FACIL -> 30;
            case DIFICIL -> 15;
            default -> 20;
        };
    }

    private Missao criarNovaMissao(Dificuldade dificuldade, int minX, int maxX, int minY, int maxY) {
        int qtdPassageiros = 5;
        int qtdAsteroides = 2;
        int qtdInimigos = 2;
        if (dificuldade == Dificuldade.FACIL) {
            qtdPassageiros = 4;
            qtdAsteroides = 1;
            qtdInimigos = 1;
        } else if (dificuldade == Dificuldade.DIFICIL) {
            qtdPassageiros = 5;
            qtdAsteroides = 3;
            qtdInimigos = 3;
        }

        Nave nave = new Nave("A-1", 0, 0, CAPACIDADE_NAVE);
        Missao missao = new Missao(nave);
        posicionarPassageiros(missao, qtdPassageiros, minX, maxX, minY, maxY);
        posicionarAsteroides(missao, qtdAsteroides, minX, maxX, minY, maxY);
        posicionarInimigos(missao, qtdInimigos, minX, maxX, minY, maxY);
        return missao;
    }

    private Passageiro criarPassageiro(int indice, int x, int y) {
        return switch (indice % 5) {
            case 0 -> new Professor("Dr. Silva", x, y);
            case 1 -> new Engenheiro("Eng. Rosa", x, y);
            case 2 -> new Professor("Dr. Lima", x, y);
            case 3 -> new Engenheiro("Eng. Carlos", x, y);
            default -> new Astronauta("Ast. Maria", x, y);
        };
    }

    private void posicionarPassageiros(Missao missao, int quantidade, int minX, int maxX, int minY, int maxY) {
        int indice = 0;
        while (missao.getPassageiros().size() < quantidade) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY);
            if (posicao == null) {
                break;
            }
            missao.adicionarPassageiro(criarPassageiro(indice, posicao[0], posicao[1]));
            indice++;
        }
        if (missao.getPassageiros().size() < quantidade) {
            System.out.printf("Aviso: o mapa não coube todos os passageiros (%d/%d).%n",
                    missao.getPassageiros().size(), quantidade);
        }
    }

    private void posicionarAsteroides(Missao missao, int quantidade, int minX, int maxX, int minY, int maxY) {
        while (missao.getAsteroides().size() < quantidade) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY);
            if (posicao == null) {
                break;
            }
            missao.adicionarAsteroide(new Asteroide(posicao[0], posicao[1]));
        }
    }

    private void posicionarInimigos(Missao missao, int quantidade, int minX, int maxX, int minY, int maxY) {
        while (missao.getInimigos().size() < quantidade) {
            int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY);
            if (posicao == null) {
                break;
            }
            missao.adicionarInimigo(new Inimigo(posicao[0], posicao[1]));
        }
    }

    private int[] sortearPosicaoLivre(Missao missao, int minX, int maxX, int minY, int maxY) {
        int largura = maxX - minX + 1;
        int altura = maxY - minY + 1;
        int tentativas = Math.max(20, largura * altura * 2);
        for (int tentativa = 0; tentativa < tentativas; tentativa++) {
            int x = random.nextInt(largura) + minX;
            int y = random.nextInt(altura) + minY;
            if (!posicaoOcupada(missao, x, y)) {
                return new int[] { x, y };
            }
        }
        return null;
    }

    private boolean posicaoOcupada(Missao missao, int x, int y) {
        if (x == 0 && y == 0) {
            return true;
        }
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
            return true;
        }
        for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
                return true;
            }
        }
        for (Asteroide asteroide : missao.getAsteroides()) {
            if (asteroide.getX() == x && asteroide.getY() == y) {
                return true;
            }
        }
        for (Inimigo inimigo : missao.getInimigos()) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void exibirRanking() {
        System.out.println("\n====== RANKING TOP 5 PILOTOS ======");
        List<RankingEntry> ranking = rankingRepository.listar();
        if (ranking.isEmpty()) {
            System.out.println(" - Nenhum registro encontrado. Seja o primeiro a jogar!");
        } else {
            int limite = Math.min(LIMITE_RANKING, ranking.size());
            for (int i = 0; i < limite; i++) {
                RankingEntry entrada = ranking.get(i);
                System.out.printf("%d. %s - %d pts | Dificuldade: %s | Coletados: %d | Tempo: %ds | %s%n",
                        i + 1, entrada.name, entrada.score, entrada.dificuldade,
                        entrada.passageirosColetados, entrada.tempoJogo, entrada.dataHora);
            }
        }
        System.out.println("===================================");
    }

    private void exibirEstatisticas(int score, int movimentos, long tempoSegundos, int passageiros) {
        System.out.println("Estatísticas da Partida:");
        System.out.printf(" - Pontuação Final: %d pontos%n", score);
        System.out.printf(" - Movimentos Efetuados: %d%n", movimentos);
        System.out.printf(" - Tempo de Jogo: %d segundos%n", tempoSegundos);
        System.out.printf(" - Passageiros Resgatados: %d%n", passageiros);

        List<RankingEntry> ranking = rankingRepository.listar();
        int recorde = ranking.isEmpty() ? 0 : ranking.get(0).score;
        if (score > recorde && recorde > 0) {
            System.out.println("Novo recorde absoluto do sistema!");
        } else if (recorde > 0) {
            System.out.printf(" - Recorde atual a ser batido: %d pontos (Piloto: %s)%n", recorde, ranking.get(0).name);
        }
        System.out.println("================================================================");
    }

    private void salvarSeEntrouNoRanking(String nome, int score, Dificuldade dificuldade, int passageiros, long tempo) {
        if (score <= 0 || !entrouNoTopo(score)) {
            return;
        }
        try {
            rankingRepository.salvar(nome, score, dificuldade, passageiros, tempo);
            System.out.println("Parabéns! Você entrou para o Top 5 de pilotos!");
        } catch (IllegalStateException e) {
            System.out.println("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    private boolean entrouNoTopo(int score) {
        List<RankingEntry> ranking = rankingRepository.listar();
        if (ranking.size() < LIMITE_RANKING) {
            return true;
        }
        return score > ranking.get(ranking.size() - 1).score;
    }

    private void resetarRanking(Scanner scanner) {
        String confirmacao = lerLinha(scanner, "Você realmente deseja limpar o histórico de ranking? (s/n): ", "n");
        if (confirmacao == null) {
            return;
        }
        if (confirmacao.equalsIgnoreCase("s") || confirmacao.equalsIgnoreCase("sim")) {
            try {
                rankingRepository.limpar();
                System.out.println("Ranking resetado com sucesso!");
            } catch (IllegalStateException e) {
                System.out.println("Erro ao resetar ranking: " + e.getMessage());
            }
            return;
        }
        System.out.println("Operação cancelada.");
    }

    private String lerLinha(Scanner scanner, String prompt, String fallback) {
        if (prompt != null && !prompt.isEmpty()) {
            System.out.print(prompt);
        }
        if (!scanner.hasNextLine()) {
            return null;
        }
        String valor = scanner.nextLine().trim();
        return valor.isEmpty() ? fallback : valor;
    }
}
