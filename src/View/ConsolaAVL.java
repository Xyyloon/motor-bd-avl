import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsolaAVL extends JPanel {

    private CardLayout cardLayout;
    private JPanel contenedor;
    private JTextField campoTabla;
    private JTextArea areaSalida;
    private Map<String, Tabla> tablasPorNombre;

    public ConsolaAVL(CardLayout cardLayout, JPanel contenedor) {
        this.cardLayout = cardLayout;
        this.contenedor = contenedor;
        this.tablasPorNombre = new LinkedHashMap<>();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Explorador del Árbol AVL", SwingConstants.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panelSuperior.setOpaque(false);

        JLabel etiquetaTabla = new JLabel("Tabla:");
        etiquetaTabla.setFont(new Font("Times New Roman", Font.BOLD, 15));

        campoTabla = new JTextField(12);
        campoTabla.setFont(new Font("Consolas", Font.PLAIN, 14));

        JButton btnInOrden = new JButton("InOrden");
        btnInOrden.addActionListener(e -> mostrarRecorrido("InOrden"));

        JButton btnPreOrden = new JButton("PreOrden");
        btnPreOrden.addActionListener(e -> mostrarRecorrido("PreOrden"));

        JButton btnPostOrden = new JButton("PostOrden");
        btnPostOrden.addActionListener(e -> mostrarRecorrido("PostOrden"));

        JButton btnEstructura = new JButton("Estructura");
        btnEstructura.addActionListener(e -> mostrarEstructuraVisual());

        JButton btnInfoArbol = new JButton("Info Índice");
        btnInfoArbol.addActionListener(e -> mostrarInfoIndice());

        panelSuperior.add(etiquetaTabla);
        panelSuperior.add(campoTabla);
        panelSuperior.add(btnInOrden);
        panelSuperior.add(btnPreOrden);
        panelSuperior.add(btnPostOrden);
        panelSuperior.add(btnEstructura);
        panelSuperior.add(btnInfoArbol);

        areaSalida = new JTextArea();
        areaSalida.setEditable(false);
        areaSalida.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaSalida.setText("Escribe el nombre de una tabla (ej. Libros) y consulta su árbol AVL.\n");

        JScrollPane scroll = new JScrollPane(areaSalida);

        panelCentro.add(panelSuperior, BorderLayout.NORTH);
        panelCentro.add(scroll, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> areaSalida.setText(""));

        JButton btnVolver = new JButton("Volver al inicio");
        btnVolver.addActionListener(e -> cardLayout.show(contenedor, "inicio"));

        panelInferior.add(btnLimpiar);
        panelInferior.add(btnVolver);
        add(panelInferior, BorderLayout.SOUTH);

        sincronizarTablas();
    }

    public void sincronizarTablas() {
        try {
            Map<String, Tabla> cargadas = Persistencia.cargarTodas();
            tablasPorNombre = new LinkedHashMap<>();
            if (cargadas != null) {
                for (Tabla t : cargadas.values()) {
                    if (t != null && t.getNombre() != null) {
                        tablasPorNombre.put(t.getNombre().toLowerCase(), t);
                    }
                }
            }
        } catch (Exception ex) {
            appendLinea("Error al cargar tablas: " + ex.getMessage());
        }
    }

    private Tabla obtenerTablaSolicitada() {
        String nombre = campoTabla.getText().trim();
        if (nombre.isEmpty()) {
            appendLinea("Escribe primero el nombre de una tabla.");
            return null;
        }

        sincronizarTablas();
        Tabla tabla = tablasPorNombre.get(nombre.toLowerCase());
        if (tabla == null) {
            appendLinea("No se encontró la tabla '" + nombre + "' en data/tables.");
        }
        return tabla;
    }

    private void mostrarInfoIndice() {
        Tabla tabla = obtenerTablaSolicitada();
        if (tabla == null) return;

        Campo pk = tabla.getCampoPK();
        Arbol indice = tabla.getIndice();

        appendLinea(" Índice AVL de '" + tabla.getNombre() + "':");
        appendLinea("Clave primaria: " + (pk != null ? pk.getNombre() + " (" + pk.getTipo() + ")" : "No definida"));
        appendLinea("Árbol inicializado: " + (indice != null ? "Sí" : "No"));

        if (indice != null) {
            List<Fila> filas = indice.obtenerInOrden();
            int total = (filas != null) ? filas.size() : 0;
            appendLinea("Nodos / filas indexadas: " + total);
        }
        appendLinea("");
    }

    private void mostrarEstructuraVisual() {
        Tabla tabla = obtenerTablaSolicitada();
        if (tabla == null) return;

        Arbol indice = tabla.getIndice();
        if (indice == null) {
            appendLinea("La tabla '" + tabla.getNombre() + "' no tiene un árbol AVL inicializado.");
            return;
        }

        appendLinea(" Estructura AVL ('" + tabla.getNombre() + "'):");
        appendLinea(indice.obtenerEstructuraVisual());
        appendLinea("");
    }

    private void mostrarRecorrido(String tipoRecorrido) {
        Tabla tabla = obtenerTablaSolicitada();
        if (tabla == null) return;

        Arbol indice = tabla.getIndice();
        if (indice == null) {
            appendLinea("La tabla '" + tabla.getNombre() + "' no tiene un árbol AVL inicializado.");
            return;
        }

        List<Fila> filas;
        switch (tipoRecorrido) {
            case "PreOrden":
                filas = indice.obtenerPreOrden();
                break;
            case "PostOrden":
                filas = indice.obtenerPostOrden();
                break;
            default:
                filas = indice.obtenerInOrden();
                break;
        }

        appendLinea("Recorrido " + tipoRecorrido + " ('" + tabla.getNombre() + "'):");
        if (filas == null || filas.isEmpty()) {
            appendLinea("El árbol está vacío (0 registros).\n");
            return;
        }

        int i = 1;
        for (Fila fila : filas) {
            appendLinea(i + ". " + fila.getDatos());
            i++;
        }
        appendLinea("");
    }

    private void appendLinea(String texto) {
        areaSalida.append(texto + "\n");
    }
}
