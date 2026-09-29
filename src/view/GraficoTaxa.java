package view;

import view.tema.TemaVisual;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class GraficoTaxa extends JComponent {

    private final List<Double> valores;
    private final TemaVisual tema;
    private final double escala;

    public GraficoTaxa(List<Double> valores, TemaVisual tema, double escala) {
        this.valores = new ArrayList<>(valores);
        this.tema = tema;
        this.escala = escala;
        setPreferredSize(new Dimension(460, 180));
    }

    @Override
    protected void paintComponent(Graphics grafico) {
        Graphics2D g2 = (Graphics2D) grafico.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int largura = getWidth();
        int altura = getHeight();
        g2.setColor(tema.getFundoClaro());
        g2.fillRoundRect(0, 0, largura, altura, 12, 12);

        Font fonte = EstiloUI.fonte(tema, escala, Font.PLAIN, 11);
        g2.setFont(fonte);
        FontMetrics metricas = g2.getFontMetrics();
        int margemEsquerda = metricas.stringWidth("100%") + 14;
        int margemDireita = 14;
        int margemTopo = 14;
        int margemBase = metricas.getHeight() + 10;
        int areaLargura = largura - margemEsquerda - margemDireita;
        int areaAltura = altura - margemTopo - margemBase;

        for (int percentual = 0; percentual <= 100; percentual += 25) {
            int y = margemTopo + areaAltura - areaAltura * percentual / 100;
            g2.setColor(EstiloUI.comAlfa(tema.getBorda(), 160));
            g2.drawLine(margemEsquerda, y, margemEsquerda + areaLargura, y);
            g2.setColor(tema.getTextoSecundario());
            String rotulo = percentual + "%";
            g2.drawString(rotulo, margemEsquerda - metricas.stringWidth(rotulo) - 6, y + metricas.getAscent() / 2 - 1);
        }

        if (valores.isEmpty()) {
            g2.setColor(tema.getTextoSecundario());
            String aviso = "Jogue algumas partidas para ver a evolução";
            g2.drawString(aviso, margemEsquerda + (areaLargura - metricas.stringWidth(aviso)) / 2, margemTopo + areaAltura / 2);
            g2.dispose();
            return;
        }

        Path2D linha = new Path2D.Double();
        Path2D area = new Path2D.Double();
        int quantidade = valores.size();
        for (int i = 0; i < quantidade; i++) {
            double x = margemEsquerda + (quantidade == 1 ? areaLargura / 2.0 : areaLargura * i / (double) (quantidade - 1));
            double y = margemTopo + areaAltura - areaAltura * valores.get(i) / 100.0;
            if (i == 0) {
                linha.moveTo(x, y);
                area.moveTo(x, margemTopo + areaAltura);
            }
            linha.lineTo(x, y);
            area.lineTo(x, y);
            if (i == quantidade - 1) {
                area.lineTo(x, margemTopo + areaAltura);
                area.closePath();
            }
        }
        g2.setColor(EstiloUI.comAlfa(tema.getDestaque(), 45));
        g2.fill(area);
        g2.setColor(tema.getDestaque());
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linha);

        g2.setColor(tema.getTextoSecundario());
        String inicio = "1ª partida";
        String fim = quantidade + "ª partida";
        int base = altura - 6;
        g2.drawString(inicio, margemEsquerda, base);
        g2.drawString(fim, margemEsquerda + areaLargura - metricas.stringWidth(fim), base);
        g2.dispose();
    }
}
