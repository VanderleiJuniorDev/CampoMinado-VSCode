package view;

import view.tema.TemaVisual;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.Border;
import javax.swing.text.DefaultFormatter;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EstiloUI {

    private static final String[] FONTES_EMOJI = {"Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji", "Segoe UI Symbol"};
    private static String familiaEmoji;

    private EstiloUI() {
    }

    public static Font fonte(TemaVisual tema, double escala, int estilo, float tamanho) {
        return tema.fonte(estilo, (float) (tamanho * escala));
    }

    public static synchronized Font fonteEmoji(int estilo, float tamanho) {
        if (familiaEmoji == null) {
            familiaEmoji = Font.DIALOG;
            try {
                Set<String> disponiveis = new HashSet<>(Arrays.asList(
                        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
                for (String candidata : FONTES_EMOJI) {
                    if (disponiveis.contains(candidata)) {
                        familiaEmoji = candidata;
                        break;
                    }
                }
            } catch (RuntimeException ignorada) {
                familiaEmoji = Font.DIALOG;
            }
        }
        return new Font(familiaEmoji, estilo, Math.round(tamanho));
    }

    public static boolean contemEmoji(String texto) {
        if (texto == null) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            char caractere = texto.charAt(i);
            if (Character.isSurrogate(caractere) || (caractere >= '\u2300' && caractere <= '\u23FF')
                    || (caractere >= '\u2600' && caractere <= '\u27BF') || (caractere >= '\u2B00' && caractere <= '\u2BFF')) {
                return true;
            }
        }
        return false;
    }

    public static Font fonteParaTexto(String texto, TemaVisual tema, double escala, int estilo, float tamanho) {
        if (contemEmoji(texto)) {
            return fonteEmoji(estilo, (float) (tamanho * escala));
        }
        return fonte(tema, escala, estilo, tamanho);
    }

    public static Color misturar(Color origem, Color destino, double proporcao) {
        double p = Math.max(0, Math.min(1, proporcao));
        int r = (int) Math.round(origem.getRed() + (destino.getRed() - origem.getRed()) * p);
        int g = (int) Math.round(origem.getGreen() + (destino.getGreen() - origem.getGreen()) * p);
        int b = (int) Math.round(origem.getBlue() + (destino.getBlue() - origem.getBlue()) * p);
        int a = (int) Math.round(origem.getAlpha() + (destino.getAlpha() - origem.getAlpha()) * p);
        return new Color(r, g, b, a);
    }

    public static Color comAlfa(Color cor, int alfa) {
        return new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    public static Color escurecer(Color cor, double fator) {
        return new Color((int) (cor.getRed() * fator), (int) (cor.getGreen() * fator), (int) (cor.getBlue() * fator), cor.getAlpha());
    }

    public static Border bordaBotao(Color cor, int espessura) {
        int interno = 7 - espessura;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cor, espessura),
                BorderFactory.createEmptyBorder(interno, 14 - espessura, interno, 14 - espessura));
    }

    public static JButton botao(String texto, TemaVisual tema, double escala, ActionListener acao) {
        return criarBotao(texto, tema, escala, acao, false);
    }

    public static JButton botaoDestaque(String texto, TemaVisual tema, double escala, ActionListener acao) {
        return criarBotao(texto, tema, escala, acao, true);
    }

    private static JButton criarBotao(String texto, TemaVisual tema, double escala, ActionListener acao, boolean destaque) {
        JButton botao = new JButton(texto);
        Color corBorda = destaque ? tema.getDestaque() : tema.getBorda();
        botao.setFont(fonteParaTexto(texto, tema, escala, destaque ? Font.BOLD : Font.PLAIN, 14));
        botao.setForeground(destaque ? tema.getDestaque() : tema.getTextoPrincipal());
        botao.setBackground(tema.getFundoClaro());
        botao.setFocusPainted(false);
        botao.setFocusable(false);
        botao.setContentAreaFilled(true);
        botao.setOpaque(true);
        botao.setBorder(bordaBotao(corBorda, 1));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (acao != null) {
            botao.addActionListener(acao);
        }
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evento) {
                if (botao.isEnabled()) {
                    botao.setBackground(tema.getCardHover());
                    botao.setBorder(bordaBotao(tema.getDestaque(), destaque ? 2 : 1));
                }
            }

            @Override
            public void mouseExited(MouseEvent evento) {
                botao.setBackground(tema.getFundoClaro());
                botao.setBorder(bordaBotao(corBorda, 1));
            }

            @Override
            public void mousePressed(MouseEvent evento) {
                if (botao.isEnabled()) {
                    botao.setBackground(misturar(tema.getCardHover(), tema.getDestaque(), 0.25));
                }
            }

            @Override
            public void mouseReleased(MouseEvent evento) {
                botao.setBackground(botao.getModel().isRollover() ? tema.getCardHover() : tema.getFundoClaro());
            }
        });
        return botao;
    }

    public static JLabel rotulo(String texto, Color cor, Font fonte) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setForeground(cor);
        rotulo.setFont(fonte);
        return rotulo;
    }

    public static JComboBox<String> combo(List<String> opcoes, String selecionada, TemaVisual tema, double escala) {
        JComboBox<String> combo = new JComboBox<>(opcoes.toArray(new String[0]));
        combo.setFont(fonte(tema, escala, Font.PLAIN, 12));
        combo.setBackground(tema.getFundoClaro());
        combo.setForeground(tema.getTextoPrincipal());
        combo.setBorder(BorderFactory.createLineBorder(tema.getBorda()));
        combo.setFocusable(false);
        if (selecionada != null) {
            combo.setSelectedItem(selecionada);
        }
        return combo;
    }

    public static JCheckBox caixa(String texto, boolean marcada, TemaVisual tema, double escala) {
        JCheckBox caixa = new JCheckBox(texto, marcada);
        caixa.setFont(fonte(tema, escala, Font.PLAIN, 13));
        caixa.setForeground(tema.getTextoPrincipal());
        caixa.setBackground(tema.getFundo());
        caixa.setOpaque(false);
        caixa.setFocusPainted(false);
        caixa.setFocusable(false);
        caixa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return caixa;
    }

    public static void confirmarAoDigitar(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField campo = ((JSpinner.DefaultEditor) editor).getTextField();
            if (campo.getFormatter() instanceof DefaultFormatter) {
                ((DefaultFormatter) campo.getFormatter()).setCommitsOnValidEdit(true);
            }
        }
    }

    public static JPanel painel(Color fundo, LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setBackground(fundo);
        return painel;
    }

    public static JPanel painelTransparente(LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setOpaque(false);
        return painel;
    }
}
