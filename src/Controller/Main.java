package Controller;

import View.VentanaPrincipal;
import javax.swing.SwingUtilities;

/** Punto de entrada de la aplicación. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
