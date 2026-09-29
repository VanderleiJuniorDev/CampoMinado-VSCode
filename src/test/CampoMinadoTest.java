package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import model.Celula;
import model.ConfiguracaoPartida;
import model.EstadoMarcacao;
import model.Tabuleiro;

public class CampoMinadoTest {

    private static final int[][] SEM_MINAS = {};

    private static boolean contemPosicao(List<int[]> posicoes, int linha, int coluna) {
        for (int[] posicao : posicoes) {
            if (posicao[0] == linha && posicao[1] == coluna) {
                return true;
            }
        }
        return false;
    }

    private static boolean mesmasPosicoes(int[][] primeiras, int[][] segundas) {
        if (primeiras.length != segundas.length) {
            return false;
        }
        for (int i = 0; i < primeiras.length; i++) {
            if (primeiras[i][0] != segundas[i][0] || primeiras[i][1] != segundas[i][1]) {
                return false;
            }
        }
        return true;
    }

    @Test
    void testContagemDeMinasVizinhas() {
        int[][] minas = { { 1, 1 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(1, tabuleiro.getCelula(0, 0).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(0, 1).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(2, 2).getMinasVizinhas());
        assertEquals(0, tabuleiro.getCelula(1, 1).getMinasVizinhas());
    }

    @Test
    void testContagemDeMinasVizinhasComDuasMinasAdjacentes() {
        int[][] minas = { { 0, 0 }, { 0, 1 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(2, tabuleiro.getCelula(1, 0).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(0, 2).getMinasVizinhas());
        assertEquals(2, tabuleiro.getCelula(1, 1).getMinasVizinhas());
        assertEquals(1, tabuleiro.getCelula(1, 2).getMinasVizinhas());
        assertEquals(0, tabuleiro.getCelula(2, 2).getMinasVizinhas());
    }

    @Test
    void testCascataRevelaTodasAsCelulasSemMinasProximas() {
        Tabuleiro tabuleiro = new Tabuleiro(4, 4, SEM_MINAS);

        List<int[]> reveladas = tabuleiro.revelar(0, 0);

        assertEquals(16, reveladas.size());
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertTrue(tabuleiro.getCelula(i, j).isRevelada(),
                        "Célula (" + i + "," + j + ") deveria ter sido revelada pela cascata");
            }
        }
    }

    @Test
    void testCascataNaoRevelaMinaNemPassaDelaAdiante() {
        int[][] minas = { { 2, 2 } };
        Tabuleiro tabuleiro = new Tabuleiro(5, 5, minas);

        tabuleiro.revelar(0, 0);

        assertFalse(tabuleiro.getCelula(2, 2).isRevelada(),
                "A célula minada nunca deve ser revelada pela cascata");
        assertTrue(tabuleiro.getCelula(1, 2).isRevelada());
    }

    @Test
    void testRevelarCelulaMinadaEncerraOJogoComDerrota() {
        int[][] minas = { { 1, 1 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        tabuleiro.revelar(1, 1);

        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertFalse(tabuleiro.isVitoria());
        assertTrue(tabuleiro.getCelula(1, 1).isRevelada());
        assertTrue(tabuleiro.isExplodida(1, 1));
        assertEquals(0, tabuleiro.getVidasRestantes());
    }

    @Test
    void testMarcarEDesmarcarCelulaComBandeira() {
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertFalse(tabuleiro.getCelula(1, 1).isMarcada());

        tabuleiro.alternarMarcacao(1, 1);
        assertTrue(tabuleiro.getCelula(1, 1).isMarcada());

        tabuleiro.alternarMarcacao(1, 1);
        assertFalse(tabuleiro.getCelula(1, 1).isMarcada());
    }

    @Test
    void testCelulaMarcadaNaoPodeSerRevelada() {
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, SEM_MINAS);

        tabuleiro.alternarMarcacao(0, 0);
        List<int[]> reveladas = tabuleiro.revelar(0, 0);

        assertTrue(reveladas.isEmpty());
        assertFalse(tabuleiro.getCelula(0, 0).isRevelada(),
                "Uma célula marcada com bandeira não deve ser revelada");
    }

    @Test
    void testVerificarVitoriaQuandoTodasAsCelulasSeguraForamReveladas() {
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, minas);

        assertFalse(tabuleiro.verificarVitoria());

        tabuleiro.revelar(0, 1);
        tabuleiro.revelar(1, 0);
        tabuleiro.revelar(1, 1);

        assertTrue(tabuleiro.verificarVitoria());
        assertTrue(tabuleiro.isJogoEncerrado());
        assertFalse(tabuleiro.isDerrota());
        assertTrue(tabuleiro.isVitoria());
    }

    @Test
    void testVerificarVitoriaEhFalsaEnquantoHouverCelulaSeguraNaoRevelada() {
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(2, 2, minas);

        tabuleiro.revelar(0, 1);

        assertFalse(tabuleiro.verificarVitoria());
        assertFalse(tabuleiro.isJogoEncerrado());
    }

    @Test
    void testModoSemCascataRevelaApenasACelulaEscolhida() {
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(3, 3, 0).comSemCascata(true);
        Tabuleiro tabuleiro = new Tabuleiro(configuracao, SEM_MINAS);

        List<int[]> reveladas = tabuleiro.revelar(0, 0);

        assertEquals(1, reveladas.size());
        assertArrayEquals(new int[] { 0, 0 }, reveladas.get(0));
        assertTrue(tabuleiro.isRevelada(0, 0));
        assertFalse(tabuleiro.isRevelada(0, 1));
        assertFalse(tabuleiro.isRevelada(1, 1));
        assertEquals(1, tabuleiro.contarReveladasSeguras());
    }

    @Test
    void testContagemToroidalConsideraBordasOpostas() {
        int[][] minas = { { 0, 0 } };
        ConfiguracaoPartida toroidal = new ConfiguracaoPartida(4, 4, 1).comToroidal(true);
        Tabuleiro tabuleiroToroidal = new Tabuleiro(toroidal, minas);
        Tabuleiro tabuleiroNormal = new Tabuleiro(4, 4, minas);

        assertEquals(1, tabuleiroToroidal.getMinasVizinhas(3, 3));
        assertEquals(1, tabuleiroToroidal.getMinasVizinhas(0, 3));
        assertEquals(1, tabuleiroToroidal.getMinasVizinhas(3, 0));
        assertEquals(0, tabuleiroToroidal.getMinasVizinhas(2, 2));
        assertEquals(0, tabuleiroNormal.getMinasVizinhas(3, 3));
        assertEquals(0, tabuleiroNormal.getMinasVizinhas(0, 3));
    }

    @Test
    void testCascataToroidalAtravessaAsBordas() {
        int[][] minas = { { 0, 3 } };
        Tabuleiro tabuleiroNormal = new Tabuleiro(1, 6, minas);
        Tabuleiro tabuleiroToroidal = new Tabuleiro(new ConfiguracaoPartida(1, 6, 1).comToroidal(true), minas);

        tabuleiroNormal.revelar(0, 0);
        List<int[]> reveladasToroidal = tabuleiroToroidal.revelar(0, 0);

        assertFalse(tabuleiroNormal.isRevelada(0, 5));
        assertFalse(tabuleiroNormal.isRevelada(0, 4));
        assertTrue(tabuleiroToroidal.isRevelada(0, 5));
        assertTrue(tabuleiroToroidal.isRevelada(0, 4));
        assertTrue(contemPosicao(reveladasToroidal, 0, 5));
        assertFalse(tabuleiroToroidal.isRevelada(0, 3));
        assertTrue(tabuleiroToroidal.isVitoria());
    }

    @Test
    void testVizinhosToroidaisSemRepeticaoEmTabuleiroPequeno() {
        Tabuleiro tabuleiro2x2 = new Tabuleiro(new ConfiguracaoPartida(2, 2, 0).comToroidal(true), SEM_MINAS);
        List<int[]> vizinhos = tabuleiro2x2.getVizinhos(0, 0);

        assertEquals(3, vizinhos.size());
        assertFalse(contemPosicao(vizinhos, 0, 0));
        assertTrue(contemPosicao(vizinhos, 0, 1));
        assertTrue(contemPosicao(vizinhos, 1, 0));
        assertTrue(contemPosicao(vizinhos, 1, 1));

        Tabuleiro tabuleiroLinha = new Tabuleiro(new ConfiguracaoPartida(1, 5, 0).comToroidal(true), SEM_MINAS);
        List<int[]> vizinhosLinha = tabuleiroLinha.getVizinhos(0, 0);
        assertEquals(2, vizinhosLinha.size());
        assertTrue(contemPosicao(vizinhosLinha, 0, 4));
        assertTrue(contemPosicao(vizinhosLinha, 0, 1));

        Tabuleiro tabuleiroUnico = new Tabuleiro(new ConfiguracaoPartida(1, 1, 0).comToroidal(true), SEM_MINAS);
        assertEquals(0, tabuleiroUnico.getVizinhos(0, 0).size());

        Tabuleiro tabuleiroNormal = new Tabuleiro(3, 3, SEM_MINAS);
        assertEquals(3, tabuleiroNormal.getVizinhos(0, 0).size());
        assertEquals(8, tabuleiroNormal.getVizinhos(1, 1).size());
        assertEquals(0, tabuleiroNormal.getVizinhos(5, 5).size());
    }

    @Test
    void testCicloDeMarcacaoSemInterrogacao() {
        Celula celula = new Celula();

        assertEquals(EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());
        celula.alternarMarcacao();
        assertEquals(EstadoMarcacao.BANDEIRA, celula.getEstadoMarcacao());
        assertEquals("F", celula.toString());
        celula.alternarMarcacao();
        assertEquals(EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());
        assertEquals(".", celula.toString());

        Tabuleiro tabuleiro = new Tabuleiro(3, 3, SEM_MINAS);
        tabuleiro.alternarMarcacao(1, 1);
        assertTrue(tabuleiro.isMarcada(1, 1));
        tabuleiro.alternarMarcacao(1, 1);
        assertFalse(tabuleiro.isMarcada(1, 1));
        assertFalse(tabuleiro.isInterrogacao(1, 1));
    }

    @Test
    void testCicloDeMarcacaoComInterrogacao() {
        Celula celula = new Celula();
        celula.alternarMarcacao(true);
        assertTrue(celula.isMarcada());
        celula.alternarMarcacao(true);
        assertTrue(celula.isInterrogacao());
        assertFalse(celula.isMarcada());
        assertEquals("?", celula.toString());
        celula.alternarMarcacao(true);
        assertEquals(EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());

        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(3, 3, 1).comInterrogacao(true);
        Tabuleiro tabuleiro = new Tabuleiro(configuracao, new int[][] { { 0, 0 } });
        tabuleiro.alternarMarcacao(2, 2);
        assertTrue(tabuleiro.isMarcada(2, 2));
        tabuleiro.alternarMarcacao(2, 2);
        assertTrue(tabuleiro.isInterrogacao(2, 2));
        assertEquals(0, tabuleiro.contarMarcadas());
        tabuleiro.alternarMarcacao(2, 2);
        assertFalse(tabuleiro.isInterrogacao(2, 2));
        assertFalse(tabuleiro.isMarcada(2, 2));
    }

    @Test
    void testCelulaComInterrogacaoPodeSerRevelada() {
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(3, 3, 1).comInterrogacao(true);
        Tabuleiro tabuleiro = new Tabuleiro(configuracao, new int[][] { { 0, 0 } });
        tabuleiro.alternarMarcacao(1, 1);
        tabuleiro.alternarMarcacao(1, 1);
        assertTrue(tabuleiro.isInterrogacao(1, 1));

        List<int[]> reveladas = tabuleiro.revelar(1, 1);

        assertEquals(1, reveladas.size());
        assertTrue(tabuleiro.isRevelada(1, 1));
        assertFalse(tabuleiro.isInterrogacao(1, 1));
        assertEquals("1", tabuleiro.getCelula(1, 1).toString());

        Celula celula = new Celula();
        celula.alternarMarcacao(true);
        celula.alternarMarcacao(true);
        celula.revelar();
        assertTrue(celula.isRevelada());
        assertEquals(EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());
        celula.alternarMarcacao(true);
        assertEquals(EstadoMarcacao.NENHUMA, celula.getEstadoMarcacao());
    }

    @Test
    void testCascataRevelaCelulasComInterrogacao() {
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(3, 3, 0).comInterrogacao(true);
        Tabuleiro tabuleiro = new Tabuleiro(configuracao, SEM_MINAS);
        tabuleiro.alternarMarcacao(2, 2);
        tabuleiro.alternarMarcacao(2, 2);
        tabuleiro.alternarMarcacao(0, 2);

        tabuleiro.revelar(0, 0);

        assertTrue(tabuleiro.isRevelada(2, 2));
        assertFalse(tabuleiro.isInterrogacao(2, 2));
        assertFalse(tabuleiro.isRevelada(0, 2));
        assertTrue(tabuleiro.isMarcada(0, 2));
    }

    @Test
    void testChordingRevelaVizinhosQuandoBandeirasConferem() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][] { { 0, 0 } });
        tabuleiro.revelar(1, 1);
        assertEquals(1, tabuleiro.getMinasVizinhas(1, 1));
        tabuleiro.alternarMarcacao(0, 0);

        assertEquals(1, tabuleiro.contarBandeirasVizinhas(1, 1));
        assertTrue(tabuleiro.podeFazerChording(1, 1));

        List<int[]> reveladas = tabuleiro.revelarVizinhos(1, 1);

        assertEquals(7, reveladas.size());
        assertFalse(contemPosicao(reveladas, 0, 0));
        assertFalse(tabuleiro.isRevelada(0, 0));
        assertTrue(tabuleiro.isVitoria());
        assertFalse(tabuleiro.podeFazerChording(1, 1));
    }

    @Test
    void testChordingRecusadoQuandoBandeirasNaoConferem() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][] { { 0, 0 } });

        assertFalse(tabuleiro.podeFazerChording(1, 1));
        assertTrue(tabuleiro.revelarVizinhos(1, 1).isEmpty());

        tabuleiro.revelar(1, 1);
        assertEquals(0, tabuleiro.contarBandeirasVizinhas(1, 1));
        assertFalse(tabuleiro.podeFazerChording(1, 1));
        assertTrue(tabuleiro.revelarVizinhos(1, 1).isEmpty());

        tabuleiro.alternarMarcacao(0, 1);
        tabuleiro.alternarMarcacao(1, 0);
        assertEquals(2, tabuleiro.contarBandeirasVizinhas(1, 1));
        assertFalse(tabuleiro.podeFazerChording(1, 1));
        assertTrue(tabuleiro.revelarVizinhos(1, 1).isEmpty());
        assertEquals(1, tabuleiro.contarReveladasSeguras());
        assertFalse(tabuleiro.isJogoEncerrado());

        Tabuleiro tabuleiroZero = new Tabuleiro(3, 3, new int[][] { { 0, 0 } });
        tabuleiroZero.revelar(2, 2);
        assertFalse(tabuleiroZero.podeFazerChording(2, 2));
    }

    @Test
    void testChordingComBandeiraErradaExplodeMina() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][] { { 0, 0 } });
        tabuleiro.revelar(1, 1);
        tabuleiro.alternarMarcacao(0, 1);

        assertTrue(tabuleiro.podeFazerChording(1, 1));
        List<int[]> reveladas = tabuleiro.revelarVizinhos(1, 1);

        assertTrue(contemPosicao(reveladas, 0, 0));
        assertTrue(tabuleiro.isExplodida(0, 0));
        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertTrue(tabuleiro.isMarcada(0, 1));
    }

    @Test
    void testTresVidasPermitemContinuarAposExplosoes() {
        int[][] minas = { { 0, 0 }, { 0, 2 }, { 2, 0 } };
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(3, 3, 3).comVidas(3);
        Tabuleiro tabuleiro = new Tabuleiro(configuracao, minas);
        assertEquals(3, tabuleiro.getVidasRestantes());

        List<int[]> primeira = tabuleiro.revelar(0, 0);
        assertEquals(1, primeira.size());
        assertTrue(tabuleiro.isExplodida(0, 0));
        assertEquals(2, tabuleiro.getVidasRestantes());
        assertFalse(tabuleiro.isJogoEncerrado());
        assertFalse(tabuleiro.isDerrota());

        tabuleiro.revelar(1, 1);
        assertTrue(tabuleiro.isRevelada(1, 1));

        tabuleiro.revelar(0, 2);
        assertEquals(1, tabuleiro.getVidasRestantes());
        assertFalse(tabuleiro.isJogoEncerrado());

        tabuleiro.revelar(2, 0);
        assertEquals(0, tabuleiro.getVidasRestantes());
        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertFalse(tabuleiro.isVitoria());
        assertTrue(tabuleiro.revelar(2, 2).isEmpty());
    }

    @Test
    void testVidasMinimasSaoUma() {
        assertEquals(1, new ConfiguracaoPartida(3, 3, 1).getVidas());
        assertEquals(1, new ConfiguracaoPartida(3, 3, 1).comVidas(0).getVidas());
        assertEquals(1, new ConfiguracaoPartida(3, 3, 1).comVidas(-5).getVidas());
    }

    @Test
    void testChordingComMinaExplodidaContandoComoSinalizada() {
        int[][] minas = { { 0, 0 } };
        Tabuleiro tabuleiro = new Tabuleiro(new ConfiguracaoPartida(3, 3, 1).comVidas(2), minas);
        tabuleiro.revelar(0, 0);
        tabuleiro.revelar(0, 1);

        assertTrue(tabuleiro.podeFazerChording(0, 1));
        tabuleiro.revelarVizinhos(0, 1);
        assertTrue(tabuleiro.isVitoria());
        assertEquals(1, tabuleiro.getVidasRestantes());
    }

    @Test
    void testMesmaSementeGeraMesmoTabuleiro() {
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(10, 10, 20).comSemente(42L);
        Tabuleiro primeiro = new Tabuleiro(configuracao);
        Tabuleiro segundo = new Tabuleiro(new ConfiguracaoPartida(10, 10, 20).comSemente(42L));

        int[][] posicoesPrimeiro = primeiro.getPosicoesMinas();
        int[][] posicoesSegundo = segundo.getPosicoesMinas();

        assertEquals(20, posicoesPrimeiro.length);
        assertEquals(posicoesPrimeiro.length, posicoesSegundo.length);
        for (int i = 0; i < posicoesPrimeiro.length; i++) {
            assertArrayEquals(posicoesPrimeiro[i], posicoesSegundo[i]);
        }
        assertEquals(Long.valueOf(42L), primeiro.getConfiguracao().getSemente());
    }

    @Test
    void testSementesDiferentesGeramTabuleirosDiferentes() {
        Tabuleiro primeiro = new Tabuleiro(new ConfiguracaoPartida(10, 10, 20).comSemente(1L));
        Tabuleiro segundo = new Tabuleiro(new ConfiguracaoPartida(10, 10, 20).comSemente(2L));

        assertFalse(mesmasPosicoes(primeiro.getPosicoesMinas(), segundo.getPosicoesMinas()));
        assertNull(new ConfiguracaoPartida(10, 10, 20).getSemente());
    }

    @Test
    void testPosicoesMinasEmOrdemDeLinha() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][] { { 2, 1 }, { 0, 2 }, { 1, 0 } });
        int[][] posicoes = tabuleiro.getPosicoesMinas();

        assertEquals(3, posicoes.length);
        assertArrayEquals(new int[] { 0, 2 }, posicoes[0]);
        assertArrayEquals(new int[] { 1, 0 }, posicoes[1]);
        assertArrayEquals(new int[] { 2, 1 }, posicoes[2]);
        assertEquals(3, tabuleiro.getNumMinas());
    }

    @Test
    void testReplayComConfiguracaoEPosicoesReproduzTabuleiro() {
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(8, 8, 10).comSemente(99L).comToroidal(true);
        Tabuleiro original = new Tabuleiro(configuracao);
        Tabuleiro replay = new Tabuleiro(configuracao, original.getPosicoesMinas());

        assertEquals(10, replay.getNumMinas());
        assertTrue(replay.getConfiguracao().isToroidal());
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                assertEquals(original.isMinada(i, j), replay.isMinada(i, j));
                assertEquals(original.getMinasVizinhas(i, j), replay.getMinasVizinhas(i, j));
            }
        }
    }

    @Test
    void testRelampagoNuncaRevelaMinasNemVence() {
        int[][] minas = { { 0, 0 }, { 2, 2 }, { 4, 4 } };
        Tabuleiro tabuleiro = new Tabuleiro(5, 5, minas);
        Random sorteio = new Random(7L);

        List<int[]> primeiras = tabuleiro.revelarAleatorias(5, sorteio);
        assertEquals(5, primeiras.size());
        assertEquals(5, tabuleiro.contarReveladasSeguras());
        for (int[] posicao : primeiras) {
            assertFalse(tabuleiro.isMinada(posicao[0], posicao[1]));
            assertTrue(tabuleiro.isRevelada(posicao[0], posicao[1]));
        }

        List<int[]> restantes = tabuleiro.revelarAleatorias(1000, sorteio);
        assertEquals(tabuleiro.getTotalSeguras() - 1 - 5, restantes.size());
        assertEquals(tabuleiro.getTotalSeguras() - 1, tabuleiro.contarReveladasSeguras());
        assertFalse(tabuleiro.isJogoEncerrado());
        assertFalse(tabuleiro.isVitoria());
        assertTrue(tabuleiro.revelarAleatorias(3, sorteio).isEmpty());
        for (int[] mina : minas) {
            assertFalse(tabuleiro.isRevelada(mina[0], mina[1]));
        }
    }

    @Test
    void testRelampagoNaoFazCascataNemRevelaBandeiras() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, SEM_MINAS);
        tabuleiro.alternarMarcacao(1, 1);

        List<int[]> reveladas = tabuleiro.revelarAleatorias(1, new Random(3L));
        assertEquals(1, reveladas.size());
        assertEquals(1, tabuleiro.contarReveladasSeguras());

        tabuleiro.revelarAleatorias(100, new Random(3L));
        assertFalse(tabuleiro.isRevelada(1, 1));
        assertEquals(8, tabuleiro.contarReveladasSeguras());
        assertFalse(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.revelarAleatorias(0, new Random()).isEmpty());
    }

    @Test
    void testEncerrarPorTempo() {
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, new int[][] { { 0, 0 } });
        tabuleiro.revelar(1, 1);
        assertFalse(tabuleiro.isJogoEncerrado());

        tabuleiro.encerrarPorTempo();

        assertTrue(tabuleiro.isJogoEncerrado());
        assertTrue(tabuleiro.isDerrota());
        assertFalse(tabuleiro.isVitoria());
        assertTrue(tabuleiro.revelar(0, 1).isEmpty());
        tabuleiro.alternarMarcacao(0, 0);
        assertFalse(tabuleiro.isMarcada(0, 0));

        Tabuleiro vencido = new Tabuleiro(2, 2, new int[][] { { 0, 0 } });
        vencido.revelar(0, 1);
        vencido.revelar(1, 0);
        vencido.revelar(1, 1);
        vencido.encerrarPorTempo();
        assertTrue(vencido.isVitoria());
        assertFalse(vencido.isDerrota());
    }

    @Test
    void testConstrutoresValidamParametros() {
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(0, 3, 1));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(3, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(3, 3, 9));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(3, 3, -1));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(new ConfiguracaoPartida(0, 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(new ConfiguracaoPartida(2, 2, 4)));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(0, 3, SEM_MINAS));
        assertThrows(IllegalArgumentException.class,
                () -> new Tabuleiro(2, 2, new int[][] { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 } }));
        assertThrows(IllegalArgumentException.class, () -> new Tabuleiro(2, 2, new int[][] { { 5, 5 } }));
        assertThrows(IllegalArgumentException.class,
                () -> new Tabuleiro(new ConfiguracaoPartida(2, 2, 1), (int[][]) null));
        assertNotNull(new Tabuleiro(3, 3, 8));
        assertNotNull(new Tabuleiro(3, 3, 0));
    }

    @Test
    void testContadoresDeMarcadasReveladasESeguras() {
        int[][] minas = { { 0, 0 }, { 2, 2 } };
        Tabuleiro tabuleiro = new Tabuleiro(3, 3, minas);

        assertEquals(2, tabuleiro.getNumMinas());
        assertEquals(7, tabuleiro.getTotalSeguras());
        assertEquals(0, tabuleiro.contarMarcadas());
        assertEquals(0, tabuleiro.contarReveladasSeguras());

        tabuleiro.alternarMarcacao(0, 0);
        tabuleiro.alternarMarcacao(1, 1);
        assertEquals(2, tabuleiro.contarMarcadas());

        tabuleiro.revelar(0, 1);
        assertEquals(1, tabuleiro.contarReveladasSeguras());
        tabuleiro.alternarMarcacao(0, 1);
        assertEquals(2, tabuleiro.contarMarcadas());

        tabuleiro.revelar(1, 0);
        assertEquals(2, tabuleiro.contarReveladasSeguras());
    }

    @Test
    void testConfiguracaoPadraoECopia() {
        ConfiguracaoPartida padrao = new ConfiguracaoPartida(9, 9, 10);
        assertEquals(9, padrao.getLinhas());
        assertEquals(9, padrao.getColunas());
        assertEquals(10, padrao.getMinas());
        assertFalse(padrao.isSemCascata());
        assertFalse(padrao.isToroidal());
        assertFalse(padrao.isPermitirInterrogacao());
        assertEquals(1, padrao.getVidas());
        assertNull(padrao.getSemente());

        ConfiguracaoPartida completa = new ConfiguracaoPartida(9, 9, 10).comSemCascata(true).comToroidal(true)
                .comVidas(3).comInterrogacao(true).comSemente(5L);
        ConfiguracaoPartida copia = completa.copiarComMinas(4);
        assertNotEquals(completa, copia);
        assertEquals(4, copia.getMinas());
        assertEquals(9, copia.getLinhas());
        assertTrue(copia.isSemCascata());
        assertTrue(copia.isToroidal());
        assertTrue(copia.isPermitirInterrogacao());
        assertEquals(3, copia.getVidas());
        assertEquals(Long.valueOf(5L), copia.getSemente());

        Tabuleiro tabuleiro = new Tabuleiro(3, 3, SEM_MINAS);
        assertNotNull(tabuleiro.getConfiguracao());
        assertEquals(1, tabuleiro.getVidasRestantes());
    }

    @Test
    void testRepresentacaoTextualDaCelula() {
        Celula celula = new Celula();
        assertEquals(".", celula.toString());
        celula.revelar();
        assertEquals(" ", celula.toString());
        celula.setMinasVizinhas(3);
        assertEquals("3", celula.toString());

        Celula minada = new Celula();
        minada.setMinada(true);
        minada.revelar();
        minada.marcarExplodida();
        assertEquals("*", minada.toString());
        assertTrue(minada.isExplodida());
    }
}
