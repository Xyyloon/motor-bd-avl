
import java.util.List;

public class NodoArbol {
    private Comparable clave;
    private Fila dato;
    private NodoArbol izquierdo;
    private NodoArbol derecho;
    private int altura;

    public NodoArbol(Comparable clave, Fila dato) {
        this.clave = clave;
        this.dato = dato;
        this.izquierdo = null;
        this.derecho = null;
        this.altura = 0; // Hoja con altura 0, igual que tu lógica original
    }

    public Comparable getClave() {
        return clave;
    }

    public Fila getDato() {
        return dato;
    }

    public void setDato(Fila dato) {
        this.dato = dato;
    }

    public NodoArbol getIzquierdo() {
        return izquierdo;
    }

    public NodoArbol getDerecho() {
        return derecho;
    }

    public int getAltura() {
        return altura(this);
    }

    public int getEquilibrio() {
        return equilibrio(this);
    }

    // Asignar altura -1 si no tiene hijos
    private int altura(NodoArbol nodo) {
        return nodo != null ? nodo.altura : -1;
    }

    // Actualiza la altura de su subárbol
    private void nuevaAltura(NodoArbol nodo) {
        int altHijoIzq = altura(nodo.izquierdo);
        int altHijoDer = altura(nodo.derecho);
        nodo.altura = Math.max(altHijoDer, altHijoIzq) + 1;
    }

    // Obtener factor de equilibrio
    private int equilibrio(NodoArbol nodo) {
        return altura(nodo.derecho) - altura(nodo.izquierdo);
    }

    // Rotación a la derecha
    private NodoArbol rotarDer(NodoArbol nodo) {
        NodoArbol hijoIzq = nodo.izquierdo;

        nodo.izquierdo = hijoIzq.derecho;
        hijoIzq.derecho = nodo;

        nuevaAltura(nodo);
        nuevaAltura(hijoIzq);

        return hijoIzq;
    }

    // Rotación a la izquierda
    private NodoArbol rotarIzq(NodoArbol nodo) {
        NodoArbol hijoDer = nodo.derecho;

        nodo.derecho = hijoDer.izquierdo;
        hijoDer.izquierdo = nodo;

        nuevaAltura(nodo);
        nuevaAltura(hijoDer);

        return hijoDer;
    }

    private NodoArbol autobalance(NodoArbol nodo) {
        int factEquilibrio = equilibrio(nodo);

        if (factEquilibrio < -1) { // Caso - -
            if (equilibrio(nodo.izquierdo) <= 0) {
                nodo = rotarDer(nodo);
            } else { // Caso - +
                nodo.izquierdo = rotarIzq(nodo.izquierdo);
                nodo = rotarDer(nodo);
            }
        }
        if (factEquilibrio > 1) { // Caso + +
            if (equilibrio(nodo.derecho) >= 0) {
                nodo = rotarIzq(nodo);
            } else { // Caso + -
                nodo.derecho = rotarDer(nodo.derecho);
                nodo = rotarIzq(nodo);
            }
        }
        return nodo;
    }

    @SuppressWarnings("unchecked")
    public NodoArbol insertar(Comparable nuevaClave, Fila nuevoDato) {
        int cmp = nuevaClave.compareTo(this.clave);

        if (cmp < 0) {
            if (this.izquierdo == null) {
                this.izquierdo = new NodoArbol(nuevaClave, nuevoDato);
            } else {
                this.izquierdo = this.izquierdo.insertar(nuevaClave, nuevoDato);
            }
        } else if (cmp > 0) {
            if (this.derecho == null) {
                this.derecho = new NodoArbol(nuevaClave, nuevoDato);
            } else {
                this.derecho = this.derecho.insertar(nuevaClave, nuevoDato);
            }
        } else {
            // Clave primaria duplicada: lanzamos excepción para que el Controlador informe el error
            throw new IllegalArgumentException("Ya existe un registro con la clave primaria: " + nuevaClave);
        }

        // Actualizar altura y auto-balancear al regresar de la recursión
        nuevaAltura(this);
        return autobalance(this);
    }

    // Busca el nodo mínimo del subárbol (sucesor InOrden, usado con 2 hijos)
    public NodoArbol encontrarMinimo() {
        if (this.izquierdo == null) {
            return this;
        }
        return this.izquierdo.encontrarMinimo();
    }

    // Elimina una clave y reequilibra el árbol (reglas AVL)
    @SuppressWarnings("unchecked")
    public NodoArbol eliminar(Comparable claveAEliminar) {
        int cmp = claveAEliminar.compareTo(this.clave);

        if (cmp < 0) {
            if (this.izquierdo != null) {
                this.izquierdo = this.izquierdo.eliminar(claveAEliminar);
            }
        } else if (cmp > 0) {
            if (this.derecho != null) {
                this.derecho = this.derecho.eliminar(claveAEliminar);
            }
        } else {
            // Caso 1: hoja
            if (this.izquierdo == null && this.derecho == null) {
                return null;
            }
            // Caso 2: un solo hijo
            if (this.izquierdo == null) {
                return this.derecho;
            }
            if (this.derecho == null) {
                return this.izquierdo;
            }
            // Caso 3: dos hijos -> reemplazar clave y dato por el sucesor InOrden
            NodoArbol sucesor = this.derecho.encontrarMinimo();
            this.clave = sucesor.clave;
            this.dato = sucesor.dato;
            this.derecho = this.derecho.eliminar(sucesor.clave);
        }

        // Al regresar de la recursión: actualizar altura y rebalancear
        nuevaAltura(this);
        return autobalance(this);
    }

    // Búsqueda exacta O(log n)
    @SuppressWarnings("unchecked")
    public NodoArbol buscar(Comparable claveBuscada) {
        int cmp = claveBuscada.compareTo(this.clave);
        if (cmp == 0) {
            return this;
        } else if (cmp < 0) {
            return (this.izquierdo != null) ? this.izquierdo.buscar(claveBuscada) : null;
        } else {
            return (this.derecho != null) ? this.derecho.buscar(claveBuscada) : null;
        }
    }

    // Búsqueda por rango [min, max] con poda de ramas
    @SuppressWarnings("unchecked")
    public void buscarRango(Comparable min, Comparable max, List<Fila> resultados) {
        int cmpMin = this.clave.compareTo(min);
        int cmpMax = this.clave.compareTo(max);

        // Solo explorar izquierda si hay posibilidad de claves >= min
        if (cmpMin > 0 && this.izquierdo != null) {
            this.izquierdo.buscarRango(min, max, resultados);
        }

        // Si está dentro del rango [min, max], agregar fila
        if (cmpMin >= 0 && cmpMax <= 0) {
            resultados.add(this.dato);
        }

        // Solo explorar derecha si hay posibilidad de claves <= max
        if (cmpMax < 0 && this.derecho != null) {
            this.derecho.buscarRango(min, max, resultados);
        }
    }
}