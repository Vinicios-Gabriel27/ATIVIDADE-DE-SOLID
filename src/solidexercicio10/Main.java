package solidexercicio10;

import java.util.Scanner;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.repository.RankingService;
import solidexercicio10.service.JogoService;

/**
 * Ponto de entrada. Só escolhe as implementações concretas e entrega o contrato ao jogo.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("             MISSÃO MARTE UNIFOR - VERSÃO SOLID                  ");
        System.out.println("================================================================");
        System.out.println("  Pilote sua nave, salve os passageiros e desvie dos perigos!   ");
        System.out.println("================================================================");

        RankingRepository repository = new RankingService("ranking-solid-exercicio10.json");
        JogoService jogo = new JogoService(repository);

        try (Scanner scanner = new Scanner(System.in)) {
            jogo.executarLoop(scanner);
        }
    }
}
