package controller;

public class Jogada {

    private final TipoJogada tipo;
    private final int linha;
    private final int coluna;

    public Jogada(TipoJogada tipo, int linha, int coluna) {
        this.tipo = tipo;
        this.linha = linha;
        this.coluna = coluna;
    }

    public TipoJogada getTipo() {
        return tipo;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }
}
