package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import persistencia.Configuracoes;
import persistencia.Conquista;
import persistencia.ExportadorCsv;
import persistencia.GerenciadorConquistas;
import persistencia.GerenciadorPerfis;
import persistencia.HistoricoPartidas;
import persistencia.PerfilJogador;
import persistencia.RankingTempos;
import persistencia.Recordes;
import persistencia.RegistroPartida;

public class PersistenciaTest {

    private Path diretorioTemporario;

    @BeforeEach
    public void prepararDiretorio() throws IOException {
        diretorioTemporario = Files.createTempDirectory("campominado-teste");
    }

    private static RegistroPartida partida(String dificuldade, boolean vitoria, long segundos, int jogadas) {
        return new RegistroPartida(LocalDateTime.of(2026, 3, 10, 14, 30, 0), dificuldade, vitoria, segundos, jogadas,
                3, 0, 0, 0, "");
    }

    private static RegistroPartida partidaComModos(boolean vitoria, int bandeiras, String modos) {
        return new RegistroPartida(LocalDateTime.of(2026, 3, 10, 14, 30, 0), "Iniciante", vitoria, 45, 20, bandeiras,
                1, 0, 0, modos);
    }

    @Test
    public void mergeSortOrdenaPorTempoComDesempatePorJogadas() {
        RegistroPartida lenta = partida("Iniciante", true, 90, 10);
        RegistroPartida rapida = partida("Iniciante", true, 15, 50);
        RegistroPartida empateMaisJogadas = partida("Iniciante", true, 40, 30);
        RegistroPartida empateMenosJogadas = partida("Iniciante", true, 40, 12);
        List<RegistroPartida> registros = new ArrayList<>();
        registros.add(lenta);
        registros.add(empateMaisJogadas);
        registros.add(rapida);
        registros.add(empateMenosJogadas);
        RankingTempos.ordenarPorTempo(registros);
        assertEquals(4, registros.size());
        assertTrue(registros.get(0) == rapida);
        assertTrue(registros.get(1) == empateMenosJogadas);
        assertTrue(registros.get(2) == empateMaisJogadas);
        assertTrue(registros.get(3) == lenta);
    }

    @Test
    public void mergeSortEhEstavelParaEmpatesCompletos() {
        List<RegistroPartida> registros = new ArrayList<>();
        List<RegistroPartida> iguais = new ArrayList<>();
        for (int indice = 0; indice < 6; indice++) {
            RegistroPartida igual = partida("Iniciante", true, 50, 20);
            iguais.add(igual);
            registros.add(partida("Iniciante", true, 100 - indice, 20));
            registros.add(igual);
        }
        RankingTempos.ordenarPorTempo(registros);
        for (int indice = 0; indice < iguais.size(); indice++) {
            assertTrue(registros.get(indice) == iguais.get(indice), "ordem relativa preservada");
        }
        for (int indice = 1; indice < registros.size(); indice++) {
            assertTrue(registros.get(indice - 1).getSegundos() <= registros.get(indice).getSegundos());
        }
    }

    @Test
    public void mergeSortAceitaListasVaziasEUnitarias() {
        List<RegistroPartida> vazia = new ArrayList<>();
        RankingTempos.ordenarPorTempo(vazia);
        assertTrue(vazia.isEmpty());
        List<RegistroPartida> unitaria = new ArrayList<>();
        unitaria.add(partida("Iniciante", true, 10, 5));
        RankingTempos.ordenarPorTempo(unitaria);
        assertEquals(1, unitaria.size());
    }

    @Test
    public void topTemposFiltraVitoriasDaDificuldadeELimita() {
        List<RegistroPartida> registros = new ArrayList<>();
        registros.add(partida("Iniciante", true, 60, 10));
        registros.add(partida("Iniciante", false, 5, 3));
        registros.add(partida("Avançado", true, 1, 1));
        registros.add(partida("iniciante", true, 20, 10));
        registros.add(partida("Iniciante", true, 40, 10));
        List<RegistroPartida> top = RankingTempos.topTempos(registros, "INICIANTE", 2);
        assertEquals(2, top.size());
        assertEquals(20L, top.get(0).getSegundos());
        assertEquals(40L, top.get(1).getSegundos());
        assertEquals(5, registros.size());
        assertEquals(60L, registros.get(0).getSegundos());
        assertTrue(RankingTempos.topTempos(registros, "Intermediário", 5).isEmpty());
    }

    @Test
    public void recordesRegistramApenasNovoMelhorTempoEPersistem() {
        Path arquivo = diretorioTemporario.resolve("sub").resolve("recordes.properties");
        Recordes recordes = new Recordes(arquivo);
        assertNull(recordes.getMelhorTempo("Intermediário"));
        assertTrue(recordes.registrar("Intermediário", 120));
        assertFalse(recordes.registrar("Intermediário", 150));
        assertFalse(recordes.registrar("Intermediário", 120));
        assertTrue(recordes.registrar("Intermediário", 95));
        assertTrue(recordes.registrar("Iniciante", 30));
        assertEquals(Long.valueOf(95), recordes.getMelhorTempo("Intermediário"));
        Recordes recarregados = new Recordes(arquivo);
        assertEquals(Long.valueOf(95), recarregados.getMelhorTempo("Intermediário"));
        assertEquals(Long.valueOf(30), recarregados.getMelhorTempo("Iniciante"));
        assertEquals(2, recarregados.todos().size());
        recarregados.todos().clear();
        assertEquals(2, recarregados.todos().size());
    }

    @Test
    public void historicoCalculaEstatisticasERecarregaDoDisco() {
        Path arquivo = diretorioTemporario.resolve("historico.csv");
        HistoricoPartidas historico = new HistoricoPartidas(arquivo);
        assertEquals(0, historico.getTotalPartidas());
        assertEquals(0.0, historico.getTaxaVitoria(), 0.0001);
        boolean[] resultados = { true, true, true, false, true, false, true, true };
        for (boolean resultado : resultados) {
            historico.adicionar(partida("Iniciante", resultado, 50, 20));
        }
        assertEquals(8, historico.getTotalPartidas());
        assertEquals(6, historico.getVitorias());
        assertEquals(2, historico.getDerrotas());
        assertEquals(75.0, historico.getTaxaVitoria(), 0.0001);
        assertEquals(75.0, historico.getTaxaVitoriaUltimas(4), 0.0001);
        assertEquals(75.0, historico.getTaxaVitoriaUltimas(100), 0.0001);
        assertEquals(2, historico.getSequenciaAtualVitorias());
        assertEquals(3, historico.getMelhorSequenciaVitorias());
        assertThrows(UnsupportedOperationException.class, () -> historico.getRegistros().add(null));

        HistoricoPartidas recarregado = new HistoricoPartidas(arquivo);
        assertEquals(8, recarregado.getTotalPartidas());
        assertEquals(6, recarregado.getVitorias());
        assertEquals(2, recarregado.getSequenciaAtualVitorias());
        assertEquals(3, recarregado.getMelhorSequenciaVitorias());
    }

    @Test
    public void historicoIgnoraCabecalhoELinhasInvalidas() throws IOException {
        Path arquivo = diretorioTemporario.resolve("historico.csv");
        List<String> linhas = new ArrayList<>();
        linhas.add(RegistroPartida.cabecalhoCsv());
        linhas.add(partida("Iniciante", true, 12, 8).paraLinhaCsv());
        linhas.add("lixo;sem;sentido");
        linhas.add("");
        linhas.add(partida("Avançado", false, 99, 40).paraLinhaCsv());
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        HistoricoPartidas historico = new HistoricoPartidas(arquivo);
        assertEquals(2, historico.getTotalPartidas());
        assertEquals("Avançado", historico.getRegistros().get(1).getDificuldade());
    }

    @Test
    public void csvIdaEVoltaPreservaCampos() {
        RegistroPartida original = new RegistroPartida(LocalDateTime.of(2026, 5, 1, 9, 5, 7), "Avançado", true, 125,
                88, 40, 2, 11, 1, "diario,toroidal");
        RegistroPartida copia = RegistroPartida.deLinhaCsv(original.paraLinhaCsv());
        assertNotNull(copia);
        assertEquals(original.getDataHora(), copia.getDataHora());
        assertEquals("Avançado", copia.getDificuldade());
        assertTrue(copia.isVitoria());
        assertEquals(125L, copia.getSegundos());
        assertEquals(88, copia.getJogadas());
        assertEquals(40, copia.getBandeirasUsadas());
        assertEquals(2, copia.getDicasUsadas());
        assertEquals(11, copia.getChordsUsados());
        assertEquals(1, copia.getVidasPerdidas());
        assertEquals("diario,toroidal", copia.getModos());
        assertTrue(copia.temModo("toroidal"));
        assertTrue(copia.temModo("diario"));
        assertFalse(copia.temModo("vidas"));
        assertEquals("02:05", copia.getTempoFormatado());
        assertEquals(original.paraLinhaCsv(), copia.paraLinhaCsv());
    }

    @Test
    public void csvTrataNulosSeparadoresELinhasMalformadas() {
        RegistroPartida semModos = new RegistroPartida(LocalDateTime.of(2026, 1, 1, 0, 0), "Ini;ciante\n", false, 7,
                3, 0, 0, 0, 0, null);
        assertEquals("", semModos.getModos());
        assertFalse(semModos.getDificuldade().contains(";"));
        assertFalse(semModos.getDificuldade().contains("\n"));
        assertTrue(semModos.paraLinhaCsv().contains("DERROTA"));
        RegistroPartida copia = RegistroPartida.deLinhaCsv(semModos.paraLinhaCsv());
        assertNotNull(copia);
        assertFalse(copia.isVitoria());
        assertEquals("", copia.getModos());
        assertEquals("00:07", copia.getTempoFormatado());
        assertNull(RegistroPartida.deLinhaCsv(null));
        assertNull(RegistroPartida.deLinhaCsv(""));
        assertNull(RegistroPartida.deLinhaCsv(RegistroPartida.cabecalhoCsv()));
        assertNull(RegistroPartida.deLinhaCsv("2026-01-01T00:00:00;Iniciante;TALVEZ;1;1;1;1;1;1;"));
        assertNull(RegistroPartida.deLinhaCsv("2026-01-01T00:00:00;Iniciante;VITORIA;abc;1;1;1;1;1;"));
        assertEquals("data_hora;dificuldade;resultado;segundos;jogadas;bandeiras;dicas;chords;vidas_perdidas;modos",
                RegistroPartida.cabecalhoCsv());
    }

    @Test
    public void exportadorGravaBomCabecalhoELinhas() throws IOException {
        Path destino = diretorioTemporario.resolve("exportacao").resolve("partidas.csv");
        List<RegistroPartida> registros = new ArrayList<>();
        registros.add(partida("Intermediário", true, 70, 30));
        registros.add(partida("Avançado", false, 200, 90));
        ExportadorCsv.exportar(registros, destino);
        byte[] bytes = Files.readAllBytes(destino);
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);
        String conteudo = new String(bytes, StandardCharsets.UTF_8).substring(1);
        String[] linhas = conteudo.split("\r\n");
        assertEquals(3, linhas.length);
        assertEquals(RegistroPartida.cabecalhoCsv(), linhas[0]);
        assertEquals("Intermediário", RegistroPartida.deLinhaCsv(linhas[1]).getDificuldade());
    }

    @Test
    public void conquistasPrimeiraVitoriaEMaosLimpasNaoRepetem() {
        Path arquivo = diretorioTemporario.resolve("conquistas.txt");
        HistoricoPartidas historico = new HistoricoPartidas(diretorioTemporario.resolve("historico.csv"));
        GerenciadorConquistas conquistas = new GerenciadorConquistas(arquivo);

        RegistroPartida derrota = partidaComModos(false, 0, "");
        historico.adicionar(derrota);
        assertTrue(conquistas.avaliar(derrota, historico).isEmpty());

        RegistroPartida vitoriaLimpa = partidaComModos(true, 0, "");
        historico.adicionar(vitoriaLimpa);
        List<Conquista> novas = conquistas.avaliar(vitoriaLimpa, historico);
        assertTrue(novas.contains(Conquista.PRIMEIRA_VITORIA));
        assertTrue(novas.contains(Conquista.MAOS_LIMPAS));
        assertFalse(novas.contains(Conquista.VELOCISTA));
        assertEquals(Conquista.PRIMEIRA_VITORIA, novas.get(0));
        assertTrue(conquistas.isDesbloqueada(Conquista.MAOS_LIMPAS));

        RegistroPartida outraVitoria = partidaComModos(true, 0, "");
        historico.adicionar(outraVitoria);
        assertTrue(conquistas.avaliar(outraVitoria, historico).isEmpty());

        GerenciadorConquistas recarregado = new GerenciadorConquistas(arquivo);
        Set<Conquista> desbloqueadas = recarregado.getDesbloqueadas();
        assertEquals(2, desbloqueadas.size());
        assertTrue(desbloqueadas.contains(Conquista.PRIMEIRA_VITORIA));
        assertThrows(UnsupportedOperationException.class, () -> desbloqueadas.add(Conquista.VETERANO));
    }

    @Test
    public void vitoriaEmTreinoNaoDesbloqueiaConquistas() {
        HistoricoPartidas historico = new HistoricoPartidas(diretorioTemporario.resolve("historico.csv"));
        GerenciadorConquistas conquistas = new GerenciadorConquistas(diretorioTemporario.resolve("conquistas.txt"));
        RegistroPartida treino = new RegistroPartida(LocalDateTime.now(), "Avançado", true, 10, 5, 0, 0, 20, 1,
                "treino,diario,toroidal,vidas");
        historico.adicionar(treino);
        assertTrue(conquistas.avaliar(treino, historico).isEmpty());
        assertTrue(conquistas.getDesbloqueadas().isEmpty());
    }

    @Test
    public void conquistasDeModosEDificuldades() {
        HistoricoPartidas historico = new HistoricoPartidas(diretorioTemporario.resolve("historico.csv"));
        GerenciadorConquistas conquistas = new GerenciadorConquistas(diretorioTemporario.resolve("conquistas.txt"));
        RegistroPartida especial = new RegistroPartida(LocalDateTime.now(), "Avançado", true, 25, 60, 5, 0, 10, 1,
                "diario,toroidal,vidas");
        historico.adicionar(especial);
        List<Conquista> novas = conquistas.avaliar(especial, historico);
        assertTrue(novas.contains(Conquista.VELOCISTA));
        assertTrue(novas.contains(Conquista.MESTRE_AVANCADO));
        assertTrue(novas.contains(Conquista.SEM_AJUDA));
        assertTrue(novas.contains(Conquista.SOBREVIVENTE));
        assertTrue(novas.contains(Conquista.DESAFIO_DIARIO));
        assertTrue(novas.contains(Conquista.MAESTRO));
        assertTrue(novas.contains(Conquista.EXPLORADOR));
        assertFalse(novas.contains(Conquista.MAOS_LIMPAS));
        assertFalse(novas.contains(Conquista.EM_CHAMAS));
        for (int indice = 0; indice < 2; indice++) {
            RegistroPartida seguida = partidaComModos(true, 2, "");
            historico.adicionar(seguida);
            conquistas.avaliar(seguida, historico);
        }
        assertTrue(conquistas.isDesbloqueada(Conquista.EM_CHAMAS));
        assertEquals("🔥", Conquista.EM_CHAMAS.getEmoji());
        assertEquals("Explorador", Conquista.EXPLORADOR.getTitulo());
    }

    @Test
    public void gerenciadorPerfisSanitizaCriaEListaOrdenado() throws IOException {
        assertEquals("jose_da_conceicao", GerenciadorPerfis.sanitizar("José da Conceição"));
        assertEquals("ana_2", GerenciadorPerfis.sanitizar("ANA-2"));
        GerenciadorPerfis gerenciador = new GerenciadorPerfis(diretorioTemporario);
        List<String> iniciais = gerenciador.listarPerfis();
        assertEquals(1, iniciais.size());
        assertEquals("Jogador", iniciais.get(0));
        assertTrue(gerenciador.criarPerfil("Zé Ninguém"));
        assertTrue(gerenciador.criarPerfil("ana"));
        assertFalse(gerenciador.criarPerfil("Ana"));
        assertFalse(gerenciador.criarPerfil("   "));
        assertFalse(gerenciador.criarPerfil(null));
        assertFalse(gerenciador.criarPerfil("Um nome grande demais para caber"));
        assertFalse(gerenciador.criarPerfil("Jogador"));

        GerenciadorPerfis outro = new GerenciadorPerfis(diretorioTemporario);
        List<String> perfis = outro.listarPerfis();
        assertEquals(3, perfis.size());
        assertEquals("ana", perfis.get(0));
        assertEquals("Jogador", perfis.get(1));
        assertEquals("Zé Ninguém", perfis.get(2));
        assertTrue(Files.isRegularFile(diretorioTemporario.resolve("perfis").resolve("ze_ninguem").resolve("nome.txt")));

        PerfilJogador perfil = outro.carregar("Zé Ninguém");
        assertEquals("Zé Ninguém", perfil.getNome());
        assertNotNull(perfil.getHistorico());
        assertNotNull(perfil.getRecordes());
        assertNotNull(perfil.getConquistas());
        perfil.getHistorico().adicionar(partida("Iniciante", true, 10, 5));
        assertTrue(Files.isRegularFile(perfil.getDiretorio().resolve("historico.csv")));

        PerfilJogador novo = outro.carregar("Visitante");
        assertTrue(Files.isDirectory(novo.getDiretorio()));
        assertEquals(4, outro.listarPerfis().size());
    }

    @Test
    public void configuracoesPadroesLimitesESalvamento() {
        Path arquivo = diretorioTemporario.resolve("config").resolve("config.properties");
        Configuracoes configuracoes = new Configuracoes(arquivo);
        assertEquals("Automático (sistema)", configuracoes.getTema());
        assertEquals("Bandeira", configuracoes.getSkinBandeira());
        assertEquals("Números", configuracoes.getSkinCelula());
        assertTrue(configuracoes.isSomAtivo());
        assertFalse(configuracoes.isMusicaAtiva());
        assertEquals(40, configuracoes.getVolumeMusica());
        assertEquals(1.0, configuracoes.getZoom(), 0.0001);
        assertEquals(1.0, configuracoes.getEscalaFonte(), 0.0001);
        assertTrue(configuracoes.isInterrogacaoAtiva());
        assertFalse(configuracoes.isModoDaltonico());
        assertEquals("Jogador", configuracoes.getPerfilAtual());
        assertEquals(15, configuracoes.getDensidadeMinasPadrao());
        assertEquals(32, configuracoes.getTecla("dica", 32));

        configuracoes.setVolumeMusica(150);
        assertEquals(100, configuracoes.getVolumeMusica());
        configuracoes.setVolumeMusica(-5);
        assertEquals(0, configuracoes.getVolumeMusica());
        configuracoes.setZoom(5.0);
        assertEquals(2.0, configuracoes.getZoom(), 0.0001);
        configuracoes.setZoom(0.1);
        assertEquals(0.6, configuracoes.getZoom(), 0.0001);
        configuracoes.setEscalaFonte(3.0);
        assertEquals(1.6, configuracoes.getEscalaFonte(), 0.0001);
        configuracoes.setEscalaFonte(0.2);
        assertEquals(0.8, configuracoes.getEscalaFonte(), 0.0001);
        configuracoes.setDensidadeMinasPadrao(90);
        assertEquals(40, configuracoes.getDensidadeMinasPadrao());
        configuracoes.setDensidadeMinasPadrao(1);
        assertEquals(5, configuracoes.getDensidadeMinasPadrao());

        configuracoes.setTema("Escuro");
        configuracoes.setSkinCelula("Pontos");
        configuracoes.setSomAtivo(false);
        configuracoes.setModoDaltonico(true);
        configuracoes.setPerfilAtual("Zé");
        configuracoes.setZoom(1.25);
        configuracoes.setTecla("dica", 72);
        configuracoes.definir("extra.numero", 7);
        assertFalse(Files.exists(arquivo));
        configuracoes.salvar();
        assertTrue(Files.exists(arquivo));

        Configuracoes recarregadas = new Configuracoes(arquivo);
        assertEquals("Escuro", recarregadas.getTema());
        assertEquals("Pontos", recarregadas.getSkinCelula());
        assertFalse(recarregadas.isSomAtivo());
        assertTrue(recarregadas.isModoDaltonico());
        assertEquals("Zé", recarregadas.getPerfilAtual());
        assertEquals(1.25, recarregadas.getZoom(), 0.0001);
        assertEquals(72, recarregadas.getTecla("dica", 32));
        assertEquals(7, recarregadas.getInteiro("extra.numero", 0));
        assertEquals("padrao", recarregadas.getTexto("inexistente", "padrao"));
        assertNotEquals("Automático (sistema)", recarregadas.getTema());
    }

    @Test
    public void configuracoesIgnoramValoresInvalidos() {
        Configuracoes configuracoes = new Configuracoes(diretorioTemporario.resolve("config.properties"));
        configuracoes.definir("numero", "abc");
        configuracoes.definir("booleano", "talvez");
        configuracoes.definir("decimal", "x");
        assertEquals(9, configuracoes.getInteiro("numero", 9));
        assertTrue(configuracoes.getBooleano("booleano", true));
        assertEquals(2.5, configuracoes.getDecimal("decimal", 2.5), 0.0001);
        configuracoes.definir("zoom", "9.0");
        assertEquals(2.0, configuracoes.getZoom(), 0.0001);
        assertNotNull(Configuracoes.diretorioRaiz());
    }
}
