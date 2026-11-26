package Model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tabla {
    private String nombre;
    private List<Campo> campos;
    private Arbol indice;

    public Tabla(String nombre, List<Campo> campos) {
        this.nombre = nombre;
        this.campos = campos;
        this.indice = new Arbol();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Campo> getCampos() {
        return campos;
    }

    public void setCampos(List<Campo> campos) {
        this.campos = campos;
    }

    public Arbol getIndice() {
        return indice;
    }

    public void setIndice(Arbol indice) {
        this.indice = indice;
    }

    public Campo getCampoPK() {
        for (Campo c : campos) {
            if (c.isEsPK()) return c;
        }
        return null;
    }

    // Valida y convierte los datos según el esquema antes de operar en el árbol
    public Fila construirYValidarFila(Map<String, Object> valoresCrudos) {
        Map<String, Object> datosValidados = new LinkedHashMap<>();

        for (Campo c : campos) {
            if (!valoresCrudos.containsKey(c.getNombre()) || valoresCrudos.get(c.getNombre()) == null) {
                throw new IllegalArgumentException("Falta el campo obligatorio: '" + c.getNombre() + "'");
            }
            Object valorConvertido = convertirYValidarTipo(valoresCrudos.get(c.getNombre()), c.getTipo(), c.getNombre());
            datosValidados.put(c.getNombre(), valorConvertido);
        }

        return new Fila(datosValidados);
    }

    public Comparable convertirClavePK(Object valorCrudo) {
        Campo pk = getCampoPK();
        if (pk == null) {
            throw new IllegalStateException("La tabla '" + nombre + "' no tiene clave primaria definida.");
        }
        return (Comparable) convertirYValidarTipo(valorCrudo, pk.getTipo(), pk.getNombre());
    }

    private Object convertirYValidarTipo(Object raw, String tipo, String nombreCampo) {
        String s = String.valueOf(raw).trim();
        try {
            return switch (tipo.toUpperCase()) {
                case "INT" -> Integer.parseInt(s);
                case "REAL" -> Double.parseDouble(s);
                case "BOOL" -> {
                    if (!s.equalsIgnoreCase("true") && !s.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException();
                    }
                    yield Boolean.parseBoolean(s);
                }
                case "TEXT" -> s;
                default -> throw new IllegalArgumentException("Tipo no soportado: " + tipo);
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Valor inválido '" + raw + "' para el campo '" + nombreCampo + "' (se esperaba " + tipo + ")."
            );
        }
    }
}