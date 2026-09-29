package view;

import controller.AcoesJogador;
import model.LeituraTabuleiro;
import view.tema.SimbolosDaltonismo;
import view.tema.SkinBandeira;
import view.tema.SkinCelula;
import view.tema.TemaVisual;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.DoubleConsumer;

public class PainelTabuleiro extends JPanel {

    public static final int TAMANHO_BASE = 34;
    private static final int ESPACO = 2;
    private static final int TAMANHO_MINIMO = 14;
    private static final String EMOJI_BOMBA = "💣";
    private static final String EMOJI_EXPLOSAO = "💥";
    private static final String SIMBOLO_ERRO = "✖";

    private final int linhas;
    private final int colunas;
    private final BotaoCelula[][] celulas;
    private final LeituraTabuleiro leitura;
    private final AcoesJogador ouvinte;
    private final DoubleConsumer aoMudarZoomPelaRoda;

    private final TemaVisual tema;
    private final SkinBandeira skinBandeira;
    private final SkinCelula skinCelula;
    private final boolean daltonico;
    private final boolean minasVisiveis;
    private double zoom;
    private int tamanhoAtual = -1;

    private int linhaCursor;
    private int colunaCursor;
    private boolean cursorVisivel;
    private boolean interacaoHabilitada = true;

    private boolean esquerdoPressionado;
    private boolean direitoPressionado;
    private boolean acordeEmAndamento;
    private BotaoCelula celulaRecemRevelada;

    public PainelTabuleiro(LeituraTabuleiro leitura, AcoesJogador ouvinte, TemaVisual tema, SkinBandeira skinBandeira,
                           SkinCelula skinCelula, boolean daltonico, boolean minasVisiveis, double zoom,
                           DoubleConsumer aoMudarZoomPelaRoda) {
        this.leitura = leitura;
        this.ouvinte = ouvinte;
        this.tema = tema;
        this.skinBandeira = skinBandeira;
        this.skinCelula = skinCelula;
        this.daltonico = daltonico;
        this.minasVisiveis = minasVisiveis;
        this.zoom = zoom;
        this.aoMudarZoomPelaRoda = aoMudarZoomPelaRoda;
        this.linhas = leitura.getLinhas();
        this.colunas = leitura.getColunas();
        this.celulas = new BotaoCelula[linhas][colunas];
        this.linhaCursor = linhas / 2;
        this.colunaCursor = colunas / 2;

        setLayout(new LayoutGrade());
        setBackground(tema.getFundo());
        if (skinCelula.usaEmoji()) {
            setToolTipText("Legenda: " + skinCelula.legenda());
        }

        OuvinteMouse ouvinteMouse = new OuvinteMouse();
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                BotaoCelula celula = new BotaoCelula(i, j);
                celula.setCores(tema.getBandeira(), tema.getDestaque());
                celula.addMouseListener(ouvinteMouse);
                celula.addMouseWheelListener(ouvinteMouse);
                if (skinCelula.usaEmoji()) {
                    celula.setToolTipText("Legenda: " + skinCelula.legenda());
                }
                celulas[i][j] = celula;
                add(celula);
            }
        }
        addMouseWheelListener(ouvinteMouse);
    }

    public int getLinhas() {
        return linhas;
    }

    public int getColunas() {
        return colunas;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        tamanhoAtual = -1;
        revalidate();
        repaint();
    }

    public void setInteracaoHabilitada(boolean habilitada) {
        this.interacaoHabilitada = habilitada;
    }

    public int getTamanhoCelulaAtual() {
        return tamanhoAtual;
    }

    public double calcularZoomQueCabe() {
        Insets margens = getInsets();
        int larguraDisponivel = getWidth() - margens.left - margens.right;
        int alturaDisponivel = getHeight() - margens.top - margens.bottom;
        if (larguraDisponivel <= 0 || alturaDisponivel <= 0) {
            return zoom;
        }
        int cabeLargura = (larguraDisponivel - ESPACO) / colunas - ESPACO;
        int cabeAltura = (alturaDisponivel - ESPACO) / linhas - ESPACO;
        return Math.min(cabeLargura, cabeAltura) / (double) TAMANHO_BASE;
    }

    private int tamanhoPreferido() {
        return (int) Math.round(TAMANHO_BASE * zoom);
    }

    @Override
    public Dimension getPreferredSize() {
        int tamanho = tamanhoPreferido();
        return new Dimension(colunas * (tamanho + ESPACO) + ESPACO, linhas * (tamanho + ESPACO) + ESPACO);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(colunas * (TAMANHO_MINIMO + ESPACO), linhas * (TAMANHO_MINIMO + ESPACO));
    }

    private void aplicarFontes(int tamanho) {
        float tamanhoFonte = tamanho * 0.55f;
        Font fonteTexto = EstiloUI.fonte(tema, 1.0, Font.BOLD, tamanhoFonte);
        Font fonteEmoji = EstiloUI.fonteEmoji(Font.PLAIN, tamanho * 0.5f);
        Font fonteCanto = EstiloUI.fonte(tema, 1.0, Font.BOLD, Math.max(7f, tamanho * 0.28f));
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                BotaoCelula celula = celulas[i][j];
                celula.setFontes(EstiloUI.contemEmoji(celula.getTexto()) ? fonteEmoji : fonteTexto, fonteCanto);
            }
        }
    }

    private void ajustarFonteDaCelula(BotaoCelula celula) {
        int tamanho = tamanhoAtual > 0 ? tamanhoAtual : tamanhoPreferido();
        Font fonteTexto = EstiloUI.contemEmoji(celula.getTexto())
                ? EstiloUI.fonteEmoji(Font.PLAIN, tamanho * 0.5f)
                : EstiloUI.fonte(tema, 1.0, Font.BOLD, tamanho * 0.55f);
        celula.setFontes(fonteTexto, EstiloUI.fonte(tema, 1.0, Font.BOLD, Math.max(7f, tamanho * 0.28f)));
    }

    public void atualizarCelula(int linha, int coluna) {
        if (dentro(linha, coluna)) {
            aplicarAparencia(linha, coluna, false);
        }
    }

    public void redesenharTudo() {
        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                aplicarAparencia(i, j, true);
            }
        }
    }

    private void aplicarAparencia(int linha, int coluna, boolean incluirEstadoFinal) {
        BotaoCelula celula = celulas[linha][coluna];
        boolean encerrado = incluirEstadoFinal && leitura.isJogoEncerrado();
        boolean derrota = encerrado && leitura.isDerrota();
        boolean vitoria = encerrado && leitura.isVitoria();

        if (!leitura.isRevelada(linha, coluna)) {
            if (leitura.isMarcada(linha, coluna)) {
                if (derrota && !leitura.isMinada(linha, coluna)) {
                    configurarBandeiraErrada(celula);
                } else {
                    celula.configurar(skinBandeira.getSimbolo(), tema.getBandeira(), tema.getCelulaOculta(),
                            tema.getCelulaOcultaHover(), tema.getBandeira(), true);
                }
            } else if (derrota && leitura.isMinada(linha, coluna)) {
                configurarMinaFinal(celula);
            } else if (vitoria && leitura.isMinada(linha, coluna)) {
                celula.configurar(skinBandeira.getSimbolo(), tema.getBandeira(), tema.getCelulaOculta(),
                        tema.getCelulaOcultaHover(), tema.getVitoria(), true);
            } else if (leitura.isInterrogacao(linha, coluna)) {
                celula.configurar("?", tema.getDestaque(), tema.getCelulaOculta(), tema.getCelulaOcultaHover(),
                        tema.getDestaque(), true);
            } else if (minasVisiveis && leitura.isMinada(linha, coluna)) {
                celula.configurar(EMOJI_BOMBA, tema.getMina(), tema.getCelulaOculta(), tema.getCelulaOcultaHover(),
                        EstiloUI.comAlfa(tema.getMina(), 140), true);
                celula.setAlfaTexto(0.4f);
            } else {
                celula.configurar("", tema.getTextoPrincipal(), tema.getCelulaOculta(), tema.getCelulaOcultaHover(),
                        tema.getBordaOculta(), true);
            }
        } else if (leitura.isMinada(linha, coluna)) {
            celula.configurar(leitura.isExplodida(linha, coluna) ? EMOJI_EXPLOSAO : EMOJI_BOMBA, tema.getMina(),
                    tema.getMinaFundo(), tema.getMinaFundo(), tema.getMina(), false);
        } else {
            int vizinhas = leitura.getMinasVizinhas(linha, coluna);
            Color fundo = vitoria ? EstiloUI.misturar(tema.getCelulaRevelada(), tema.getVitoria(), 0.3) : tema.getCelulaRevelada();
            if (vizinhas == 0) {
                celula.configurar("", tema.getTextoSobreRevelada(), fundo, fundo, tema.getBordaRevelada(), false);
            } else {
                celula.configurar(skinCelula.simbolo(vizinhas), tema.getCorNumero(vizinhas), fundo, fundo,
                        tema.getBordaRevelada(), false);
                if (daltonico) {
                    celula.setTextoCanto(SimbolosDaltonismo.simbolo(vizinhas), tema.getCorNumero(vizinhas));
                }
            }
        }
        ajustarFonteDaCelula(celula);
    }

    private void configurarMinaFinal(BotaoCelula celula) {
        celula.configurar(EMOJI_BOMBA, tema.getMina(), tema.getMinaFundo(), tema.getMinaFundo(),
                EstiloUI.comAlfa(tema.getMina(), 160), false);
        ajustarFonteDaCelula(celula);
    }

    private void configurarBandeiraErrada(BotaoCelula celula) {
        celula.configurar(SIMBOLO_ERRO, tema.getMina(), tema.getCelulaOculta(), tema.getCelulaOculta(),
                tema.getMina(), false);
        celula.setTextoCanto(skinBandeira.getSimbolo(), tema.getBandeira());
        ajustarFonteDaCelula(celula);
    }

    public void revelarMinaFinal(int linha, int coluna) {
        if (dentro(linha, coluna)) {
            configurarMinaFinal(celulas[linha][coluna]);
        }
    }

    public void marcarBandeiraErrada(int linha, int coluna) {
        if (dentro(linha, coluna)) {
            configurarBandeiraErrada(celulas[linha][coluna]);
        }
    }

    public void destacarCelulaVencedora(int linha, int coluna) {
        if (!dentro(linha, coluna) || !leitura.isRevelada(linha, coluna)) {
            return;
        }
        aplicarAparencia(linha, coluna, true);
    }

    public void sinalizarMinaNaVitoria(int linha, int coluna) {
        if (dentro(linha, coluna)) {
            aplicarAparencia(linha, coluna, true);
        }
    }

    public void destacarCelula(int linha, int coluna) {
        if (dentro(linha, coluna)) {
            celulas[linha][coluna].pulsarDestaque();
        }
    }

    public void posicionarCursor(int linha, int coluna, boolean visivel) {
        if (!dentro(linha, coluna)) {
            return;
        }
        celulas[linhaCursor][colunaCursor].setFocada(false);
        linhaCursor = linha;
        colunaCursor = coluna;
        cursorVisivel = visivel;
        celulas[linhaCursor][colunaCursor].setFocada(visivel);
    }

    public void moverCursor(int deltaLinha, int deltaColuna) {
        int novaLinha = Math.floorMod(linhaCursor + deltaLinha, linhas);
        int novaColuna = Math.floorMod(colunaCursor + deltaColuna, colunas);
        posicionarCursor(novaLinha, novaColuna, true);
    }

    public boolean isCursorVisivel() {
        return cursorVisivel;
    }

    public int getLinhaCursor() {
        return linhaCursor;
    }

    public int getColunaCursor() {
        return colunaCursor;
    }

    private boolean dentro(int linha, int coluna) {
        return linha >= 0 && linha < linhas && coluna >= 0 && coluna < colunas;
    }

    private class OuvinteMouse extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent evento) {
            if (evento.getSource() instanceof BotaoCelula) {
                BotaoCelula celula = (BotaoCelula) evento.getSource();
                if (celula.isElevada() && interacaoHabilitada) {
                    celula.animarHover(true);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent evento) {
            if (evento.getSource() instanceof BotaoCelula) {
                BotaoCelula celula = (BotaoCelula) evento.getSource();
                celula.animarHover(false);
                celula.setPressionada(false);
            }
        }

        @Override
        public void mousePressed(MouseEvent evento) {
            if (!(evento.getSource() instanceof BotaoCelula) || !interacaoHabilitada) {
                return;
            }
            BotaoCelula celula = (BotaoCelula) evento.getSource();
            if (SwingUtilities.isLeftMouseButton(evento)) {
                esquerdoPressionado = true;
            }
            if (SwingUtilities.isRightMouseButton(evento) || evento.getButton() == MouseEvent.BUTTON3) {
                direitoPressionado = true;
            }
            if (esquerdoPressionado && direitoPressionado) {
                acordeEmAndamento = true;
            }
            if (SwingUtilities.isLeftMouseButton(evento) || SwingUtilities.isMiddleMouseButton(evento)) {
                celula.setPressionada(true);
            }
        }

        @Override
        public void mouseReleased(MouseEvent evento) {
            if (!(evento.getSource() instanceof BotaoCelula)) {
                return;
            }
            BotaoCelula celula = (BotaoCelula) evento.getSource();
            celula.setPressionada(false);
            boolean esquerdo = SwingUtilities.isLeftMouseButton(evento);
            boolean direito = SwingUtilities.isRightMouseButton(evento) || evento.getButton() == MouseEvent.BUTTON3;
            boolean meio = SwingUtilities.isMiddleMouseButton(evento);
            if (esquerdo) {
                esquerdoPressionado = false;
            }
            if (direito) {
                direitoPressionado = false;
            }
            if (!interacaoHabilitada || ouvinte == null) {
                acordeEmAndamento = false;
                return;
            }
            if (!celula.contains(evento.getPoint())) {
                if (!esquerdoPressionado && !direitoPressionado) {
                    acordeEmAndamento = false;
                }
                return;
            }
            int linha = celula.getLinha();
            int coluna = celula.getColuna();
            posicionarCursor(linha, coluna, cursorVisivel);

            if (acordeEmAndamento) {
                if (!esquerdoPressionado && !direitoPressionado) {
                    acordeEmAndamento = false;
                } else {
                    ouvinte.aoFazerChording(linha, coluna);
                }
                return;
            }
            if (meio) {
                ouvinte.aoFazerChording(linha, coluna);
            } else if (direito) {
                ouvinte.aoMarcarCelula(linha, coluna);
            } else if (esquerdo) {
                if (leitura.isRevelada(linha, coluna)) {
                    if (evento.getClickCount() >= 2 && celula != celulaRecemRevelada) {
                        ouvinte.aoFazerChording(linha, coluna);
                    }
                    if (evento.getClickCount() < 2) {
                        celulaRecemRevelada = null;
                    }
                } else {
                    celulaRecemRevelada = celula;
                    ouvinte.aoRevelarCelula(linha, coluna);
                }
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent evento) {
            if (!evento.isControlDown() || aoMudarZoomPelaRoda == null) {
                if (getParent() != null) {
                    getParent().dispatchEvent(SwingUtilities.convertMouseEvent(evento.getComponent(), evento, getParent()));
                }
                return;
            }
            aoMudarZoomPelaRoda.accept(evento.getWheelRotation() < 0 ? 0.1 : -0.1);
        }
    }

    private class LayoutGrade implements LayoutManager {

        @Override
        public void addLayoutComponent(String nome, Component componente) {
        }

        @Override
        public void removeLayoutComponent(Component componente) {
        }

        @Override
        public Dimension preferredLayoutSize(Container pai) {
            return getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container pai) {
            return getMinimumSize();
        }

        @Override
        public void layoutContainer(Container pai) {
            Insets margens = pai.getInsets();
            int larguraDisponivel = pai.getWidth() - margens.left - margens.right;
            int alturaDisponivel = pai.getHeight() - margens.top - margens.bottom;
            int cabeLargura = (larguraDisponivel - ESPACO) / colunas - ESPACO;
            int cabeAltura = (alturaDisponivel - ESPACO) / linhas - ESPACO;
            int tamanho = Math.max(TAMANHO_MINIMO, Math.min(tamanhoPreferido(), Math.min(cabeLargura, cabeAltura)));
            int larguraGrade = colunas * (tamanho + ESPACO) + ESPACO;
            int alturaGrade = linhas * (tamanho + ESPACO) + ESPACO;
            int inicioX = margens.left + Math.max(0, (larguraDisponivel - larguraGrade) / 2) + ESPACO;
            int inicioY = margens.top + Math.max(0, (alturaDisponivel - alturaGrade) / 2) + ESPACO;
            for (int i = 0; i < linhas; i++) {
                for (int j = 0; j < colunas; j++) {
                    celulas[i][j].setBounds(inicioX + j * (tamanho + ESPACO), inicioY + i * (tamanho + ESPACO), tamanho, tamanho);
                }
            }
            if (tamanho != tamanhoAtual) {
                tamanhoAtual = tamanho;
                aplicarFontes(tamanho);
            }
        }
    }
}
