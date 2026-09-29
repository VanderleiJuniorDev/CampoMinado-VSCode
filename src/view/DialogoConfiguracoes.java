package view;

import controller.DadosConfiguracao;
import view.tema.CatalogoTemas;
import view.tema.SkinBandeira;
import view.tema.SkinCelula;
import view.tema.TemaVisual;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class DialogoConfiguracoes extends JDialog {

    private final TemaVisual tema;
    private final double escala;
    private final DadosConfiguracao dados;
    private DadosConfiguracao resultado;

    private final JComboBox<String> comboTema;
    private final JComboBox<String> comboBandeira;
    private final JComboBox<String> comboCelula;
    private final JSlider sliderFonte;
    private final JSlider sliderZoom;
    private final JSpinner spinnerDensidade;
    private final JCheckBox caixaInterrogacao;
    private final JCheckBox caixaDaltonico;
    private final JCheckBox caixaSom;
    private final JCheckBox caixaMusica;
    private final JSlider sliderVolume;
    private final Map<String, JButton> botoesTeclas = new LinkedHashMap<>();
    private final Map<String, Integer> teclas = new LinkedHashMap<>();
    private KeyEventDispatcher capturaAtual;

    private DialogoConfiguracoes(JFrame pai, DadosConfiguracao atual, TemaVisual tema, double escala) {
        super(pai, "Configurações", true);
        this.tema = tema;
        this.escala = escala;
        this.dados = atual.copiar();
        this.teclas.putAll(dados.getTeclas());

        JPanel formulario = EstiloUI.painel(tema.getFundo(), new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        int linha = 0;

        linha = secao(formulario, "🎨 Aparência", linha);
        comboTema = EstiloUI.combo(CatalogoTemas.nomes(), dados.getTema(), tema, escala);
        linha = campo(formulario, "Tema", comboTema, linha);
        comboBandeira = EstiloUI.combo(SkinBandeira.nomes(), SkinBandeira.porNome(dados.getSkinBandeira()).getNome(), tema, escala);
        linha = campo(formulario, "Skin da bandeira", comboBandeira, linha);
        comboCelula = EstiloUI.combo(SkinCelula.nomes(), SkinCelula.porNome(dados.getSkinCelula()).getNome(), tema, escala);
        linha = campo(formulario, "Símbolos das células", comboCelula, linha);
        sliderFonte = slider(80, 160, (int) Math.round(dados.getEscalaFonte() * 100));
        linha = campo(formulario, "Tamanho da fonte (%)", sliderFonte, linha);
        sliderZoom = slider(60, 200, (int) Math.round(dados.getZoom() * 100));
        linha = campo(formulario, "Zoom do tabuleiro (%)", sliderZoom, linha);

        linha = secao(formulario, "♿ Jogo e acessibilidade", linha);
        spinnerDensidade = new JSpinner(new SpinnerNumberModel(dados.getDensidadeMinas(), 5, 40, 1));
        EstiloUI.confirmarAoDigitar(spinnerDensidade);
        linha = campo(formulario, "Densidade de minas padrão (%)", spinnerDensidade, linha);
        caixaInterrogacao = EstiloUI.caixa("Permitir marcação com ?", dados.isInterrogacaoAtiva(), tema, escala);
        linha = campo(formulario, "Marcação", caixaInterrogacao, linha);
        caixaDaltonico = EstiloUI.caixa("Símbolos extras nos números", dados.isModoDaltonico(), tema, escala);
        linha = campo(formulario, "Modo daltônico", caixaDaltonico, linha);

        linha = secao(formulario, "🔊 Som", linha);
        caixaSom = EstiloUI.caixa("Efeitos sonoros", dados.isSomAtivo(), tema, escala);
        linha = campo(formulario, "Efeitos", caixaSom, linha);
        caixaMusica = EstiloUI.caixa("Música ambiente", dados.isMusicaAtiva(), tema, escala);
        linha = campo(formulario, "Música", caixaMusica, linha);
        sliderVolume = slider(0, 100, dados.getVolumeMusica());
        linha = campo(formulario, "Volume da música", sliderVolume, linha);

        linha = secao(formulario, "⌨ Atalhos de teclado", linha);
        for (Map.Entry<String, Integer> tecla : teclas.entrySet()) {
            String acao = tecla.getKey();
            JButton botao = EstiloUI.botao(KeyEvent.getKeyText(tecla.getValue()), tema, escala, null);
            botao.addActionListener(e -> capturarTecla(acao));
            botoesTeclas.put(acao, botao);
            linha = campo(formulario, DadosConfiguracao.descreverAcao(acao), botao, linha);
        }
        JLabel observacao = EstiloUI.rotulo("Setas movem o cursor · Ctrl + roda do mouse ou +/− ajustam o zoom · F11 tela cheia",
                tema.getTextoSecundario(), EstiloUI.fonte(tema, escala, Font.ITALIC, 11));
        GridBagConstraints restricao = restricao(0, linha);
        restricao.gridwidth = 2;
        formulario.add(observacao, restricao);

        JScrollPane rolagem = new JScrollPane(formulario);
        rolagem.setBorder(null);
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        rolagem.getViewport().setBackground(tema.getFundo());

        JPanel rodape = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.RIGHT, 8, 10));
        rodape.add(EstiloUI.botao("Restaurar padrões", tema, escala, e -> restaurarPadroes()));
        rodape.add(EstiloUI.botao("Cancelar", tema, escala, e -> fechar()));
        rodape.add(EstiloUI.botaoDestaque("Salvar", tema, escala, e -> salvar()));

        JPanel conteudo = EstiloUI.painel(tema.getFundo(), new BorderLayout());
        conteudo.add(rolagem, BorderLayout.CENTER);
        conteudo.add(rodape, BorderLayout.SOUTH);
        setContentPane(conteudo);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                cancelarCaptura();
            }

            @Override
            public void windowClosed(WindowEvent evento) {
                cancelarCaptura();
            }
        });
        pack();
        Dimension tamanho = getSize();
        setSize(new Dimension(Math.max(tamanho.width + 20, 520), Math.min(tamanho.height, 680)));
        setLocationRelativeTo(pai);
    }

    private JSlider slider(int minimo, int maximo, int valor) {
        JSlider slider = new JSlider(minimo, maximo, Math.max(minimo, Math.min(maximo, valor)));
        slider.setOpaque(false);
        slider.setForeground(tema.getTextoPrincipal());
        slider.setMajorTickSpacing((maximo - minimo) / 4);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setFont(EstiloUI.fonte(tema, escala, Font.PLAIN, 10));
        slider.setFocusable(false);
        return slider;
    }

    private int secao(JPanel formulario, String titulo, int linha) {
        GridBagConstraints restricao = restricao(0, linha);
        restricao.gridwidth = 2;
        restricao.insets = new Insets(linha == 0 ? 0 : 14, 0, 6, 0);
        formulario.add(EstiloUI.rotulo(titulo, tema.getDestaque(), EstiloUI.fonte(tema, escala, Font.BOLD, 15)), restricao);
        return linha + 1;
    }

    private int campo(JPanel formulario, String rotulo, JComponent componente, int linha) {
        formulario.add(EstiloUI.rotulo(rotulo, tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.PLAIN, 13)),
                restricao(0, linha));
        GridBagConstraints restricao = restricao(1, linha);
        restricao.fill = GridBagConstraints.HORIZONTAL;
        restricao.weightx = 1;
        formulario.add(componente, restricao);
        return linha + 1;
    }

    private GridBagConstraints restricao(int coluna, int linha) {
        GridBagConstraints restricao = new GridBagConstraints();
        restricao.gridx = coluna;
        restricao.gridy = linha;
        restricao.anchor = GridBagConstraints.WEST;
        restricao.insets = new Insets(4, 0, 4, 14);
        return restricao;
    }

    private void capturarTecla(String acao) {
        cancelarCaptura();
        JButton botao = botoesTeclas.get(acao);
        botao.setText("Pressione uma tecla...");
        capturaAtual = evento -> {
            if (evento.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            int codigo = evento.getKeyCode();
            if (codigo != KeyEvent.VK_ESCAPE && !ehTeclaReservada(codigo)) {
                for (Map.Entry<String, Integer> outra : teclas.entrySet()) {
                    if (outra.getValue() == codigo && !outra.getKey().equals(acao)) {
                        teclas.put(outra.getKey(), teclas.get(acao));
                        botoesTeclas.get(outra.getKey()).setText(KeyEvent.getKeyText(teclas.get(acao)));
                    }
                }
                teclas.put(acao, codigo);
            }
            botao.setText(KeyEvent.getKeyText(teclas.get(acao)));
            cancelarCaptura();
            return true;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(capturaAtual);
    }

    private boolean ehTeclaReservada(int codigo) {
        return codigo == KeyEvent.VK_SHIFT || codigo == KeyEvent.VK_CONTROL || codigo == KeyEvent.VK_ALT
                || codigo == KeyEvent.VK_ALT_GRAPH || codigo == KeyEvent.VK_META || codigo == KeyEvent.VK_WINDOWS
                || codigo == KeyEvent.VK_UNDEFINED
                || codigo == KeyEvent.VK_UP || codigo == KeyEvent.VK_DOWN || codigo == KeyEvent.VK_LEFT
                || codigo == KeyEvent.VK_RIGHT || codigo == KeyEvent.VK_F11 || codigo == KeyEvent.VK_PLUS
                || codigo == KeyEvent.VK_MINUS || codigo == KeyEvent.VK_EQUALS || codigo == KeyEvent.VK_ADD
                || codigo == KeyEvent.VK_SUBTRACT;
    }

    private void cancelarCaptura() {
        if (capturaAtual != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(capturaAtual);
            capturaAtual = null;
        }
        for (Map.Entry<String, JButton> botao : botoesTeclas.entrySet()) {
            botao.getValue().setText(KeyEvent.getKeyText(teclas.get(botao.getKey())));
        }
    }

    private void restaurarPadroes() {
        DadosConfiguracao padrao = new DadosConfiguracao();
        comboTema.setSelectedItem(padrao.getTema());
        comboBandeira.setSelectedItem(padrao.getSkinBandeira());
        comboCelula.setSelectedItem(padrao.getSkinCelula());
        sliderFonte.setValue((int) Math.round(padrao.getEscalaFonte() * 100));
        sliderZoom.setValue((int) Math.round(padrao.getZoom() * 100));
        spinnerDensidade.setValue(padrao.getDensidadeMinas());
        caixaInterrogacao.setSelected(padrao.isInterrogacaoAtiva());
        caixaDaltonico.setSelected(padrao.isModoDaltonico());
        caixaSom.setSelected(padrao.isSomAtivo());
        caixaMusica.setSelected(padrao.isMusicaAtiva());
        sliderVolume.setValue(padrao.getVolumeMusica());
        teclas.clear();
        teclas.putAll(DadosConfiguracao.teclasPadrao());
        for (Map.Entry<String, JButton> botao : botoesTeclas.entrySet()) {
            botao.getValue().setText(KeyEvent.getKeyText(teclas.get(botao.getKey())));
        }
    }

    private void salvar() {
        dados.setTema((String) comboTema.getSelectedItem());
        dados.setSkinBandeira((String) comboBandeira.getSelectedItem());
        dados.setSkinCelula((String) comboCelula.getSelectedItem());
        dados.setEscalaFonte(sliderFonte.getValue() / 100.0);
        dados.setZoom(sliderZoom.getValue() / 100.0);
        dados.setDensidadeMinas((Integer) spinnerDensidade.getValue());
        dados.setInterrogacaoAtiva(caixaInterrogacao.isSelected());
        dados.setModoDaltonico(caixaDaltonico.isSelected());
        dados.setSomAtivo(caixaSom.isSelected());
        dados.setMusicaAtiva(caixaMusica.isSelected());
        dados.setVolumeMusica(sliderVolume.getValue());
        for (Map.Entry<String, Integer> tecla : teclas.entrySet()) {
            dados.setTecla(tecla.getKey(), tecla.getValue());
        }
        resultado = dados;
        fechar();
    }

    private void fechar() {
        cancelarCaptura();
        dispose();
    }

    public static DadosConfiguracao mostrar(JFrame pai, DadosConfiguracao atual, TemaVisual tema, double escala) {
        DialogoConfiguracoes dialogo = new DialogoConfiguracoes(pai, atual, tema, escala);
        dialogo.setVisible(true);
        return dialogo.resultado;
    }
}
