import java.awt.*;
import javax.swing.*;

public class VentanaPrincipal extends JFrame {

    private CardLayout cardLayout;
    private JPanel contenedor;

    public VentanaPrincipal() {
        setTitle("Motor de Base de Datos");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);

        ConsolaInicio consolaInicio = new ConsolaInicio(cardLayout, contenedor);
        contenedor.add(consolaInicio, "inicio");
        ConsolaTablas consolaTablas = new ConsolaTablas(cardLayout, contenedor);
        contenedor.add(consolaTablas, "tablas");
        ConsolaRegistros consolaRegistros = new ConsolaRegistros(cardLayout, contenedor);
        contenedor.add(consolaRegistros, "registros");
        ConsolaAVL consolaAVL = new ConsolaAVL(cardLayout, contenedor);
        contenedor.add(consolaAVL, "avl");
        ConsolaAyuda consolaAyuda = new ConsolaAyuda (cardLayout, contenedor);
        contenedor.add(consolaAyuda, "ayuda");

        add(contenedor);
        cardLayout.show(contenedor, "inicio");
    }

    private JPanel crearPanelSeccion(String nombreSeccion) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel(nombreSeccion, SwingConstants.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 20));

        JLabel texto = new JLabel("Seccion de " + nombreSeccion + " lista.", SwingConstants.CENTER);
        texto.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        JButton btnVolver = new JButton("Volver al inicio");
        btnVolver.addActionListener(e -> cardLayout.show(contenedor, "inicio"));

        JPanel pie = new JPanel();
        pie.add(btnVolver);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(texto, BorderLayout.CENTER);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}