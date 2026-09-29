package controller;

public class OpcoesPartida {

    private boolean semCascata;
    private boolean relampago;
    private boolean tresVidas;
    private boolean toroidal;
    private boolean minasVisiveis;
    private ModoJogadores modoJogadores = ModoJogadores.SOLO;
    private int tempoLimiteSegundos;

    public OpcoesPartida copiar() {
        OpcoesPartida copia = new OpcoesPartida();
        copia.semCascata = semCascata;
        copia.relampago = relampago;
        copia.tresVidas = tresVidas;
        copia.toroidal = toroidal;
        copia.minasVisiveis = minasVisiveis;
        copia.modoJogadores = modoJogadores;
        copia.tempoLimiteSegundos = tempoLimiteSegundos;
        return copia;
    }

    public boolean isSemCascata() {
        return semCascata;
    }

    public void setSemCascata(boolean semCascata) {
        this.semCascata = semCascata;
    }

    public boolean isRelampago() {
        return relampago;
    }

    public void setRelampago(boolean relampago) {
        this.relampago = relampago;
    }

    public boolean isTresVidas() {
        return tresVidas;
    }

    public void setTresVidas(boolean tresVidas) {
        this.tresVidas = tresVidas;
    }

    public boolean isToroidal() {
        return toroidal;
    }

    public void setToroidal(boolean toroidal) {
        this.toroidal = toroidal;
    }

    public boolean isMinasVisiveis() {
        return minasVisiveis;
    }

    public void setMinasVisiveis(boolean minasVisiveis) {
        this.minasVisiveis = minasVisiveis;
    }

    public ModoJogadores getModoJogadores() {
        return modoJogadores;
    }

    public void setModoJogadores(ModoJogadores modoJogadores) {
        this.modoJogadores = modoJogadores == null ? ModoJogadores.SOLO : modoJogadores;
    }

    public int getTempoLimiteSegundos() {
        return tempoLimiteSegundos;
    }

    public void setTempoLimiteSegundos(int tempoLimiteSegundos) {
        this.tempoLimiteSegundos = Math.max(0, tempoLimiteSegundos);
    }
}
