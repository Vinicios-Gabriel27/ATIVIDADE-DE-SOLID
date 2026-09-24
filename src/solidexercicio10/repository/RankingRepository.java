package solidexercicio10.repository;

import java.util.List;
import solidexercicio10.model.Dificuldade;

/**
 * O que o jogo precisa do ranking: salvar, listar e limpar.
 * A regra da partida depende deste contrato, não de arquivo ou banco (DIP).
 */
public interface RankingRepository {
    /**
     * Atalho sem os dados completos da missão.
     * Fica na interface para uma implementação nova não repetir o mesmo encaminhamento.
     */
    default void salvar(String nome, int pontuacao) {
        salvar(nome, pontuacao, Dificuldade.MEDIO, 0, 0);
    }

    void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo);

    List<RankingEntry> listar();

    void limpar();
}
