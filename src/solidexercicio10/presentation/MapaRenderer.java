package solidexercicio10.presentation;

import solidexercicio10.model.Asteroide;
import solidexercicio10.model.EntidadeMapa;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Passageiro;

/**
 * Desenha o estado da missão no console. Não move a nave, não pontua e não grava ranking.
 */
public class MapaRenderer {
    public void desenhar(Missao missao, int pontos, String piloto, int minX, int maxX, int minY, int maxY) {
        System.out.println();
        System.out.printf("Mapa da Missão | Pontos: %d | Piloto: %s%n", pontos, piloto);
        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.printf(" %2d", x);
        }
        System.out.println();
        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.print(" __");
        }
        System.out.println();

        for (int y = maxY; y >= minY; y--) {
            System.out.printf("%3d|", y);
            for (int x = minX; x <= maxX; x++) {
                System.out.printf(" %2c", simbolo(missao, x, y));
            }
            System.out.println();
        }

        System.out.println("Legenda: @=Nave, L=Plataforma, P=Professor, E=Engenheiro, T=Astronauta, #=Asteroide, X=Inimigo, .=Vazio");
        System.out.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
        System.out.println("Passageiros na superfície marciana:");
        for (Passageiro passageiro : missao.getPassageiros()) {
            System.out.printf(" - %s (%s) em (%d,%d)%n",
                    passageiro.getNome(), passageiro.getTipo(), passageiro.getX(), passageiro.getY());
        }
    }

    private char simbolo(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
            return simboloDe(missao.getNave());
        }
        for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
                return simboloDe(passageiro);
            }
        }
        for (Asteroide asteroide : missao.getAsteroides()) {
            if (asteroide.getX() == x && asteroide.getY() == y) {
                return simboloDe(asteroide);
            }
        }
        for (Inimigo inimigo : missao.getInimigos()) {
            if (inimigo.getX() == x && inimigo.getY() == y) {
                return simboloDe(inimigo);
            }
        }
        if (x == 0 && y == 0) {
            return 'L';
        }
        return '.';
    }

    private char simboloDe(EntidadeMapa entidade) {
        String simbolo = entidade.getSimbolo();
        if (simbolo == null || simbolo.isEmpty()) {
            return '?';
        }
        return simbolo.charAt(0);
    }
}
