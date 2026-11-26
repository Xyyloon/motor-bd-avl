import javax.swing.*;
import java.awt.*;

public class ConsolaInicio extends JPanel {

    private CardLayout cardLayout;
    private JPanel contenedor;

    public ConsolaInicio(CardLayout cardLayout, JPanel contenedor) {
        this.cardLayout = cardLayout;
        this.contenedor = contenedor;

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;

        JLabel titulo = new JLabel("Motor de Base de Datos");
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 22));
        gbc.gridy = 0;
        add(titulo, gbc);

        JLabel subtitulo = new JLabel("Selecciona una opcion para comenzar");
        subtitulo.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        gbc.gridy = 1;
        add(subtitulo, gbc);

        gbc.gridy = 2;
        add(crearBoton("TABLAS", new Color(52, 152, 219), "tablas"), gbc);

        gbc.gridy = 3;
        add(crearBoton("REGISTROS / COMANDOS", new Color(52, 152, 219), "registros"), gbc);

        gbc.gridy = 4;
        add(crearBoton("ARBOL AVL", new Color(52, 152, 219), "avl"), gbc);

        gbc.gridy = 5;
        add(crearBoton("AYUDA", new Color(52, 152, 219), "ayuda"), gbc);
    }

    private JButton crearBoton(String texto, Color colorFondo, String destino) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(250, 42));
        boton.addActionListener(e -> cardLayout.show(contenedor, destino));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        return boton;
    }
}