import java.util.ArrayList;
import java.util.List;

public class Arbol {
    private NodoArbol raiz;

    public Arbol() {
        this.raiz = null;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public void insertar(Comparable clave, Fila dato) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave primaria no puede ser nula.");
        }
        if (raiz == null) {
            raiz = new NodoArbol(clave, dato);
        } else {
            raiz = raiz.insertar(clave, dato);
        }
    }

    public Fila buscar(Comparable clave) {
        if (raiz == null || clave == null) {
            return null;
        }
        NodoArbol encontrado = raiz.buscar(clave);
        return (encontrado != null) ? encontrado.getDato() : null;
    }

    public boolean existe(Comparable clave) {
        return buscar(clave) != null;
    }

    public List<Fila> buscarRango(Comparable min, Comparable max) {
        List<Fila> resultados = new ArrayList<>();
        if (raiz != null && min != null && max != null) {
            raiz.buscarRango(min, max, resultados);
        }
        return resultados;
    }

    public boolean actualizar(Comparable clave, Fila nuevoDato) {
        if (raiz == null || clave == null) {
            return false;
        }
        NodoArbol nodo = raiz.buscar(clave);
        if (nodo != null) {
            nodo.setDato(nuevoDato);
            return true;
        }
        return false;
    }

    public boolean eliminar(Comparable clave) {
        if (raiz == null || clave == null || !existe(clave)) {
            return false;
        }
        raiz = raiz.eliminar(clave);
        return true;
    }

    // Devuelve todas las filas ordenadas por clave primaria (para SELECT * y Persistencia)
    public List<Fila> obtenerInOrden() {
        List<Fila> lista = new ArrayList<>();
        inOrden(raiz, lista);
        return lista;
    }

    private void inOrden(NodoArbol nodo, List<Fila> lista) {
        if (nodo != null) {
            inOrden(nodo.getIzquierdo(), lista);
            lista.add(nodo.getDato());
            inOrden(nodo.getDerecho(), lista);
        }
    }
    // Devuelve todas las filas en PreOrden y PostOrden (útil para depuración o persistencia)
    public List<Fila> obtenerPreOrden() {
    List<Fila> lista = new ArrayList<>();
    preOrdenFilas(raiz, lista);
    return lista;
}

private void preOrdenFilas(NodoArbol nodo, List<Fila> lista) {
    if (nodo != null) {
        lista.add(nodo.getDato());
        preOrdenFilas(nodo.getIzquierdo(), lista);
        preOrdenFilas(nodo.getDerecho(), lista);
    }
}

public List<Fila> obtenerPostOrden() {
    List<Fila> lista = new ArrayList<>();
    postOrdenFilas(raiz, lista);
    return lista;
}

private void postOrdenFilas(NodoArbol nodo, List<Fila> lista) {
    if (nodo != null) {
        postOrdenFilas(nodo.getIzquierdo(), lista);
        postOrdenFilas(nodo.getDerecho(), lista);
        lista.add(nodo.getDato());
    }
}

    // Devuelve las claves en PreOrden (útil si necesitan depurar o persistir topología)
    public List<Comparable> obtenerClavesPreOrden() {
        List<Comparable> claves = new ArrayList<>();
        preOrden(raiz, claves);
        return claves;
    }

    private void preOrden(NodoArbol nodo, List<Comparable> claves) {
        if (nodo != null) {
            claves.add(nodo.getClave());
            preOrden(nodo.getIzquierdo(), claves);
            preOrden(nodo.getDerecho(), claves);
        }
    }

    // Devuelve la estructura visual como String para que la Vista la imprima sin acoplar System.out al Modelo
    public String obtenerEstructuraVisual() {
        if (raiz == null) {
            return "(árbol vacío)";
        }
        StringBuilder sb = new StringBuilder();
        construirEstructura(raiz, 0, sb);
        return sb.toString();
    }

    private void construirEstructura(NodoArbol nodo, int nivel, StringBuilder sb) {
        if (nodo == null) return;
        construirEstructura(nodo.getDerecho(), nivel + 1, sb);
        sb.append("    ".repeat(nivel))
          .append(nodo.getClave())
          .append(" [h=").append(nodo.getAltura())
          .append(", fe=").append(nodo.getEquilibrio()).append("]\n");
        construirEstructura(nodo.getIzquierdo(), nivel + 1, sb);
    }
}