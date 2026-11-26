package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Persistencia {

    private static final String TABLES_DIR = "data" + File.separator + "tables";

    // Guarda la tabla usando un archivo temporal (.tmp) y movimiento atómico para evitar corrupción
    public static void guardarTabla(Tabla tabla) {
        try {
            Files.createDirectories(Path.of(TABLES_DIR));
            Path rutaFinal = Path.of(TABLES_DIR + File.separator + tabla.getNombre() + ".json");
            Path rutaTemp = Path.of(TABLES_DIR + File.separator + tabla.getNombre() + ".json.tmp");

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"nombre\": ").append(jsonString(tabla.getNombre())).append(",\n");

            // Esquema
            sb.append("  \"campos\": [\n");
            List<Campo> campos = tabla.getCampos();
            for (int i = 0; i < campos.size(); i++) {
                Campo c = campos.get(i);
                sb.append("    {")
                  .append(" \"nombre\": ").append(jsonString(c.getNombre())).append(",")
                  .append(" \"tipo\": ").append(jsonString(c.getTipo())).append(",")
                  .append(" \"esPK\": ").append(c.isEsPK())
                  .append(" }");
                if (i < campos.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            // Filas en recorrido InOrden del AVL
            sb.append("  \"filas\": [\n");
            List<Fila> filas = tabla.getIndice().obtenerInOrden();
            for (int i = 0; i < filas.size(); i++) {
                sb.append("    {");
                List<Map.Entry<String, Object>> entradas =new ArrayList<>(filas.get(i).getDatos().entrySet());
                for (int j = 0; j < entradas.size(); j++) {
                    String k = entradas.get(j).getKey();
                    Object v = entradas.get(j).getValue();
                    sb.append(" ").append(jsonString(k)).append(": ").append(jsonValor(v));
                    if (j < entradas.size() - 1) sb.append(",");
                }
                sb.append(" }");
                if (i < filas.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n}");

            // Escritura atómica
            Files.writeString(rutaTemp, sb.toString());
            try {
                Files.move(rutaTemp, rutaFinal, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                Files.move(rutaTemp, rutaFinal, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la tabla '" + tabla.getNombre() + "': " + e.getMessage());
        }
    }

    public static void eliminarArchivo(String nombreTabla) {
        try {
            Files.deleteIfExists(Path.of(TABLES_DIR + File.separator + nombreTabla + ".json"));
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar archivo de '" + nombreTabla + "': " + e.getMessage());
        }
    }

    public static Map<String, Tabla> cargarTodas() {
        Map<String, Tabla> tablas = new LinkedHashMap<>();
        File dir = new File(TABLES_DIR);
        if (!dir.exists()) return tablas;

        File[] archivos = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (archivos == null) return tablas;

        for (File archivo : archivos) {
            try {
                String contenido = Files.readString(archivo.toPath());
                Tabla tabla = parsearTabla(contenido);
                if (tabla != null) tablas.put(tabla.getNombre(), tabla);
            } catch (Exception e) {
                System.err.println("WARN: no se pudo cargar '" + archivo.getName() + "': " + e.getMessage());
            }
        }
        return tablas;
    }

    @SuppressWarnings("unchecked")
    private static Tabla parsearTabla(String json) {
        JsonReader jr = new JsonReader(json);
        Map<String, Object> raiz = jr.leerObjeto();

        String nombre = (String) raiz.get("nombre");
        List<Campo> campos = new ArrayList<>();
        List<Object> camposRaw = (List<Object>) raiz.get("campos");

        for (Object obj : camposRaw) {
            Map<String, Object> c = (Map<String, Object>) obj;
            campos.add(new Campo(
                (String) c.get("nombre"),
                (String) c.get("tipo"),
                Boolean.parseBoolean(String.valueOf(c.get("esPK")))
            ));
        }

        Tabla tabla = new Tabla(nombre, campos);
        Campo pk = tabla.getCampoPK();

        List<Object> filasRaw = (List<Object>) raiz.get("filas");
        for (Object obj : filasRaw) {
            Map<String, Object> filaRaw = (Map<String, Object>) obj;
            Fila fila = tabla.construirYValidarFila(filaRaw);

            if (pk != null) {
                Comparable clavePK = (Comparable) fila.getValor(pk.getNombre());
                tabla.getIndice().insertar(clavePK, fila);
            }
        }

        return tabla;
    }

    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r") + "\"";
    }

    private static String jsonValor(Object v) {
        if (v instanceof String)  return jsonString((String) v);
        if (v instanceof Boolean) return v.toString();
        if (v instanceof Number)  return v.toString();
        return jsonString(String.valueOf(v));
    }

    private static class JsonReader {
        private final String src;
        private int pos;

        JsonReader(String src) {
            this.src = src;
            this.pos = 0;
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        private char consume() {
            return src.charAt(pos++);
        }

        Object leerValor() {
            skipWhitespace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return leerObjeto();
            if (c == '[') return leerArray();
            if (c == '"') return leerString();
            if (c == 't' || c == 'f') return leerBoolean();
            if (c == 'n') { pos += 4; return null; }
            return leerNumero();
        }

        Map<String, Object> leerObjeto() {
            Map<String, Object> map = new LinkedHashMap<>();
            consume(); // '{'
            skipWhitespace();
            if (src.charAt(pos) == '}') { consume(); return map; }
            while (true) {
                skipWhitespace();
                String clave = leerString();
                skipWhitespace();
                consume(); // ':'
                Object valor = leerValor();
                map.put(clave, valor);
                skipWhitespace();
                char next = consume();
                if (next == '}') break;
            }
            return map;
        }

        List<Object> leerArray() {
            List<Object> list = new ArrayList<>();
            consume(); // '['
            skipWhitespace();
            if (src.charAt(pos) == ']') { consume(); return list; }
            while (true) {
                list.add(leerValor());
                skipWhitespace();
                char next = consume();
                if (next == ']') break;
            }
            return list;
        }

        String leerString() {
            consume();
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = consume();
                if (c == '"') break;
                if (c == '\\') {
                    char esc = consume();
                    switch (esc) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case 'n'  -> sb.append('\n');
                        case 'r'  -> sb.append('\r');
                        case 't'  -> sb.append('\t');
                        default   -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        boolean leerBoolean() {
            if (src.startsWith("true", pos))  { pos += 4; return true; }
            if (src.startsWith("false", pos)) { pos += 5; return false; }
            throw new RuntimeException("Boolean inválido en pos " + pos);
        }

        Object leerNumero() {
            int inicio = pos;
            if (pos < src.length() && src.charAt(pos) == '-') pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            boolean esDecimal = pos < src.length() && src.charAt(pos) == '.';
            if (esDecimal) {
                pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            String num = src.substring(inicio, pos);
            if (esDecimal) return Double.parseDouble(num);
            return Integer.parseInt(num);
        }
    }
}