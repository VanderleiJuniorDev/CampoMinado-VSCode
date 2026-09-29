package view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.Timer;

public class PainelConfete extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final int QUANTIDADE_PARTICULAS = 180;
    private static final int INTERVALO_QUADRO_MS = 16;
    private static final long DURACAO_TOTAL_MS = 4500;
    private static final long INICIO_DESVANECIMENTO_MS = 3000;
    private static final double GRAVIDADE = 0.12;
    private static final double VELOCIDADE_TERMINAL = 5.5;
    private static final int LARGURA_PADRAO = 800;
    private static final Color[] CORES_PADRAO = {
        new Color(244, 67, 54), new Color(255, 193, 7), new Color(76, 175, 80),
        new Color(33, 150, 243), new Color(156, 39, 176), new Color(255, 105, 180)
    };

    private final transient List<Particula> particulas = new ArrayList<>();
    private final Random aleatorio = new Random();
    private final Timer temporizador;
    private long instanteInicio;
    private float opacidadeGlobal = 1f;

    public PainelConfete() {
        temporizador = new Timer(INTERVALO_QUADRO_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                avancarQuadro();
            }
        });
        temporizador.setCoalesce(true);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    public void iniciar(Color[] cores) {
        Color[] paleta = cores == null || cores.length == 0 ? CORES_PADRAO : cores.clone();
        temporizador.stop();
        particulas.clear();
        int largura = getWidth() > 0 ? getWidth() : LARGURA_PADRAO;
        for (int i = 0; i < QUANTIDADE_PARTICULAS; i++) {
            particulas.add(criarParticula(largura, paleta));
        }
        instanteInicio = System.currentTimeMillis();
        opacidadeGlobal = 1f;
        setVisible(true);
        temporizador.start();
        repaint();
    }

    public void parar() {
        temporizador.stop();
        particulas.clear();
        setVisible(false);
    }

    public boolean isAnimando() {
        return temporizador.isRunning();
    }

    private Particula criarParticula(int largura, Color[] paleta) {
        Particula particula = new Particula();
        particula.x = aleatorio.nextDouble() * largura;
        particula.y = -10 - aleatorio.nextDouble() * 60;
        particula.velocidadeX = (aleatorio.nextDouble() - 0.5) * 2.4;
        particula.velocidadeY = -2.0 + aleatorio.nextDouble() * 4.0;
        particula.angulo = aleatorio.nextDouble() * Math.PI * 2;
        particula.velocidadeAngular = (aleatorio.nextDouble() - 0.5) * 0.3;
        particula.faseOscilacao = aleatorio.nextDouble() * Math.PI * 2;
        particula.frequenciaOscilacao = 0.05 + aleatorio.nextDouble() * 0.08;
        particula.amplitudeOscilacao = 0.6 + aleatorio.nextDouble() * 1.4;
        particula.largura = 6 + aleatorio.nextInt(6);
        particula.altura = 3 + aleatorio.nextInt(5);
        particula.circular = aleatorio.nextInt(4) == 0;
        Color escolhida = paleta[aleatorio.nextInt(paleta.length)];
        particula.cor = escolhida == null ? CORES_PADRAO[0] : escolhida;
        return particula;
    }

    private void avancarQuadro() {
        long decorrido = System.currentTimeMillis() - instanteInicio;
        if (decorrido >= DURACAO_TOTAL_MS) {
            parar();
            return;
        }
        if (decorrido > INICIO_DESVANECIMENTO_MS) {
            double proporcao = (double) (decorrido - INICIO_DESVANECIMENTO_MS)
                    / (DURACAO_TOTAL_MS - INICIO_DESVANECIMENTO_MS);
            opacidadeGlobal = (float) Math.max(0.0, 1.0 - proporcao);
        } else {
            opacidadeGlobal = 1f;
        }
        for (Particula particula : particulas) {
            particula.atualizar();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        if (particulas.isEmpty() || opacidadeGlobal <= 0f) {
            return;
        }
        Graphics2D g2 = (Graphics2D) graficos.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadeGlobal));
            AffineTransform transformacaoOriginal = g2.getTransform();
            int alturaVisivel = getHeight();
            for (Particula particula : particulas) {
                if (alturaVisivel > 0 && particula.y > alturaVisivel + 20) {
                    continue;
                }
                g2.setTransform(transformacaoOriginal);
                g2.translate(particula.x, particula.y);
                g2.rotate(particula.angulo);
                double achatamento = Math.abs(Math.cos(particula.angulo * 1.7));
                int alturaDesenhada = Math.max(1, (int) Math.round(particula.altura * (0.35 + 0.65 * achatamento)));
                g2.setColor(particula.cor);
                if (particula.circular) {
                    int diametro = Math.max(3, particula.altura + 1);
                    g2.fillOval(-diametro / 2, -diametro / 2, diametro, diametro);
                } else {
                    g2.fillRect(-particula.largura / 2, -alturaDesenhada / 2, particula.largura, alturaDesenhada);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private static final class Particula {
        private double x;
        private double y;
        private double velocidadeX;
        private double velocidadeY;
        private double angulo;
        private double velocidadeAngular;
        private double faseOscilacao;
        private double frequenciaOscilacao;
        private double amplitudeOscilacao;
        private int largura;
        private int altura;
        private boolean circular;
        private Color cor;

        private void atualizar() {
            velocidadeY = Math.min(VELOCIDADE_TERMINAL, velocidadeY + GRAVIDADE);
            faseOscilacao += frequenciaOscilacao;
            x += velocidadeX + Math.sin(faseOscilacao) * amplitudeOscilacao;
            y += velocidadeY;
            velocidadeX *= 0.99;
            angulo += velocidadeAngular;
        }
    }
}
