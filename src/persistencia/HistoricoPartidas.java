package persistencia;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoricoPartidas {

    private final Path arquivo;
    private final List<RegistroPartida> registros = new ArrayList<>();

    public HistoricoPartidas(Path arquivo) {
        this.arquivo = arquivo;
        carregarDoDisco();
    }

    private void carregarDoDisco() {
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            return;
        }
        try {
            List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
            for (String linha : linhas) {
                RegistroPartida registro = RegistroPartida.deLinhaCsv(linha);
                if (registro != null) {
                    registros.add(registro);
                }
            }
        } catch (IOException e) {
            registros.clear();
        }
    }

    public void adicionar(RegistroPartida registro) {
        if (registro == null) {
            return;
        }
        registros.add(registro);
        anexarNoDisco(registro);
    }

    private void anexarNoDisco(RegistroPartida registro) {
        if (arquivo == null) {
            return;
        }
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) {
                Files.createDirectories(pai);
            }
            boolean precisaCabecalho = !Files.exists(arquivo) || Files.size(arquivo) == 0;
            try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (precisaCabecalho) {
                    escritor.write(RegistroPartida.cabecalhoCsv());
                    escritor.write('\n');
                }
                escritor.write(registro.paraLinhaCsv());
                escritor.write('\n');
            }
        } catch (IOException e) {
            return;
        }
    }

    public List<RegistroPartida> getRegistros() {
        return Collections.unmodifiableList(new ArrayList<>(registros));
    }

    public int getTotalPartidas() {
        return registros.size();
    }

    public int getVitorias() {
        int vitorias = 0;
        for (RegistroPartida registro : registros) {
            if (registro.isVitoria()) {
                vitorias++;
            }
        }
        return vitorias;
    }

    public int getDerrotas() {
        return getTotalPartidas() - getVitorias();
    }

    public double getTaxaVitoria() {
        if (registros.isEmpty()) {
            return 0.0;
        }
        return getVitorias() * 100.0 / registros.size();
    }

    public double getTaxaVitoriaUltimas(int quantidade) {
        if (quantidade <= 0 || registros.isEmpty()) {
            return 0.0;
        }
        int considerados = Math.min(quantidade, registros.size());
        int vitorias = 0;
        for (int indice = registros.size() - considerados; indice < registros.size(); indice++) {
            if (registros.get(indice).isVitoria()) {
                vitorias++;
            }
        }
        return vitorias * 100.0 / considerados;
    }

    public int getSequenciaAtualVitorias() {
        int sequencia = 0;
        for (int indice = registros.size() - 1; indice >= 0; indice--) {
            if (!registros.get(indice).isVitoria()) {
                break;
            }
            sequencia++;
        }
        return sequencia;
    }

    public int getMelhorSequenciaVitorias() {
        int melhor = 0;
        int atual = 0;
        for (RegistroPartida registro : registros) {
            if (registro.isVitoria()) {
                atual++;
                if (atual > melhor) {
                    melhor = atual;
                }
            } else {
                atual = 0;
            }
        }
        return melhor;
    }
}
