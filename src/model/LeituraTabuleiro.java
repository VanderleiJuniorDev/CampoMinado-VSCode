package model;

public interface LeituraTabuleiro {

    int getLinhas();

    int getColunas();

    boolean isRevelada(int linha, int coluna);

    boolean isMarcada(int linha, int coluna);

    boolean isInterrogacao(int linha, int coluna);

    boolean isMinada(int linha, int coluna);

    boolean isExplodida(int linha, int coluna);

    int getMinasVizinhas(int linha, int coluna);

    boolean isJogoEncerrado();

    boolean isDerrota();

    boolean isVitoria();

    int getVidasRestantes();

    int getNumMinas();

    int contarMarcadas();

    int contarReveladasSeguras();

    int getTotalSeguras();
}
