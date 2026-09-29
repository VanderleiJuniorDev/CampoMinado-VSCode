package view;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class BotaoCelula extends JComponent {

    private static final int PASSOS_HOVER = 6;

    private final int linha;
    private final int coluna;

    private String texto = "";
    private String textoCanto = "";
    private Color corTexto = Color.WHITE;
    private Color corCanto = Color.WHITE;
    private Color fundo = Color.DARK_GRAY;
    private Color fundoHover = Color.GRAY;
    private Color borda = Color.BLACK;
    private Color corFoco = Color.YELLOW;
    private Color corDestaque = Color.YELLOW;
    private Font fonteTexto = new Font(Font.DIALOG, Font.BOLD, 16);
    private Font fonteCanto = new Font(Font.DIALOG, Font.BOLD, 9);
    private float alfaTexto = 1f;
    private boolean elevada = true;
    private boolean pressionada;
    private boolean focada;
    private float progressoHover;
    private boolean hoverAtivo;
    private float intensidadeDestaque;
    private Timer animacaoHover;
    private Timer animacaoDestaque;

    public BotaoCelula(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
        setOpaque(false);
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void configurar(String texto, Color corTexto, Color fundo, Color fundoHover, Color borda, boolean elevada) {
        this.texto = texto == null ? "" : texto;
        this.corTexto = corTexto;
        this.fundo = fundo;
        this.fundoHover = fundoHover;
        this.borda = borda;
        this.elevada = elevada;
        this.alfaTexto = 1f;
        this.textoCanto = "";
        if (!elevada) {
            pressionada = false;
        }
        repaint();
    }

    public void setTextoCanto(String textoCanto, Color corCanto) {
        this.textoCanto = textoCanto == null ? "" : textoCanto;
        this.corCanto = corCanto;
        repaint();
    }

    public void setAlfaTexto(float alfaTexto) {
        this.alfaTexto = Math.max(0f, Math.min(1f, alfaTexto));
        repaint();
    }

    public void setFontes(Font fonteTexto, Font fonteCanto) {
        this.fonteTexto = fonteTexto;
        this.fonteCanto = fonteCanto;
        repaint();
    }

    public void setCores(Color corFoco, Color corDestaque) {
        this.corFoco = corFoco;
        this.corDestaque = corDestaque;
    }

    public boolean isElevada() {
        return elevada;
    }

    public String getTexto() {
        return texto;
    }

    public void setPressionada(boolean pressionada) {
        boolean novoEstado = pressionada && elevada;
        if (this.pressionada != novoEstado) {
            this.pressionada = novoEstado;
            repaint();
        }
    }

    public void setFocada(boolean focada) {
        if (this.focada != focada) {
            this.focada = focada;
            repaint();
        }
    }

    public void animarHover(boolean entrando) {
        hoverAtivo = entrando;
        if (animacaoHover == null) {
            animacaoHover = new Timer(16, e -> passoHover());
        }
        if (!animacaoHover.isRunning()) {
            animacaoHover.start();
        }
    }

    private void passoHover() {
        float alvo = hoverAtivo ? 1f : 0f;
        float passo = 1f / PASSOS_HOVER;
        if (Math.abs(progressoHover - alvo) <= passo) {
            progressoHover = alvo;
            animacaoHover.stop();
        } else {
            progressoHover += progressoHover < alvo ? passo : -passo;
        }
        repaint();
    }

    public void pulsarDestaque() {
        if (animacaoDestaque != null) {
            animacaoDestaque.stop();
        }
        final int[] quadro = {0};
        animacaoDestaque = new Timer(30, null);
        animacaoDestaque.addActionListener(e -> {
            quadro[0]++;
            double onda = Math.abs(Math.sin(quadro[0] / 6.0));
            intensidadeDestaque = (float) (onda * Math.max(0, 1 - quadro[0] / 70.0));
            if (quadro[0] >= 70) {
                intensidadeDestaque = 0;
                animacaoDestaque.stop();
            }
            repaint();
        });
        animacaoDestaque.start();
    }

    @Override
    protected void paintComponent(Graphics grafico) {
        Graphics2D g2 = (Graphics2D) grafico.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int largura = getWidth();
        int altura = getHeight();
        int arco = Math.max(4, largura / 5);
        boolean levantada = elevada && !pressionada && progressoHover > 0.5f;
        int topo;
        int alturaCorpo;
        if (!elevada) {
            topo = 0;
            alturaCorpo = altura - 1;
        } else if (pressionada) {
            topo = 2;
            alturaCorpo = altura - 3;
        } else {
            topo = levantada ? 0 : 1;
            alturaCorpo = altura - 3;
        }

        Color corBase = elevada ? EstiloUI.misturar(fundo, fundoHover, progressoHover) : fundo;
        if (pressionada) {
            corBase = EstiloUI.escurecer(corBase, 0.82);
        }

        if (elevada && !pressionada) {
            g2.setColor(new Color(0, 0, 0, levantada ? 95 : 70));
            g2.fillRoundRect(1, topo + 2, largura - 2, alturaCorpo, arco, arco);
        }

        g2.setColor(corBase);
        g2.fillRoundRect(0, topo, largura - 1, alturaCorpo, arco, arco);

        if (elevada) {
            g2.setColor(new Color(255, 255, 255, pressionada ? 12 : 35 + (int) (40 * progressoHover)));
            g2.drawLine(arco / 2, topo + 1, largura - arco / 2, topo + 1);
        }

        g2.setColor(borda);
        g2.drawRoundRect(0, topo, largura - 1, alturaCorpo, arco, arco);

        if (intensidadeDestaque > 0) {
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(EstiloUI.comAlfa(corDestaque, (int) (230 * intensidadeDestaque)));
            g2.drawRoundRect(1, topo + 1, largura - 3, alturaCorpo - 2, arco, arco);
        }

        if (!texto.isEmpty()) {
            g2.setFont(fonteTexto);
            FontMetrics metricas = g2.getFontMetrics();
            int x = (largura - metricas.stringWidth(texto)) / 2;
            int y = topo + (alturaCorpo - metricas.getHeight()) / 2 + metricas.getAscent();
            g2.setColor(EstiloUI.comAlfa(corTexto, Math.round(255 * alfaTexto * corTexto.getAlpha() / 255f)));
            g2.drawString(texto, x, y);
        }

        if (!textoCanto.isEmpty()) {
            g2.setFont(fonteCanto);
            FontMetrics metricas = g2.getFontMetrics();
            g2.setColor(corCanto);
            g2.drawString(textoCanto, largura - metricas.stringWidth(textoCanto) - 3, topo + alturaCorpo - 3);
        }

        if (focada) {
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 3f}, 0f));
            g2.setColor(corFoco);
            g2.drawRoundRect(2, topo + 2, largura - 5, alturaCorpo - 4, arco, arco);
        }
        g2.dispose();
    }
}
