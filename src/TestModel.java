import Model.*;
import java.util.*;

public class TestModel {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Map<String, Tabla> tablas = Persistencia.cargarTodas();
        System.out.println("=== MOTOR DE BASE DE DATOS AVL (CLI TERMINAL) ===");

        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Listar tablas disponibles");
            System.out.println("2. Crear nueva tabla (id INT PK, nombre TEXT)");
            System.out.println("3. Eliminar tabla");
            System.out.println("4. SELECT * (mostrar todos los registros)");
            System.out.println("5. Buscar por clave primaria (PK)");
            System.out.println("6. Buscar por rango (min .. max)");
            System.out.println("7. INSERT (insertar registro)");
            System.out.println("8. UPDATE (actualizar registro)");
            System.out.println("9. DELETE (eliminar registro)");
            System.out.println("10. Ver estructura visual del Árbol AVL");
            System.out.println("0. Salir");
            System.out.print("Elige una opción: ");

            try {
                opcion = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido.");
                continue;
            }

            try {
                switch (opcion) {
                    case 1 -> {
                        tablas = Persistencia.cargarTodas();
                        if (tablas.isEmpty()) {
                            System.out.println("No hay tablas en data/tables.");
                        } else {
                            tablas.values().forEach(t ->
                                System.out.println("- " + t.getNombre() + " (" + t.getIndice().obtenerInOrden().size() + " filas)")
                            );
                        }
                    }
                    case 2 -> {
                        System.out.print("Nombre de la nueva tabla: ");
                        String nombre = sc.nextLine().trim();
                        List<Campo> campos = List.of(
                            new Campo("id", "INT", true),
                            new Campo("nombre", "TEXT", false)
                        );
                        Tabla nueva = new Tabla(nombre, campos);
                        Persistencia.guardarTabla(nueva);
                        tablas.put(nombre, nueva);
                        System.out.println("Tabla '" + nombre + "' creada y guardada.");
                    }
                    case 3 -> {
                        System.out.print("Nombre de la tabla a eliminar: ");
                        String nombre = sc.nextLine().trim();
                        Persistencia.eliminarArchivo(nombre);
                        tablas.remove(nombre);
                        System.out.println("Tabla '" + nombre + "' eliminada.");
                    }
                    case 4 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            List<Fila> filas = t.getIndice().obtenerInOrden();
                            if (filas.isEmpty()) System.out.println("(Tabla vacía)");
                            else filas.forEach(System.out::println);
                        }
                    }
                    case 5 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            System.out.print("Valor de PK a buscar: ");
                            Comparable pk = t.convertirClavePK(sc.nextLine().trim());
                            Fila f = t.getIndice().buscar(pk);
                            System.out.println(f != null ? "Encontrado: " + f : "No existe PK = " + pk);
                        }
                    }
                    case 6 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            System.out.print("PK mínima: ");
                            Comparable min = t.convertirClavePK(sc.nextLine().trim());
                            System.out.print("PK máxima: ");
                            Comparable max = t.convertirClavePK(sc.nextLine().trim());
                            t.getIndice().buscarRango(min, max).forEach(System.out::println);
                        }
                    }
                    case 7 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            Map<String, Object> valores = pedirValoresCampos(t);
                            Fila fila = t.construirYValidarFila(valores);
                            Comparable pk = (Comparable) fila.getValor(t.getCampoPK().getNombre());
                            t.getIndice().insertar(pk, fila);
                            Persistencia.guardarTabla(t);
                            System.out.println("Insertado y guardado: " + fila);
                        }
                    }
                    case 8 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            Map<String, Object> valores = pedirValoresCampos(t);
                            Fila fila = t.construirYValidarFila(valores);
                            Comparable pk = (Comparable) fila.getValor(t.getCampoPK().getNombre());
                            if (t.getIndice().actualizar(pk, fila)) {
                                Persistencia.guardarTabla(t);
                                System.out.println("Actualizado y guardado: " + fila);
                            } else {
                                System.out.println("No existe PK = " + pk);
                            }
                        }
                    }
                    case 9 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            System.out.print("Valor de PK a eliminar: ");
                            Comparable pk = t.convertirClavePK(sc.nextLine().trim());
                            if (t.getIndice().eliminar(pk)) {
                                Persistencia.guardarTabla(t);
                                System.out.println("Eliminado y guardado PK = " + pk);
                            } else {
                                System.out.println("No existe PK = " + pk);
                            }
                        }
                    }
                    case 10 -> {
                        Tabla t = seleccionarTabla(tablas);
                        if (t != null) {
                            System.out.println(t.getIndice().obtenerEstructuraVisual());
                        }
                    }
                    case 0 -> System.out.println("Saliendo del CLI...");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private static Tabla seleccionarTabla(Map<String, Tabla> tablas) {
        System.out.print("Nombre de la tabla: ");
        String nombre = sc.nextLine().trim();
        Tabla t = tablas.get(nombre);
        if (t == null) System.out.println("No se encontró la tabla '" + nombre + "'.");
        return t;
    }

    private static Map<String, Object> pedirValoresCampos(Tabla t) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (Campo c : t.getCampos()) {
            System.out.print("Valor para '" + c.getNombre() + "' (" + c.getTipo() + "): ");
            mapa.put(c.getNombre(), sc.nextLine().trim());
        }
        return mapa;
    }
}