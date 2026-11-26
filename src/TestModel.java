
import java.util.*;

import Model.Fila;
import Model.Persistencia;
import Model.Tabla;

public class TestModel {
    public static void main(String[] args) {
        // 1. Cargar las tablas desde data/tables/
        Map<String, Tabla> tablas = Persistencia.cargarTodas();
        Tabla libros = tablas.get("libros");

        if (libros == null) {
            System.out.println("No se encontró la tabla 'libros' en data/tables/. Verifica que el archivo esté allí.");
            return;
        }

        // 2. Ver el árbol AVL construido con los 20 libros insertados en orden (1..20)
        // Sin AVL sería una lista de altura 19; con AVL tendrá altura 4 y fe en {-1, 0, 1}
        System.out.println("=== 1. ÁRBOL AVL RECONSTRUIDO (20 LIBROS) ===");
        System.out.println(libros.getIndice().obtenerEstructuraVisual());

        // 3. Búsqueda exacta por PK (O(log n))
        System.out.println("=== 2. BUSCAR LIBRO ID = 13 (Dune) ===");
        System.out.println(libros.getIndice().buscar(13));

        // 4. Búsqueda por rango de IDs [5, 10]
        System.out.println("\n=== 3. BÚSQUEDA POR RANGO ID [5, 10] ===");
        libros.getIndice().buscarRango(5, 10).forEach(System.out::println);

        // 5. Insertar nuevo libro (id = 21)
        System.out.println("\n=== 4. INSERTAR LIBRO ID = 21 ===");
        Map<String, Object> nuevoLibro = Map.of(
            "id", 21,
            "nombre", "Pedro Páramo",
            "precio", 28
        );
        Fila filaNueva = libros.construirYValidarFila(nuevoLibro);
        libros.getIndice().insertar(21, filaNueva);
        System.out.println("Insertado: " + libros.getIndice().buscar(21));

        // 6. Actualizar precio de un libro (id = 1 -> precio 45)
        System.out.println("\n=== 5. ACTUALIZAR LIBRO ID = 1 (precio -> 45) ===");
        Map<String, Object> libroActualizado = Map.of(
            "id", 1,
            "nombre", "Cien años de soledad",
            "precio", 45
        );
        libros.getIndice().actualizar(1, libros.construirYValidarFila(libroActualizado));
        System.out.println("Actualizado: " + libros.getIndice().buscar(1));

        // 7. Eliminar la raíz actual del árbol (id = 8) y verificar rebalanceo
        System.out.println("\n=== 6. ELIMINAR RAÍZ (ID = 8) Y REBALANCEAR ===");
        libros.getIndice().eliminar(8);
        System.out.println(libros.getIndice().obtenerEstructuraVisual());

        // 8. Recorrido In-Orden completo
        System.out.println("=== 7. CATÁLOGO COMPLETO ORDENADO POR ID ===");
        libros.getIndice().obtenerInOrden().forEach(System.out::println);

        // Descomenta la siguiente línea si quieres guardar los cambios en data/tables/libros.json:
        // Persistencia.guardarTabla(libros);
    }
}