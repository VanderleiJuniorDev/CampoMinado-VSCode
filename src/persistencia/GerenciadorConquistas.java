package persistencia;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GerenciadorConquistas {

    private static final String MODO_TREINO = "treino";
    private static final String MODO_DIARIO = "diario";
    private static final String MODO_VIDAS = "vidas";
    private static final String MODO_TOROIDAL = "toroidal";
    private static final String DIFICULDADE_INTERMEDIARIO = "Intermediário";
    private static final String DIFICULDADE_AVANCADO = "Avançado";
    private static final long LIMITE_SEGUNDOS_VELOCISTA = 30;
    private static final int VITORIAS_VETERANO = 10;
    private static final int PARTIDAS_MARATONISTA = 50;
    private static final int CHORDS_MAESTRO = 10;
    private static final int SEQUENCIA_EM_CHAMAS = 3;

    private final Path arquivo;
    private final EnumSet<Conquista> desbloqueadas = EnumSet.noneOf(Conquista.class);

    public GerenciadorConquistas(Path arquivo) {
        this.arquivo = arquivo;
        carregarDoDisco();
    }

    private void carregarDoDisco() {
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            return;
        }
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                String nome = linha.trim();
                if (nome.isEmpty()) {
                    continue;
                }
                try {
                    desbloqueadas.add(Conquista.valueOf(nome));
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            desbloqueadas.clear();
        }
    }

    public Set<Conquista> getDesbloqueadas() {
        return Collections.unmodifiableSet(EnumSet.copyOf(desbloqueadas));
    }

    public boolean isDesbloqueada(Conquista conquista) {
        return conquista != null && desbloqueadas.contains(conquista);
    }

    public List<Conquista> avaliar(RegistroPartida ultima, HistoricoPartidas historico) {
        List<Conquista> novas = new ArrayList<>();
        if (ultima == null || ultima.temModo(MODO_TREINO)) {
            return novas;
        }
        EnumSet<Conquista> atingidas = calcularAtingidas(ultima, historico);
        for (Conquista conquista : Conquista.values()) {
            if (atingidas.contains(conquista) && !desbloqueadas.contains(conquista)) {
                desbloqueadas.add(conquista);
                novas.add(conquista);
            }
        }
        if (!novas.isEmpty()) {
            salvar();
        }
        return novas;
    }

    private EnumSet<Conquista> calcularAtingidas(RegistroPartida ultima, HistoricoPartidas historico) {
        EnumSet<Conquista> atingidas = EnumSet.noneOf(Conquista.class);
        if (contarPartidasValidas(historico) >= PARTIDAS_MARATONISTA) {
            atingidas.add(Conquista.MARATONISTA);
        }
        if (!ultima.isVitoria()) {
            return atingidas;
        }
        atingidas.add(Conquista.PRIMEIRA_VITORIA);
        if (ultima.getBandeirasUsadas() == 0) {
            atingidas.add(Conquista.MAOS_LIMPAS);
        }
        if (ultima.getSegundos() < LIMITE_SEGUNDOS_VELOCISTA) {
            atingidas.add(Conquista.VELOCISTA);
        }
        if (contarVitoriasValidas(historico) >= VITORIAS_VETERANO) {
            atingidas.add(Conquista.VETERANO);
        }
        boolean avancado = mesmaDificuldade(ultima.getDificuldade(), DIFICULDADE_AVANCADO);
        boolean intermediario = mesmaDificuldade(ultima.getDificuldade(), DIFICULDADE_INTERMEDIARIO);
        if (avancado) {
            atingidas.add(Conquista.MESTRE_AVANCADO);
        }
        if ((avancado || intermediario) && ultima.getDicasUsadas() == 0) {
            atingidas.add(Conquista.SEM_AJUDA);
        }
        if (ultima.temModo(MODO_VIDAS) && ultima.getVidasPerdidas() >= 1) {
            atingidas.add(Conquista.SOBREVIVENTE);
        }
        if (ultima.temModo(MODO_DIARIO)) {
            atingidas.add(Conquista.DESAFIO_DIARIO);
        }
        if (ultima.getChordsUsados() >= CHORDS_MAESTRO) {
            atingidas.add(Conquista.MAESTRO);
        }
        if (historico != null && historico.getSequenciaAtualVitorias() >= SEQUENCIA_EM_CHAMAS) {
            atingidas.add(Conquista.EM_CHAMAS);
        }
        if (ultima.temModo(MODO_TOROIDAL)) {
            atingidas.add(Conquista.EXPLORADOR);
        }
        return atingidas;
    }

    private static int contarPartidasValidas(HistoricoPartidas historico) {
        if (historico == null) {
            return 1;
        }
        int total = 0;
        for (RegistroPartida registro : historico.getRegistros()) {
            if (!registro.temModo(MODO_TREINO)) {
                total++;
            }
        }
        return total;
    }

    private static int contarVitoriasValidas(HistoricoPartidas historico) {
        if (historico == null) {
            return 1;
        }
        int total = 0;
        for (RegistroPartida registro : historico.getRegistros()) {
            if (registro.isVitoria() && !registro.temModo(MODO_TREINO)) {
                total++;
            }
        }
        return total;
    }

    private static boolean mesmaDificuldade(String primeira, String segunda) {
        return semAcentos(primeira).equals(semAcentos(segunda));
    }

    private static String semAcentos(String texto) {
        if (texto == null) {
            return "";
        }
        String decomposto = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        return decomposto.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private void salvar() {
        if (arquivo == null) {
            return;
        }
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) {
                Files.createDirectories(pai);
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                for (Conquista conquista : desbloqueadas) {
                    escritor.write(conquista.name());
                    escritor.write('\n');
                }
            }
        } catch (IOException e) {
            return;
        }
    }
}
