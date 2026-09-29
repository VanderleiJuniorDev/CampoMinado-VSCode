package view.tema;

import java.awt.Color;
import java.awt.Font;

public final class TemaVisual {

    private static final Color[] NUMEROS_PADRAO = {
        null,
        new Color(25, 118, 210),
        new Color(56, 142, 60),
        new Color(211, 47, 47),
        new Color(13, 71, 161),
        new Color(136, 14, 14),
        new Color(0, 131, 143),
        new Color(33, 33, 33),
        new Color(97, 97, 97)
    };

    private final String nome;
    private final Color fundo;
    private final Color fundoClaro;
    private final Color card;
    private final Color cardHover;
    private final Color destaque;
    private final Color textoPrincipal;
    private final Color textoSecundario;
    private final Color borda;
    private final Color celulaOculta;
    private final Color celulaOcultaHover;
    private final Color bordaOculta;
    private final Color celulaRevelada;
    private final Color bordaRevelada;
    private final Color textoSobreRevelada;
    private final Color mina;
    private final Color minaFundo;
    private final Color bandeira;
    private final Color vitoria;
    private final Color alerta;
    private final Color[] coresNumeros;
    private final String familiaFonte;
    private final boolean escuro;

    private TemaVisual(Construtor construtor) {
        this.nome = construtor.nome == null ? "Tema" : construtor.nome;
        this.escuro = construtor.escuro;
        this.fundo = valorOu(construtor.fundo, escuro ? new Color(30, 30, 35) : new Color(240, 240, 245));
        this.fundoClaro = valorOu(construtor.fundoClaro, escuro ? fundo.brighter() : fundo.darker());
        this.card = valorOu(construtor.card, fundoClaro);
        this.cardHover = valorOu(construtor.cardHover, escuro ? card.brighter() : card.darker());
        this.destaque = valorOu(construtor.destaque, new Color(70, 130, 180));
        this.textoPrincipal = valorOu(construtor.textoPrincipal,
                escuro ? new Color(230, 230, 235) : new Color(25, 25, 30));
        this.textoSecundario = valorOu(construtor.textoSecundario,
                escuro ? new Color(150, 150, 160) : new Color(90, 90, 100));
        this.borda = valorOu(construtor.borda, escuro ? new Color(80, 80, 90) : new Color(190, 190, 200));
        this.celulaOculta = valorOu(construtor.celulaOculta, new Color(72, 78, 96));
        this.celulaOcultaHover = valorOu(construtor.celulaOcultaHover, celulaOculta.brighter());
        this.bordaOculta = valorOu(construtor.bordaOculta, celulaOcultaHover);
        this.celulaRevelada = valorOu(construtor.celulaRevelada, new Color(228, 228, 233));
        this.bordaRevelada = valorOu(construtor.bordaRevelada, celulaRevelada.darker());
        this.textoSobreRevelada = valorOu(construtor.textoSobreRevelada, new Color(40, 40, 45));
        this.mina = valorOu(construtor.mina, new Color(220, 60, 60));
        this.minaFundo = valorOu(construtor.minaFundo, new Color(60, 20, 20));
        this.bandeira = valorOu(construtor.bandeira, new Color(230, 180, 50));
        this.vitoria = valorOu(construtor.vitoria, new Color(50, 180, 80));
        this.alerta = valorOu(construtor.alerta, new Color(230, 140, 40));
        this.coresNumeros = new Color[9];
        for (int i = 1; i <= 8; i++) {
            Color informada = construtor.coresNumeros == null || construtor.coresNumeros.length <= i
                    ? null : construtor.coresNumeros[i];
            this.coresNumeros[i] = valorOu(informada, NUMEROS_PADRAO[i]);
        }
        this.familiaFonte = construtor.familiaFonte == null ? "Segoe UI" : construtor.familiaFonte;
    }

    private static Color valorOu(Color valor, Color padrao) {
        return valor == null ? padrao : valor;
    }

    public static Construtor novo(String nome) {
        return new Construtor(nome);
    }

    public String getNome() {
        return nome;
    }

    public Color getFundo() {
        return fundo;
    }

    public Color getFundoClaro() {
        return fundoClaro;
    }

    public Color getCard() {
        return card;
    }

    public Color getCardHover() {
        return cardHover;
    }

    public Color getDestaque() {
        return destaque;
    }

    public Color getTextoPrincipal() {
        return textoPrincipal;
    }

    public Color getTextoSecundario() {
        return textoSecundario;
    }

    public Color getBorda() {
        return borda;
    }

    public Color getCelulaOculta() {
        return celulaOculta;
    }

    public Color getCelulaOcultaHover() {
        return celulaOcultaHover;
    }

    public Color getBordaOculta() {
        return bordaOculta;
    }

    public Color getCelulaRevelada() {
        return celulaRevelada;
    }

    public Color getBordaRevelada() {
        return bordaRevelada;
    }

    public Color getTextoSobreRevelada() {
        return textoSobreRevelada;
    }

    public Color getMina() {
        return mina;
    }

    public Color getMinaFundo() {
        return minaFundo;
    }

    public Color getBandeira() {
        return bandeira;
    }

    public Color getVitoria() {
        return vitoria;
    }

    public Color getAlerta() {
        return alerta;
    }

    public Color[] getCoresNumeros() {
        return coresNumeros.clone();
    }

    public Color getCorNumero(int n) {
        if (n < 1 || n > 8) {
            return textoSobreRevelada;
        }
        return coresNumeros[n];
    }

    public String getFamiliaFonte() {
        return familiaFonte;
    }

    public boolean isEscuro() {
        return escuro;
    }

    public Font fonte(int estilo, float tamanho) {
        return new Font(familiaFonte, estilo, Math.round(tamanho));
    }

    @Override
    public String toString() {
        return nome;
    }

    public static class Construtor {

        private final String nome;
        private Color fundo;
        private Color fundoClaro;
        private Color card;
        private Color cardHover;
        private Color destaque;
        private Color textoPrincipal;
        private Color textoSecundario;
        private Color borda;
        private Color celulaOculta;
        private Color celulaOcultaHover;
        private Color bordaOculta;
        private Color celulaRevelada;
        private Color bordaRevelada;
        private Color textoSobreRevelada;
        private Color mina;
        private Color minaFundo;
        private Color bandeira;
        private Color vitoria;
        private Color alerta;
        private Color[] coresNumeros;
        private String familiaFonte;
        private boolean escuro = true;

        public Construtor(String nome) {
            this.nome = nome;
        }

        public Construtor fundo(Color fundo) {
            this.fundo = fundo;
            return this;
        }

        public Construtor fundoClaro(Color fundoClaro) {
            this.fundoClaro = fundoClaro;
            return this;
        }

        public Construtor card(Color card) {
            this.card = card;
            return this;
        }

        public Construtor cardHover(Color cardHover) {
            this.cardHover = cardHover;
            return this;
        }

        public Construtor destaque(Color destaque) {
            this.destaque = destaque;
            return this;
        }

        public Construtor textoPrincipal(Color textoPrincipal) {
            this.textoPrincipal = textoPrincipal;
            return this;
        }

        public Construtor textoSecundario(Color textoSecundario) {
            this.textoSecundario = textoSecundario;
            return this;
        }

        public Construtor borda(Color borda) {
            this.borda = borda;
            return this;
        }

        public Construtor celulaOculta(Color celulaOculta) {
            this.celulaOculta = celulaOculta;
            return this;
        }

        public Construtor celulaOcultaHover(Color celulaOcultaHover) {
            this.celulaOcultaHover = celulaOcultaHover;
            return this;
        }

        public Construtor bordaOculta(Color bordaOculta) {
            this.bordaOculta = bordaOculta;
            return this;
        }

        public Construtor celulaRevelada(Color celulaRevelada) {
            this.celulaRevelada = celulaRevelada;
            return this;
        }

        public Construtor bordaRevelada(Color bordaRevelada) {
            this.bordaRevelada = bordaRevelada;
            return this;
        }

        public Construtor textoSobreRevelada(Color textoSobreRevelada) {
            this.textoSobreRevelada = textoSobreRevelada;
            return this;
        }

        public Construtor mina(Color mina) {
            this.mina = mina;
            return this;
        }

        public Construtor minaFundo(Color minaFundo) {
            this.minaFundo = minaFundo;
            return this;
        }

        public Construtor bandeira(Color bandeira) {
            this.bandeira = bandeira;
            return this;
        }

        public Construtor vitoria(Color vitoria) {
            this.vitoria = vitoria;
            return this;
        }

        public Construtor alerta(Color alerta) {
            this.alerta = alerta;
            return this;
        }

        public Construtor coresNumeros(Color... coresNumeros) {
            if (coresNumeros == null) {
                this.coresNumeros = null;
                return this;
            }
            if (coresNumeros.length == 8) {
                Color[] ajustadas = new Color[9];
                System.arraycopy(coresNumeros, 0, ajustadas, 1, 8);
                this.coresNumeros = ajustadas;
            } else {
                this.coresNumeros = coresNumeros.clone();
            }
            return this;
        }

        public Construtor familiaFonte(String familiaFonte) {
            this.familiaFonte = familiaFonte;
            return this;
        }

        public Construtor escuro(boolean escuro) {
            this.escuro = escuro;
            return this;
        }

        public TemaVisual construir() {
            return new TemaVisual(this);
        }
    }
}
