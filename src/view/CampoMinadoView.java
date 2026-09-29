package view;

import controller.AcoesJogador;
import controller.DadosConfiguracao;
import controller.ModoJogadores;
import controller.OpcoesPartida;
import controller.ResumoEstatisticas;
import model.LeituraTabuleiro;
import view.tema.CatalogoTemas;
import view.tema.DetectorTemaSistema;
import view.tema.SkinBandeira;
import view.tema.SkinCelula;
import view.tema.TemaVisual;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class CampoMinadoView extends JFrame {

    private static final String EMOJI_BOMBA = "💣";
    private static final String[] TEMPOS_JOGO = {"Sem limite", "1 minuto", "2 minutos", "3 minutos", "5 minutos", "10 minutos"};
    private static final int[] SEGUNDOS_TEMPOS = {0, 60, 120, 180, 300, 600};
    private static final int MARGEM = 8;
    private static final double PASSO_ZOOM = 0.1;

    private enum Tela {
        INICIAL,
        TUTORIAL,
        JOGO
    }

    private AcoesJogador ouvinte;
    private DadosConfiguracao dados = new DadosConfiguracao();
    private TemaVisual tema = CatalogoTemas.porNome(CatalogoTemas.ESCURO);
    private SkinBandeira skinBandeira = SkinBandeira.PADRAO;
    private SkinCelula skinCelula = SkinCelula.NUMEROS;
    private double escala = 1.0;
    private double zoom = 1.0;

    private final OpcoesPartida opcoes = new OpcoesPartida();
    private List<String> perfis = new ArrayList<>(Arrays.asList("Jogador"));
    private String perfilAtual = "Jogador";

    private Tela telaAtual = Tela.INICIAL;
    private final JPanel raiz;
    private final PainelConfete painelConfete;

    private LeituraTabuleiro leitura;
    private String tituloPartida = "";
    private boolean minasVisiveis;
    private String textoTempo = "00:00";
    private boolean tempoEmAlerta;
    private int minasRestantes;
    private int celulasReveladas;
    private int totalSeguras = 1;
    private int jogadas;
    private int vidasRestantes = 1;
    private int vidasTotais = 1;
    private int dicasRestantes;
    private int dicasTotais;
    private String jogadorDaVez;
    private String textoRecorde = "—";
    private String textoStatus = "Boa sorte!";
    private TipoMensagem tipoStatus = TipoMensagem.NEUTRA;
    private boolean fimDeJogoVisivel;
    private boolean podeReplay;
    private boolean proximoDuelo;
    private boolean modoReplay;

    private PainelTabuleiro painelTabuleiro;
    private JPanel painelPrincipal;
    private JPanel painelFimDeJogo;
    private JLabel labelStatus;
    private JLabel lblTempo;
    private JLabel lblMinasRestantes;
    private JLabel lblCelulasReveladas;
    private JLabel lblJogadas;
    private JLabel lblVidas;
    private JLabel lblDicas;
    private JLabel lblJogadorDaVez;
    private JLabel lblRecorde;
    private JPanel itemVidas;
    private JPanel itemJogador;
    private JProgressBar barraProgresso;
    private JButton botaoDica;
    private JButton botaoSom;
    private JComboBox<String> comboPerfis;
    private boolean atualizandoPerfis;

    private boolean telaCheia;
    private Rectangle limitesAntesDaTelaCheia;
    private boolean ignorarRedimensionamento;
    private Timer timerTremor;

    private final Deque<String> filaNotificacoes = new ArrayDeque<>();
    private boolean notificacaoAtiva;

    public CampoMinadoView() {
        super("Campo Minado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        raiz = new JPanel(new BorderLayout());
        raiz.setBorder(new EmptyBorder(MARGEM, MARGEM, MARGEM, MARGEM));
        setContentPane(raiz);
        painelConfete = new PainelConfete();
        setGlassPane(painelConfete);
        setResizable(true);
        setMinimumSize(new Dimension(520, 420));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                if (ouvinte != null) {
                    ouvinte.aoFecharAplicacao();
                }
            }
        });
        instalarAtalhosGlobais();
    }

    public void setOuvinte(AcoesJogador ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void aplicarConfiguracao(DadosConfiguracao novos) {
        dados = novos.copiar();
        tema = CatalogoTemas.resolver(dados.getTema());
        skinBandeira = SkinBandeira.porNome(dados.getSkinBandeira());
        skinCelula = SkinCelula.porNome(dados.getSkinCelula());
        escala = dados.getEscalaFonte();
        zoom = dados.getZoom();
        reconstruirTelaAtual();
    }

    public void atualizarPerfis(List<String> lista, String atual) {
        perfis = new ArrayList<>(lista);
        perfilAtual = atual;
        if (comboPerfis != null) {
            atualizandoPerfis = true;
            comboPerfis.removeAllItems();
            for (String perfil : perfis) {
                comboPerfis.addItem(perfil);
            }
            comboPerfis.setSelectedItem(atual);
            atualizandoPerfis = false;
        }
    }

    public OpcoesPartida getOpcoesPartida() {
        return opcoes.copiar();
    }

    private void reconstruirTelaAtual() {
        if (telaAtual == Tela.JOGO && leitura != null) {
            PainelTabuleiro anterior = painelTabuleiro;
            montarTelaDeJogo(true);
            painelTabuleiro.redesenharTudo();
            if (anterior != null && anterior.getLinhas() == painelTabuleiro.getLinhas()
                    && anterior.getColunas() == painelTabuleiro.getColunas()) {
                painelTabuleiro.posicionarCursor(anterior.getLinhaCursor(), anterior.getColunaCursor(),
                        anterior.isCursorVisivel());
            }
            restaurarEstadoDoJogo();
        } else if (telaAtual == Tela.TUTORIAL) {
            mostrarTutorial();
        } else if (isDisplayable() || isVisible()) {
            mostrarTelaInicial();
        }
    }

    private Font fonte(int estilo, float tamanho) {
        return EstiloUI.fonte(tema, escala, estilo, tamanho);
    }

    private JLabel rotulo(String texto, Color cor, int estilo, float tamanho) {
        JLabel rotulo = EstiloUI.rotulo(texto, cor, EstiloUI.fonteParaTexto(texto, tema, escala, estilo, tamanho));
        rotulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        return rotulo;
    }

    private void prepararRaiz() {
        painelConfete.parar();
        raiz.removeAll();
        raiz.setBackground(tema.getFundo());
        raiz.setBorder(new EmptyBorder(MARGEM, MARGEM, MARGEM, MARGEM));
        painelTabuleiro = null;
        comboPerfis = null;
        limparAtalhosDeJogo();
    }

    private void finalizarMontagem(boolean redimensionar) {
        raiz.revalidate();
        raiz.repaint();
        if (redimensionar && !telaCheia && (getExtendedState() & MAXIMIZED_BOTH) == 0) {
            ignorarRedimensionamento = true;
            pack();
            limitarAoEspacoDaTela();
            setLocationRelativeTo(null);
            SwingUtilities.invokeLater(() -> ignorarRedimensionamento = false);
        }
    }

    private void limitarAoEspacoDaTela() {
        try {
            Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            int largura = Math.min(getWidth(), area.width);
            int altura = Math.min(getHeight(), area.height);
            if (largura != getWidth() || altura != getHeight()) {
                setSize(largura, altura);
            }
        } catch (RuntimeException ignorada) {
            setSize(getWidth(), getHeight());
        }
    }

    public void mostrarTelaInicial() {
        telaAtual = Tela.INICIAL;
        leitura = null;
        prepararRaiz();

        JPanel conteudo = EstiloUI.painel(tema.getFundo(), null);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        conteudo.add(rotulo(EMOJI_BOMBA + " Campo Minado", tema.getTextoPrincipal(), Font.BOLD, 30));
        JLabel subtitulo = rotulo("Escolha sua dificuldade", tema.getTextoSecundario(), Font.PLAIN, 14);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(6, 0, 16, 0));
        conteudo.add(subtitulo);

        conteudo.add(criarLinhaPerfil());
        conteudo.add(Box.createVerticalStrut(18));

        JPanel cartoes = EstiloUI.painel(tema.getFundo(), new GridLayout(1, 5, 12, 0));
        cartoes.setAlignmentX(Component.CENTER_ALIGNMENT);
        cartoes.add(criarCartao("Iniciante", "9 × 9", EMOJI_BOMBA + " 10 minas",
                () -> escolher("Iniciante", 9, 9, 10)));
        cartoes.add(criarCartao("Intermediário", "16 × 16", EMOJI_BOMBA + " 40 minas",
                () -> escolher("Intermediário", 16, 16, 40)));
        cartoes.add(criarCartao("Avançado", "16 × 30", EMOJI_BOMBA + " 99 minas",
                () -> escolher("Avançado", 16, 30, 99)));
        cartoes.add(criarCartao("Personalizado", "? × ?", "✏ você escolhe", this::abrirPersonalizado));
        cartoes.add(criarCartao("Desafio diário", "📅 16 × 16", "mesmo tabuleiro hoje", () -> {
            if (ouvinte != null) {
                ouvinte.aoPedirDesafioDiario();
            }
        }));
        conteudo.add(cartoes);
        conteudo.add(Box.createVerticalStrut(20));
        conteudo.add(criarPainelModos());
        conteudo.add(Box.createVerticalStrut(16));
        conteudo.add(criarLinhaRodapeInicial());

        JLabel dica = rotulo("Esquerdo: revelar · Direito: bandeira · Meio, duplo clique ou esquerdo+direito: chording",
                tema.getTextoSecundario(), Font.PLAIN, 12);
        dica.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        conteudo.add(dica);

        JScrollPane rolagem = new JScrollPane(conteudo);
        rolagem.setBorder(null);
        rolagem.getViewport().setBackground(tema.getFundo());
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        raiz.add(rolagem, BorderLayout.CENTER);
        finalizarMontagem(true);
    }

    private void escolher(String nome, int linhas, int colunas, int minas) {
        if (ouvinte != null) {
            ouvinte.aoEscolherDificuldade(nome, linhas, colunas, minas);
        }
    }

    private void abrirPersonalizado() {
        int[] valores = DialogoPersonalizado.mostrar(this, tema, escala, dados.getDensidadeMinas());
        if (valores != null) {
            escolher("Personalizado", valores[0], valores[1], valores[2]);
        }
    }

    private JPanel criarLinhaPerfil() {
        JPanel linha = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.CENTER, 8, 0));
        linha.setAlignmentX(Component.CENTER_ALIGNMENT);
        linha.add(rotulo("👤 Perfil:", tema.getTextoSecundario(), Font.PLAIN, 13));
        comboPerfis = EstiloUI.combo(perfis, perfilAtual, tema, escala);
        comboPerfis.setPreferredSize(new Dimension((int) (150 * escala), comboPerfis.getPreferredSize().height));
        comboPerfis.addActionListener(e -> {
            Object selecionado = comboPerfis.getSelectedItem();
            if (!atualizandoPerfis && selecionado != null && ouvinte != null && !selecionado.equals(perfilAtual)) {
                ouvinte.aoSelecionarPerfil(selecionado.toString());
            }
        });
        linha.add(comboPerfis);
        linha.add(EstiloUI.botao("+ Novo perfil", tema, escala, e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome do novo perfil (até 20 caracteres):",
                    "Novo perfil", JOptionPane.PLAIN_MESSAGE);
            if (nome != null && ouvinte != null) {
                ouvinte.aoCriarPerfil(nome);
            }
        }));
        linha.add(EstiloUI.botao("📊 Estatísticas", tema, escala, e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirEstatisticas();
            }
        }));
        return linha;
    }

    private JPanel criarCartao(String titulo, String dimensao, String descricao, Runnable acao) {
        JPanel cartao = EstiloUI.painel(tema.getCard(), null);
        cartao.setLayout(new BoxLayout(cartao, BoxLayout.Y_AXIS));
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.getBorda(), 1),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        cartao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cartao.add(rotulo(titulo, tema.getDestaque(), Font.BOLD, 15));
        JLabel rotuloDimensao = rotulo(dimensao, tema.getTextoPrincipal(), Font.BOLD, 20);
        rotuloDimensao.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        cartao.add(rotuloDimensao);
        cartao.add(rotulo(descricao, tema.getTextoSecundario(), Font.PLAIN, 12));
        cartao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evento) {
                cartao.setBackground(tema.getCardHover());
                cartao.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(tema.getDestaque(), 2),
                        BorderFactory.createEmptyBorder(15, 11, 15, 11)));
            }

            @Override
            public void mouseExited(MouseEvent evento) {
                cartao.setBackground(tema.getCard());
                cartao.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(tema.getBorda(), 1),
                        BorderFactory.createEmptyBorder(16, 12, 16, 12)));
            }

            @Override
            public void mouseReleased(MouseEvent evento) {
                if (SwingUtilities.isLeftMouseButton(evento) && cartao.contains(evento.getPoint())) {
                    acao.run();
                }
            }
        });
        return cartao;
    }

    private JPanel criarPainelModos() {
        JPanel painel = EstiloUI.painel(tema.getFundoClaro(), new BorderLayout(0, 10));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.getBorda()),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        painel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titulo = EstiloUI.rotulo("🎮 Modos de jogo", tema.getDestaque(),
                EstiloUI.fonteParaTexto("🎮", tema, escala, Font.BOLD, 14));
        painel.add(titulo, BorderLayout.NORTH);

        JPanel caixas = EstiloUI.painelTransparente(new GridLayout(0, 3, 12, 4));
        caixas.add(caixaModo("Sem cascata", "Revela só a célula clicada, mesmo com zero vizinhas",
                opcoes.isSemCascata(), opcoes::setSemCascata));
        caixas.add(caixaModo("Relâmpago", "Algumas células seguras já começam reveladas",
                opcoes.isRelampago(), opcoes::setRelampago));
        caixas.add(caixaModo("3 vidas", "Tolera até 2 explosões antes de perder",
                opcoes.isTresVidas(), opcoes::setTresVidas));
        caixas.add(caixaModo("Toroidal", "As bordas se conectam (topo↔base, esquerda↔direita)",
                opcoes.isToroidal(), opcoes::setToroidal));
        caixas.add(caixaModo("Minas visíveis (treino)", "Mostra as minas em transparência; não conta recorde",
                opcoes.isMinasVisiveis(), opcoes::setMinasVisiveis));
        painel.add(caixas, BorderLayout.CENTER);

        JPanel selecoes = EstiloUI.painelTransparente(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selecoes.add(EstiloUI.rotulo("Jogadores:", tema.getTextoSecundario(), fonte(Font.PLAIN, 12)));
        List<String> modos = new ArrayList<>();
        for (ModoJogadores modo : ModoJogadores.values()) {
            modos.add(modo.getNome());
        }
        JComboBox<String> comboJogadores = EstiloUI.combo(modos, opcoes.getModoJogadores().getNome(), tema, escala);
        comboJogadores.addActionListener(e -> opcoes.setModoJogadores(
                ModoJogadores.porNome((String) comboJogadores.getSelectedItem())));
        selecoes.add(comboJogadores);
        selecoes.add(Box.createHorizontalStrut(8));
        selecoes.add(EstiloUI.rotulo("Cronômetro regressivo:", tema.getTextoSecundario(), fonte(Font.PLAIN, 12)));
        JComboBox<String> comboTempo = EstiloUI.combo(Arrays.asList(TEMPOS_JOGO), nomeDoTempo(opcoes.getTempoLimiteSegundos()), tema, escala);
        comboTempo.addActionListener(e -> opcoes.setTempoLimiteSegundos(SEGUNDOS_TEMPOS[comboTempo.getSelectedIndex()]));
        selecoes.add(comboTempo);
        painel.add(selecoes, BorderLayout.SOUTH);
        return painel;
    }

    private String nomeDoTempo(int segundos) {
        for (int i = 0; i < SEGUNDOS_TEMPOS.length; i++) {
            if (SEGUNDOS_TEMPOS[i] == segundos) {
                return TEMPOS_JOGO[i];
            }
        }
        return TEMPOS_JOGO[0];
    }

    private interface AlteracaoOpcao {
        void aplicar(boolean valor);
    }

    private JCheckBox caixaModo(String texto, String descricao, boolean marcada, AlteracaoOpcao alteracao) {
        JCheckBox caixa = EstiloUI.caixa(texto, marcada, tema, escala);
        caixa.setToolTipText(descricao);
        caixa.addActionListener(e -> alteracao.aplicar(caixa.isSelected()));
        return caixa;
    }

    private JPanel criarLinhaRodapeInicial() {
        JPanel linha = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.CENTER, 8, 0));
        linha.setAlignmentX(Component.CENTER_ALIGNMENT);
        linha.add(EstiloUI.rotulo("Tema:", tema.getTextoSecundario(), fonte(Font.PLAIN, 12)));
        linha.add(criarComboTema());
        linha.add(EstiloUI.botao("⚙ Configurações", tema, escala, e -> abrirConfiguracoes()));
        linha.add(EstiloUI.botao("📖 Tutorial", tema, escala, e -> mostrarTutorial()));
        linha.add(EstiloUI.botao(telaCheia ? "Sair da tela cheia" : "⛶ Tela cheia", tema, escala, e -> alternarTelaCheia()));
        return linha;
    }

    private JComboBox<String> criarComboTema() {
        JComboBox<String> combo = EstiloUI.combo(CatalogoTemas.nomes(), dados.getTema(), tema, escala);
        combo.setToolTipText("Automático segue o sistema: " + DetectorTemaSistema.descreverSistema());
        combo.addActionListener(e -> {
            String escolhido = (String) combo.getSelectedItem();
            if (escolhido != null && ouvinte != null && !escolhido.equals(dados.getTema())) {
                SwingUtilities.invokeLater(() -> ouvinte.aoTrocarTema(escolhido));
            }
        });
        return combo;
    }

    private void abrirConfiguracoes() {
        DadosConfiguracao novos = DialogoConfiguracoes.mostrar(this, dados, tema, escala);
        if (novos != null && ouvinte != null) {
            ouvinte.aoSalvarConfiguracoes(novos);
        }
    }

    private void mostrarTutorial() {
        telaAtual = Tela.TUTORIAL;
        prepararRaiz();
        JPanel painel = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 14));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));
        painel.add(rotulo("Como jogar Campo Minado", tema.getTextoPrincipal(), Font.BOLD, 26), BorderLayout.NORTH);

        String texto = "OBJETIVO\n"
                + "Revele todas as células sem minas. O número de uma célula indica quantas minas existem ao redor dela.\n\n"
                + "MOUSE\n"
                + "• Botão esquerdo: revela a célula.\n"
                + "• Botão direito: alterna bandeira → interrogação (?) → vazio.\n"
                + "• Botão do meio, duplo clique ou esquerdo+direito juntos sobre um número: chording — se a quantidade de "
                + "bandeiras ao redor for igual ao número, todos os vizinhos são revelados de uma vez.\n"
                + "• Ctrl + roda do mouse: zoom.\n\n"
                + "TECLADO\n"
                + "• Setas: movem o cursor · " + KeyEvent.getKeyText(dados.getTecla(DadosConfiguracao.ACAO_REVELAR)) + "/Enter: revelar · "
                + KeyEvent.getKeyText(dados.getTecla(DadosConfiguracao.ACAO_MARCAR)) + ": marcar · "
                + KeyEvent.getKeyText(dados.getTecla(DadosConfiguracao.ACAO_CHORDING)) + ": chording · "
                + KeyEvent.getKeyText(dados.getTecla(DadosConfiguracao.ACAO_DICA)) + ": dica\n"
                + "• + / −: zoom · F11: tela cheia (os atalhos podem ser trocados em Configurações).\n\n"
                + "MODOS\n"
                + "• Sem cascata, Relâmpago, 3 vidas, Toroidal e Minas visíveis (treino) podem ser combinados.\n"
                + "• Cronômetro regressivo: se o tempo acabar, você perde.\n"
                + "• Cooperativo: dois jogadores alternam as jogadas no mesmo tabuleiro.\n"
                + "• Duelo: cada jogador joga o mesmo tabuleiro, um depois do outro; vence quem terminar primeiro.\n"
                + "• Desafio diário: todo mundo recebe o mesmo tabuleiro no mesmo dia.\n\n"
                + "AJUDA\n"
                + "• Botão Dica: revela uma célula segura (3 por partida).\n"
                + "• O contador de minas fica vermelho quando há mais bandeiras do que minas.\n"
                + "• Estatísticas, recordes, ranking, conquistas e histórico ficam salvos por perfil.";
        JTextArea area = new JTextArea(texto);
        area.setFont(fonte(Font.PLAIN, 14));
        area.setForeground(tema.getTextoPrincipal());
        area.setBackground(tema.getFundoClaro());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JScrollPane rolagem = new JScrollPane(area);
        rolagem.setBorder(BorderFactory.createLineBorder(tema.getBorda()));
        rolagem.setPreferredSize(new Dimension((int) (720 * escala), (int) (440 * escala)));
        painel.add(rolagem, BorderLayout.CENTER);

        JPanel rodape = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.CENTER));
        rodape.add(EstiloUI.botaoDestaque("← Voltar", tema, escala, e -> mostrarTelaInicial()));
        painel.add(rodape, BorderLayout.SOUTH);
        raiz.add(painel, BorderLayout.CENTER);
        finalizarMontagem(true);
        SwingUtilities.invokeLater(() -> area.setCaretPosition(0));
    }

    public void iniciarTelaDeJogo(LeituraTabuleiro novaLeitura, String titulo, boolean mostrarMinas) {
        leitura = novaLeitura;
        tituloPartida = titulo;
        minasVisiveis = mostrarMinas;
        textoTempo = "00:00";
        tempoEmAlerta = false;
        minasRestantes = novaLeitura.getNumMinas();
        celulasReveladas = 0;
        totalSeguras = Math.max(1, novaLeitura.getTotalSeguras());
        jogadas = 0;
        vidasRestantes = novaLeitura.getVidasRestantes();
        vidasTotais = novaLeitura.getVidasRestantes();
        jogadorDaVez = null;
        textoStatus = "Boa sorte!";
        tipoStatus = TipoMensagem.NEUTRA;
        fimDeJogoVisivel = false;
        podeReplay = false;
        proximoDuelo = false;
        modoReplay = false;
        boolean mesmaTela = telaAtual == Tela.JOGO && painelTabuleiro != null
                && painelTabuleiro.getLinhas() == novaLeitura.getLinhas()
                && painelTabuleiro.getColunas() == novaLeitura.getColunas();
        telaAtual = Tela.JOGO;
        montarTelaDeJogo(!mesmaTela);
        painelTabuleiro.redesenharTudo();
    }

    private void montarTelaDeJogo(boolean redimensionar) {
        prepararRaiz();
        telaAtual = Tela.JOGO;
        raiz.add(criarBarraSuperior(), BorderLayout.NORTH);

        painelPrincipal = EstiloUI.painel(tema.getFundo(), new BorderLayout(12, 0));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        painelTabuleiro = new PainelTabuleiro(leitura, ouvinte, tema, skinBandeira, skinCelula, dados.isModoDaltonico(),
                minasVisiveis, zoom, this::alterarZoomPor);
        painelTabuleiro.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evento) {
                ajustarZoomAoTamanho();
            }
        });
        painelTabuleiro.setInteracaoHabilitada(!modoReplay);
        painelPrincipal.add(painelTabuleiro, BorderLayout.CENTER);
        painelPrincipal.add(criarPainelLateral(), BorderLayout.EAST);
        raiz.add(painelPrincipal, BorderLayout.CENTER);
        instalarAtalhosDeJogo();
        finalizarMontagem(redimensionar);
    }

    private void ajustarZoomAoTamanho() {
        if (ignorarRedimensionamento || painelTabuleiro == null) {
            return;
        }
        double cabe = painelTabuleiro.calcularZoomQueCabe();
        double novo = Math.max(0.6, Math.min(2.0, Math.round(cabe * 20) / 20.0));
        if (Math.abs(novo - zoom) >= 0.05) {
            zoom = novo;
            painelTabuleiro.setZoom(zoom);
        }
    }

    private JPanel criarBarraSuperior() {
        JPanel barra = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 6));
        barra.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        JPanel linhaBotoes = EstiloUI.painelTransparente(new BorderLayout(10, 0));
        barra.add(linhaBotoes, BorderLayout.NORTH);

        JPanel esquerda = EstiloUI.painelTransparente(new FlowLayout(FlowLayout.LEFT, 6, 0));
        esquerda.add(EstiloUI.botao("← Início", tema, escala, e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirNovoJogo();
            }
        }));
        botaoDica = EstiloUI.botao("💡 Dica", tema, escala, e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirDica();
            }
        });
        esquerda.add(botaoDica);
        linhaBotoes.add(esquerda, BorderLayout.WEST);

        labelStatus = new JLabel(textoStatus, SwingConstants.CENTER);
        labelStatus.setOpaque(true);
        labelStatus.setBackground(tema.getFundoClaro());
        labelStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.getBorda()),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        barra.add(labelStatus, BorderLayout.SOUTH);

        JPanel direita = EstiloUI.painelTransparente(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton menos = EstiloUI.botao("−", tema, escala, e -> alterarZoomPor(-PASSO_ZOOM));
        menos.setToolTipText("Diminuir zoom (−)");
        JButton mais = EstiloUI.botao("+", tema, escala, e -> alterarZoomPor(PASSO_ZOOM));
        mais.setToolTipText("Aumentar zoom (+)");
        JButton cheia = EstiloUI.botao("⛶", tema, escala, e -> alternarTelaCheia());
        cheia.setToolTipText("Tela cheia (F11)");
        botaoSom = EstiloUI.botao(dados.isSomAtivo() ? "🔊" : "🔇", tema, escala, e -> {
            if (ouvinte != null) {
                ouvinte.aoAlternarSom();
            }
        });
        botaoSom.setToolTipText("Ligar/desligar efeitos sonoros");
        JButton configuracoes = EstiloUI.botao("⚙", tema, escala, e -> abrirConfiguracoes());
        configuracoes.setToolTipText("Configurações");
        direita.add(menos);
        direita.add(mais);
        direita.add(cheia);
        direita.add(botaoSom);
        direita.add(configuracoes);
        linhaBotoes.add(direita, BorderLayout.EAST);
        aplicarStatus();
        return barra;
    }

    private JPanel criarPainelLateral() {
        JPanel painel = EstiloUI.painel(tema.getFundoClaro(), null);
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.getBorda(), 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        painel.setPreferredSize(new Dimension((int) (210 * Math.max(1.0, escala)), 0));

        JLabel titulo = rotulo("<html><center>" + tituloPartida.replace(" · ", "<br>") + "</center></html>",
                tema.getDestaque(), Font.BOLD, 15);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(14));

        JPanel grade = EstiloUI.painelTransparente(new GridLayout(0, 2, 8, 10));
        grade.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel itemTempo = criarItem("⏱ Tempo", textoTempo);
        lblTempo = valorDe(itemTempo);
        grade.add(itemTempo);
        JPanel itemMinas = criarItem(EMOJI_BOMBA + " Minas", String.valueOf(minasRestantes));
        lblMinasRestantes = valorDe(itemMinas);
        grade.add(itemMinas);
        JPanel itemReveladas = criarItem("✔ Reveladas", celulasReveladas + "/" + totalSeguras);
        lblCelulasReveladas = valorDe(itemReveladas);
        grade.add(itemReveladas);
        JPanel itemJogadas = criarItem("👆 Jogadas", String.valueOf(jogadas));
        lblJogadas = valorDe(itemJogadas);
        grade.add(itemJogadas);
        JPanel itemDicas = criarItem("💡 Dicas", dicasRestantes + "/" + dicasTotais);
        lblDicas = valorDe(itemDicas);
        grade.add(itemDicas);
        JPanel itemRecorde = criarItem("🏅 Recorde", textoRecorde);
        lblRecorde = valorDe(itemRecorde);
        grade.add(itemRecorde);
        painel.add(grade);
        painel.add(Box.createVerticalStrut(10));

        itemVidas = criarItem("Vidas", "");
        lblVidas = valorDe(itemVidas);
        lblVidas.setFont(EstiloUI.fonteEmoji(Font.PLAIN, (float) (18 * escala)));
        painel.add(itemVidas);
        itemJogador = criarItem("👥 Vez de", "");
        lblJogadorDaVez = valorDe(itemJogador);
        painel.add(itemJogador);
        painel.add(Box.createVerticalStrut(10));

        painel.add(rotulo("Progresso", tema.getTextoSecundario(), Font.PLAIN, 12));
        barraProgresso = new JProgressBar(0, totalSeguras);
        barraProgresso.setStringPainted(true);
        barraProgresso.setFont(fonte(Font.BOLD, 11));
        barraProgresso.setBackground(tema.getFundo());
        barraProgresso.setForeground(tema.getDestaque());
        barraProgresso.setBorder(BorderFactory.createLineBorder(tema.getBorda()));
        barraProgresso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        barraProgresso.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(barraProgresso);
        painel.add(Box.createVerticalStrut(14));

        painelFimDeJogo = EstiloUI.painelTransparente(new GridLayout(0, 1, 0, 6));
        painelFimDeJogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(painelFimDeJogo);
        painel.add(Box.createVerticalGlue());

        painel.add(rotulo("Tema", tema.getTextoSecundario(), Font.PLAIN, 11));
        JComboBox<String> comboTema = criarComboTema();
        comboTema.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboTema.getPreferredSize().height));
        comboTema.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(comboTema);
        painel.add(Box.createVerticalStrut(8));
        String legenda = skinCelula.usaEmoji() ? skinCelula.legenda() : "Setas + Espaço/F/C/H no teclado";
        JLabel rotuloLegenda = rotulo("<html><center>" + legenda + "</center></html>", tema.getTextoSecundario(), Font.PLAIN, 11);
        painel.add(rotuloLegenda);
        return painel;
    }

    private JPanel criarItem(String titulo, String valor) {
        JPanel item = EstiloUI.painelTransparente(null);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setAlignmentX(Component.CENTER_ALIGNMENT);
        item.add(rotulo(titulo, tema.getTextoSecundario(), Font.PLAIN, 11));
        JLabel rotuloValor = rotulo(valor, tema.getTextoPrincipal(), Font.BOLD, 17);
        rotuloValor.setFont(tema.fonte(Font.BOLD, (float) (17 * escala)));
        item.add(rotuloValor);
        item.putClientProperty("valor", rotuloValor);
        return item;
    }

    private JLabel valorDe(JPanel item) {
        return (JLabel) item.getClientProperty("valor");
    }

    private void restaurarEstadoDoJogo() {
        atualizarTempo(textoTempo, tempoEmAlerta);
        atualizarEstatisticas(minasRestantes, celulasReveladas, totalSeguras, jogadas);
        atualizarVidas(vidasRestantes, vidasTotais);
        atualizarDicas(dicasRestantes, dicasTotais);
        atualizarJogadorDaVez(jogadorDaVez);
        atualizarRecorde(textoRecorde);
        aplicarStatus();
        if (modoReplay) {
            setModoReplay(true);
        } else if (fimDeJogoVisivel) {
            mostrarOpcoesFimDeJogo(podeReplay, proximoDuelo);
        }
    }

    public void atualizarCelula(int linha, int coluna, LeituraTabuleiro atual) {
        if (painelTabuleiro != null && atual == leitura) {
            painelTabuleiro.atualizarCelula(linha, coluna);
        }
    }

    public void redesenharTabuleiro() {
        if (painelTabuleiro != null) {
            painelTabuleiro.redesenharTudo();
        }
    }

    public void atualizarTempo(String texto, boolean alerta) {
        textoTempo = texto;
        tempoEmAlerta = alerta;
        if (lblTempo != null) {
            lblTempo.setText(texto);
            lblTempo.setForeground(alerta ? tema.getMina() : tema.getTextoPrincipal());
        }
    }

    public void atualizarEstatisticas(int restantes, int reveladas, int total, int quantidadeJogadas) {
        minasRestantes = restantes;
        celulasReveladas = reveladas;
        totalSeguras = Math.max(1, total);
        jogadas = quantidadeJogadas;
        if (lblMinasRestantes == null) {
            return;
        }
        lblMinasRestantes.setText(String.valueOf(restantes));
        if (restantes < 0) {
            lblMinasRestantes.setForeground(tema.getMina());
            lblMinasRestantes.setToolTipText("Há " + (-restantes) + " bandeira(s) a mais do que minas!");
        } else {
            lblMinasRestantes.setForeground(tema.getTextoPrincipal());
            lblMinasRestantes.setToolTipText(null);
        }
        lblCelulasReveladas.setText(reveladas + "/" + total);
        lblJogadas.setText(String.valueOf(quantidadeJogadas));
        int progresso = (int) (reveladas * 100.0 / totalSeguras);
        barraProgresso.setMaximum(totalSeguras);
        barraProgresso.setValue(reveladas);
        barraProgresso.setString(progresso + "%");
        if (progresso < 30) {
            barraProgresso.setForeground(tema.getMina());
        } else if (progresso < 70) {
            barraProgresso.setForeground(tema.getBandeira());
        } else {
            barraProgresso.setForeground(tema.getVitoria());
        }
    }

    public void atualizarVidas(int restantes, int total) {
        vidasRestantes = restantes;
        vidasTotais = total;
        if (itemVidas == null) {
            return;
        }
        itemVidas.setVisible(total > 1);
        StringBuilder coracoes = new StringBuilder();
        for (int i = 0; i < total; i++) {
            coracoes.append(i < restantes ? "❤" : "♡");
        }
        lblVidas.setText(coracoes.toString());
        lblVidas.setForeground(tema.getMina());
    }

    public void atualizarDicas(int restantes, int total) {
        dicasRestantes = restantes;
        dicasTotais = total;
        if (lblDicas != null) {
            lblDicas.setText(restantes + "/" + total);
        }
        if (botaoDica != null) {
            botaoDica.setText("💡 Dica (" + restantes + ")");
            botaoDica.setEnabled(restantes > 0 && !modoReplay && !fimDeJogoVisivel);
        }
    }

    public void atualizarJogadorDaVez(String nome) {
        jogadorDaVez = nome;
        if (itemJogador == null) {
            return;
        }
        itemJogador.setVisible(nome != null);
        lblJogadorDaVez.setText(nome == null ? "" : nome);
        lblJogadorDaVez.setForeground(tema.getDestaque());
    }

    public void atualizarRecorde(String texto) {
        textoRecorde = texto;
        if (lblRecorde != null) {
            lblRecorde.setText(texto);
        }
    }

    public void mostrarStatus(String texto, TipoMensagem tipo) {
        textoStatus = texto;
        tipoStatus = tipo;
        aplicarStatus();
    }

    private void aplicarStatus() {
        if (labelStatus == null) {
            return;
        }
        labelStatus.setText(textoStatus);
        labelStatus.setFont(EstiloUI.fonteParaTexto(textoStatus, tema, escala, Font.BOLD, 15));
        Color cor;
        if (tipoStatus == TipoMensagem.ALERTA) {
            cor = tema.getAlerta();
        } else if (tipoStatus == TipoMensagem.SUCESSO) {
            cor = tema.getVitoria();
        } else if (tipoStatus == TipoMensagem.ERRO) {
            cor = tema.getMina();
        } else {
            cor = tema.getTextoSecundario();
        }
        labelStatus.setForeground(cor);
    }

    public void mostrarVitoria(String texto) {
        mostrarStatus(texto, TipoMensagem.SUCESSO);
    }

    public void mostrarDerrota(String texto) {
        mostrarStatus(texto, TipoMensagem.ERRO);
    }

    public void piscarFundoDeExplosao(boolean explodindo) {
        Color cor = explodindo ? tema.getMinaFundo() : tema.getFundo();
        raiz.setBackground(cor);
        if (painelPrincipal != null) {
            painelPrincipal.setBackground(cor);
        }
        if (painelTabuleiro != null) {
            painelTabuleiro.setBackground(cor);
        }
        raiz.repaint();
    }

    public void tremerTela() {
        if (timerTremor != null) {
            timerTremor.stop();
        }
        Random aleatorio = new Random();
        int[] quadro = {0};
        int totalQuadros = 18;
        timerTremor = new Timer(16, null);
        timerTremor.addActionListener(e -> {
            quadro[0]++;
            int amplitude = (int) Math.round(MARGEM * (1 - quadro[0] / (double) totalQuadros));
            if (quadro[0] >= totalQuadros || amplitude <= 0) {
                timerTremor.stop();
                raiz.setBorder(new EmptyBorder(MARGEM, MARGEM, MARGEM, MARGEM));
            } else {
                int dx = aleatorio.nextInt(amplitude * 2 + 1) - amplitude;
                int dy = aleatorio.nextInt(amplitude * 2 + 1) - amplitude;
                raiz.setBorder(new EmptyBorder(MARGEM + dy, MARGEM + dx, MARGEM - dy, MARGEM - dx));
            }
            raiz.revalidate();
            raiz.repaint();
        });
        timerTremor.start();
    }

    public void celebrarVitoria() {
        Color[] cores = {tema.getDestaque(), tema.getVitoria(), tema.getBandeira(), tema.getMina(),
                tema.getCorNumero(1), tema.getCorNumero(2), tema.getCorNumero(4), Color.WHITE};
        painelConfete.iniciar(cores);
    }

    public void revelarMinaFinal(int linha, int coluna) {
        if (painelTabuleiro != null) {
            painelTabuleiro.revelarMinaFinal(linha, coluna);
        }
    }

    public void marcarBandeiraErrada(int linha, int coluna) {
        if (painelTabuleiro != null) {
            painelTabuleiro.marcarBandeiraErrada(linha, coluna);
        }
    }

    public void destacarCelulaVencedora(int linha, int coluna) {
        if (painelTabuleiro != null) {
            painelTabuleiro.destacarCelulaVencedora(linha, coluna);
        }
    }

    public void sinalizarMinaNaVitoria(int linha, int coluna) {
        if (painelTabuleiro != null) {
            painelTabuleiro.sinalizarMinaNaVitoria(linha, coluna);
        }
    }

    public void destacarDica(int linha, int coluna) {
        if (painelTabuleiro != null) {
            painelTabuleiro.destacarCelula(linha, coluna);
        }
    }

    public void destacarJogadaReplay(int linha, int coluna) {
        if (painelTabuleiro != null) {
            painelTabuleiro.posicionarCursor(linha, coluna, true);
            painelTabuleiro.destacarCelula(linha, coluna);
        }
    }

    public void mostrarOpcoesFimDeJogo(boolean permitirReplay, boolean haProximoDuelo) {
        fimDeJogoVisivel = true;
        podeReplay = permitirReplay;
        proximoDuelo = haProximoDuelo;
        if (painelFimDeJogo == null) {
            return;
        }
        painelFimDeJogo.removeAll();
        if (haProximoDuelo) {
            painelFimDeJogo.add(EstiloUI.botaoDestaque("👥 Vez do Jogador 2", tema, escala, e -> {
                if (ouvinte != null) {
                    ouvinte.aoIniciarSegundoJogadorDuelo();
                }
            }));
        }
        if (permitirReplay) {
            painelFimDeJogo.add(EstiloUI.botao("▶ Assistir replay", tema, escala, e -> {
                if (ouvinte != null) {
                    ouvinte.aoPedirReplay();
                }
            }));
        }
        painelFimDeJogo.add(EstiloUI.botao("📤 Compartilhar", tema, escala, e -> {
            if (ouvinte != null) {
                ouvinte.aoPedirCompartilhar();
            }
        }));
        if (!haProximoDuelo) {
            painelFimDeJogo.add(EstiloUI.botaoDestaque("🔁 Jogar de novo", tema, escala, e -> {
                if (ouvinte != null) {
                    ouvinte.aoJogarNovamente();
                }
            }));
        }
        if (botaoDica != null) {
            botaoDica.setEnabled(false);
        }
        painelFimDeJogo.revalidate();
        painelFimDeJogo.repaint();
        garantirAlturaDoConteudo();
    }

    private void garantirAlturaDoConteudo() {
        if (telaCheia || (getExtendedState() & MAXIMIZED_BOTH) != 0) {
            return;
        }
        int alturaNecessaria = getPreferredSize().height;
        if (alturaNecessaria > getHeight()) {
            ignorarRedimensionamento = true;
            setSize(getWidth(), alturaNecessaria);
            limitarAoEspacoDaTela();
            SwingUtilities.invokeLater(() -> ignorarRedimensionamento = false);
        }
    }

    public void setModoReplay(boolean ativo) {
        modoReplay = ativo;
        if (painelTabuleiro != null) {
            painelTabuleiro.setInteracaoHabilitada(!ativo);
        }
        if (botaoDica != null) {
            botaoDica.setEnabled(!ativo && dicasRestantes > 0 && !fimDeJogoVisivel);
        }
        if (painelFimDeJogo == null) {
            return;
        }
        painelFimDeJogo.removeAll();
        if (ativo) {
            fimDeJogoVisivel = false;
            painelFimDeJogo.add(EstiloUI.botaoDestaque("⏹ Parar replay", tema, escala, e -> {
                if (ouvinte != null) {
                    ouvinte.aoPararReplay();
                }
            }));
        }
        painelFimDeJogo.revalidate();
        painelFimDeJogo.repaint();
    }

    public void atualizarEstadoSom(boolean ativo) {
        dados.setSomAtivo(ativo);
        if (botaoSom != null) {
            botaoSom.setText(ativo ? "🔊" : "🔇");
        }
        mostrarNotificacao(ativo ? "🔊 Som ligado" : "🔇 Som desligado");
    }

    public void mostrarMensagem(String titulo, String texto) {
        JOptionPane.showMessageDialog(this, texto, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public void compartilharTexto(String texto) {
        boolean copiado;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
            copiado = true;
        } catch (RuntimeException erro) {
            copiado = false;
        }
        JTextArea area = new JTextArea(texto, 3, 36);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(EstiloUI.fonteEmoji(Font.PLAIN, 14));
        JPanel painel = new JPanel(new BorderLayout(0, 8));
        painel.add(new JLabel(copiado ? "Texto copiado para a área de transferência:" : "Copie o texto abaixo:"),
                BorderLayout.NORTH);
        painel.add(new JScrollPane(area), BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, painel, "Compartilhar resultado", JOptionPane.PLAIN_MESSAGE);
    }

    public void mostrarEstatisticas(ResumoEstatisticas resumo) {
        new DialogoEstatisticas(this, resumo, tema, escala, ouvinte).setVisible(true);
    }

    public void mostrarNotificacao(String texto) {
        filaNotificacoes.addLast(texto);
        if (!notificacaoAtiva) {
            exibirProximaNotificacao();
        }
    }

    private void exibirProximaNotificacao() {
        String texto = filaNotificacoes.pollFirst();
        if (texto == null) {
            notificacaoAtiva = false;
            return;
        }
        notificacaoAtiva = true;
        JLabel aviso = new JLabel(texto, SwingConstants.CENTER);
        aviso.setFont(EstiloUI.fonteParaTexto(texto, tema, escala, Font.BOLD, 13));
        aviso.setForeground(tema.getTextoPrincipal());
        aviso.setBackground(tema.getCard());
        aviso.setOpaque(true);
        aviso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.getDestaque(), 2),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        Dimension tamanho = aviso.getPreferredSize();
        JLayeredPane camadas = getLayeredPane();
        int largura = Math.min(tamanho.width, Math.max(200, camadas.getWidth() - 40));
        aviso.setBounds((camadas.getWidth() - largura) / 2, camadas.getHeight() - tamanho.height - 28, largura, tamanho.height);
        camadas.add(aviso, JLayeredPane.POPUP_LAYER);
        camadas.repaint();
        Timer remover = new Timer(2600, e -> {
            camadas.remove(aviso);
            camadas.repaint();
            exibirProximaNotificacao();
        });
        remover.setRepeats(false);
        remover.start();
    }

    private void alterarZoomPor(double delta) {
        double novo = Math.max(0.6, Math.min(2.0, Math.round((zoom + delta) * 10) / 10.0));
        if (Math.abs(novo - zoom) < 0.001 || painelTabuleiro == null) {
            return;
        }
        zoom = novo;
        dados.setZoom(zoom);
        ignorarRedimensionamento = true;
        painelTabuleiro.setZoom(zoom);
        if (!telaCheia && (getExtendedState() & MAXIMIZED_BOTH) == 0) {
            pack();
            limitarAoEspacoDaTela();
        } else {
            raiz.revalidate();
        }
        SwingUtilities.invokeLater(() -> ignorarRedimensionamento = false);
        if (ouvinte != null) {
            ouvinte.aoAlterarZoom(zoom);
        }
        mostrarStatusTemporarioDeZoom();
    }

    private void mostrarStatusTemporarioDeZoom() {
        if (labelStatus != null) {
            String anterior = textoStatus;
            TipoMensagem tipoAnterior = tipoStatus;
            mostrarStatus("🔍 Zoom " + Math.round(zoom * 100) + "%", TipoMensagem.NEUTRA);
            Timer restaurar = new Timer(900, e -> {
                if (textoStatus.startsWith("🔍")) {
                    mostrarStatus(anterior, tipoAnterior);
                }
            });
            restaurar.setRepeats(false);
            restaurar.start();
        }
    }

    private void alternarTelaCheia() {
        boolean entrando = !telaCheia;
        if (entrando) {
            limitesAntesDaTelaCheia = getBounds();
        }
        ignorarRedimensionamento = !entrando;
        dispose();
        setUndecorated(entrando);
        telaCheia = entrando;
        if (entrando) {
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            setExtendedState(NORMAL);
            if (limitesAntesDaTelaCheia != null) {
                setBounds(limitesAntesDaTelaCheia);
            }
            zoom = dados.getZoom();
            if (painelTabuleiro != null) {
                painelTabuleiro.setZoom(zoom);
            }
        }
        setVisible(true);
        if (telaAtual == Tela.INICIAL) {
            mostrarTelaInicial();
        }
        SwingUtilities.invokeLater(() -> ignorarRedimensionamento = false);
    }

    private void instalarAtalhosGlobais() {
        InputMap mapa = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acoes = getRootPane().getActionMap();
        mapa.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "telaCheia");
        acoes.put("telaCheia", acao(this::alternarTelaCheia));
        mapa.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "sairTelaCheia");
        acoes.put("sairTelaCheia", acao(() -> {
            if (telaCheia) {
                alternarTelaCheia();
            }
        }));
    }

    private static final String PREFIXO_ATALHO = "jogo.";

    private void limparAtalhosDeJogo() {
        InputMap mapa = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke[] teclas = mapa.keys();
        if (teclas == null) {
            return;
        }
        for (KeyStroke tecla : teclas) {
            Object nome = mapa.get(tecla);
            if (nome != null && nome.toString().startsWith(PREFIXO_ATALHO)) {
                mapa.remove(tecla);
            }
        }
    }

    private void instalarAtalhosDeJogo() {
        registrarAtalho(KeyEvent.VK_UP, "cima", () -> moverCursor(-1, 0));
        registrarAtalho(KeyEvent.VK_DOWN, "baixo", () -> moverCursor(1, 0));
        registrarAtalho(KeyEvent.VK_LEFT, "esquerda", () -> moverCursor(0, -1));
        registrarAtalho(KeyEvent.VK_RIGHT, "direita", () -> moverCursor(0, 1));
        registrarAtalho(dados.getTecla(DadosConfiguracao.ACAO_REVELAR), "revelar", this::revelarNoCursor);
        registrarAtalho(KeyEvent.VK_ENTER, "revelarEnter", this::revelarNoCursor);
        registrarAtalho(dados.getTecla(DadosConfiguracao.ACAO_MARCAR), "marcar", () -> {
            if (cursorPronto()) {
                ouvinte.aoMarcarCelula(painelTabuleiro.getLinhaCursor(), painelTabuleiro.getColunaCursor());
            }
        });
        registrarAtalho(dados.getTecla(DadosConfiguracao.ACAO_CHORDING), "chording", () -> {
            if (cursorPronto()) {
                ouvinte.aoFazerChording(painelTabuleiro.getLinhaCursor(), painelTabuleiro.getColunaCursor());
            }
        });
        registrarAtalho(dados.getTecla(DadosConfiguracao.ACAO_DICA), "dica", () -> {
            if (ouvinte != null && !modoReplay && painelTabuleiro != null) {
                ouvinte.aoPedirDica();
            }
        });
        registrarAtalho(KeyEvent.VK_EQUALS, "zoomMais", () -> alterarZoomPor(PASSO_ZOOM));
        registrarAtalho(KeyEvent.VK_PLUS, "zoomMais2", () -> alterarZoomPor(PASSO_ZOOM));
        registrarAtalho(KeyEvent.VK_ADD, "zoomMais3", () -> alterarZoomPor(PASSO_ZOOM));
        registrarAtalho(KeyEvent.VK_MINUS, "zoomMenos", () -> alterarZoomPor(-PASSO_ZOOM));
        registrarAtalho(KeyEvent.VK_SUBTRACT, "zoomMenos2", () -> alterarZoomPor(-PASSO_ZOOM));
    }

    private void registrarAtalho(int codigo, String nome, Runnable execucao) {
        if (codigo == KeyEvent.VK_UNDEFINED) {
            return;
        }
        String chave = PREFIXO_ATALHO + nome;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(codigo, 0), chave);
        getRootPane().getActionMap().put(chave, acao(execucao));
    }

    private AbstractAction acao(Runnable execucao) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                execucao.run();
            }
        };
    }

    private boolean cursorPronto() {
        if (painelTabuleiro == null || ouvinte == null || modoReplay) {
            return false;
        }
        if (!painelTabuleiro.isCursorVisivel()) {
            painelTabuleiro.posicionarCursor(painelTabuleiro.getLinhaCursor(), painelTabuleiro.getColunaCursor(), true);
            return false;
        }
        return true;
    }

    private void moverCursor(int deltaLinha, int deltaColuna) {
        if (painelTabuleiro == null) {
            return;
        }
        if (!painelTabuleiro.isCursorVisivel()) {
            painelTabuleiro.posicionarCursor(painelTabuleiro.getLinhaCursor(), painelTabuleiro.getColunaCursor(), true);
            return;
        }
        painelTabuleiro.moverCursor(deltaLinha, deltaColuna);
    }

    private void revelarNoCursor() {
        if (!cursorPronto()) {
            return;
        }
        int linha = painelTabuleiro.getLinhaCursor();
        int coluna = painelTabuleiro.getColunaCursor();
        if (leitura != null && leitura.isRevelada(linha, coluna)) {
            ouvinte.aoFazerChording(linha, coluna);
        } else {
            ouvinte.aoRevelarCelula(linha, coluna);
        }
    }
}
