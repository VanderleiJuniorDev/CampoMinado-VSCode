package persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PerfilJogador {

    private static final String ARQUIVO_HISTORICO = "historico.csv";
    private static final String ARQUIVO_RECORDES = "recordes.properties";
    private static final String ARQUIVO_CONQUISTAS = "conquistas.txt";

    private final String nome;
    private final Path diretorio;
    private final HistoricoPartidas historico;
    private final Recordes recordes;
    private final GerenciadorConquistas conquistas;

    public PerfilJogador(String nome, Path diretorio) {
        this.nome = nome;
        this.diretorio = diretorio;
        criarDiretorio(diretorio);
        this.historico = new HistoricoPartidas(diretorio.resolve(ARQUIVO_HISTORICO));
        this.recordes = new Recordes(diretorio.resolve(ARQUIVO_RECORDES));
        this.conquistas = new GerenciadorConquistas(diretorio.resolve(ARQUIVO_CONQUISTAS));
    }

    private static void criarDiretorio(Path diretorio) {
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            return;
        }
    }

    public String getNome() {
        return nome;
    }

    public HistoricoPartidas getHistorico() {
        return historico;
    }

    public Recordes getRecordes() {
        return recordes;
    }

    public GerenciadorConquistas getConquistas() {
        return conquistas;
    }

    public Path getDiretorio() {
        return diretorio;
    }
}
