package persistencia;

public enum Conquista {

    PRIMEIRA_VITORIA("Primeira vitória", "Vença sua primeira partida", "🥇"),
    MAOS_LIMPAS("Mãos limpas", "Vença sem usar nenhuma bandeira", "🧤"),
    VELOCISTA("Velocista", "Vença em menos de 30 segundos", "⚡"),
    VETERANO("Veterano", "Acumule 10 vitórias", "🎖"),
    MARATONISTA("Maratonista", "Jogue 50 partidas", "🏃"),
    MESTRE_AVANCADO("Mestre do Avançado", "Vença no nível Avançado", "👑"),
    SEM_AJUDA("Sem ajuda", "Vença no Intermediário ou Avançado sem usar dicas", "🧠"),
    SOBREVIVENTE("Sobrevivente", "Vença no modo 3 vidas depois de perder ao menos uma vida", "❤"),
    DESAFIO_DIARIO("Desafio diário", "Vença o tabuleiro do dia", "📅"),
    MAESTRO("Maestro", "Use o chording 10 vezes em uma única vitória", "🎼"),
    EM_CHAMAS("Em chamas", "Vença 3 partidas seguidas", "🔥"),
    EXPLORADOR("Explorador", "Vença em um tabuleiro toroidal", "🌐");

    private final String titulo;
    private final String descricao;
    private final String emoji;

    Conquista(String titulo, String descricao, String emoji) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.emoji = emoji;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getEmoji() {
        return emoji;
    }
}
