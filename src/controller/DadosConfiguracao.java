package controller;

import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class DadosConfiguracao {

    public static final String ACAO_REVELAR = "revelar";
    public static final String ACAO_MARCAR = "marcar";
    public static final String ACAO_CHORDING = "chording";
    public static final String ACAO_DICA = "dica";

    private String tema = "Automático (sistema)";
    private String skinBandeira = "Bandeira";
    private String skinCelula = "Números";
    private boolean somAtivo = true;
    private boolean musicaAtiva;
    private int volumeMusica = 40;
    private double zoom = 1.0;
    private double escalaFonte = 1.0;
    private boolean interrogacaoAtiva = true;
    private boolean modoDaltonico;
    private int densidadeMinas = 15;
    private final Map<String, Integer> teclas = new LinkedHashMap<>();

    public DadosConfiguracao() {
        teclas.putAll(teclasPadrao());
    }

    public static Map<String, Integer> teclasPadrao() {
        Map<String, Integer> padrao = new LinkedHashMap<>();
        padrao.put(ACAO_REVELAR, KeyEvent.VK_SPACE);
        padrao.put(ACAO_MARCAR, KeyEvent.VK_F);
        padrao.put(ACAO_CHORDING, KeyEvent.VK_C);
        padrao.put(ACAO_DICA, KeyEvent.VK_H);
        return padrao;
    }

    public static String descreverAcao(String acao) {
        if (ACAO_REVELAR.equals(acao)) {
            return "Revelar célula";
        }
        if (ACAO_MARCAR.equals(acao)) {
            return "Marcar bandeira";
        }
        if (ACAO_CHORDING.equals(acao)) {
            return "Chording (abrir vizinhos)";
        }
        if (ACAO_DICA.equals(acao)) {
            return "Pedir dica";
        }
        return acao;
    }

    public DadosConfiguracao copiar() {
        DadosConfiguracao copia = new DadosConfiguracao();
        copia.tema = tema;
        copia.skinBandeira = skinBandeira;
        copia.skinCelula = skinCelula;
        copia.somAtivo = somAtivo;
        copia.musicaAtiva = musicaAtiva;
        copia.volumeMusica = volumeMusica;
        copia.zoom = zoom;
        copia.escalaFonte = escalaFonte;
        copia.interrogacaoAtiva = interrogacaoAtiva;
        copia.modoDaltonico = modoDaltonico;
        copia.densidadeMinas = densidadeMinas;
        copia.teclas.clear();
        copia.teclas.putAll(teclas);
        return copia;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getSkinBandeira() {
        return skinBandeira;
    }

    public void setSkinBandeira(String skinBandeira) {
        this.skinBandeira = skinBandeira;
    }

    public String getSkinCelula() {
        return skinCelula;
    }

    public void setSkinCelula(String skinCelula) {
        this.skinCelula = skinCelula;
    }

    public boolean isSomAtivo() {
        return somAtivo;
    }

    public void setSomAtivo(boolean somAtivo) {
        this.somAtivo = somAtivo;
    }

    public boolean isMusicaAtiva() {
        return musicaAtiva;
    }

    public void setMusicaAtiva(boolean musicaAtiva) {
        this.musicaAtiva = musicaAtiva;
    }

    public int getVolumeMusica() {
        return volumeMusica;
    }

    public void setVolumeMusica(int volumeMusica) {
        this.volumeMusica = Math.max(0, Math.min(100, volumeMusica));
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(0.6, Math.min(2.0, zoom));
    }

    public double getEscalaFonte() {
        return escalaFonte;
    }

    public void setEscalaFonte(double escalaFonte) {
        this.escalaFonte = Math.max(0.8, Math.min(1.6, escalaFonte));
    }

    public boolean isInterrogacaoAtiva() {
        return interrogacaoAtiva;
    }

    public void setInterrogacaoAtiva(boolean interrogacaoAtiva) {
        this.interrogacaoAtiva = interrogacaoAtiva;
    }

    public boolean isModoDaltonico() {
        return modoDaltonico;
    }

    public void setModoDaltonico(boolean modoDaltonico) {
        this.modoDaltonico = modoDaltonico;
    }

    public int getDensidadeMinas() {
        return densidadeMinas;
    }

    public void setDensidadeMinas(int densidadeMinas) {
        this.densidadeMinas = Math.max(5, Math.min(40, densidadeMinas));
    }

    public Map<String, Integer> getTeclas() {
        return new LinkedHashMap<>(teclas);
    }

    public int getTecla(String acao) {
        Integer codigo = teclas.get(acao);
        return codigo == null ? KeyEvent.VK_UNDEFINED : codigo;
    }

    public void setTecla(String acao, int codigo) {
        teclas.put(acao, codigo);
    }
}
