package view.tema;

public final class SimbolosDaltonismo {

    private static final String[] FORMAS = {"●", "▲", "■", "◆", "★", "✚", "✖", "⬢"};

    private SimbolosDaltonismo() {
    }

    public static String simbolo(int minasVizinhas) {
        if (minasVizinhas < 1 || minasVizinhas > FORMAS.length) {
            return "";
        }
        return FORMAS[minasVizinhas - 1];
    }

    public static String comSimbolo(String texto, int minasVizinhas) {
        String base = texto == null ? "" : texto;
        return base + simbolo(minasVizinhas);
    }
}
