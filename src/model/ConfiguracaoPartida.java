package model;

public class ConfiguracaoPartida {

    private final int linhas;
    private final int colunas;
    private final int minas;
    private boolean semCascata;
    private boolean toroidal;
    private int vidas;
    private boolean permitirInterrogacao;
    private Long semente;

    public ConfiguracaoPartida(int linhas, int colunas, int minas) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.minas = minas;
        this.semCascata = false;
        this.toroidal = false;
        this.vidas = 1;
        this.permitirInterrogacao = false;
        this.semente = null;
    }

    public ConfiguracaoPartida comSemCascata(boolean semCascata) {
        this.semCascata = semCascata;
        return this;
    }

    public ConfiguracaoPartida comToroidal(boolean toroidal) {
        this.toroidal = toroidal;
        return this;
    }

    public ConfiguracaoPartida comVidas(int vidas) {
        this.vidas = Math.max(1, vidas);
        return this;
    }

    public ConfiguracaoPartida comInterrogacao(boolean permitirInterrogacao) {
        this.permitirInterrogacao = permitirInterrogacao;
        return this;
    }

    public ConfiguracaoPartida comSemente(Long semente) {
        this.semente = semente;
        return this;
    }

    public int getLinhas() {
        return linhas;
    }

    public int getColunas() {
        return colunas;
    }

    public int getMinas() {
        return minas;
    }

    public boolean isSemCascata() {
        return semCascata;
    }

    public boolean isToroidal() {
        return toroidal;
    }

    public int getVidas() {
        return vidas;
    }

    public boolean isPermitirInterrogacao() {
        return permitirInterrogacao;
    }

    public Long getSemente() {
        return semente;
    }

    public ConfiguracaoPartida copiarComMinas(int minas) {
        return new ConfiguracaoPartida(linhas, colunas, minas)
                .comSemCascata(semCascata)
                .comToroidal(toroidal)
                .comVidas(vidas)
                .comInterrogacao(permitirInterrogacao)
                .comSemente(semente);
    }
}
