package view.tema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SkinBandeira {

    PADRAO("Bandeira", "🚩"),
    TRIANGULO("Triângulo", "▲"),
    ESTRELA("Estrela", "★"),
    CORACAO("Coração", "♥"),
    ALFINETE("Alfinete", "📌");

    private final String nome;
    private final String simbolo;

    SkinBandeira(String nome, String simbolo) {
        this.nome = nome;
        this.simbolo = simbolo;
    }

    public String getNome() {
        return nome;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public static SkinBandeira porNome(String nome) {
        if (nome != null) {
            for (SkinBandeira skin : values()) {
                if (skin.nome.equals(nome) || skin.name().equals(nome)) {
                    return skin;
                }
            }
        }
        return PADRAO;
    }

    public static List<String> nomes() {
        List<String> nomes = new ArrayList<>();
        for (SkinBandeira skin : values()) {
            nomes.add(skin.nome);
        }
        return Collections.unmodifiableList(nomes);
    }

    @Override
    public String toString() {
        return nome;
    }
}
