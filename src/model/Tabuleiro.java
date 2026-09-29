package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Tabuleiro implements LeituraTabuleiro {

    private final ConfiguracaoPartida configuracao;
    private final int linhas;
    private final int colunas;
    private final int numMinas;
    private final Celula[][] grade;

    private boolean jogoEncerrado;
    private boolean derrota;
    private int vidasRestantes;

    public Tabuleiro(int linhas, int colunas, int numMinas) {
        this(new ConfiguracaoPartida(linhas, colunas, numMinas));
    }

    public Tabuleiro(int linhas, int colunas, int[][] posicoesMinas) {
        this(new ConfiguracaoPartida(linhas, colunas, posicoesMinas == null ? 0 : posicoesMinas.length),
                posicoesMinas);
    }

    public Tabuleiro(ConfiguracaoPartida configuracao) {
        this(validarConfiguracao(configuracao), (int[][]) null, criarSorteio(configuracao));
    }

    public Tabuleiro(ConfiguracaoPartida configuracao, int[][] posicoesMinas) {
        this(validarConfiguracao(ajustarQuantidadeDeMinas(configuracao, posicoesMinas)), posicoesMinas,
                (Random) null);
    }

    private Tabuleiro(ConfiguracaoPartida configuracao, int[][] posicoesMinas, Random sorteio) {
        this.configuracao = configuracao;
        this.linhas = configuracao.getLinhas();
        this.colunas = configuracao.getColunas();
        this.numMinas = configuracao.getMinas();
        this.grade = new Celula[linhas][colunas];
        this.jogoEncerrado = false;
        this.derrota = false;
        this.vidasRestantes = configuracao.getVidas();
        inicializarGrade();
        if (posicoesMinas != null) {
            posicionarMinasFixas(posicoesMinas);
        } else {
            posicionarMinasAleatoriamente(sorteio);
        }
        calcularMinasVizinhasDeTodasAsCelulas();
    }

    private static ConfiguracaoPartida validarConfiguracao(ConfiguracaoPartida configuracao) {
        if (configuracao == null) {
            throw new IllegalArgumentException("Configuração da partida não pode ser nula.");
        }
        int linhas = configuracao.getLinhas();
        int colunas = configuracao.getColunas();
        int minas = configuracao.getMinas();
        if (linhas <= 0 || colunas <= 0) {
            throw new IllegalArgumentException("Linhas e colunas devem ser maiores que zero.");
        }
        if (minas < 0 || (long) minas >= (long) linhas * colunas) {
            throw new IllegalArgumentException("Número de minas inválido para esse tabuleiro.");
        }
        return configuracao;
    }

    private static ConfiguracaoPartida ajustarQuantidadeDeMinas(ConfiguracaoPartida configuracao,
            int[][] posicoesMinas) {
        if (configuracao == null) {
            throw new IllegalArgumentException("Configuração da partida não pode ser nula.");
        }
        if (posicoesMinas == null) {
            throw new IllegalArgumentException("Posições das minas não podem ser nulas.");
        }
        if (configuracao.getMinas() == posicoesMinas.length) {
            return configuracao;
        }
        return configuracao.copiarComMinas(posicoesMinas.length);
    }

    private static Random criarSorteio(ConfiguracaoPartida configuracao) {
        if (configuracao == null || configuracao.getSemente() == null) {
            return new Random();
        }
        return new Random(configuracao.getSemente());
    }

    private void inicializarGrade() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                grade[i][j] = new Celula();
            }
        }
    }

    private void posicionarMinasFixas(int[][] posicoesMinas) {
        for (int[] posicao : posicoesMinas) {
            if (posicao == null || posicao.length < 2 || !posicaoValida(posicao[0], posicao[1])) {
                throw new IllegalArgumentException("Posição de mina fora do tabuleiro.");
            }
            Celula celula = grade[posicao[0]][posicao[1]];
            if (celula.isMinada()) {
                throw new IllegalArgumentException("Posição de mina repetida.");
            }
            celula.setMinada(true);
        }
    }

    private void posicionarMinasAleatoriamente(Random sorteio) {
        int totalCelulas = linhas * colunas;
        int[] indices = new int[totalCelulas];
        for (int i = 0; i < totalCelulas; i++) {
            indices[i] = i;
        }
        for (int i = 0; i < numMinas; i++) {
            int escolhido = i + sorteio.nextInt(totalCelulas - i);
            int temporario = indices[i];
            indices[i] = indices[escolhido];
            indices[escolhido] = temporario;
            grade[indices[i] / colunas][indices[i] % colunas].setMinada(true);
        }
    }

    private void calcularMinasVizinhasDeTodasAsCelulas() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                grade[i][j].setMinasVizinhas(contarMinasVizinhas(i, j));
            }
        }
    }

    private int contarMinasVizinhas(int linha, int coluna) {
        int total = 0;
        for (int[] vizinho : calcularVizinhos(linha, coluna)) {
            if (grade[vizinho[0]][vizinho[1]].isMinada()) {
                total++;
            }
        }
        return total;
    }

    public boolean dentroDosLimites(int linha, int coluna) {
        return posicaoValida(linha, coluna);
    }

    private boolean posicaoValida(int linha, int coluna) {
        return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
    }

    public List<int[]> getVizinhos(int linha, int coluna) {
        return calcularVizinhos(linha, coluna);
    }

    private List<int[]> calcularVizinhos(int linha, int coluna) {
        List<int[]> vizinhos = new ArrayList<>();
        if (!posicaoValida(linha, coluna)) {
            return vizinhos;
        }
        boolean toroidal = configuracao.isToroidal();
        List<Integer> jaIncluidos = new ArrayList<>();
        for (int deltaLinha = -1; deltaLinha <= 1; deltaLinha++) {
            for (int deltaColuna = -1; deltaColuna <= 1; deltaColuna++) {
                if (deltaLinha == 0 && deltaColuna == 0) {
                    continue;
                }
                int vizinhoLinha = linha + deltaLinha;
                int vizinhoColuna = coluna + deltaColuna;
                if (toroidal) {
                    vizinhoLinha = (vizinhoLinha % linhas + linhas) % linhas;
                    vizinhoColuna = (vizinhoColuna % colunas + colunas) % colunas;
                } else if (!posicaoValida(vizinhoLinha, vizinhoColuna)) {
                    continue;
                }
                if (vizinhoLinha == linha && vizinhoColuna == coluna) {
                    continue;
                }
                Integer indice = vizinhoLinha * colunas + vizinhoColuna;
                if (jaIncluidos.contains(indice)) {
                    continue;
                }
                jaIncluidos.add(indice);
                vizinhos.add(new int[] { vizinhoLinha, vizinhoColuna });
            }
        }
        return vizinhos;
    }

    public List<int[]> revelar(int linha, int coluna) {
        List<int[]> ordemRevelacao = new ArrayList<>();

        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return ordemRevelacao;
        }

        Celula celulaInicial = grade[linha][coluna];
        if (celulaInicial.isRevelada() || celulaInicial.isMarcada()) {
            return ordemRevelacao;
        }

        if (celulaInicial.isMinada()) {
            celulaInicial.revelar();
            celulaInicial.marcarExplodida();
            ordemRevelacao.add(new int[] { linha, coluna });
            if (vidasRestantes > 1) {
                vidasRestantes--;
            } else {
                vidasRestantes = 0;
                jogoEncerrado = true;
                derrota = true;
            }
            return ordemRevelacao;
        }

        boolean semCascata = configuracao.isSemCascata();
        List<int[]> pendentes = new ArrayList<>();
        pendentes.add(new int[] { linha, coluna });

        while (!pendentes.isEmpty()) {
            int[] posicaoAtual = pendentes.remove(pendentes.size() - 1);
            int linhaAtual = posicaoAtual[0];
            int colunaAtual = posicaoAtual[1];
            Celula atual = grade[linhaAtual][colunaAtual];

            if (atual.isRevelada() || atual.isMarcada() || atual.isMinada()) {
                continue;
            }

            atual.revelar();
            ordemRevelacao.add(new int[] { linhaAtual, colunaAtual });

            if (!semCascata && atual.getMinasVizinhas() == 0) {
                for (int[] vizinho : getVizinhos(linhaAtual, colunaAtual)) {
                    Celula celulaVizinha = grade[vizinho[0]][vizinho[1]];
                    if (!celulaVizinha.isRevelada() && !celulaVizinha.isMarcada() && !celulaVizinha.isMinada()) {
                        pendentes.add(vizinho);
                    }
                }
            }
        }

        if (verificarVitoria()) {
            jogoEncerrado = true;
        }

        return ordemRevelacao;
    }

    public int contarBandeirasVizinhas(int linha, int coluna) {
        int total = 0;
        for (int[] vizinho : getVizinhos(linha, coluna)) {
            if (grade[vizinho[0]][vizinho[1]].isMarcada()) {
                total++;
            }
        }
        return total;
    }

    private int contarMinasExplodidasVizinhas(int linha, int coluna) {
        int total = 0;
        for (int[] vizinho : getVizinhos(linha, coluna)) {
            Celula celulaVizinha = grade[vizinho[0]][vizinho[1]];
            if (celulaVizinha.isRevelada() && celulaVizinha.isMinada()) {
                total++;
            }
        }
        return total;
    }

    private boolean possuiVizinhoOcultoSemBandeira(int linha, int coluna) {
        for (int[] vizinho : getVizinhos(linha, coluna)) {
            Celula celulaVizinha = grade[vizinho[0]][vizinho[1]];
            if (!celulaVizinha.isRevelada() && !celulaVizinha.isMarcada()) {
                return true;
            }
        }
        return false;
    }

    public boolean podeFazerChording(int linha, int coluna) {
        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return false;
        }
        Celula celula = grade[linha][coluna];
        if (!celula.isRevelada() || celula.isMinada() || celula.getMinasVizinhas() <= 0) {
            return false;
        }
        int minasSinalizadas = contarBandeirasVizinhas(linha, coluna) + contarMinasExplodidasVizinhas(linha, coluna);
        if (minasSinalizadas != celula.getMinasVizinhas()) {
            return false;
        }
        return possuiVizinhoOcultoSemBandeira(linha, coluna);
    }

    public List<int[]> revelarVizinhos(int linha, int coluna) {
        List<int[]> ordemRevelacao = new ArrayList<>();
        if (!podeFazerChording(linha, coluna)) {
            return ordemRevelacao;
        }
        boolean[][] jaRegistradas = new boolean[linhas][colunas];
        for (int[] vizinho : getVizinhos(linha, coluna)) {
            if (jogoEncerrado) {
                break;
            }
            Celula celulaVizinha = grade[vizinho[0]][vizinho[1]];
            if (celulaVizinha.isRevelada() || celulaVizinha.isMarcada()) {
                continue;
            }
            for (int[] revelada : revelar(vizinho[0], vizinho[1])) {
                if (!jaRegistradas[revelada[0]][revelada[1]]) {
                    jaRegistradas[revelada[0]][revelada[1]] = true;
                    ordemRevelacao.add(revelada);
                }
            }
        }
        return ordemRevelacao;
    }

    public List<int[]> revelarAleatorias(int quantidade, Random sorteio) {
        List<int[]> reveladas = new ArrayList<>();
        if (jogoEncerrado || quantidade <= 0) {
            return reveladas;
        }
        List<int[]> candidatas = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (!celula.isRevelada() && !celula.isMinada() && !celula.isMarcada()) {
                    candidatas.add(new int[] { i, j });
                }
            }
        }
        int limite = getTotalSeguras() - 1 - contarReveladasSeguras();
        int total = Math.min(quantidade, Math.min(limite, candidatas.size()));
        if (total <= 0) {
            return reveladas;
        }
        Collections.shuffle(candidatas, sorteio == null ? new Random() : sorteio);
        for (int i = 0; i < total; i++) {
            int[] posicao = candidatas.get(i);
            grade[posicao[0]][posicao[1]].revelar();
            reveladas.add(posicao);
        }
        return reveladas;
    }

    public void alternarMarcacao(int linha, int coluna) {
        if (jogoEncerrado || !dentroDosLimites(linha, coluna)) {
            return;
        }
        grade[linha][coluna].alternarMarcacao(configuracao.isPermitirInterrogacao());
    }

    public void encerrarPorTempo() {
        if (jogoEncerrado) {
            return;
        }
        jogoEncerrado = true;
        derrota = true;
    }

    public boolean verificarVitoria() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (!celula.isMinada() && !celula.isRevelada()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getPosicoesMinas() {
        List<int[]> posicoes = new ArrayList<>();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (grade[i][j].isMinada()) {
                    posicoes.add(new int[] { i, j });
                }
            }
        }
        return posicoes.toArray(new int[0][]);
    }

    public ConfiguracaoPartida getConfiguracao() {
        return configuracao;
    }

    public Celula getCelula(int linha, int coluna) {
        return grade[linha][coluna];
    }

    @Override
    public boolean isDerrota() {
        return derrota;
    }

    @Override
    public boolean isVitoria() {
        return jogoEncerrado && !derrota;
    }

    @Override
    public boolean isJogoEncerrado() {
        return jogoEncerrado;
    }

    @Override
    public int getVidasRestantes() {
        return vidasRestantes;
    }

    @Override
    public int getLinhas() {
        return linhas;
    }

    @Override
    public int getColunas() {
        return colunas;
    }

    @Override
    public int getNumMinas() {
        return numMinas;
    }

    @Override
    public boolean isRevelada(int linha, int coluna) {
        return grade[linha][coluna].isRevelada();
    }

    @Override
    public boolean isMarcada(int linha, int coluna) {
        return grade[linha][coluna].isMarcada();
    }

    @Override
    public boolean isInterrogacao(int linha, int coluna) {
        return grade[linha][coluna].isInterrogacao();
    }

    @Override
    public boolean isMinada(int linha, int coluna) {
        return grade[linha][coluna].isMinada();
    }

    @Override
    public boolean isExplodida(int linha, int coluna) {
        return grade[linha][coluna].isExplodida();
    }

    @Override
    public int getMinasVizinhas(int linha, int coluna) {
        return grade[linha][coluna].getMinasVizinhas();
    }

    @Override
    public int contarMarcadas() {
        int total = 0;
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (grade[i][j].isMarcada()) {
                    total++;
                }
            }
        }
        return total;
    }

    @Override
    public int contarReveladasSeguras() {
        int total = 0;
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (celula.isRevelada() && !celula.isMinada()) {
                    total++;
                }
            }
        }
        return total;
    }

    @Override
    public int getTotalSeguras() {
        return linhas * colunas - numMinas;
    }

    public void imprimir(boolean revelarTudo) {
        StringBuilder cabecalho = new StringBuilder("   ");
        for (int j = 0; j < colunas; j++) {
            cabecalho.append(String.format("%2d", j));
        }
        System.out.println(cabecalho);

        for (int i = 0; i < linhas; i++) {
            StringBuilder linhaTexto = new StringBuilder(String.format("%2d ", i));
            for (int j = 0; j < colunas; j++) {
                Celula celula = grade[i][j];
                if (revelarTudo && celula.isMinada()) {
                    linhaTexto.append(" *");
                } else {
                    linhaTexto.append(" ").append(celula);
                }
            }
            System.out.println(linhaTexto);
        }
    }
}
