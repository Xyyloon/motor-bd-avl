package View;

import javax.swing.*;

import Model.Campo;
import Model.Fila;
import Model.Persistencia;
import Model.Tabla;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsolaRegistros extends JPanel {

    private CardLayout cardLayout;
    private JPanel contenedor;
    private JTextField campoTabla;
    private JTextField campoEntrada;
    private JTextArea areaSalida;
    private Map<String, Tabla> tablasPorNombre;

    public ConsolaRegistros(CardLayout cardLayout, JPanel contenedor) {
        this.cardLayout = cardLayout;
        this.contenedor = contenedor;
        this.tablasPorNombre = new LinkedHashMap<>();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(new Color(245, 247, 250));

        JLabel titulo = new JLabel("Gestión de Registros (CRUD y Búsquedas)", SwingConstants.CENTER);
        titulo.setFont(new Font("Times New Roman", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));
        panelCentro.setOpaque(false);

        JPanel panelEntradas = new JPanel(new GridLayout(2, 1, 6, 6));
        panelEntradas.setOpaque(false);

        JPanel filaSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filaSuperior.setOpaque(false);
        JLabel lblTabla = new JLabel("Tabla:");
        lblTabla.setFont(new Font("Times New Roman", Font.BOLD, 15));
        campoTabla = new JTextField(16);
        campoTabla.setFont(new Font("Consolas", Font.PLAIN, 14));

        JButton btnSelectAll = new JButton("SELECT *");
        btnSelectAll.addActionListener(e -> selectTodos());

        JButton btnBuscarPK = new JButton("Buscar PK");
        btnBuscarPK.addActionListener(e -> buscarPorPK());

        JButton btnRango = new JButton("Rango (min..max)");
        btnRango.addActionListener(e -> buscarRango());

        filaSuperior.add(lblTabla);
        filaSuperior.add(campoTabla);
        filaSuperior.add(btnSelectAll);
        filaSuperior.add(btnBuscarPK);
        filaSuperior.add(btnRango);

        JPanel filaInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filaInferior.setOpaque(false);
        JLabel lblDatos = new JLabel("PK / Rango / Campos (k=v, k2=v2):");
        lblDatos.setFont(new Font("Times New Roman", Font.BOLD, 14));
        campoEntrada = new JTextField(32);
        campoEntrada.setFont(new Font("Consolas", Font.PLAIN, 14));

        filaInferior.add(lblDatos);
        filaInferior.add(campoEntrada);

        panelEntradas.add(filaSuperior);
        panelEntradas.add(filaInferior);

        areaSalida = new JTextArea();
        areaSalida.setEditable(false);
        areaSalida.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaSalida.setText(
            "Ejemplos de uso:\n" +
            " - SELECT *: escribe la tabla y pulsa 'SELECT *'.\n" +
            " - Buscar PK: escribe la PK en el campo inferior y pulsa 'Buscar PK'.\n" +
            " - Rango: escribe 'min..max' (ej. 1..5) y pulsa 'Rango (min..max)'.\n" +
            " - INSERT / UPDATE: escribe 'campo1=valor1, campo2=valor2' y pulsa 'Insertar' o 'Actualizar'.\n" +
            " - DELETE: escribe la PK en el campo inferior y pulsa 'Eliminar'.\n\n"
        );

        JScrollPane scroll = new JScrollPane(areaSalida);

        panelCentro.add(panelEntradas, BorderLayout.NORTH);
        panelCentro.add(scroll, BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnInsertar = new JButton("Insertar");
        btnInsertar.addActionListener(e -> insertarRegistro());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarRegistro());

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarRegistro());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> areaSalida.setText(""));

        JButton btnVolver = new JButton("Volver al inicio");
        btnVolver.addActionListener(e -> cardLayout.show(contenedor, "inicio"));

        panelBotones.add(btnInsertar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnVolver);

        add(panelBotones, BorderLayout.SOUTH);

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
            linea("Error al cargar tablas: " + ex.getMessage());
        }
    }

    private Tabla obtenerTabla() {
        String nombre = campoTabla.getText().trim();
        if (nombre.isEmpty()) {
            linea("Escribe el nombre de una tabla.");
            return null;
        }
        sincronizarTablas();
        Tabla tabla = tablasPorNombre.get(nombre.toLowerCase());
        if (tabla == null) {
            linea("No existe la tabla '" + nombre + "' en data/tables.");
        }
        return tabla;
    }

    private Map<String, Object> parsearParesClaveValor(String texto) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        String[] pares = texto.split(",");
        for (String par : pares) {
            String p = par.trim();
            if (p.isEmpty()) continue;
            int idx = p.indexOf('=');
            if (idx <= 0) {
                throw new IllegalArgumentException("Formato inválido en '" + p + "'. Usa campo=valor.");
            }
            String clave = p.substring(0, idx).trim();
            String valor = p.substring(idx + 1).trim();
            mapa.put(clave, valor);
        }
        return mapa;
    }

    private void selectTodos() {
        Tabla tabla = obtenerTabla();
        if (tabla == null) return;

        List<Fila> filas = tabla.getIndice().obtenerInOrden();
        linea("=== SELECT * FROM " + tabla.getNombre() + " ===");
        if (filas == null || filas.isEmpty()) {
            linea("(Sin registros)\n");
            return;
        }
        int i = 1;
        for (Fila f : filas) {
            linea(i++ + ". " + f.getDatos());
        }
        linea("");
    }

    private void buscarPorPK() {
        Tabla tabla = obtenerTabla();
        if (tabla == null) return;

        String rawPK = campoEntrada.getText().trim();
        if (rawPK.isEmpty()) {
            linea("Escribe el valor de la clave primaria a buscar.");
            return;
        }

        try {
            Comparable clave = tabla.convertirClavePK(rawPK);
            Fila encontrada = tabla.getIndice().buscar(clave);
            if (encontrada == null) {
                linea("No se encontró ningún registro con PK = " + clave);
            } else {
                linea("Registro encontrado: " + encontrada.getDatos());
            }
        } catch (Exception ex) {
            linea("Error al buscar por PK: " + ex.getMessage());
        }
    }

    private void buscarRango() {
        Tabla tabla = obtenerTabla();
        if (tabla == null) return;

        String texto = campoEntrada.getText().trim();
        if (!texto.contains("..")) {
            linea("Para buscar por rango usa el formato min..max (ejemplo: 1..10).");
            return;
        }

        String[] partes = texto.split("\\.\\.", 2);
        try {
            Comparable min = tabla.convertirClavePK(partes[0].trim());
            Comparable max = tabla.convertirClavePK(partes[1].trim());
            List<Fila> resultados = tabla.getIndice().buscarRango(min, max);

            linea(" Rango [" + min + " .. " + max + "] en " + tabla.getNombre() + ":");
            if (resultados == null || resultados.isEmpty()) {
                linea("(Sin coincidencias en el rango)\n");
                return;
            }
            for (Fila f : resultados) {
                linea(" - " + f.getDatos());
            }
            linea("");
        } catch (Exception ex) {
            linea("Error en búsqueda por rango: " + ex.getMessage());
        }
    }

    private void insertarRegistro() {
        Tabla tabla = obtenerTabla();
        if (tabla == null) return;

        Campo pkCampo = tabla.getCampoPK();
        if (pkCampo == null) {
            linea("La tabla '" + tabla.getNombre() + "' no tiene clave primaria definida.");
            return;
        }

        String texto = campoEntrada.getText().trim();
        if (texto.isEmpty()) {
            linea("Escribe los valores como campo1=valor1, campo2=valor2.");
            return;
        }

        try {
            Map<String, Object> crudos = parsearParesClaveValor(texto);
            Fila fila = tabla.construirYValidarFila(crudos);
            Comparable clavePK = (Comparable) fila.getDatos().get(pkCampo.getNombre());

            if (tabla.getIndice().existe(clavePK)) {
                linea("Ya existe un registro con PK = " + clavePK + ". Usa 'Actualizar' si deseas modificarlo.");
                return;
            }

            tabla.getIndice().insertar(clavePK, fila);
            Persistencia.guardarTabla(tabla);
            linea("Registro insertado y guardado (PK = " + clavePK + "): " + fila.getDatos());
        } catch (Exception ex) {
            linea("Error al insertar: " + ex.getMessage());
        }
    }

    private void actualizarRegistro() {
        Tabla tabla = obtenerTabla();
        if (tabla == null) return;

        Campo pkCampo = tabla.getCampoPK();
        if (pkCampo == null) {
            linea("La tabla '" + tabla.getNombre() + "' no tiene clave primaria definida.");
            return;
        }

        String texto = campoEntrada.getText().trim();
        if (texto.isEmpty()) {
            linea("Escribe los campos actualizados incluyendo la PK (ej. id=1, titulo=Nuevo).");
            return;
        }

        try {
            Map<String, Object> crudos = parsearParesClaveValor(texto);
            Fila nuevaFila = tabla.construirYValidarFila(crudos);
            Comparable clavePK = (Comparable) nuevaFila.getDatos().get(pkCampo.getNombre());

            boolean actualizado = tabla.getIndice().actualizar(clavePK, nuevaFila);
            if (!actualizado) {
                linea("No existe ningún registro con PK = " + clavePK + " para actualizar.");
                return;
            }

            Persistencia.guardarTabla(tabla);
            linea("Registro actualizado y guardado (PK = " + clavePK + "): " + nuevaFila.getDatos());
        } catch (Exception ex) {
            linea("Error al actualizar: " + ex.getMessage());
        }
    }

    private void eliminarRegistro() {
        Tabla tabla = obtenerTabla();
        if (tabla == null) return;

        String rawPK = campoEntrada.getText().trim();
        if (rawPK.isEmpty()) {
            linea("Escribe la clave primaria del registro a eliminar.");
            return;
        }

        try {
            Comparable clave = tabla.convertirClavePK(rawPK);
            boolean eliminado = tabla.getIndice().eliminar(clave);
            if (!eliminado) {
                linea("No se encontró ningún registro con PK = " + clave + " para eliminar.");
                return;
            }

            Persistencia.guardarTabla(tabla);
            linea("Registro con PK = " + clave + " eliminado y cambios guardados.");
        } catch (Exception ex) {
            linea("Error al eliminar: " + ex.getMessage());
        }
    }

    private void linea(String msg) {
        areaSalida.append(msg + "\n");
    }
}