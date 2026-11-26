import Model.*;
import java.util.*;

public class SuitePruebas {
    public static void main(String[] args) {
        System.out.println("=== SUITE DE PRUEBAS AUTOMATIZADAS ===\n");

        // Crear tabla temporal de prueba
        String nombreTabla = "_prueba_avl";
        Tabla tabla = new Tabla(nombreTabla, List.of(
            new Campo("id", "INT", true),
            new Campo("nombre", "TEXT", false),
            new Campo("precio", "REAL", false),
            new Campo("activo", "BOOL", false)
        ));

        // 1. Inserción y recorrido InOrden mostrando todos los registros creados
        for (int i = 1; i <= 10; i++) {
            Fila f = tabla.construirYValidarFila(Map.of(
                "id", i, "nombre", "Item_" + i, "precio", i * 10.5, "activo", true
            ));
            tabla.getIndice().insertar(i, f);
        }
        List<Fila> todos = tabla.getIndice().obtenerInOrden();
        probar("1. Inserción de 10 registros", todos.size() == 10);
        for (Fila f : todos) {
            System.out.println("   -> " + f);
        }

        // 2. Búsqueda exacta mostrando el registro encontrado
        Fila encontrado = tabla.getIndice().buscar(5);
        probar("2. Búsqueda exacta (id = 5)", encontrado != null);
        System.out.println("   Encontrado: " + encontrado);

        // 3. Búsqueda por rango mostrando los elementos dentro de [3..7]
        List<Fila> enRango = tabla.getIndice().buscarRango(3, 7);
        probar("3. Búsqueda por rango [3..7]", enRango.size() == 5);
        for (Fila f : enRango) {
            System.out.println("   -> " + f);
        }

        // 4. Actualización mostrando el antes y el después
        Fila antes = tabla.getIndice().buscar(5);
        String estadoAntes = (antes != null) ? antes.toString() : "null";

        Fila editada = tabla.construirYValidarFila(Map.of(
            "id", 5, "nombre", "Editado", "precio", 99.0, "activo", false
        ));
        tabla.getIndice().actualizar(5, editada);

        Fila despues = tabla.getIndice().buscar(5);
        probar("4. Actualización (id = 5)", despues != null && "Editado".equals(despues.getValor("nombre")));
        System.out.println("   Antes:   " + estadoAntes);
        System.out.println("   Después: " + despues);

        // 5. Eliminación simple de un registro (id = 3) mostrando los restantes
        boolean eliminado = tabla.getIndice().eliminar(3);
        List<Fila> restantes = tabla.getIndice().obtenerInOrden();
        probar("5. Eliminación simple (id = 3)", eliminado && !tabla.getIndice().existe(3) && restantes.size() == 9);
        System.out.println("   Registros restantes (" + restantes.size() + "):");
        for (Fila f : restantes) {
            System.out.println("   -> " + f);
        }

        // 6. Persistencia y recuperación tras reinicio
        Persistencia.guardarTabla(tabla);
        Tabla recuperada = Persistencia.cargarTodas().get(nombreTabla);

        boolean okPersistencia = recuperada != null
            && recuperada.getIndice().obtenerInOrden().size() == 9
            && !recuperada.getIndice().existe(3)
            && "Editado".equals(recuperada.getIndice().buscar(5).getValor("nombre"));

        probar("6. Persistencia y recuperación JSON", okPersistencia);
        if (recuperada != null) {
            System.out.println("   Archivo leído: data/tables/" + nombreTabla + ".json");
            System.out.println("   Filas recuperadas (" + recuperada.getIndice().obtenerInOrden().size() + "):");
            for (Fila f : recuperada.getIndice().obtenerInOrden()) {
                System.out.println("   -> " + f);
            }
        }

        // 7. Caso de fallo intencional (buscar PK inexistente id = 999)
        probar("7. Búsqueda de clave inexistente (id = 999)", tabla.getIndice().buscar(999) != null);

        // Limpiar archivo temporal de prueba
        Persistencia.eliminarArchivo(nombreTabla);
    }

    private static void probar(String caso, boolean ok) {
        System.out.println("\n" + (ok ? "[OK] " : "[FALLO] ") + caso);
    }
}