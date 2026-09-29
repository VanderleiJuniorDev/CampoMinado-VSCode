package main;

import java.util.List;
import java.util.Scanner;
import model.ConfiguracaoPartida;
import model.EstadoMarcacao;
import model.Tabuleiro;

public class JogoCampoMinado {

    private static final int MAXIMO_ALTERNANCIAS = 3;

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        System.out.println("=== Campo Minado ===");
        Tabuleiro tabuleiro = criarTabuleiro(teclado);
        if (tabuleiro == null) {
            System.out.println("\nEntrada encerrada antes do início da partida.");
            teclado.close();
            return;
        }

        while (!tabuleiro.isJogoEncerrado()) {
            tabuleiro.imprimir(false);
            System.out.println();
            System.out.println("Minas: " + tabuleiro.getNumMinas() + " | Bandeiras: " + tabuleiro.contarMarcadas());
            System.out.println("Comandos: 'r linha coluna' revelar, 'm linha coluna' bandeira,"
                    + " 'i linha coluna' interrogação, 'c linha coluna' revelar vizinhos");
            System.out.print("> ");

            if (!teclado.hasNextLine()) {
                System.out.println("\nEntrada encerrada. Partida interrompida.");
                teclado.close();
                return;
            }
            executarComando(tabuleiro, teclado.nextLine().trim());
        }

        tabuleiro.imprimir(true);
        if (tabuleiro.isDerrota()) {
            System.out.println("\nVocê pisou em uma mina. Fim de jogo!");
        } else {
            System.out.println("\nParabéns, você venceu! Todas as células seguras foram reveladas.");
        }

        teclado.close();
    }

    private static Tabuleiro criarTabuleiro(Scanner teclado) {
        while (true) {
            Integer linhas = lerInteiro(teclado, "Número de linhas: ");
            Integer colunas = lerInteiro(teclado, "Número de colunas: ");
            Integer minas = lerInteiro(teclado, "Número de minas: ");
            if (linhas == null || colunas == null || minas == null) {
                return null;
            }
            try {
                ConfiguracaoPartida configuracao = new ConfiguracaoPartida(linhas, colunas, minas)
                        .comInterrogacao(true);
                return new Tabuleiro(configuracao);
            } catch (IllegalArgumentException erro) {
                System.out.println(erro.getMessage() + " Tente novamente.");
            }
        }
    }

    private static void executarComando(Tabuleiro tabuleiro, String entrada) {
        String[] partes = entrada.split("\\s+");
        if (partes.length != 3) {
            System.out.println("Comando inválido. Use 'r', 'm', 'i' ou 'c' seguido de linha e coluna.");
            return;
        }

        int linha;
        int coluna;
        try {
            linha = Integer.parseInt(partes[1]);
            coluna = Integer.parseInt(partes[2]);
        } catch (NumberFormatException erro) {
            System.out.println("Linha e coluna devem ser números inteiros.");
            return;
        }

        if (!tabuleiro.dentroDosLimites(linha, coluna)) {
            System.out.println("Posição fora do tabuleiro.");
            return;
        }

        String comando = partes[0];
        if (comando.equalsIgnoreCase("r")) {
            if (tabuleiro.revelar(linha, coluna).isEmpty()) {
                System.out.println("Nada foi revelado (célula já revelada ou com bandeira).");
            }
        } else if (comando.equalsIgnoreCase("m")) {
            alternarAte(tabuleiro, linha, coluna,
                    tabuleiro.isMarcada(linha, coluna) ? EstadoMarcacao.NENHUMA : EstadoMarcacao.BANDEIRA);
        } else if (comando.equalsIgnoreCase("i")) {
            alternarAte(tabuleiro, linha, coluna,
                    tabuleiro.isInterrogacao(linha, coluna) ? EstadoMarcacao.NENHUMA : EstadoMarcacao.INTERROGACAO);
        } else if (comando.equalsIgnoreCase("c")) {
            List<int[]> reveladas = tabuleiro.revelarVizinhos(linha, coluna);
            if (reveladas.isEmpty()) {
                System.out.println("Não é possível revelar os vizinhos dessa célula.");
            }
        } else {
            System.out.println("Comando inválido. Use 'r', 'm', 'i' ou 'c'.");
        }
    }

    private static void alternarAte(Tabuleiro tabuleiro, int linha, int coluna, EstadoMarcacao estadoDesejado) {
        if (tabuleiro.isRevelada(linha, coluna)) {
            System.out.println("Não é possível marcar uma célula já revelada.");
            return;
        }
        for (int tentativa = 0; tentativa < MAXIMO_ALTERNANCIAS; tentativa++) {
            if (estadoAtual(tabuleiro, linha, coluna) == estadoDesejado) {
                return;
            }
            tabuleiro.alternarMarcacao(linha, coluna);
        }
    }

    private static EstadoMarcacao estadoAtual(Tabuleiro tabuleiro, int linha, int coluna) {
        if (tabuleiro.isMarcada(linha, coluna)) {
            return EstadoMarcacao.BANDEIRA;
        }
        if (tabuleiro.isInterrogacao(linha, coluna)) {
            return EstadoMarcacao.INTERROGACAO;
        }
        return EstadoMarcacao.NENHUMA;
    }

    private static Integer lerInteiro(Scanner teclado, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            if (!teclado.hasNextLine()) {
                return null;
            }
            String texto = teclado.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número inteiro.");
            }
        }
    }
}
