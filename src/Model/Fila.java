package Model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Fila {
    private Map<String, Object> datos;

    public Fila(Map<String, Object> datos) {
        this.datos = new LinkedHashMap<>(datos); // Conserva el orden de las columnas
    }

    public Map<String, Object> getDatos() {
        return datos;
    }

    public Object getValor(String campo) {
        return datos.get(campo);
    }

    public void setValor(String campo, Object valor) {
        datos.put(campo, valor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        datos.forEach((k, v) -> sb.append(k).append(": ").append(v).append("  "));
        return sb.append("}").toString();
    }
}