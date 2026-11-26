[Readme.md](https://github.com/user-attachments/files/35484864/Readme.md)
# Motor de Base de Datos Relacional con Indexación AVL

Este repositorio contiene la implementación de un motor de base de datos relacional en memoria con persistencia en disco desarrollado en **Java**. El sistema está estructurado bajo el patrón arquitectónico **Modelo–Vista–Controlador (MVC)** y basa su indexación principal en un **Árbol AVL autobalanceable** implementado desde cero, garantizando una complejidad temporal de $O(\log n)$ en las operaciones de búsqueda exacta, inserción y eliminación, así como búsqueda por rango con poda de ramas.

Para garantizar la integridad referencial de cada tabla, el árbol AVL exige **unicidad estricta en la clave primaria (PK)** (rechazando inserciones con claves duplicadas) y respalda cada operación de escritura mediante persistencia atómica en formato JSON.

## Arquitectura del Proyecto

El código fuente se encuentra organizado en la carpeta `src/` bajo tres paquetes principales y dos puntos de prueba/consola:

* **`Controller`**:
  * `Main.java`: Punto de entrada principal que lanza la interfaz gráfica en el hilo de eventos de Swing (`SwingUtilities.invokeLater`).
  * `ControladorRegistros.java`: Orquestador de las operaciones CRUD y consultas (`listar`, `buscar`, `buscarRango`, `insertar`, `actualizar`, `eliminar`) delegando las validaciones y el guardado al modelo.

* **`Model`**:
  * `Arbol.java` y `NodoArbol.java`: Implementación propia del índice AVL genérico (`Comparable` $\rightarrow$ `Fila`) con cálculo de alturas, factores de equilibrio, rotaciones simples y dobles (LL, RR, LR, RL), búsqueda por rango y recorridos (`InOrden`, `PreOrden`, `PostOrden`).
  * `Tabla.java`, `Campo.java` y `Fila.java`: Gestión de esquemas relacionales, metadatos de columnas, validación estricta de tipos y encapsulamiento ordenado de registros.
  * `Persistencia.java`: Serialización y deserialización en JSON dentro de `data/tables/` mediante escritura atómica (`.json.tmp` + `ATOMIC_MOVE`) y analizador léxico propio (`JsonReader`) sin librerías externas.

* **`View`**:
  * `VentanaPrincipal.java`: Ventana principal en Java Swing basada en `CardLayout`.
  * `ConsolaInicio.java`, `ConsolaTablas.java`, `ConsolaRegistros.java`, `ConsolaAVL.java` y `ConsolaAyuda.java`: Interfaces gráficas para administrar esquemas, ejecutar operaciones CRUD, explorar la estructura visual del árbol AVL y consultar la guía rápida.

* **Puntos de Ejecución Auxiliares (`src/`)**:
  * `TestModel.java`: Interfaz interactiva de línea de comandos (CLI / menú en terminal) para operar el motor sin interfaz gráfica.
  * `SuitePruebas.java`: Suite de pruebas automatizadas que valida inserción, búsqueda, rango, actualización, eliminación, recuperación tras reinicio y manejo de casos inválidos.

## Requisitos Previos

* **Java Development Kit (JDK) 17 o superior** (utiliza expresiones `switch` modernas y `Files.writeString` / `Files.readString`).
* **Visual Studio Code**, **Apache NetBeans**, **IntelliJ IDEA** o **Eclipse IDE** (o terminal con `javac` y `java`).
* **Git** para clonar el repositorio.

## Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/Xyyloon/motor-bd-avl.git
cd motor-bd-avl
```

### 2. Compilar el proyecto desde terminal

Desde la carpeta raíz del proyecto, compila todas las clases hacia el directorio `bin/`:

* **En Windows (PowerShell / CMD):**
  ```powershell
  javac -encoding UTF-8 -d bin src\Model\*.java src\Controller\*.java src\View\*.java src\TestModel.java src\SuitePruebas.java
  ```

* **En Linux / macOS:**
  ```bash
  javac -encoding UTF-8 -d bin src/Model/*.java src/Controller/*.java src/View/*.java src/TestModel.java src/SuitePruebas.java
  ```

### 3. Modos de ejecución

1. **Interfaz Gráfica (GUI Swing):**
   ```bash
   java -cp bin Controller.Main
   ```
   *(También puedes abrir `src/Controller/Main.java` en tu IDE y ejecutar `Run`).*

2. **Interfaz de Línea de Comandos (CLI en terminal):**
   ```bash
   java -cp bin TestModel
   ```

3. **Suite de Pruebas Automatizadas:**
   ```bash
   java -cp bin SuitePruebas
   ```

## Operaciones Soportadas (GUI y CLI)

El sistema permite operar sobre las tablas almacenadas en `data/tables/` tanto desde las consolas gráficas (`ConsolaTablas`, `ConsolaRegistros`, `ConsolaAVL`) como desde el menú interactivo en terminal (`TestModel`):

### Gestión de Tablas (DDL)

* **Listar Tablas (`Listar Tablas` / Opción 1 CLI):** Muestra todas las tablas persistidas en `data/tables/`, indicando la cantidad de campos y registros indexados.
* **Ver Esquema (`Ver Campos`):** Lista las columnas de una tabla, sus tipos de dato y marca con `[PK]` la clave primaria.
* **Crear Tabla (`Crear Tabla` / Opción 2 CLI):** Crea un nuevo espacio de almacenamiento con esquema base (`id` `INT [PK]`, `nombre` `TEXT`) y genera su archivo `<nombre>.json`.
* **Eliminar Tabla (`Eliminar Tabla` / Opción 3 CLI):** Borra el archivo JSON correspondiente en `data/tables/` y libera la tabla de memoria.

### Manipulación y Consulta de Registros (DML)

* **Insertar Registro (`Insertar` / Opción 7 CLI):**
  * Formato en GUI: `id=4, nombre=Delirio, precio=25`
  * Valida que todos los campos obligatorios estén presentes, convierte cada valor al tipo definido en el esquema, rechaza claves primarias duplicadas y guarda atómicamente en disco.
* **Consultar Todos (`SELECT *` / Opción 4 CLI):** Retorna todos los registros ordenados ascendentemente por su clave primaria mediante el recorrido **In-Order** del árbol AVL.
* **Buscar por Clave Primaria (`Buscar PK` / Opción 5 CLI):** Localiza un registro exacto en tiempo $O(\log n)$ ingresando el valor de su PK (ej. `13`).
* **Buscar por Rango (`Rango (min..max)` / Opción 6 CLI):**
  * Formato en GUI: `5..10`
  * Recupera todos los registros cuya clave primaria esté en el intervalo `[min, max]`, podando las ramas del árbol que quedan fuera de rango.
* **Actualizar Registro (`Actualizar` / Opción 8 CLI):**
  * Formato en GUI: `id=1, nombre=Cien años de soledad, precio=45`
  * Actualiza los valores asociados a una clave primaria existente y persiste los cambios. *(La clave primaria identifica el registro y no se modifica en sitio).*
* **Eliminar Registro (`Eliminar` / Opción 9 CLI):** Elimina el registro asociado a la clave primaria indicada (ej. `4`), ejecuta el rebalanceo AVL (casos hoja, un hijo o sucesor In-Order) y actualiza el archivo JSON.

### Exploración del Índice AVL

* **Estructura Visual (`Estructura` / Opción 10 CLI):** Dibuja jerárquicamente el árbol AVL mostrando en cada nodo su clave, altura (`h`) y factor de equilibrio (`fe ∈ {-1, 0, 1}`).
* **Recorridos (`InOrden`, `PreOrden`, `PostOrden`):** Permite auditar el orden de visita de los nodos desde `ConsolaAVL`.

## Tipos de Datos Soportados

| Tipo | Descripción | Ejemplos válidos |
| :--- | :--- | :--- |
| **`INT`** | Números enteros con signo (`Integer`). | `1`, `20`, `-3` |
| **`REAL`** | Números decimales de punto flotante (`Double`). | `3.7`, `19.99`, `-0.5` |
| **`TEXT`** | Cadenas de texto (`String`). | `Cien años de soledad`, `Dune` |
| **`BOOL`** | Valores lógicos estrictos (`Boolean`). | `true`, `false` |

## Datasets Incluidos (`data/tables/`)

El repositorio incluye dos conjuntos de datos en formato JSON listos para probar y demostrar el comportamiento del árbol AVL:

1. **`libros.json`**: Tabla con esquema `id (INT PK)`, `nombre (TEXT)` y `precio (INT)`, poblada con registros secuenciales ideales para observar las rotaciones automáticas del árbol AVL.
2. **`autores.json`**: Tabla con esquema `id (INT PK)` y `nombre (TEXT)` utilizada para pruebas de inserción, claves negativas y consultas.

## Tecnologías Utilizadas

* **Lenguaje:** Java (JDK 17+, 100% librerías estándar: `java.io`, `java.nio`, `java.util`, `javax.swing`, `java.awt`).
* **Arquitectura:** Modelo–Vista–Controlador (MVC).
* **Estructura de Datos:** Árbol AVL propio (Autobalanceable).
* **Persistencia:** Archivos JSON con escritura atómica (`.tmp` + `ATOMIC_MOVE`).
* **Control de Versiones:** Git / GitHub.

## Ejemplo Rápido de Uso (Consola Registros)

1. Inicia la aplicación con `java -cp bin Controller.Main` y entra a **REGISTROS / COMANDOS**.
2. En el campo **Tabla:** escribe `libros`.
3. Ejecuta las siguientes acciones usando el campo inferior:
   * Pulsa **`SELECT *`** para listar todo el catálogo ordenado por `id`.
   * Escribe `4` y pulsa **`Buscar PK`** para obtener `{id=4, nombre=Delirio, precio=25}`.
   * Escribe `5..10` y pulsa **`Rango (min..max)`** para filtrar los libros con `id` entre 5 y 10.
   * Escribe `id=21, nombre=Pedro Páramo, precio=28` y pulsa **`Insertar`**.
   * Dirígete a **ARBOL AVL**, escribe `libros` y pulsa **`Estructura`** para verificar que todos los nodos conservan `fe ∈ {-1, 0, 1}`.

## Autores

* **Kevin Santiago Silva Gonzalez**
* **Juan David Merchan**
* **Eliana Andrea Cárdenas**
