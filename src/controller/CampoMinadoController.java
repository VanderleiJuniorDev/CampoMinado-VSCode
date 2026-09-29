package controller;

import audio.EfeitosSonoros;
import audio.MusicaAmbiente;
import model.ConfiguracaoPartida;
import model.LeituraTabuleiro;
import model.Tabuleiro;
import persistencia.Configuracoes;
import persistencia.Conquista;
import persistencia.ExportadorCsv;
import persistencia.GerenciadorPerfis;
import persistencia.HistoricoPartidas;
import persistencia.PerfilJogador;
import persistencia.RankingTempos;
import persistencia.RegistroPartida;
import view.CampoMinadoView;
import view.TipoMensagem;

import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CampoMinadoController implements AcoesJogador {

    public static final int LIMITE_DICAS = 3;
    public static final String DIFICULDADE_DIARIO = "Diário";
    public static final String DIFICULDADE_PERSONALIZADA = "Personalizado";

    private static final String[] DIFICULDADES_RANKING = {"Iniciante", "Intermediário", "Avançado", DIFICULDADE_DIARIO};
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CampoMinadoView view;
    private final Configuracoes configuracoes;
    private final GerenciadorPerfis gerenciadorPerfis;
    private final EfeitosSonoros efeitos;
    private final MusicaAmbiente musica;
    private final Random sorteio = new Random();

    private PerfilJogador perfil;
    private DadosConfiguracao dadosConfiguracao;

    private Tabuleiro tabuleiro;
    private Tabuleiro tabuleiroReplay;
    private ConfiguracaoPartida configuracaoPartida;
    private OpcoesPartida opcoes = new OpcoesPartida();
    private String nomeDificuldade;
    private boolean desafioDiario;
    private int[][] posicoesMinas;

    private int jogadas;
    private int dicasUsadas;
    private int chordsUsados;
    private int bandeirasUsadas;
    private boolean jogoIniciado;
    private boolean partidaFinalizada;
    private long tempoInicio;
    private long segundosFinais;
    private String mensagemFinal;
    private int limiteSegundos;
    private Timer timerJogo;
    private int geracaoPartida;

    private final List<Jogada> jogadasRegistradas = new ArrayList<>();
    private long sementeRelampago;
    private int quantidadeRelampago;

    private int jogadorDaVez = 1;
    private final int[] reveladasPorJogador = new int[3];

    private int etapaDuelo;
    private boolean dueloVitoriaPrimeiro;
    private long dueloSegundosPrimeiro;
    private int dueloReveladasPrimeiro;
    private int dueloJogadasPrimeiro;

    private boolean emReplay;
    private Timer timerReplay;
    private int indiceReplay;

    public CampoMinadoController(CampoMinadoView view) {
        this(view, Configuracoes.carregarPadrao(), GerenciadorPerfis.padrao());
    }

    public CampoMinadoController(CampoMinadoView view, Configuracoes configuracoes, GerenciadorPerfis gerenciadorPerfis) {
        this.view = view;
        this.configuracoes = configuracoes;
        this.gerenciadorPerfis = gerenciadorPerfis;
        this.efeitos = new EfeitosSonoros();
        this.musica = new MusicaAmbiente();
        this.view.setOuvinte(this);
    }

    public void iniciar() {
        perfil = gerenciadorPerfis.carregar(configuracoes.getPerfilAtual());
        dadosConfiguracao = lerConfiguracoes();
        aplicarAudio();
        view.aplicarConfiguracao(dadosConfiguracao);
        view.atualizarPerfis(gerenciadorPerfis.listarPerfis(), perfil.getNome());
        view.mostrarTelaInicial();
        view.setVisible(true);
    }

    private DadosConfiguracao lerConfiguracoes() {
        DadosConfiguracao dados = new DadosConfiguracao();
        dados.setTema(configuracoes.getTema());
        dados.setSkinBandeira(configuracoes.getSkinBandeira());
        dados.setSkinCelula(configuracoes.getSkinCelula());
        dados.setSomAtivo(configuracoes.isSomAtivo());
        dados.setMusicaAtiva(configuracoes.isMusicaAtiva());
        dados.setVolumeMusica(configuracoes.getVolumeMusica());
        dados.setZoom(configuracoes.getZoom());
        dados.setEscalaFonte(configuracoes.getEscalaFonte());
        dados.setInterrogacaoAtiva(configuracoes.isInterrogacaoAtiva());
        dados.setModoDaltonico(configuracoes.isModoDaltonico());
        dados.setDensidadeMinas(configuracoes.getDensidadeMinasPadrao());
        for (Map.Entry<String, Integer> tecla : DadosConfiguracao.teclasPadrao().entrySet()) {
            dados.setTecla(tecla.getKey(), configuracoes.getTecla(tecla.getKey(), tecla.getValue()));
        }
        return dados;
    }

    private void gravarConfiguracoes(DadosConfiguracao dados) {
        configuracoes.setTema(dados.getTema());
        configuracoes.setSkinBandeira(dados.getSkinBandeira());
        configuracoes.setSkinCelula(dados.getSkinCelula());
        configuracoes.setSomAtivo(dados.isSomAtivo());
        configuracoes.setMusicaAtiva(dados.isMusicaAtiva());
        configuracoes.setVolumeMusica(dados.getVolumeMusica());
        configuracoes.setZoom(dados.getZoom());
        configuracoes.setEscalaFonte(dados.getEscalaFonte());
        configuracoes.setInterrogacaoAtiva(dados.isInterrogacaoAtiva());
        configuracoes.setModoDaltonico(dados.isModoDaltonico());
        configuracoes.setDensidadeMinasPadrao(dados.getDensidadeMinas());
        for (Map.Entry<String, Integer> tecla : dados.getTeclas().entrySet()) {
            configuracoes.setTecla(tecla.getKey(), tecla.getValue());
        }
        configuracoes.setPerfilAtual(perfil.getNome());
        configuracoes.salvar();
    }

    private void aplicarAudio() {
        efeitos.setAtivo(dadosConfiguracao.isSomAtivo());
        musica.setVolume(dadosConfiguracao.getVolumeMusica());
        if (dadosConfiguracao.isMusicaAtiva()) {
            musica.iniciar();
        } else {
            musica.parar();
        }
    }

    @Override
    public void aoEscolherDificuldade(String nome, int linhas, int colunas, int minas) {
        OpcoesPartida escolhidas = view.getOpcoesPartida();
        Long semente = escolhidas.getModoJogadores() == ModoJogadores.DUELO ? sorteio.nextLong() : null;
        ConfiguracaoPartida configuracao;
        Tabuleiro novo;
        try {
            configuracao = new ConfiguracaoPartida(linhas, colunas, minas)
                    .comSemCascata(escolhidas.isSemCascata())
                    .comToroidal(escolhidas.isToroidal())
                    .comVidas(escolhidas.isTresVidas() ? 3 : 1)
                    .comInterrogacao(dadosConfiguracao.isInterrogacaoAtiva())
                    .comSemente(semente);
            novo = new Tabuleiro(configuracao);
        } catch (IllegalArgumentException erro) {
            view.mostrarErro(erro.getMessage());
            return;
        }
        opcoes = escolhidas;
        desafioDiario = false;
        nomeDificuldade = nome;
        etapaDuelo = opcoes.getModoJogadores() == ModoJogadores.DUELO ? 1 : 0;
        sementeRelampago = sorteio.nextLong();
        comecarPartida(novo);
    }

    @Override
    public void aoPedirDesafioDiario() {
        long semente = LocalDate.now().toEpochDay() * 7919L + 2026L;
        ConfiguracaoPartida configuracao = new ConfiguracaoPartida(16, 16, 40)
                .comInterrogacao(dadosConfiguracao.isInterrogacaoAtiva())
                .comSemente(semente);
        opcoes = new OpcoesPartida();
        desafioDiario = true;
        nomeDificuldade = DIFICULDADE_DIARIO;
        etapaDuelo = 0;
        sementeRelampago = semente;
        comecarPartida(new Tabuleiro(configuracao));
    }

    private void comecarPartida(Tabuleiro novoTabuleiro) {
        pararTimer();
        pararTimerReplay();
        emReplay = false;
        geracaoPartida++;

        tabuleiro = novoTabuleiro;
        configuracaoPartida = novoTabuleiro.getConfiguracao();
        posicoesMinas = novoTabuleiro.getPosicoesMinas();
        jogadas = 0;
        dicasUsadas = 0;
        chordsUsados = 0;
        bandeirasUsadas = 0;
        jogoIniciado = false;
        partidaFinalizada = false;
        segundosFinais = 0;
        mensagemFinal = null;
        jogadasRegistradas.clear();
        jogadorDaVez = 1;
        reveladasPorJogador[1] = 0;
        reveladasPorJogador[2] = 0;
        limiteSegundos = opcoes.getTempoLimiteSegundos();

        view.iniciarTelaDeJogo(tabuleiro, tituloPartida(), opcoes.isMinasVisiveis());

        quantidadeRelampago = 0;
        if (opcoes.isRelampago()) {
            quantidadeRelampago = Math.max(3, tabuleiro.getTotalSeguras() / 12);
            tabuleiro.revelarAleatorias(quantidadeRelampago, new Random(sementeRelampago));
            view.redesenharTabuleiro();
        }

        view.atualizarTempo(limiteSegundos > 0 ? "-" + formatarTempo(limiteSegundos) : "00:00", false);
        atualizarRecordeNaView();
        atualizarPainel();
        view.mostrarStatus(mensagemInicial(), TipoMensagem.NEUTRA);
    }

    private String tituloPartida() {
        String titulo = nomeRegistro();
        if (etapaDuelo > 0) {
            titulo += " · Duelo";
        } else if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            titulo += " · Cooperativo";
        }
        return titulo;
    }

    private String mensagemInicial() {
        if (etapaDuelo == 1) {
            return "Duelo: vez do Jogador 1";
        }
        if (etapaDuelo == 2) {
            return "Duelo: vez do Jogador 2 (mesmo tabuleiro)";
        }
        if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            return "Cooperativo: vez do Jogador 1";
        }
        if (desafioDiario) {
            return "Desafio de " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM")) + ": boa sorte!";
        }
        if (opcoes.isRelampago()) {
            return "Relâmpago: " + quantidadeRelampago + " células já reveladas";
        }
        return "Boa sorte!";
    }

    private boolean podeJogar() {
        return tabuleiro != null && !emReplay && !partidaFinalizada && !tabuleiro.isJogoEncerrado();
    }

    @Override
    public void aoRevelarCelula(int linha, int coluna) {
        if (!podeJogar() || tabuleiro.isRevelada(linha, coluna) || tabuleiro.isMarcada(linha, coluna)) {
            return;
        }
        if (!prepararJogada()) {
            return;
        }
        jogadas++;
        jogadasRegistradas.add(new Jogada(TipoJogada.REVELAR, linha, coluna));
        List<int[]> reveladas = tabuleiro.revelar(linha, coluna);
        if (!tabuleiro.isMinada(linha, coluna)) {
            efeitos.tocarClique();
        }
        processarRevelacao(reveladas);
    }

    @Override
    public void aoMarcarCelula(int linha, int coluna) {
        if (!podeJogar() || tabuleiro.isRevelada(linha, coluna)) {
            return;
        }
        boolean estavaMarcada = tabuleiro.isMarcada(linha, coluna);
        tabuleiro.alternarMarcacao(linha, coluna);
        if (!estavaMarcada && tabuleiro.isMarcada(linha, coluna)) {
            bandeirasUsadas++;
        }
        jogadasRegistradas.add(new Jogada(TipoJogada.MARCAR, linha, coluna));
        efeitos.tocarBandeira();
        view.atualizarCelula(linha, coluna, tabuleiro);
        atualizarPainel();
        if (tabuleiro.contarMarcadas() > tabuleiro.getNumMinas()) {
            view.mostrarStatus("⚠ Há mais bandeiras do que minas!", TipoMensagem.ALERTA);
        } else {
            view.mostrarStatus(mensagemDeTurno(), TipoMensagem.NEUTRA);
        }
    }

    @Override
    public void aoFazerChording(int linha, int coluna) {
        if (!podeJogar()) {
            return;
        }
        if (!tabuleiro.podeFazerChording(linha, coluna)) {
            if (tabuleiro.isRevelada(linha, coluna) && !tabuleiro.isMinada(linha, coluna)
                    && tabuleiro.getMinasVizinhas(linha, coluna) > 0
                    && contarMinasSinalizadasVizinhas(linha, coluna) != tabuleiro.getMinasVizinhas(linha, coluna)) {
                efeitos.tocarErro();
                view.mostrarStatus("Chording exige " + tabuleiro.getMinasVizinhas(linha, coluna)
                        + " bandeira(s) ao redor", TipoMensagem.ALERTA);
            }
            return;
        }
        if (!prepararJogada()) {
            return;
        }
        jogadas++;
        chordsUsados++;
        jogadasRegistradas.add(new Jogada(TipoJogada.CHORDING, linha, coluna));
        List<int[]> reveladas = tabuleiro.revelarVizinhos(linha, coluna);
        efeitos.tocarClique();
        processarRevelacao(reveladas);
    }

    private int contarMinasSinalizadasVizinhas(int linha, int coluna) {
        int total = 0;
        for (int[] vizinho : tabuleiro.getVizinhos(linha, coluna)) {
            boolean explodida = tabuleiro.isRevelada(vizinho[0], vizinho[1]) && tabuleiro.isMinada(vizinho[0], vizinho[1]);
            if (explodida || tabuleiro.isMarcada(vizinho[0], vizinho[1])) {
                total++;
            }
        }
        return total;
    }

    @Override
    public void aoPedirDica() {
        if (!podeJogar()) {
            return;
        }
        if (dicasUsadas >= LIMITE_DICAS) {
            efeitos.tocarErro();
            view.mostrarStatus("Você já usou as " + LIMITE_DICAS + " dicas desta partida", TipoMensagem.ALERTA);
            return;
        }
        int[] alvo = escolherCelulaSegura();
        if (alvo == null) {
            return;
        }
        if (!prepararJogada()) {
            return;
        }
        dicasUsadas++;
        jogadas++;
        jogadasRegistradas.add(new Jogada(TipoJogada.DICA, alvo[0], alvo[1]));
        efeitos.tocarDica();
        List<int[]> reveladas = tabuleiro.revelar(alvo[0], alvo[1]);
        view.destacarDica(alvo[0], alvo[1]);
        processarRevelacao(reveladas);
    }

    private int[] escolherCelulaSegura() {
        List<int[]> fronteira = new ArrayList<>();
        List<int[]> todas = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                if (tabuleiro.isRevelada(i, j) || tabuleiro.isMinada(i, j) || tabuleiro.isMarcada(i, j)) {
                    continue;
                }
                int[] posicao = {i, j};
                todas.add(posicao);
                if (temVizinhoRevelado(i, j)) {
                    fronteira.add(posicao);
                }
            }
        }
        List<int[]> candidatas = fronteira.isEmpty() ? todas : fronteira;
        return candidatas.isEmpty() ? null : candidatas.get(sorteio.nextInt(candidatas.size()));
    }

    private boolean temVizinhoRevelado(int linha, int coluna) {
        for (int[] vizinho : tabuleiro.getVizinhos(linha, coluna)) {
            if (tabuleiro.isRevelada(vizinho[0], vizinho[1])) {
                return true;
            }
        }
        return false;
    }

    private boolean prepararJogada() {
        if (!jogoIniciado) {
            jogoIniciado = true;
            tempoInicio = System.currentTimeMillis();
            iniciarTimer();
        }
        if (limiteSegundos > 0 && obterSegundosPassados() >= limiteSegundos) {
            encerrarPorTempo();
            return false;
        }
        return true;
    }

    private void processarRevelacao(List<int[]> reveladas) {
        boolean explodiu = false;
        int seguras = 0;
        for (int[] posicao : reveladas) {
            if (tabuleiro.isMinada(posicao[0], posicao[1])) {
                explodiu = true;
            } else {
                seguras++;
            }
        }
        reveladasPorJogador[jogadorDaVez] += seguras;

        if (tabuleiro.isJogoEncerrado()) {
            pararTimer();
            segundosFinais = obterSegundosPassados();
        } else if (explodiu) {
            efeitos.tocarExplosao();
            view.tremerTela();
            int restantes = tabuleiro.getVidasRestantes();
            view.mostrarStatus("💔 BOOM! Você perdeu uma vida. Restam " + restantes, TipoMensagem.ERRO);
        }

        if (!tabuleiro.isJogoEncerrado() && opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO && !reveladas.isEmpty()) {
            jogadorDaVez = 3 - jogadorDaVez;
            if (!explodiu) {
                view.mostrarStatus(mensagemDeTurno(), TipoMensagem.NEUTRA);
            }
        }

        int atraso = reveladas.size() > 80 ? 3 : (reveladas.size() > 25 ? 8 : 18);
        animarRevelacao(reveladas, 0, atraso, geracaoPartida);
    }

    private String mensagemDeTurno() {
        if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            return "Cooperativo: vez do Jogador " + jogadorDaVez;
        }
        if (etapaDuelo > 0) {
            return "Duelo: vez do Jogador " + etapaDuelo;
        }
        return "Boa sorte!";
    }

    private void animarRevelacao(List<int[]> celulas, int indice, int atraso, int geracao) {
        if (geracao != geracaoPartida) {
            return;
        }
        if (indice >= celulas.size()) {
            finalizarJogada();
            return;
        }
        if (partidaFinalizada) {
            for (int k = indice; k < celulas.size(); k++) {
                int[] restante = celulas.get(k);
                view.atualizarCelula(restante[0], restante[1], tabuleiro);
                if (tabuleiro.isVitoria()) {
                    view.destacarCelulaVencedora(restante[0], restante[1]);
                }
            }
            atualizarPainel();
            return;
        }
        int[] posicao = celulas.get(indice);
        view.atualizarCelula(posicao[0], posicao[1], tabuleiro);
        Timer timer = new Timer(atraso, e -> animarRevelacao(celulas, indice + 1, atraso, geracao));
        timer.setRepeats(false);
        timer.start();
    }

    private void finalizarJogada() {
        atualizarPainel();
        if (!tabuleiro.isJogoEncerrado() || partidaFinalizada) {
            return;
        }
        partidaFinalizada = true;
        encerrarPartida(null);
    }

    private void encerrarPorTempo() {
        if (tabuleiro == null || tabuleiro.isJogoEncerrado() || partidaFinalizada) {
            return;
        }
        pararTimer();
        tabuleiro.encerrarPorTempo();
        segundosFinais = limiteSegundos;
        partidaFinalizada = true;
        view.atualizarTempo("-00:00", true);
        encerrarPartida("⏰ Tempo esgotado!");
    }

    private void encerrarPartida(String motivoDerrota) {
        pararTimer();
        boolean vitoria = tabuleiro.isVitoria();
        mensagemFinal = vitoria ? textoVitoria() : (motivoDerrota != null ? motivoDerrota : textoDerrota());
        if (vitoria) {
            efeitos.tocarVitoria();
            view.mostrarVitoria(mensagemFinal);
            view.celebrarVitoria();
            animarVitoria(geracaoPartida);
        } else {
            efeitos.tocarExplosao();
            view.mostrarDerrota(mensagemFinal);
            view.tremerTela();
            animarExplosao(geracaoPartida);
        }
        atualizarPainel();
        registrarResultado(vitoria);

        if (etapaDuelo == 1) {
            dueloVitoriaPrimeiro = vitoria;
            dueloSegundosPrimeiro = segundosFinais;
            dueloReveladasPrimeiro = tabuleiro.contarReveladasSeguras();
            dueloJogadasPrimeiro = jogadas;
            view.mostrarOpcoesFimDeJogo(true, true);
        } else if (etapaDuelo == 2) {
            view.mostrarOpcoesFimDeJogo(true, false);
            view.mostrarMensagem("Resultado do duelo", resultadoDuelo(vitoria));
        } else {
            view.mostrarOpcoesFimDeJogo(true, false);
        }
    }

    private String textoVitoria() {
        String texto = "🏆 Vitória em " + formatarTempo(segundosFinais) + "!";
        if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            texto = "🏆 Vitória em equipe! J1: " + reveladasPorJogador[1] + " · J2: " + reveladasPorJogador[2];
        } else if (etapaDuelo > 0) {
            texto = "🏆 Jogador " + etapaDuelo + " venceu em " + formatarTempo(segundosFinais);
        }
        return texto;
    }

    private String textoDerrota() {
        if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            return "💥 O Jogador " + jogadorDaVez + " pisou na mina!";
        }
        if (etapaDuelo > 0) {
            return "💥 Jogador " + etapaDuelo + " explodiu!";
        }
        return "💥 Você perdeu!";
    }

    private String resultadoDuelo(boolean vitoriaSegundo) {
        long segundosSegundo = segundosFinais;
        int reveladasSegundo = tabuleiro.contarReveladasSeguras();
        String resumo = "Jogador 1: " + (dueloVitoriaPrimeiro ? "venceu" : "perdeu") + " · "
                + formatarTempo(dueloSegundosPrimeiro) + " · " + dueloReveladasPrimeiro + " células\n"
                + "Jogador 2: " + (vitoriaSegundo ? "venceu" : "perdeu") + " · "
                + formatarTempo(segundosSegundo) + " · " + reveladasSegundo + " células\n\n";
        int vencedor;
        if (dueloVitoriaPrimeiro != vitoriaSegundo) {
            vencedor = dueloVitoriaPrimeiro ? 1 : 2;
        } else if (dueloVitoriaPrimeiro) {
            if (dueloSegundosPrimeiro != segundosSegundo) {
                vencedor = dueloSegundosPrimeiro < segundosSegundo ? 1 : 2;
            } else if (dueloJogadasPrimeiro != jogadas) {
                vencedor = dueloJogadasPrimeiro < jogadas ? 1 : 2;
            } else {
                vencedor = 0;
            }
        } else if (dueloReveladasPrimeiro != reveladasSegundo) {
            vencedor = dueloReveladasPrimeiro > reveladasSegundo ? 1 : 2;
        } else {
            vencedor = 0;
        }
        return resumo + (vencedor == 0 ? "🤝 Empate!" : "🏆 Jogador " + vencedor + " venceu o duelo!");
    }

    private void registrarResultado(boolean vitoria) {
        int vidasPerdidas = configuracaoPartida.getVidas() - tabuleiro.getVidasRestantes();
        RegistroPartida registro = new RegistroPartida(LocalDateTime.now(), nomeRegistro(), vitoria, segundosFinais,
                jogadas, bandeirasUsadas, dicasUsadas, chordsUsados, vidasPerdidas, modosDaPartida());
        HistoricoPartidas historico = perfil.getHistorico();
        historico.adicionar(registro);

        boolean contaParaRecorde = vitoria && !opcoes.isMinasVisiveis()
                && opcoes.getModoJogadores() == ModoJogadores.SOLO;
        if (contaParaRecorde && perfil.getRecordes().registrar(nomeRegistro(), segundosFinais)) {
            view.mostrarNotificacao("🏅 Novo recorde em " + nomeRegistro() + ": " + formatarTempo(segundosFinais));
        }

        List<Conquista> novas = perfil.getConquistas().avaliar(registro, historico);
        if (!novas.isEmpty()) {
            efeitos.tocarConquista();
        }
        for (Conquista conquista : novas) {
            view.mostrarNotificacao(conquista.getEmoji() + " Conquista: " + conquista.getTitulo()
                    + " — " + conquista.getDescricao());
        }
        atualizarRecordeNaView();
    }

    private String nomeRegistro() {
        if (desafioDiario) {
            return DIFICULDADE_DIARIO;
        }
        if (DIFICULDADE_PERSONALIZADA.equals(nomeDificuldade)) {
            return DIFICULDADE_PERSONALIZADA + " " + configuracaoPartida.getLinhas() + "x"
                    + configuracaoPartida.getColunas() + " (" + configuracaoPartida.getMinas() + ")";
        }
        return nomeDificuldade;
    }

    private String modosDaPartida() {
        List<String> modos = new ArrayList<>();
        if (desafioDiario) {
            modos.add("diario");
        }
        if (configuracaoPartida.isToroidal()) {
            modos.add("toroidal");
        }
        if (configuracaoPartida.getVidas() > 1) {
            modos.add("vidas");
        }
        if (configuracaoPartida.isSemCascata()) {
            modos.add("semcascata");
        }
        if (opcoes.isRelampago()) {
            modos.add("relampago");
        }
        if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            modos.add("cooperativo");
        }
        if (opcoes.getModoJogadores() == ModoJogadores.DUELO) {
            modos.add("duelo");
        }
        if (limiteSegundos > 0) {
            modos.add("regressivo");
        }
        if (opcoes.isMinasVisiveis()) {
            modos.add("treino");
        }
        return String.join(",", modos);
    }

    private void atualizarRecordeNaView() {
        Long melhor = perfil.getRecordes().getMelhorTempo(nomeRegistro());
        view.atualizarRecorde(melhor == null ? "—" : formatarTempo(melhor));
    }

    private void atualizarPainel() {
        LeituraTabuleiro exibido = emReplay && tabuleiroReplay != null ? tabuleiroReplay : tabuleiro;
        if (exibido == null) {
            return;
        }
        int minasRestantes = exibido.getNumMinas() - exibido.contarMarcadas();
        view.atualizarEstatisticas(minasRestantes, exibido.contarReveladasSeguras(), exibido.getTotalSeguras(),
                emReplay ? indiceReplay : jogadas);
        view.atualizarVidas(exibido.getVidasRestantes(), configuracaoPartida.getVidas());
        view.atualizarDicas(LIMITE_DICAS - dicasUsadas, LIMITE_DICAS);
        if (opcoes.getModoJogadores() == ModoJogadores.COOPERATIVO) {
            view.atualizarJogadorDaVez("Jogador " + jogadorDaVez);
        } else if (etapaDuelo > 0) {
            view.atualizarJogadorDaVez("Jogador " + etapaDuelo + " de 2");
        } else {
            view.atualizarJogadorDaVez(null);
        }
    }

    private void iniciarTimer() {
        pararTimer();
        int geracao = geracaoPartida;
        timerJogo = new Timer(250, e -> {
            if (geracao == geracaoPartida) {
                atualizarTempo();
            }
        });
        timerJogo.start();
    }

    private void pararTimer() {
        if (timerJogo != null) {
            timerJogo.stop();
            timerJogo = null;
        }
    }

    private long obterSegundosPassados() {
        return (System.currentTimeMillis() - tempoInicio) / 1000;
    }

    private void atualizarTempo() {
        if (partidaFinalizada || tabuleiro == null || tabuleiro.isJogoEncerrado()) {
            return;
        }
        long passados = obterSegundosPassados();
        if (limiteSegundos > 0) {
            long restantes = Math.max(0, limiteSegundos - passados);
            view.atualizarTempo("-" + formatarTempo(restantes), restantes <= 10);
            if (restantes <= 0) {
                encerrarPorTempo();
            }
        } else {
            view.atualizarTempo(formatarTempo(passados), false);
        }
    }

    public static String formatarTempo(long segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    private void animarExplosao(int geracao) {
        Timer piscar = new Timer(90, null);
        int[] contador = {0};
        piscar.addActionListener(e -> {
            if (geracao != geracaoPartida) {
                piscar.stop();
                return;
            }
            contador[0]++;
            view.piscarFundoDeExplosao(contador[0] % 2 == 1);
            if (contador[0] >= 6) {
                piscar.stop();
                view.piscarFundoDeExplosao(false);
                revelarMinasComAnimacao(geracao);
            }
        });
        piscar.start();
    }

    private void revelarMinasComAnimacao(int geracao) {
        List<int[]> pendentes = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                boolean minaEscondida = tabuleiro.isMinada(i, j) && !tabuleiro.isRevelada(i, j) && !tabuleiro.isMarcada(i, j);
                boolean bandeiraErrada = tabuleiro.isMarcada(i, j) && !tabuleiro.isMinada(i, j);
                if (minaEscondida || bandeiraErrada) {
                    pendentes.add(new int[]{i, j});
                }
            }
        }
        revelarMinasPasso(pendentes, 0, geracao);
    }

    private void revelarMinasPasso(List<int[]> pendentes, int indice, int geracao) {
        if (indice >= pendentes.size() || geracao != geracaoPartida) {
            return;
        }
        int[] posicao = pendentes.get(indice);
        if (tabuleiro.isMarcada(posicao[0], posicao[1])) {
            view.marcarBandeiraErrada(posicao[0], posicao[1]);
        } else {
            view.revelarMinaFinal(posicao[0], posicao[1]);
        }
        int atraso = pendentes.size() > 60 ? 15 : 60;
        Timer timer = new Timer(atraso, e -> revelarMinasPasso(pendentes, indice + 1, geracao));
        timer.setRepeats(false);
        timer.start();
    }

    private void animarVitoria(int geracao) {
        List<int[]> celulas = new ArrayList<>();
        for (int i = 0; i < tabuleiro.getLinhas(); i++) {
            for (int j = 0; j < tabuleiro.getColunas(); j++) {
                celulas.add(new int[]{i, j});
            }
        }
        vitoriaPasso(celulas, 0, geracao);
    }

    private void vitoriaPasso(List<int[]> celulas, int indice, int geracao) {
        if (geracao != geracaoPartida) {
            return;
        }
        int fim = Math.min(celulas.size(), indice + Math.max(1, celulas.size() / 60));
        for (int k = indice; k < fim; k++) {
            int[] atual = celulas.get(k);
            if (tabuleiro.isMinada(atual[0], atual[1])) {
                view.sinalizarMinaNaVitoria(atual[0], atual[1]);
            } else {
                view.destacarCelulaVencedora(atual[0], atual[1]);
            }
        }
        if (fim >= celulas.size()) {
            return;
        }
        Timer timer = new Timer(12, e -> vitoriaPasso(celulas, fim, geracao));
        timer.setRepeats(false);
        timer.start();
    }

    private void registrarResultadoPendente() {
        if (tabuleiro != null && !emReplay && !partidaFinalizada && tabuleiro.isJogoEncerrado()) {
            partidaFinalizada = true;
            registrarResultado(tabuleiro.isVitoria());
        }
    }

    @Override
    public void aoPedirNovoJogo() {
        registrarResultadoPendente();
        pararTimer();
        pararTimerReplay();
        emReplay = false;
        geracaoPartida++;
        tabuleiro = null;
        tabuleiroReplay = null;
        etapaDuelo = 0;
        view.atualizarPerfis(gerenciadorPerfis.listarPerfis(), perfil.getNome());
        view.mostrarTelaInicial();
    }

    @Override
    public void aoJogarNovamente() {
        if (configuracaoPartida == null) {
            aoPedirNovoJogo();
            return;
        }
        if (desafioDiario) {
            aoPedirDesafioDiario();
            return;
        }
        aoEscolherDificuldade(nomeDificuldade, configuracaoPartida.getLinhas(), configuracaoPartida.getColunas(),
                configuracaoPartida.getMinas());
    }

    @Override
    public void aoIniciarSegundoJogadorDuelo() {
        if (etapaDuelo != 1 || !partidaFinalizada) {
            return;
        }
        etapaDuelo = 2;
        comecarPartida(new Tabuleiro(configuracaoPartida, posicoesMinas));
    }

    @Override
    public void aoPedirReplay() {
        if (tabuleiro == null || !partidaFinalizada || emReplay) {
            return;
        }
        geracaoPartida++;
        emReplay = true;
        indiceReplay = 0;
        tabuleiroReplay = new Tabuleiro(configuracaoPartida, posicoesMinas);
        view.iniciarTelaDeJogo(tabuleiroReplay, tituloPartida() + " · Replay", opcoes.isMinasVisiveis());
        view.setModoReplay(true);
        if (quantidadeRelampago > 0) {
            tabuleiroReplay.revelarAleatorias(quantidadeRelampago, new Random(sementeRelampago));
        }
        view.redesenharTabuleiro();
        view.atualizarTempo(formatarTempo(segundosFinais), false);
        atualizarPainel();
        view.mostrarStatus("▶ Replay: 0 / " + jogadasRegistradas.size(), TipoMensagem.NEUTRA);
        int intervalo = jogadasRegistradas.size() > 80 ? 180 : 450;
        timerReplay = new Timer(intervalo, e -> passoReplay());
        timerReplay.setInitialDelay(700);
        timerReplay.start();
    }

    private void passoReplay() {
        if (!emReplay) {
            pararTimerReplay();
            return;
        }
        if (indiceReplay >= jogadasRegistradas.size()) {
            pararTimerReplay();
            if (tabuleiro.isDerrota() && !tabuleiroReplay.isJogoEncerrado()) {
                tabuleiroReplay.encerrarPorTempo();
                view.redesenharTabuleiro();
            }
            view.mostrarStatus("✔ Replay concluído", TipoMensagem.SUCESSO);
            view.setModoReplay(false);
            emReplay = false;
            view.mostrarOpcoesFimDeJogo(true, etapaDuelo == 1);
            return;
        }
        Jogada jogada = jogadasRegistradas.get(indiceReplay);
        indiceReplay++;
        aplicarJogadaReplay(jogada);
        view.destacarJogadaReplay(jogada.getLinha(), jogada.getColuna());
        view.redesenharTabuleiro();
        atualizarPainel();
        view.mostrarStatus("▶ Replay: " + indiceReplay + " / " + jogadasRegistradas.size()
                + " · " + descreverJogada(jogada), TipoMensagem.NEUTRA);
    }

    private void aplicarJogadaReplay(Jogada jogada) {
        if (jogada.getTipo() == TipoJogada.MARCAR) {
            tabuleiroReplay.alternarMarcacao(jogada.getLinha(), jogada.getColuna());
        } else if (jogada.getTipo() == TipoJogada.CHORDING) {
            tabuleiroReplay.revelarVizinhos(jogada.getLinha(), jogada.getColuna());
        } else {
            tabuleiroReplay.revelar(jogada.getLinha(), jogada.getColuna());
        }
    }

    private String descreverJogada(Jogada jogada) {
        String posicao = "(" + (jogada.getLinha() + 1) + ", " + (jogada.getColuna() + 1) + ")";
        if (jogada.getTipo() == TipoJogada.MARCAR) {
            return "marcou " + posicao;
        }
        if (jogada.getTipo() == TipoJogada.CHORDING) {
            return "chording em " + posicao;
        }
        if (jogada.getTipo() == TipoJogada.DICA) {
            return "dica em " + posicao;
        }
        return "revelou " + posicao;
    }

    @Override
    public void aoPararReplay() {
        if (!emReplay) {
            return;
        }
        pararTimerReplay();
        emReplay = false;
        geracaoPartida++;
        view.iniciarTelaDeJogo(tabuleiro, tituloPartida(), opcoes.isMinasVisiveis());
        view.redesenharTabuleiro();
        view.atualizarTempo(formatarTempo(segundosFinais), false);
        atualizarRecordeNaView();
        atualizarPainel();
        if (tabuleiro.isVitoria()) {
            view.mostrarVitoria(mensagemFinal != null ? mensagemFinal : textoVitoria());
        } else {
            view.mostrarDerrota(mensagemFinal != null ? mensagemFinal : textoDerrota());
        }
        view.mostrarOpcoesFimDeJogo(true, etapaDuelo == 1);
    }

    private void pararTimerReplay() {
        if (timerReplay != null) {
            timerReplay.stop();
            timerReplay = null;
        }
    }

    @Override
    public void aoPedirCompartilhar() {
        if (tabuleiro == null || !partidaFinalizada) {
            return;
        }
        String modo = desafioDiario
                ? "no Desafio Diário de " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "no modo " + nomeRegistro();
        String texto;
        if (tabuleiro.isVitoria()) {
            texto = "💣 Campo Minado: terminei em " + formatarTempo(segundosFinais) + " " + modo + "! 🏆 ("
                    + jogadas + " jogadas" + (dicasUsadas == 0 ? ", sem dicas" : ", " + dicasUsadas + " dica(s)") + ")";
        } else {
            int percentual = (int) Math.round(tabuleiro.contarReveladasSeguras() * 100.0 / tabuleiro.getTotalSeguras());
            texto = "💣 Campo Minado: explodi " + modo + " depois de " + formatarTempo(segundosFinais)
                    + ", com " + percentual + "% do campo limpo. Revanche? 💥";
        }
        String extras = descreverModosParaCompartilhar();
        if (!extras.isEmpty()) {
            texto += " [" + extras + "]";
        }
        view.compartilharTexto(texto);
    }

    private String descreverModosParaCompartilhar() {
        List<String> nomes = new ArrayList<>();
        if (configuracaoPartida.isToroidal()) {
            nomes.add("toroidal");
        }
        if (configuracaoPartida.getVidas() > 1) {
            nomes.add("3 vidas");
        }
        if (configuracaoPartida.isSemCascata()) {
            nomes.add("sem cascata");
        }
        if (opcoes.isRelampago()) {
            nomes.add("relâmpago");
        }
        if (limiteSegundos > 0) {
            nomes.add("contra o relógio");
        }
        return String.join(", ", nomes);
    }

    @Override
    public void aoPedirEstatisticas() {
        view.mostrarEstatisticas(montarResumo());
    }

    private ResumoEstatisticas montarResumo() {
        HistoricoPartidas historico = perfil.getHistorico();
        List<RegistroPartida> registros = historico.getRegistros();
        ResumoEstatisticas resumo = new ResumoEstatisticas();
        resumo.setNomePerfil(perfil.getNome());
        resumo.setTotalPartidas(historico.getTotalPartidas());
        resumo.setVitorias(historico.getVitorias());
        resumo.setDerrotas(historico.getDerrotas());
        resumo.setTaxaVitoria(historico.getTaxaVitoria());
        resumo.setTaxaUltimasDez(historico.getTaxaVitoriaUltimas(10));
        resumo.setSequenciaAtual(historico.getSequenciaAtualVitorias());
        resumo.setMelhorSequencia(historico.getMelhorSequenciaVitorias());

        int vitoriasAcumuladas = 0;
        for (int i = 0; i < registros.size(); i++) {
            if (registros.get(i).isVitoria()) {
                vitoriasAcumuladas++;
            }
            resumo.getEvolucaoTaxa().add(vitoriasAcumuladas * 100.0 / (i + 1));
        }

        for (Map.Entry<String, Long> recorde : perfil.getRecordes().todos().entrySet()) {
            resumo.getRecordes().add(new String[]{recorde.getKey(), formatarTempo(recorde.getValue())});
        }

        List<RegistroPartida> validosParaRanking = new ArrayList<>();
        for (RegistroPartida registro : registros) {
            if (!registro.temModo("treino") && !registro.temModo("cooperativo") && !registro.temModo("duelo")) {
                validosParaRanking.add(registro);
            }
        }
        for (String dificuldade : DIFICULDADES_RANKING) {
            List<String[]> linhas = new ArrayList<>();
            List<RegistroPartida> melhores = RankingTempos.topTempos(validosParaRanking, dificuldade, 10);
            for (int i = 0; i < melhores.size(); i++) {
                RegistroPartida registro = melhores.get(i);
                linhas.add(new String[]{String.valueOf(i + 1) + "º", registro.getTempoFormatado(),
                        String.valueOf(registro.getJogadas()), registro.getDataHora().format(FORMATO_DATA)});
            }
            resumo.getRanking().put(dificuldade, linhas);
        }

        for (int i = registros.size() - 1; i >= 0; i--) {
            RegistroPartida registro = registros.get(i);
            resumo.getHistorico().add(new String[]{registro.getDataHora().format(FORMATO_DATA),
                    registro.getDificuldade(), registro.isVitoria() ? "Vitória" : "Derrota",
                    registro.getTempoFormatado(), String.valueOf(registro.getJogadas()), registro.getModos()});
        }

        Set<Conquista> desbloqueadas = perfil.getConquistas().getDesbloqueadas();
        for (Conquista conquista : Conquista.values()) {
            resumo.getConquistas().add(new String[]{conquista.getEmoji(), conquista.getTitulo(),
                    conquista.getDescricao(), desbloqueadas.contains(conquista) ? "sim" : "nao"});
        }
        return resumo;
    }

    @Override
    public void aoExportarCsv(File destino) {
        if (destino == null) {
            return;
        }
        File arquivo = destino.getName().toLowerCase().endsWith(".csv")
                ? destino
                : new File(destino.getParentFile(), destino.getName() + ".csv");
        try {
            ExportadorCsv.exportar(perfil.getHistorico().getRegistros(), arquivo.toPath());
            view.mostrarMensagem("Exportação concluída", perfil.getHistorico().getTotalPartidas()
                    + " partida(s) exportada(s) para:\n" + arquivo.getAbsolutePath());
        } catch (IOException erro) {
            view.mostrarErro("Não foi possível exportar: " + erro.getMessage());
        }
    }

    @Override
    public void aoSelecionarPerfil(String nome) {
        if (nome == null || (perfil != null && nome.equals(perfil.getNome()))) {
            return;
        }
        perfil = gerenciadorPerfis.carregar(nome);
        configuracoes.setPerfilAtual(perfil.getNome());
        configuracoes.salvar();
        view.atualizarPerfis(gerenciadorPerfis.listarPerfis(), perfil.getNome());
        view.mostrarNotificacao("👤 Perfil ativo: " + perfil.getNome());
    }

    @Override
    public void aoCriarPerfil(String nome) {
        if (nome == null) {
            return;
        }
        String limpo = nome.trim();
        if (!gerenciadorPerfis.criarPerfil(limpo)) {
            view.mostrarErro("Não foi possível criar o perfil. Use de 1 a 20 caracteres e um nome que ainda não exista.");
            return;
        }
        aoSelecionarPerfil(limpo);
    }

    @Override
    public void aoTrocarTema(String nomeTema) {
        if (nomeTema == null || nomeTema.equals(dadosConfiguracao.getTema())) {
            return;
        }
        dadosConfiguracao.setTema(nomeTema);
        configuracoes.setTema(nomeTema);
        configuracoes.salvar();
        view.aplicarConfiguracao(dadosConfiguracao);
    }

    @Override
    public void aoAlternarSom() {
        boolean ativo = !dadosConfiguracao.isSomAtivo();
        dadosConfiguracao.setSomAtivo(ativo);
        efeitos.setAtivo(ativo);
        configuracoes.setSomAtivo(ativo);
        configuracoes.salvar();
        view.atualizarEstadoSom(ativo);
        if (ativo) {
            efeitos.tocarClique();
        }
    }

    @Override
    public void aoAlterarZoom(double zoom) {
        dadosConfiguracao.setZoom(zoom);
        configuracoes.setZoom(dadosConfiguracao.getZoom());
        configuracoes.salvar();
    }

    @Override
    public void aoSalvarConfiguracoes(DadosConfiguracao dados) {
        dadosConfiguracao = dados.copiar();
        gravarConfiguracoes(dadosConfiguracao);
        aplicarAudio();
        view.aplicarConfiguracao(dadosConfiguracao);
        view.mostrarNotificacao("⚙ Configurações salvas");
    }

    @Override
    public void aoFecharAplicacao() {
        registrarResultadoPendente();
        pararTimer();
        pararTimerReplay();
        musica.parar();
        if (dadosConfiguracao != null && perfil != null) {
            gravarConfiguracoes(dadosConfiguracao);
        }
    }
}
