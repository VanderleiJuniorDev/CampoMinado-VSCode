package controller;

public enum ModoJogadores {
    SOLO("Solo"),
    COOPERATIVO("Cooperativo (2 jogadores)"),
    DUELO("Duelo (2 jogadores)");

    private final String nome;

    ModoJogadores(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static ModoJogadores porNome(String nome) {
        for (ModoJogadores modo : values()) {
            if (modo.nome.equals(nome)) {
                return modo;
            }
        }
        return SOLO;
    }

    @Override
    public String toString() {
        return nome;
    }
}
