package view.tema;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CatalogoTemas {

    public static final String AUTOMATICO = "Automático (sistema)";

    public static final String ESCURO = "Escuro";
    public static final String CLARO = "Claro";

    private static final List<TemaVisual> TEMAS = criarTemas();

    private CatalogoTemas() {
    }

    public static List<TemaVisual> todos() {
        return TEMAS;
    }

    public static List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        nomes.add(AUTOMATICO);
        for (TemaVisual tema : TEMAS) {
            nomes.add(tema.getNome());
        }
        return Collections.unmodifiableList(nomes);
    }

    public static TemaVisual porNome(String nome) {
        if (nome != null) {
            for (TemaVisual tema : TEMAS) {
                if (tema.getNome().equals(nome)) {
                    return tema;
                }
            }
        }
        return TEMAS.get(0);
    }

    public static TemaVisual resolver(String nome) {
        if (AUTOMATICO.equals(nome)) {
            return porNome(DetectorTemaSistema.sistemaEmModoEscuro() ? ESCURO : CLARO);
        }
        return porNome(nome);
    }

    private static Color cor(int vermelho, int verde, int azul) {
        return new Color(vermelho, verde, azul);
    }

    private static List<TemaVisual> criarTemas() {
        List<TemaVisual> temas = new ArrayList<>();
        temas.add(criarEscuro());
        temas.add(criarClaro());
        temas.add(criarTerminalRetro());
        temas.add(criarCyberpunkNeon());
        temas.add(criarHalloween());
        temas.add(criarOceanoTropical());
        temas.add(criarAltoContraste());
        return Collections.unmodifiableList(temas);
    }

    private static TemaVisual criarEscuro() {
        return TemaVisual.novo(ESCURO)
                .fundo(cor(30, 30, 35))
                .fundoClaro(cor(45, 45, 52))
                .card(cor(50, 50, 58))
                .cardHover(cor(65, 65, 78))
                .destaque(cor(70, 130, 180))
                .textoPrincipal(cor(230, 230, 235))
                .textoSecundario(cor(150, 150, 160))
                .borda(cor(80, 80, 90))
                .celulaOculta(cor(72, 78, 96))
                .celulaOcultaHover(cor(90, 97, 118))
                .bordaOculta(cor(100, 107, 128))
                .celulaRevelada(cor(228, 228, 233))
                .bordaRevelada(cor(195, 195, 202))
                .textoSobreRevelada(cor(40, 40, 45))
                .mina(cor(220, 60, 60))
                .minaFundo(cor(60, 20, 20))
                .bandeira(cor(230, 180, 50))
                .vitoria(cor(50, 180, 80))
                .alerta(cor(230, 140, 40))
                .coresNumeros(
                        cor(25, 118, 210), cor(56, 142, 60), cor(211, 47, 47), cor(13, 71, 161),
                        cor(136, 14, 14), cor(0, 131, 143), cor(33, 33, 33), cor(97, 97, 97))
                .familiaFonte("Segoe UI")
                .escuro(true)
                .construir();
    }

    private static TemaVisual criarClaro() {
        return TemaVisual.novo(CLARO)
                .fundo(cor(242, 243, 247))
                .fundoClaro(cor(255, 255, 255))
                .card(cor(255, 255, 255))
                .cardHover(cor(230, 233, 240))
                .destaque(cor(30, 100, 170))
                .textoPrincipal(cor(25, 25, 35))
                .textoSecundario(cor(90, 95, 110))
                .borda(cor(200, 203, 212))
                .celulaOculta(cor(160, 173, 200))
                .celulaOcultaHover(cor(184, 195, 218))
                .bordaOculta(cor(135, 148, 175))
                .celulaRevelada(cor(252, 252, 254))
                .bordaRevelada(cor(214, 217, 226))
                .textoSobreRevelada(cor(40, 40, 45))
                .mina(cor(211, 47, 47))
                .minaFundo(cor(255, 205, 205))
                .bandeira(cor(214, 120, 0))
                .vitoria(cor(46, 125, 50))
                .alerta(cor(214, 110, 0))
                .coresNumeros(
                        cor(25, 118, 210), cor(56, 142, 60), cor(211, 47, 47), cor(13, 71, 161),
                        cor(136, 14, 14), cor(0, 131, 143), cor(33, 33, 33), cor(97, 97, 97))
                .familiaFonte("Segoe UI")
                .escuro(false)
                .construir();
    }

    private static TemaVisual criarTerminalRetro() {
        return TemaVisual.novo("Terminal Retrô")
                .fundo(cor(4, 10, 4))
                .fundoClaro(cor(10, 22, 10))
                .card(cor(12, 28, 12))
                .cardHover(cor(20, 44, 20))
                .destaque(cor(51, 255, 51))
                .textoPrincipal(cor(51, 255, 51))
                .textoSecundario(cor(40, 180, 40))
                .borda(cor(25, 95, 25))
                .celulaOculta(cor(28, 86, 28))
                .celulaOcultaHover(cor(38, 112, 38))
                .bordaOculta(cor(51, 200, 51))
                .celulaRevelada(cor(0, 0, 0))
                .bordaRevelada(cor(18, 48, 18))
                .textoSobreRevelada(cor(51, 255, 51))
                .mina(cor(255, 90, 40))
                .minaFundo(cor(60, 12, 0))
                .bandeira(cor(255, 176, 0))
                .vitoria(cor(51, 255, 51))
                .alerta(cor(255, 176, 0))
                .coresNumeros(
                        cor(51, 255, 51), cor(170, 255, 0), cor(255, 176, 0), cor(0, 230, 120),
                        cor(255, 130, 40), cor(130, 255, 200), cor(255, 220, 110), cor(180, 210, 180))
                .familiaFonte("Monospaced")
                .escuro(true)
                .construir();
    }

    private static TemaVisual criarCyberpunkNeon() {
        return TemaVisual.novo("Cyberpunk Neon")
                .fundo(cor(13, 2, 33))
                .fundoClaro(cor(26, 8, 56))
                .card(cor(32, 10, 66))
                .cardHover(cor(50, 16, 94))
                .destaque(cor(255, 20, 147))
                .textoPrincipal(cor(242, 240, 255))
                .textoSecundario(cor(120, 230, 240))
                .borda(cor(120, 40, 160))
                .celulaOculta(cor(92, 36, 170))
                .celulaOcultaHover(cor(118, 50, 205))
                .bordaOculta(cor(0, 240, 255))
                .celulaRevelada(cor(8, 0, 20))
                .bordaRevelada(cor(70, 24, 110))
                .textoSobreRevelada(cor(0, 255, 255))
                .mina(cor(255, 40, 80))
                .minaFundo(cor(70, 0, 40))
                .bandeira(cor(255, 240, 31))
                .vitoria(cor(57, 255, 20))
                .alerta(cor(255, 110, 40))
                .coresNumeros(
                        cor(0, 240, 255), cor(57, 255, 20), cor(255, 20, 147), cor(255, 240, 31),
                        cor(255, 120, 40), cor(190, 100, 255), cor(255, 255, 255), cor(255, 150, 220))
                .familiaFonte("Segoe UI")
                .escuro(true)
                .construir();
    }

    private static TemaVisual criarHalloween() {
        return TemaVisual.novo("Halloween")
                .fundo(cor(22, 14, 26))
                .fundoClaro(cor(36, 22, 40))
                .card(cor(44, 26, 50))
                .cardHover(cor(64, 36, 72))
                .destaque(cor(255, 117, 24))
                .textoPrincipal(cor(255, 236, 210))
                .textoSecundario(cor(196, 164, 140))
                .borda(cor(96, 58, 40))
                .celulaOculta(cor(98, 48, 138))
                .celulaOcultaHover(cor(124, 64, 170))
                .bordaOculta(cor(255, 140, 40))
                .celulaRevelada(cor(245, 228, 200))
                .bordaRevelada(cor(196, 164, 124))
                .textoSobreRevelada(cor(60, 35, 20))
                .mina(cor(200, 40, 0))
                .minaFundo(cor(80, 20, 0))
                .bandeira(cor(255, 117, 24))
                .vitoria(cor(140, 200, 60))
                .alerta(cor(255, 160, 30))
                .coresNumeros(
                        cor(96, 40, 150), cor(30, 105, 40), cor(186, 58, 0), cor(60, 20, 110),
                        cor(120, 30, 10), cor(0, 98, 108), cor(40, 20, 10), cor(98, 68, 48))
                .familiaFonte("Segoe UI")
                .escuro(true)
                .construir();
    }

    private static TemaVisual criarOceanoTropical() {
        return TemaVisual.novo("Oceano Tropical")
                .fundo(cor(4, 30, 60))
                .fundoClaro(cor(8, 45, 85))
                .card(cor(12, 55, 100))
                .cardHover(cor(20, 75, 130))
                .destaque(cor(255, 127, 80))
                .textoPrincipal(cor(230, 248, 255))
                .textoSecundario(cor(140, 200, 220))
                .borda(cor(30, 90, 140))
                .celulaOculta(cor(0, 122, 136))
                .celulaOcultaHover(cor(0, 150, 164))
                .bordaOculta(cor(64, 224, 208))
                .celulaRevelada(cor(238, 250, 247))
                .bordaRevelada(cor(178, 214, 214))
                .textoSobreRevelada(cor(10, 40, 70))
                .mina(cor(240, 70, 60))
                .minaFundo(cor(90, 20, 30))
                .bandeira(cor(255, 127, 80))
                .vitoria(cor(64, 224, 208))
                .alerta(cor(255, 190, 60))
                .coresNumeros(
                        cor(0, 90, 180), cor(0, 118, 88), cor(214, 58, 40), cor(10, 40, 120),
                        cor(160, 40, 60), cor(0, 112, 124), cor(40, 40, 70), cor(96, 96, 118))
                .familiaFonte("Segoe UI")
                .escuro(true)
                .construir();
    }

    private static TemaVisual criarAltoContraste() {
        return TemaVisual.novo("Alto Contraste")
                .fundo(cor(0, 0, 0))
                .fundoClaro(cor(18, 18, 18))
                .card(cor(10, 10, 10))
                .cardHover(cor(48, 48, 0))
                .destaque(cor(255, 255, 0))
                .textoPrincipal(cor(255, 255, 255))
                .textoSecundario(cor(230, 230, 230))
                .borda(cor(255, 255, 255))
                .celulaOculta(cor(64, 64, 64))
                .celulaOcultaHover(cor(96, 96, 0))
                .bordaOculta(cor(255, 255, 0))
                .celulaRevelada(cor(0, 0, 0))
                .bordaRevelada(cor(128, 128, 128))
                .textoSobreRevelada(cor(255, 255, 255))
                .mina(cor(255, 64, 64))
                .minaFundo(cor(128, 0, 0))
                .bandeira(cor(255, 255, 0))
                .vitoria(cor(0, 255, 0))
                .alerta(cor(255, 190, 0))
                .coresNumeros(
                        cor(255, 255, 255), cor(255, 255, 0), cor(0, 255, 255), cor(255, 170, 255),
                        cor(255, 190, 0), cor(160, 255, 160), cor(255, 255, 255), cor(255, 255, 0))
                .familiaFonte("Segoe UI")
                .escuro(true)
                .construir();
    }
}
