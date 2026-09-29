package persistencia;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class Recordes {

    private final Path arquivo;
    private final Map<String, Long> melhoresTempos = new LinkedHashMap<>();

    public Recordes(Path arquivo) {
        this.arquivo = arquivo;
        carregarDoDisco();
    }

    private void carregarDoDisco() {
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            return;
        }
        Properties propriedades = new Properties();
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            propriedades.load(leitor);
        } catch (IOException | IllegalArgumentException e) {
            return;
        }
        for (String dificuldade : propriedades.stringPropertyNames()) {
            try {
                melhoresTempos.put(dificuldade, Long.parseLong(propriedades.getProperty(dificuldade).trim()));
            } catch (NumberFormatException e) {
                continue;
            }
        }
    }

    public Long getMelhorTempo(String dificuldade) {
        if (dificuldade == null) {
            return null;
        }
        return melhoresTempos.get(dificuldade);
    }

    public boolean registrar(String dificuldade, long segundos) {
        if (dificuldade == null || segundos < 0) {
            return false;
        }
        Long atual = melhoresTempos.get(dificuldade);
        if (atual != null && atual <= segundos) {
            return false;
        }
        melhoresTempos.put(dificuldade, segundos);
        salvar();
        return true;
    }

    public Map<String, Long> todos() {
        return new LinkedHashMap<>(melhoresTempos);
    }

    private void salvar() {
        if (arquivo == null) {
            return;
        }
        Properties propriedades = new Properties();
        for (Map.Entry<String, Long> entrada : melhoresTempos.entrySet()) {
            propriedades.setProperty(entrada.getKey(), String.valueOf(entrada.getValue()));
        }
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) {
                Files.createDirectories(pai);
            }
            try (Writer escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                propriedades.store(escritor, "Recordes");
            }
        } catch (IOException e) {
            return;
        }
    }
}
