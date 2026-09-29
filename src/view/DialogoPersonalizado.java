package view;

import view.tema.TemaVisual;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class DialogoPersonalizado extends JDialog {

    private final JSpinner spinnerLinhas;
    private final JSpinner spinnerColunas;
    private final JSpinner spinnerMinas;
    private final JLabel rotuloDensidade;
    private int[] resultado;

    private DialogoPersonalizado(JFrame pai, TemaVisual tema, double escala, int densidadePadrao) {
        super(pai, "Dificuldade personalizada", true);
        JPanel conteudo = EstiloUI.painel(tema.getFundo(), new BorderLayout(0, 14));
        conteudo.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel titulo = EstiloUI.rotulo("Monte seu próprio tabuleiro", tema.getDestaque(),
                EstiloUI.fonte(tema, escala, Font.BOLD, 16));
        conteudo.add(titulo, BorderLayout.NORTH);

        int linhasIniciais = 12;
        int colunasIniciais = 12;
        int minasIniciais = Math.max(1, linhasIniciais * colunasIniciais * densidadePadrao / 100);
        spinnerLinhas = new JSpinner(new SpinnerNumberModel(linhasIniciais, 2, 40, 1));
        spinnerColunas = new JSpinner(new SpinnerNumberModel(colunasIniciais, 2, 60, 1));
        spinnerMinas = new JSpinner(new SpinnerNumberModel(minasIniciais, 1, linhasIniciais * colunasIniciais - 1, 1));
        EstiloUI.confirmarAoDigitar(spinnerLinhas);
        EstiloUI.confirmarAoDigitar(spinnerColunas);
        EstiloUI.confirmarAoDigitar(spinnerMinas);
        rotuloDensidade = EstiloUI.rotulo("", tema.getTextoSecundario(), EstiloUI.fonte(tema, escala, Font.PLAIN, 12));

        JPanel campos = EstiloUI.painel(tema.getFundo(), new GridLayout(4, 2, 10, 8));
        campos.add(EstiloUI.rotulo("Linhas", tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.PLAIN, 13)));
        campos.add(spinnerLinhas);
        campos.add(EstiloUI.rotulo("Colunas", tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.PLAIN, 13)));
        campos.add(spinnerColunas);
        campos.add(EstiloUI.rotulo("Minas", tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.PLAIN, 13)));
        campos.add(spinnerMinas);
        campos.add(EstiloUI.rotulo("Densidade", tema.getTextoPrincipal(), EstiloUI.fonte(tema, escala, Font.PLAIN, 13)));
        campos.add(rotuloDensidade);
        conteudo.add(campos, BorderLayout.CENTER);

        spinnerLinhas.addChangeListener(e -> ajustarLimiteMinas(densidadePadrao));
        spinnerColunas.addChangeListener(e -> ajustarLimiteMinas(densidadePadrao));
        spinnerMinas.addChangeListener(e -> atualizarDensidade());
        atualizarDensidade();

        JPanel botoes = EstiloUI.painel(tema.getFundo(), new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelar = EstiloUI.botao("Cancelar", tema, escala, e -> dispose());
        JButton jogar = EstiloUI.botaoDestaque("Jogar", tema, escala, e -> confirmar());
        botoes.add(cancelar);
        botoes.add(jogar);
        conteudo.add(botoes, BorderLayout.SOUTH);

        setContentPane(conteudo);
        getRootPane().setDefaultButton(jogar);
        pack();
        setResizable(false);
        setLocationRelativeTo(pai);
    }

    private void ajustarLimiteMinas(int densidadePadrao) {
        int total = (Integer) spinnerLinhas.getValue() * (Integer) spinnerColunas.getValue();
        SpinnerNumberModel modelo = (SpinnerNumberModel) spinnerMinas.getModel();
        modelo.setMaximum(total - 1);
        modelo.setValue(Math.max(1, Math.min(total - 1, total * densidadePadrao / 100)));
        atualizarDensidade();
    }

    private void atualizarDensidade() {
        int total = (Integer) spinnerLinhas.getValue() * (Integer) spinnerColunas.getValue();
        int minas = (Integer) spinnerMinas.getValue();
        rotuloDensidade.setText(String.format("%.1f%% do tabuleiro", minas * 100.0 / total));
    }

    private void confirmar() {
        resultado = new int[]{(Integer) spinnerLinhas.getValue(), (Integer) spinnerColunas.getValue(),
                (Integer) spinnerMinas.getValue()};
        dispose();
    }

    public static int[] mostrar(JFrame pai, TemaVisual tema, double escala, int densidadePadrao) {
        DialogoPersonalizado dialogo = new DialogoPersonalizado(pai, tema, escala, densidadePadrao);
        dialogo.setVisible(true);
        return dialogo.resultado;
    }
}
