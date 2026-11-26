import Model.*;
import java.util.*;

public class SuitePruebas {
    private static int pasadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== EJECUTANDO SUITE DE PRUEBAS AUTOMATIZADAS ===\n");

        String nombreTabla = "_test_suite_tmp";
        List<Campo> esquema = List.of(
            new Campo("id", "INT", true),
            new Campo("titulo", "TEXT", false),
            new Campo("puntaje", "REAL", false),
            new Campo("activo", "BOOL", false)
        );

        Tabla tabla = new Tabla(nombreTabla, esquema);

        // PRUEBA 1: Inserción secuencial y autobalanceo AVL (peor caso BST)
        for (int i = 1; i <= 15; i++) {
            Fila f = tabla.construirYValidarFila(Map.of(
                "id", i,
                "titulo", "Item_" + i,
                "puntaje", i * 1.5,
                "activo", i % 2 == 0
            ));
            tabla.getIndice().insertar(i, f);
        }
        verificar("1. Inserción de 15 registros ordenados y conteo InOrden",
            tabla.getIndice().obtenerInOrden().size() == 15);

        // PRUEBA 2: Rechazo de clave primaria duplicada
        boolean rechazoDuplicado = false;
        try {
            Fila dup = tabla.construirYValidarFila(Map.of(
                "id", 5, "titulo", "Dup", "puntaje", 1.0, "activo", true
            ));
            tabla.getIndice().insertar(5, dup);
        } catch (IllegalArgumentException e) {
            rechazoDuplicado = true;
        }
        verificar("2. Bloqueo de clave primaria duplicada (PK = 5)", rechazoDuplicado);

        // PRUEBA 3: Validación estricta de tipos de dato
        boolean rechazoTipo = false;
        try {
            tabla.construirYValidarFila(Map.of(
                "id", "no_es_entero", "titulo", "Error", "puntaje", 2.0, "activo", false
            ));
        } catch (IllegalArgumentException e) {
            rechazoTipo = true;
        }
        verificar("3. Validación de esquema y rechazo de tipo inválido", rechazoTipo);

        // PRUEBA 4: Búsqueda exacta O(log n)
        Fila encontrada = tabla.getIndice().buscar(10);
        verificar("4. Búsqueda exacta por PK existente (id = 10) e inexistente (id = 99)",
            encontrada != null && "Item_10".equals(encontrada.getValor("titulo")) && tabla.getIndice().buscar(99) == null);

        // PRUEBA 5: Búsqueda por rango [4, 8]
        List<Fila> rango = tabla.getIndice().buscarRango(4, 8);
        verificar("5. Búsqueda por rango [4..8] devuelve exactamente 5 registros",
            rango.size() == 5 && (int) rango.get(0).getValor("id") == 4 && (int) rango.get(4).getValor("id") == 8);

        // PRUEBA 6: Actualización (UPDATE)
        Fila actualizada = tabla.construirYValidarFila(Map.of(
            "id", 10, "titulo", "Item_10_Editado", "puntaje", 99.9, "activo", true
        ));
        boolean okUpdate = tabla.getIndice().actualizar(10, actualizada);
        verificar("6. Actualización de registro existente (id = 10)",
            okUpdate && "Item_10_Editado".equals(tabla.getIndice().buscar(10).getValor("titulo")));

        // PRUEBA 7: Eliminación (DELETE) de hoja, nodo con 1 hijo y raíz (2 hijos)
        boolean delHoja = tabla.getIndice().eliminar(1);
        boolean delRaiz = tabla.getIndice().eliminar(8);
        verificar("7. Eliminación con rebalanceo AVL (id = 1 y raíz id = 8)",
            delHoja && delRaiz && !tabla.getIndice().existe(8) && tabla.getIndice().obtenerInOrden().size() == 13);

        // PRUEBA 8: Persistencia atómica y recuperación tras reinicio
        Persistencia.guardarTabla(tabla);
        Map<String, Tabla> recargadas = Persistencia.cargarTodas();
        Tabla recuperada = recargadas.get(nombreTabla);
        verificar("8. Persistencia JSON y reconstrucción completa tras reinicio",
            recuperada != null
            && recuperada.getIndice().obtenerInOrden().size() == 13
            && "Item_10_Editado".equals(recuperada.getIndice().buscar(10).getValor("titulo")));

        // Limpieza del archivo temporal de pruebas
        Persistencia.eliminarArchivo(nombreTabla);

        System.out.println("\n=== RESUMEN DE PRUEBAS ===");
        System.out.println("Pasadas: " + pasadas + " | Fallidas: " + fallidas);
    }

    private static void verificar(String nombrePrueba, boolean condicion) {
        if (condicion) {
            pasadas++;
            System.out.println("[PASS] " + nombrePrueba);
        } else {
            fallidas++;
            System.out.println("[FAIL] " + nombrePrueba);
        }
    }
}