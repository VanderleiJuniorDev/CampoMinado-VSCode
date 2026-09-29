package persistencia;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuracoes {

    private static final String NOME_ARQUIVO = "config.properties";
    private static final String PREFIXO_TECLA = "tecla.";

    private static final String CHAVE_TEMA = "tema";
    private static final String CHAVE_SKIN_BANDEIRA = "skin.bandeira";
    private static final String CHAVE_SKIN_CELULA = "skin.celula";
    private static final String CHAVE_SOM_ATIVO = "som.ativo";
    private static final String CHAVE_MUSICA_ATIVA = "musica.ativa";
    private static final String CHAVE_VOLUME_MUSICA = "musica.volume";
    private static final String CHAVE_ZOOM = "zoom";
    private static final String CHAVE_ESCALA_FONTE = "fonte.escala";
    private static final String CHAVE_INTERROGACAO = "interrogacao.ativa";
    private static final String CHAVE_DALTONICO = "modo.daltonico";
    private static final String CHAVE_PERFIL_ATUAL = "perfil.atual";
    private static final String CHAVE_DENSIDADE_MINAS = "minas.densidade";

    private static final String TEMA_PADRAO = "Automático (sistema)";
    private static final String SKIN_BANDEIRA_PADRAO = "Bandeira";
    private static final String SKIN_CELULA_PADRAO = "Números";
    private static final String PERFIL_PADRAO = "Jogador";
    private static final int VOLUME_PADRAO = 40;
    private static final double ZOOM_PADRAO = 1.0;
    private static final double ZOOM_MINIMO = 0.6;
    private static final double ZOOM_MAXIMO = 2.0;
    private static final double ESCALA_FONTE_PADRAO = 1.0;
    private static final double ESCALA_FONTE_MINIMA = 0.8;
    private static final double ESCALA_FONTE_MAXIMA = 1.6;
    private static final int DENSIDADE_PADRAO = 15;
    private static final int DENSIDADE_MINIMA = 5;
    private static final int DENSIDADE_MAXIMA = 40;

    private final Path arquivo;
    private final Properties propriedades = new Properties();

    public Configuracoes(Path arquivo) {
        this.arquivo = arquivo;
        carregarDoDisco();
    }

    public static Configuracoes carregarPadrao() {
        return new Configuracoes(diretorioRaiz().resolve(NOME_ARQUIVO));
    }

    public static Path diretorioRaiz() {
        return Paths.get(System.getProperty("user.home"), ".campominado");
    }

    private void carregarDoDisco() {
        if (arquivo == null || !Files.isRegularFile(arquivo)) {
            return;
        }
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            propriedades.load(leitor);
        } catch (IOException | IllegalArgumentException e) {
            propriedades.clear();
        }
    }

    public String getTexto(String chave, String padrao) {
        String valor = propriedades.getProperty(chave);
        return valor == null ? padrao : valor;
    }

    public int getInteiro(String chave, int padrao) {
        String valor = propriedades.getProperty(chave);
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    public boolean getBooleano(String chave, boolean padrao) {
        String valor = propriedades.getProperty(chave);
        if (valor == null) {
            return padrao;
        }
        String normalizado = valor.trim();
        if ("true".equalsIgnoreCase(normalizado)) {
            return true;
        }
        if ("false".equalsIgnoreCase(normalizado)) {
            return false;
        }
        return padrao;
    }

    public double getDecimal(String chave, double padrao) {
        String valor = propriedades.getProperty(chave);
        if (valor == null) {
            return padrao;
        }
        try {
            double convertido = Double.parseDouble(valor.trim().replace(',', '.'));
            if (Double.isNaN(convertido) || Double.isInfinite(convertido)) {
                return padrao;
            }
            return convertido;
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    public void definir(String chave, Object valor) {
        if (chave == null) {
            return;
        }
        if (valor == null) {
            propriedades.remove(chave);
        } else {
            propriedades.setProperty(chave, String.valueOf(valor));
        }
    }

    public void salvar() {
        if (arquivo == null) {
            return;
        }
        try {
            Path pai = arquivo.toAbsolutePath().getParent();
            if (pai != null) {
                Files.createDirectories(pai);
            }
            try (Writer escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                propriedades.store(escritor, "Campo Minado");
            }
        } catch (IOException e) {
            return;
        }
    }

    public String getTema() {
        return getTexto(CHAVE_TEMA, TEMA_PADRAO);
    }

    public void setTema(String tema) {
        definir(CHAVE_TEMA, tema);
    }

    public String getSkinBandeira() {
        return getTexto(CHAVE_SKIN_BANDEIRA, SKIN_BANDEIRA_PADRAO);
    }

    public void setSkinBandeira(String skin) {
        definir(CHAVE_SKIN_BANDEIRA, skin);
    }

    public String getSkinCelula() {
        return getTexto(CHAVE_SKIN_CELULA, SKIN_CELULA_PADRAO);
    }

    public void setSkinCelula(String skin) {
        definir(CHAVE_SKIN_CELULA, skin);
    }

    public boolean isSomAtivo() {
        return getBooleano(CHAVE_SOM_ATIVO, true);
    }

    public void setSomAtivo(boolean ativo) {
        definir(CHAVE_SOM_ATIVO, ativo);
    }

    public boolean isMusicaAtiva() {
        return getBooleano(CHAVE_MUSICA_ATIVA, false);
    }

    public void setMusicaAtiva(boolean ativa) {
        definir(CHAVE_MUSICA_ATIVA, ativa);
    }

    public int getVolumeMusica() {
        return limitar(getInteiro(CHAVE_VOLUME_MUSICA, VOLUME_PADRAO), 0, 100);
    }

    public void setVolumeMusica(int volume) {
        definir(CHAVE_VOLUME_MUSICA, limitar(volume, 0, 100));
    }

    public double getZoom() {
        return limitar(getDecimal(CHAVE_ZOOM, ZOOM_PADRAO), ZOOM_MINIMO, ZOOM_MAXIMO);
    }

    public void setZoom(double zoom) {
        definir(CHAVE_ZOOM, limitarDecimalValido(zoom, ZOOM_PADRAO, ZOOM_MINIMO, ZOOM_MAXIMO));
    }

    public double getEscalaFonte() {
        return limitar(getDecimal(CHAVE_ESCALA_FONTE, ESCALA_FONTE_PADRAO), ESCALA_FONTE_MINIMA, ESCALA_FONTE_MAXIMA);
    }

    public void setEscalaFonte(double escala) {
        definir(CHAVE_ESCALA_FONTE,
                limitarDecimalValido(escala, ESCALA_FONTE_PADRAO, ESCALA_FONTE_MINIMA, ESCALA_FONTE_MAXIMA));
    }

    public boolean isInterrogacaoAtiva() {
        return getBooleano(CHAVE_INTERROGACAO, true);
    }

    public void setInterrogacaoAtiva(boolean ativa) {
        definir(CHAVE_INTERROGACAO, ativa);
    }

    public boolean isModoDaltonico() {
        return getBooleano(CHAVE_DALTONICO, false);
    }

    public void setModoDaltonico(boolean ativo) {
        definir(CHAVE_DALTONICO, ativo);
    }

    public String getPerfilAtual() {
        return getTexto(CHAVE_PERFIL_ATUAL, PERFIL_PADRAO);
    }

    public void setPerfilAtual(String perfil) {
        definir(CHAVE_PERFIL_ATUAL, perfil);
    }

    public int getDensidadeMinasPadrao() {
        return limitar(getInteiro(CHAVE_DENSIDADE_MINAS, DENSIDADE_PADRAO), DENSIDADE_MINIMA, DENSIDADE_MAXIMA);
    }

    public void setDensidadeMinasPadrao(int densidade) {
        definir(CHAVE_DENSIDADE_MINAS, limitar(densidade, DENSIDADE_MINIMA, DENSIDADE_MAXIMA));
    }

    public int getTecla(String acao, int padrao) {
        return getInteiro(PREFIXO_TECLA + acao, padrao);
    }

    public void setTecla(String acao, int codigoTecla) {
        definir(PREFIXO_TECLA + acao, codigoTecla);
    }

    private static int limitar(int valor, int minimo, int maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private static double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    private static double limitarDecimalValido(double valor, double padrao, double minimo, double maximo) {
        if (Double.isNaN(valor)) {
            return padrao;
        }
        return limitar(valor, minimo, maximo);
    }
}
