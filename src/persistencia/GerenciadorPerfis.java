package persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GerenciadorPerfis {

    private static final String PASTA_PERFIS = "perfis";
    private static final String ARQUIVO_NOME = "nome.txt";
    private static final String PERFIL_PADRAO = "Jogador";
    private static final int TAMANHO_MAXIMO_NOME = 20;

    private final Path diretorioPerfis;
    private final Map<String, String> perfisEmMemoria = new LinkedHashMap<>();

    public GerenciadorPerfis(Path raiz) {
        this.diretorioPerfis = raiz.resolve(PASTA_PERFIS);
    }

    public static GerenciadorPerfis padrao() {
        return new GerenciadorPerfis(Configuracoes.diretorioRaiz());
    }

    public List<String> listarPerfis() {
        garantirPerfil(PERFIL_PADRAO);
        Map<String, String> encontrados = new LinkedHashMap<>(perfisEmMemoria);
        if (Files.isDirectory(diretorioPerfis)) {
            try (DirectoryStream<Path> pastas = Files.newDirectoryStream(diretorioPerfis)) {
                for (Path pasta : pastas) {
                    if (Files.isDirectory(pasta)) {
                        String chave = pasta.getFileName().toString();
                        encontrados.put(chave, lerNomeExibicao(pasta, chave));
                    }
                }
            } catch (IOException e) {
                encontrados.putAll(perfisEmMemoria);
            }
        }
        List<String> nomes = new ArrayList<>(encontrados.values());
        Collections.sort(nomes, String.CASE_INSENSITIVE_ORDER);
        return nomes;
    }

    public PerfilJogador carregar(String nome) {
        String nomeLimpo = nome == null ? "" : nome.trim();
        if (nomeLimpo.isEmpty()) {
            nomeLimpo = PERFIL_PADRAO;
        }
        Path pasta = garantirPerfil(nomeLimpo);
        String nomeExibicao = lerNomeExibicao(pasta, nomeLimpo);
        return new PerfilJogador(nomeExibicao, pasta);
    }

    public boolean criarPerfil(String nome) {
        if (nome == null) {
            return false;
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.isEmpty() || nomeLimpo.length() > TAMANHO_MAXIMO_NOME) {
            return false;
        }
        String chave = sanitizar(nomeLimpo);
        if (perfisEmMemoria.containsKey(chave) || Files.exists(diretorioPerfis.resolve(chave))) {
            return false;
        }
        garantirPerfil(nomeLimpo);
        return true;
    }

    public static String sanitizar(String nome) {
        if (nome == null) {
            return "";
        }
        String decomposto = Normalizer.normalize(nome, Normalizer.Form.NFD);
        String semAcentos = decomposto.replaceAll("\\p{M}", "");
        String minusculo = semAcentos.toLowerCase(Locale.ROOT);
        StringBuilder resultado = new StringBuilder(minusculo.length());
        for (int indice = 0; indice < minusculo.length(); indice++) {
            char caractere = minusculo.charAt(indice);
            boolean letra = caractere >= 'a' && caractere <= 'z';
            boolean digito = caractere >= '0' && caractere <= '9';
            resultado.append(letra || digito ? caractere : '_');
        }
        return resultado.toString();
    }

    private Path garantirPerfil(String nomeExibicao) {
        String chave = sanitizar(nomeExibicao);
        Path pasta = diretorioPerfis.resolve(chave);
        if (!perfisEmMemoria.containsKey(chave)) {
            perfisEmMemoria.put(chave, nomeExibicao);
        }
        try {
            Files.createDirectories(pasta);
            Path arquivoNome = pasta.resolve(ARQUIVO_NOME);
            if (!Files.exists(arquivoNome)) {
                Files.write(arquivoNome, nomeExibicao.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            return pasta;
        }
        return pasta;
    }

    private String lerNomeExibicao(Path pasta, String alternativo) {
        Path arquivoNome = pasta.resolve(ARQUIVO_NOME);
        if (Files.isRegularFile(arquivoNome)) {
            try {
                String conteudo = new String(Files.readAllBytes(arquivoNome), StandardCharsets.UTF_8).trim();
                if (!conteudo.isEmpty()) {
                    return conteudo;
                }
            } catch (IOException e) {
                return valorEmMemoria(pasta, alternativo);
            }
        }
        return valorEmMemoria(pasta, alternativo);
    }

    private String valorEmMemoria(Path pasta, String alternativo) {
        String guardado = perfisEmMemoria.get(pasta.getFileName().toString());
        return guardado == null ? alternativo : guardado;
    }
}
