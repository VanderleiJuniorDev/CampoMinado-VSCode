package view;

import controller.AcoesJogador;
import controller.ResumoEstatisticas;
import view.tema.TemaVisual;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DialogoEstatisticas extends JDialog {

    private final TemaVisual tema;
    private final double escala;
    private final ResumoEstatisticas resumo;
    private final AcoesJogador ouvinte;

    public DialogoEstatisticas(JFrame pai, ResumoEstatisticas resumo, TemaVisual tema, double escala, AcoesJogador ouvinte) {
        super(pai, "Estatísticas — " + resumo.getNomePerfil(), true);
        this.tema = tema;
        this.escala = escala;
        this.resumo = resumo;
        this.ouvinte = ouvinte;

        JPanel conteudo = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 10));
        conteudo.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titulo = EstiloUI.rotulo("📊 Estatísticas de " + resumo.getNomePerfil(), tema.getDestaque(),
                EstiloUI.fonte(tema, escala, Font.BOLD, 18));
        conteudo.add(titulo, BorderLayout.NORTH);

        UIManager.put("TabbedPane.selected", tema.getCard());
        UIManager.put("TabbedPane.contentAreaColor", tema.getFundo());
        UIManager.put("TabbedPane.focus", tema.getDestaque());
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(EstiloUI.fonte(tema, escala, Font.PLAIN, 13));
        abas.setBackground(tema.getFundoClaro());
        abas.setForeground(tema.getTextoPrincipal());
        abas.addTab("Resumo", criarAbaResumo());
        abas.addTab("Ranking", criarAbaRanking());
        abas.addTab("Histórico", criarAbaHistorico());
        abas.addTab("Conquistas", criarAbaConquistas());
        conteudo.add(abas, BorderLayout.CENTER);

        JPanel rodape = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rodape.add(EstiloUI.botao("📤 Exportar CSV", tema, escala, e -> exportar()));
        rodape.add(EstiloUI.botaoDestaque("Fechar", tema, escala, e -> dispose()));
        conteudo.add(rodape, BorderLayout.SOUTH);

        setContentPane(conteudo);
        setSize(new Dimension((int) (640 * Math.min(escala, 1.3)), (int) (560 * Math.min(escala, 1.3))));
        setLocationRelativeTo(pai);
    }

    private JPanel criarAbaResumo() {
        JPanel aba = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 12));
        aba.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel cartoes = EstiloUI.painel(tema.getFundo(), new GridLayout(2, 4, 10, 10));
        cartoes.add(cartao("Partidas", String.valueOf(resumo.getTotalPartidas())));
        cartoes.add(cartao("Vitórias", String.valueOf(resumo.getVitorias())));
        cartoes.add(cartao("Derrotas", String.valueOf(resumo.getDerrotas())));
        cartoes.add(cartao("Taxa de acerto", String.format("%.1f%%", resumo.getTaxaVitoria())));
        cartoes.add(cartao("Últimas 10", String.format("%.0f%%", resumo.getTaxaUltimasDez())));
        cartoes.add(cartao("Sequência atual", String.valueOf(resumo.getSequenciaAtual())));
        cartoes.add(cartao("Melhor sequência", String.valueOf(resumo.getMelhorSequencia())));
        cartoes.add(cartao("Conquistas", contarConquistas() + " / " + resumo.getConquistas().size()));
        aba.add(cartoes, BorderLayout.NORTH);

        JPanel painelGrafico = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 6));
        painelGrafico.add(EstiloUI.rotulo("Taxa de acerto ao longo do tempo", tema.getTextoSecundario(),
                EstiloUI.fonte(tema, escala, Font.BOLD, 12)), BorderLayout.NORTH);
        painelGrafico.add(new GraficoTaxa(resumo.getEvolucaoTaxa(), tema, escala), BorderLayout.CENTER);
        aba.add(painelGrafico, BorderLayout.CENTER);

        List<String[]> recordes = resumo.getRecordes();
        String[][] dados = recordes.toArray(new String[0][]);
        JTable tabela = criarTabela(new String[]{"Dificuldade", "Melhor tempo"}, dados);
        JScrollPane rolagem = rolagem(tabela);
        rolagem.setPreferredSize(new Dimension(200, 110));
        JPanel painelRecordes = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 6));
        painelRecordes.add(EstiloUI.rotulo("🏅 Recordes por dificuldade", tema.getTextoSecundario(),
                EstiloUI.fonte(tema, escala, Font.BOLD, 12)), BorderLayout.NORTH);
        painelRecordes.add(rolagem, BorderLayout.CENTER);
        aba.add(painelRecordes, BorderLayout.SOUTH);
        return aba;
    }

    private int contarConquistas() {
        int total = 0;
        for (String[] conquista : resumo.getConquistas()) {
            if ("sim".equals(conquista[3])) {
                total++;
            }
        }
        return total;
    }

    private JPanel cartao(String titulo, String valor) {
        JPanel cartao = EstiloUI.painel(tema.getCard(), new BorderLayout());
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.getBorda()),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel rotuloTitulo = EstiloUI.rotulo(titulo, tema.getTextoSecundario(), EstiloUI.fonte(tema, escala, Font.PLAIN, 11));
        JLabel rotuloValor = EstiloUI.rotulo(valor, tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.BOLD, 18));
        cartao.add(rotuloTitulo, BorderLayout.NORTH);
        cartao.add(rotuloValor, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel criarAbaRanking() {
        JPanel aba = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 10));
        aba.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        Map<String, List<String[]>> ranking = resumo.getRanking();
        List<String> dificuldades = new ArrayList<>(ranking.keySet());
        JComboBox<String> combo = EstiloUI.combo(dificuldades, dificuldades.isEmpty() ? null : dificuldades.get(0), tema, escala);
        String[] colunas = {"Posição", "Tempo", "Jogadas", "Data"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        JTable tabela = criarTabela(modelo);
        Runnable preencher = () -> {
            modelo.setRowCount(0);
            Object selecionada = combo.getSelectedItem();
            List<String[]> linhas = selecionada == null ? null : ranking.get(selecionada.toString());
            if (linhas != null) {
                for (String[] linha : linhas) {
                    modelo.addRow(linha);
                }
            }
        };
        combo.addActionListener(e -> preencher.run());
        preencher.run();

        JPanel topo = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.LEFT, 8, 0));
        topo.add(EstiloUI.rotulo("🏆 Top 10 da dificuldade:", tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.PLAIN, 13)));
        topo.add(combo);
        aba.add(topo, BorderLayout.NORTH);
        aba.add(rolagem(tabela), BorderLayout.CENTER);
        aba.add(EstiloUI.rotulo("Ordenado com merge sort implementado manualmente (RankingTempos).",
                tema.getTextoSecundario(), EstiloUI.fonte(tema, escala, Font.ITALIC, 11)), BorderLayout.SOUTH);
        return aba;
    }

    private JPanel criarAbaHistorico() {
        JPanel aba = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 10));
        aba.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        String[][] dados = resumo.getHistorico().toArray(new String[0][]);
        JTable tabela = criarTabela(new String[]{"Data", "Dificuldade", "Resultado", "Tempo", "Jogadas", "Modos"}, dados);
        aba.add(rolagem(tabela), BorderLayout.CENTER);
        return aba;
    }

    private JComponent criarAbaConquistas() {
        JPanel lista = EstiloUI.painel(tema.getFundo(), null);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (String[] conquista : resumo.getConquistas()) {
            boolean desbloqueada = "sim".equals(conquista[3]);
            JPanel linha = EstiloUI.painel(desbloqueada ? tema.getCard() : tema.getFundoClaro(), new BorderLayout(12, 0));
            linha.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(desbloqueada ? tema.getVitoria() : tema.getBorda()),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            linha.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel icone = new JLabel(desbloqueada ? conquista[0] : "🔒");
            icone.setFont(EstiloUI.fonteEmoji(Font.PLAIN, (float) (24 * escala)));
            linha.add(icone, BorderLayout.WEST);
            JPanel textos = EstiloUI.painelTransparente(new GridLayout(2, 1));
            Color corTitulo = desbloqueada ? tema.getTextoPrincipal() : tema.getTextoSecundario();
            textos.add(EstiloUI.rotulo(conquista[1], corTitulo, EstiloUI.fonte(tema, escala, Font.BOLD, 14)));
            textos.add(EstiloUI.rotulo(conquista[2], tema.getTextoSecundario(), EstiloUI.fonte(tema, escala, Font.PLAIN, 12)));
            linha.add(textos, BorderLayout.CENTER);
            lista.add(linha);
            lista.add(javax.swing.Box.createVerticalStrut(6));
        }
        return rolagem(lista);
    }

    private JTable criarTabela(String[] colunas, String[][] dados) {
        DefaultTableModel modelo = new DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        return criarTabela(modelo);
    }

    private JTable criarTabela(DefaultTableModel modelo) {
        JTable tabela = new JTable(modelo);
        tabela.setFont(EstiloUI.fonte(tema, escala, Font.PLAIN, 12));
        tabela.setRowHeight((int) (24 * escala));
        tabela.setBackground(tema.getFundoClaro());
        tabela.setForeground(tema.getTextoPrincipal());
        tabela.setGridColor(tema.getBorda());
        tabela.setSelectionBackground(tema.getDestaque());
        tabela.setSelectionForeground(tema.getFundo());
        tabela.setFillsViewportHeight(true);
        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setFont(EstiloUI.fonte(tema, escala, Font.BOLD, 12));
        cabecalho.setBackground(tema.getCard());
        cabecalho.setForeground(tema.getTextoPrincipal());
        cabecalho.setReorderingAllowed(false);
        return tabela;
    }

    private JScrollPane rolagem(JComponent componente) {
        JScrollPane rolagem = new JScrollPane(componente);
        rolagem.setBorder(BorderFactory.createLineBorder(tema.getBorda()));
        rolagem.getViewport().setBackground(tema.getFundoClaro());
        rolagem.getVerticalScrollBar().setUnitIncrement(16);
        return rolagem;
    }

    private void exportar() {
        JFileChooser seletor = new JFileChooser();
        seletor.setDialogTitle("Exportar histórico para CSV");
        seletor.setSelectedFile(new File("campo-minado-" + resumo.getNomePerfil().replaceAll("[^A-Za-z0-9]+", "_") + ".csv"));
        if (seletor.showSaveDialog(this) == JFileChooser.APPROVE_OPTION && ouvinte != null) {
            ouvinte.aoExportarCsv(seletor.getSelectedFile());
        }
    }
}
