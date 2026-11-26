import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsolaTablas extends JPanel {

    private CardLayout cardLayout;
    private JPanel contenedor;
    private JTextField campoNombreTabla;
    private JTextArea areaSalida;
    private Map<String, Tabla> tablasPorNombre;

    public ConsolaTablas(CardLayout cardLayout, JPanel contenedor) {
        this.cardLayout = cardLayout;
        this.contenedor = contenedor;
        this.tablasPorNombre = new LinkedHashMap<>();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Gestion de Tablas y Campos", SwingConstants.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);

        JPanel panelEntrada = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panelEntrada.setOpaque(false);

        JLabel etiquetaTabla = new JLabel("Nombre de tabla:");
        etiquetaTabla.setFont(new Font("Times New Roman", Font.BOLD, 15));

        campoNombreTabla = new JTextField(20);
        campoNombreTabla.setFont(new Font("Consolas", Font.PLAIN, 14));

        JButton btnListar = new JButton("Listar Tablas");
        btnListar.addActionListener(e -> recargarYListarTablas());

        JButton btnVerCampos = new JButton("Ver Campos");
        btnVerCampos.addActionListener(e -> accionVerCampos());

        panelEntrada.add(etiquetaTabla);
        panelEntrada.add(campoNombreTabla);
        panelEntrada.add(btnListar);
        panelEntrada.add(btnVerCampos);

        areaSalida = new JTextArea();
        areaSalida.setEditable(false);
        areaSalida.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaSalida.setText("Cargando tablas...\n"); //carga desde persistencia en Model

        JScrollPane scroll = new JScrollPane(areaSalida);

        panelCentro.add(panelEntrada, BorderLayout.NORTH);
        panelCentro.add(scroll, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnCrear = new JButton("Crear Tabla");
        btnCrear.addActionListener(e -> accionCrearTablaBase());

        JButton btnEliminar = new JButton("Eliminar Tabla");
        btnEliminar.addActionListener(e -> accionEliminarTabla());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> areaSalida.setText(""));

        JButton btnVolver = new JButton("Volver al inicio");
        btnVolver.addActionListener(e -> cardLayout.show(contenedor, "inicio"));

        panelBotones.add(btnCrear);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnVolver);

        add(panelBotones, BorderLayout.SOUTH);

        sincronizarDesdePersistencia(true);
    }

    public void sincronizarDesdePersistencia(boolean mostrarResumen) {
        try {
            Map<String, Tabla> cargadas = Persistencia.cargarTodas();
            this.tablasPorNombre = new LinkedHashMap<>();
            if (cargadas != null) {
                for (Map.Entry<String, Tabla> entry : cargadas.entrySet()) {
                    Tabla t = entry.getValue();
                    if (t != null && t.getNombre() != null) {
                        this.tablasPorNombre.put(t.getNombre().toLowerCase(), t);
                    }
                }
            }
            if (mostrarResumen) {
                imprimirListaTablas();
            }
        } catch (Exception ex) {
            mostrarMensaje("Error al cargar tablas " + ex.getMessage());
        }
    }

    public void mostrarCamposDeTabla(Tabla tabla) {
        if (tabla == null) {
            mostrarMensaje("No se encontro la tabla solicitada.");
            return;
        }

        areaSalida.append("Campos de la tabla '" + tabla.getNombre() + "\n");
        List<Campo> campos = tabla.getCampos();

        if (campos == null || campos.isEmpty()) {
            mostrarMensaje("Esta tabla no tiene campos definidos.\n");
            return;
        }

        for (Campo c : campos) {
            String marcaPK = c.isEsPK() ? " [PK]" : "";
            mostrarMensaje(" - " + c.getNombre() + " : " + c.getTipo() + marcaPK);
        }

        Campo pk = tabla.getCampoPK();
        if (pk != null) {
            mostrarMensaje("Clave primaria actual: " + pk.getNombre() + " (" + pk.getTipo() + ")\n");
        } else {
            mostrarMensaje("Sin clave primaria definida.\n");
        }
    }

    private void recargarYListarTablas() {
        sincronizarDesdePersistencia(false);
        imprimirListaTablas();
    }

    private void imprimirListaTablas() {
        if (tablasPorNombre.isEmpty()) {
            mostrarMensaje("No se encontraron tablas.");
            return;
        }

        areaSalida.append("Tablas en data/tables\n");
        for (Tabla t : tablasPorNombre.values()) {
            int totalCampos = (t.getCampos() != null) ? t.getCampos().size() : 0;
            int totalFilas = (t.getIndice() != null && t.getIndice().obtenerInOrden() != null)
                    ? t.getIndice().obtenerInOrden().size()
                    : 0;
            mostrarMensaje(" - " + t.getNombre() + " (" + totalCampos + " campos, " + totalFilas + " filas)");
        }
        areaSalida.append("\n");
    }

    private void accionVerCampos() {
        String nombreBuscado = campoNombreTabla.getText().trim();
        if (nombreBuscado.isEmpty()) {
            mostrarMensaje("Escribe el nombre de una tabla para ver sus campos.");
            return;
        }

        sincronizarDesdePersistencia(false);
        Tabla encontrada = tablasPorNombre.get(nombreBuscado.toLowerCase());
        if (encontrada == null) {
            mostrarMensaje("No se encontro ninguna tabla llamada '" + nombreBuscado + "'.");
            return;
        }

        mostrarCamposDeTabla(encontrada);
    }

    private void accionCrearTablaBase() {
        String nombre = campoNombreTabla.getText().trim();
        if (nombre.isEmpty()) {
            mostrarMensaje("Escribe un nombre para la tabla.");
            return;
        }

        sincronizarDesdePersistencia(false);
        if (tablasPorNombre.containsKey(nombre.toLowerCase())) {
            mostrarMensaje("Ya existe una tabla llamada '" + nombre + "'.");
            return;
        }

        List<Campo> camposIniciales = new ArrayList<>();
        camposIniciales.add(new Campo("id", "INT", true));
        camposIniciales.add(new Campo("nombre", "TEXT", false));

        Tabla nuevaTabla = new Tabla(nombre, camposIniciales);
        try {
            Persistencia.guardarTabla(nuevaTabla);
            tablasPorNombre.put(nombre.toLowerCase(), nuevaTabla);
            mostrarMensaje("Tabla '" + nombre + "' creada y guardada en data/tables.");
            mostrarCamposDeTabla(nuevaTabla);
        } catch (RuntimeException ex) {
            mostrarMensaje("No se pudo guardar la tabla: " + ex.getMessage());
        }
    }

    private void accionEliminarTabla() {
        String nombre = campoNombreTabla.getText().trim();
        if (nombre.isEmpty()) {
            mostrarMensaje("Escribe el nombre de la tabla a eliminar.");
            return;
        }

        try {
            Persistencia.eliminarArchivo(nombre);
            tablasPorNombre.remove(nombre.toLowerCase());
            mostrarMensaje("Archivo de tabla '" + nombre + "' eliminado de data/tables.");
        } catch (RuntimeException ex) {
            mostrarMensaje("Error al eliminar la tabla: " + ex.getMessage());
        }
    }

    private void mostrarMensaje(String mensaje) {
        areaSalida.append(mensaje + "\n");
    }
}