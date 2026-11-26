package Controller;

import Model.Campo;
import Model.Fila;
import Model.Persistencia;
import Model.Tabla;
import java.util.List;
import java.util.Map;

/**
 * Centraliza las operaciones CRUD de registros sobre una tabla persistida.
 * La interfaz solo prepara los datos y muestra el resultado.
 */
public class ControladorRegistros {

    public List<Fila> listar(String nombreTabla) {
        return obtenerTabla(nombreTabla).getIndice().obtenerInOrden();
    }

    public Fila buscar(String nombreTabla, Object claveCruda) {
        Tabla tabla = obtenerTabla(nombreTabla);
        Comparable clave = tabla.convertirClavePK(claveCruda);
        return tabla.getIndice().buscar(clave);
    }

    public List<Fila> buscarRango(String nombreTabla, Object minimoCrudo, Object maximoCrudo) {
        Tabla tabla = obtenerTabla(nombreTabla);
        Comparable minimo = tabla.convertirClavePK(minimoCrudo);
        Comparable maximo = tabla.convertirClavePK(maximoCrudo);
        if (minimo.compareTo(maximo) > 0) {
            throw new IllegalArgumentException("El valor mínimo no puede ser mayor que el máximo.");
        }
        return tabla.getIndice().buscarRango(minimo, maximo);
    }

    public Fila insertar(String nombreTabla, Map<String, Object> valores) {
        Tabla tabla = obtenerTabla(nombreTabla);
        Campo pk = obtenerClavePrimaria(tabla);
        Fila fila = tabla.construirYValidarFila(valores);
        Comparable clave = (Comparable) fila.getValor(pk.getNombre());

        if (tabla.getIndice().existe(clave)) {
            throw new IllegalArgumentException("Ya existe un registro con PK = " + clave + ".");
        }

        tabla.getIndice().insertar(clave, fila);
        Persistencia.guardarTabla(tabla);
        return fila;
    }

    public Fila actualizar(String nombreTabla, Map<String, Object> valores) {
        Tabla tabla = obtenerTabla(nombreTabla);
        Campo pk = obtenerClavePrimaria(tabla);
        Fila fila = tabla.construirYValidarFila(valores);
        Comparable clave = (Comparable) fila.getValor(pk.getNombre());

        if (!tabla.getIndice().actualizar(clave, fila)) {
            throw new IllegalArgumentException("No existe ningún registro con PK = " + clave + " para actualizar.");
        }

        Persistencia.guardarTabla(tabla);
        return fila;
    }

    public boolean eliminar(String nombreTabla, Object claveCruda) {
        Tabla tabla = obtenerTabla(nombreTabla);
        Comparable clave = tabla.convertirClavePK(claveCruda);
        if (!tabla.getIndice().eliminar(clave)) {
            return false;
        }
        Persistencia.guardarTabla(tabla);
        return true;
    }

    private Tabla obtenerTabla(String nombreTabla) {
        if (nombreTabla == null || nombreTabla.trim().isEmpty()) {
            throw new IllegalArgumentException("Escribe el nombre de una tabla.");
        }

        for (Tabla tabla : Persistencia.cargarTodas().values()) {
            if (tabla.getNombre().equalsIgnoreCase(nombreTabla.trim())) {
                return tabla;
            }
        }

        throw new IllegalArgumentException("No existe la tabla '" + nombreTabla.trim() + "' en data/tables.");
    }

    private Campo obtenerClavePrimaria(Tabla tabla) {
        Campo pk = tabla.getCampoPK();
        if (pk == null) {
            throw new IllegalArgumentException("La tabla '" + tabla.getNombre() + "' no tiene clave primaria definida.");
        }
        return pk;
    }
}
