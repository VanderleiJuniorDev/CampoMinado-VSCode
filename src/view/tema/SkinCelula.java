package view.tema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SkinCelula {

    NUMEROS("Números", null),
    FRUTAS("Frutas", new String[] {"🍒", "🍋", "🍇", "🍉", "🍊", "🍍", "🥝", "🍓"}),
    ANIMAIS("Animais", new String[] {"🐱", "🐶", "🐸", "🐵", "🐼", "🦊", "🐯", "🐷"});

    private final String nome;
    private final String[] simbolos;

    SkinCelula(String nome, String[] simbolos) {
        this.nome = nome;
        this.simbolos = simbolos;
    }

    public String getNome() {
        return nome;
    }

    public String simbolo(int minasVizinhas) {
        if (simbolos == null || minasVizinhas < 1 || minasVizinhas > simbolos.length) {
            return String.valueOf(minasVizinhas);
        }
        return simbolos[minasVizinhas - 1];
    }

    public boolean usaEmoji() {
        return simbolos != null;
    }

    public String legenda() {
        if (simbolos == null) {
            return "";
        }
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < simbolos.length; i++) {
            if (i > 0) {
                texto.append("  ");
            }
            texto.append(simbolos[i]).append('=').append(i + 1);
        }
        return texto.toString();
    }

    public static SkinCelula porNome(String nome) {
        if (nome != null) {
            for (SkinCelula skin : values()) {
                if (skin.nome.equals(nome) || skin.name().equals(nome)) {
                    return skin;
                }
            }
        }
        return NUMEROS;
    }

    public static List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        for (SkinCelula skin : values()) {
            nomes.add(skin.nome);
        }
        return Collections.unmodifiableList(nomes);
    }

    @Override
    public String toString() {
        return nome;
    }
}
