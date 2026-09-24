package solidexercicio10.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import solidexercicio10.model.Dificuldade;

/**
 * Persistência do ranking em arquivo texto (campos separados por "|").
 * Detalhe de infraestrutura: o restante do jogo só enxerga {@link RankingRepository}.
 */
public class RankingService implements RankingRepository {
    private static final int LIMITE_RANKING = 5;
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Path arquivo;

    public RankingService(String nomeArquivo) {
        this.arquivo = Paths.get(nomeArquivo);
    }

    @Override
    public void salvar(String nome, int pontuacao, Dificuldade dificuldade, int passageirosColetados, long tempoJogo) {
        try {
            List<RankingEntry> entradas = lerArquivo();
            String dataHora = LocalDateTime.now().format(DATA_HORA);
            entradas.add(new RankingEntry(
                    sanitizar(nome),
                    pontuacao,
                    dificuldade == null ? Dificuldade.MEDIO : dificuldade,
                    passageirosColetados,
                    dataHora,
                    tempoJogo
            ));
            entradas.sort(Comparator.comparingInt((RankingEntry entrada) -> entrada.score).reversed());
            if (entradas.size() > LIMITE_RANKING) {
                entradas = new ArrayList<>(entradas.subList(0, LIMITE_RANKING));
            }
            gravar(entradas);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o ranking", e);
        }
    }

    @Override
    public List<RankingEntry> listar() {
        try {
            List<RankingEntry> ranking = lerArquivo();
            ranking.sort(Comparator.comparingInt((RankingEntry entrada) -> entrada.score).reversed());
            return ranking;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void limpar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível limpar o ranking", e);
        }
    }

    private List<RankingEntry> lerArquivo() throws IOException {
        List<RankingEntry> ranking = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return ranking;
        }
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            if (linha.isBlank()) {
                continue;
            }
            String[] partes = linha.split("\\|", -1);
            if (partes.length < 6) {
                continue;
            }
            try {
                ranking.add(new RankingEntry(
                        partes[0],
                        Integer.parseInt(partes[1].trim()),
                        Dificuldade.deString(partes[2]),
                        Integer.parseInt(partes[3].trim()),
                        partes[4],
                        Long.parseLong(partes[5].trim())
                ));
            } catch (NumberFormatException ignored) {
                // Linha corrompida não derruba o ranking inteiro.
            }
        }
        return ranking;
    }

    private void gravar(List<RankingEntry> entradas) throws IOException {
        List<String> linhas = new ArrayList<>();
        for (RankingEntry entrada : entradas) {
            linhas.add(entrada.name + "|" + entrada.score + "|" + entrada.dificuldade.name()
                    + "|" + entrada.passageirosColetados + "|" + entrada.dataHora + "|" + entrada.tempoJogo);
        }
        Path pai = arquivo.getParent();
        if (pai != null) {
            Files.createDirectories(pai);
        }
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    private String sanitizar(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Piloto Anônimo";
        }
        return nome.replace("|", " ").replace("\n", " ").replace("\r", " ").trim();
    }
}
