import javax.swing.*;
import java.awt.*;

public class ConsolaAyuda extends JPanel {

    private CardLayout cardLayout;
    private JPanel contenedor;

    public ConsolaAyuda(CardLayout cardLayout, JPanel contenedor) {
        this.cardLayout = cardLayout;
        this.contenedor = contenedor;

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Ayuda - Comandos y Reglas del Sistema", SwingConstants.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JTextArea areaAyuda = new JTextArea();
        areaAyuda.setEditable(false);
        areaAyuda.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaAyuda.setLineWrap(true);
        areaAyuda.setWrapStyleWord(true);
        areaAyuda.setText(construirTextoAyuda());
        areaAyuda.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(areaAyuda);
        add(scroll, BorderLayout.CENTER);

        JPanel panelPie = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnVolver = new JButton("Volver al inicio");
        btnVolver.addActionListener(e -> cardLayout.show(contenedor, "inicio"));
        panelPie.add(btnVolver);

        add(panelPie, BorderLayout.SOUTH);
    }

    private String construirTextoAyuda() {
        return "=== GUIA RAPIDA DE COMANDOS ===\n\n"
                + "1. TABLAS\n"
                + "   - Ver / crear / eliminar tablas y revisar sus campos (clave primaria y tipos de dato).\n\n"
                + "2. REGISTROS\n"
                + "   - INSERT: insertar filas respetando el esquema y la clave primaria.\n"
                + "   - SELECT: consultar registros de la tabla.\n"
                + "   - UPDATE: actualizar valores de una fila.\n"
                + "   - DELETE: eliminar un registro por clave.\n\n"
                + "3. ARBOL AVL\n"
                + "   - Mantiene los registros indexados y balanceados automaticamente.\n"
                + "   - Recorridos disponibles: Inorden, Preorden y Postorden.\n\n";
    } //revisar sintaxis de comandos
}