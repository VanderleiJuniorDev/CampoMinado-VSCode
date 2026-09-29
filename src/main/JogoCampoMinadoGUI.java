package main;

import controller.CampoMinadoController;
import view.CampoMinadoView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class JogoCampoMinadoGUI {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            UIManager.getLookAndFeelDefaults();
        }

        SwingUtilities.invokeLater(() -> {
            CampoMinadoView view = new CampoMinadoView();
            CampoMinadoController controller = new CampoMinadoController(view);
            controller.iniciar();
        });
    }
}
