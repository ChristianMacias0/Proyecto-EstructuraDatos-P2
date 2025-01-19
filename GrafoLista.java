
import java.util.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.*;

/**
 *
 * @author miguel
 * @version 3.0
 * @param <V> el tipo de dato de los Vertices
 */
public class GrafoLista<V>{
    
    //---------------- nested Vertex class ----------------
    private class Vertex<V> {
        private V element;
        private LinkedList<Edge<V>> arcos; //outgoing
        private boolean visitados;  //para recorridos

        public Vertex(V element) {
            this.element = element;
            arcos = new LinkedList<>();
            visitados = false;
        }
        public Edge<V> buscarArco(Vertex<V> destino) {
            for (Edge<V>a: arcos) 
                if (a.destino.equals(destino))
                    return a;
            return null;
        }
        public void agregarArco(Vertex<V> destino) {
            agregarArco(destino,1);
        }
        public void agregarArco(Vertex<V> destino, float peso) { //Se puede parametrizar peso E
            if (buscarArco(destino) == null) 
                arcos.add(new Edge<>(destino,peso));
        }
        public void removerArco(Vertex<V> destino) {
            Edge<V> arco = buscarArco(destino);
            if (arco != null) 
                arcos.remove(arco);
        }
        
        @Override
        public String toString() {
            return "Vertice{" + "contenido=" + element + '}';
        }

        public boolean equals(Vertex<V> v) {
            return element.equals(v.element);
        }
        
    }//----------- end of nested Vertex class ---------
    
    
    
    //---------------- nested Edge class ----------------
    private class Edge<V> {
        private Vertex<V> destino;
        private float peso; //Se puede parametrizar peso E

        public Edge(Vertex<V> destino, float peso) {
            this.destino = destino;
            this.peso = peso;
        }
        
        public Edge(Vertex<V> destino) {
            this(destino,1);
        }
        
        @Override
        public String toString() {
            return "Arco{" + "destino=" + destino + ", peso=" + peso + '}';
        }
    }//----------- end of nested Edge class ---------
    
    
    // instance variables of GrafoLista
    private LinkedList<Vertex<V>> vertices;
    private boolean dirigido;
    
    public GrafoLista(){
        this(false);
    }
    public GrafoLista(boolean dirigido){
        this.dirigido = dirigido;
        vertices = new LinkedList<>();
    }

    public boolean isDirigido() {
        return dirigido;
    }
    public void setDirigido(boolean dirigido) {
        this.dirigido = dirigido;
    }
    
    private Vertex<V> searchVertex(V element) {
        for (Vertex<V> v: vertices)
            if (v.element.equals(element))
                return v;
        return null;
    }
    public void agregarVertice(V element){
        if (searchVertex(element) == null)
            vertices.add(new Vertex<>(element));
    }
    public void removerVertice(V element) {
        Vertex<V> v = searchVertex(element);
        if (v != null){
            v.arcos.clear();
            for (Vertex<V> v1 : vertices)
                v1.removerArco(v);
            vertices.remove(v);
        }
    }
    
    public boolean agregarArco(V origen, V destino) {
        return agregarArco(origen, destino,1);
    }
    public boolean agregarArco(V origen, V destino, float peso) {
        Vertex<V> v1 = searchVertex(origen);
        Vertex<V> v2 = searchVertex(destino);
        if (v1 == null || v2 == null) 
            return false;
        v1.agregarArco(v2,peso);
        if (!dirigido) 
            v2.agregarArco(v1,peso);
        return true;
    }
        
    public void removerArco(V elem1, V elem2) {
        Vertex<V> v1 = searchVertex(elem1);
        Vertex<V> v2 = searchVertex(elem2);
        if (v1 == null || v2 == null)
            return;
        v1.removerArco(v2);
        if (!dirigido) {
            v2.removerArco(v1);
        }
    }
    
    public void resetearVisitados() {
        for (Vertex<V> v : vertices)
            v.visitados=false;
    }
    
    @Override
    public String toString() {
        String s = "{\n";
        for (Vertex<V> v: vertices)
            s+= "\t" + v.element + ":" + v.arcos+ "\n";
        s+="}\n";
        return s;
    }
    
    /**
     * Recorrido en anchura Breadth-First Search
     * 
     * @param inicio vertice de inicio del recorrido
     * @return lista de vertices recorridos
     */
    public List<V> BFS(V inicio){
        List<V> retorno =new LinkedList<>();
        
        //Encolar vertice de partida​ y Marcarlo como “visitado”​
        Vertex<V> v = searchVertex(inicio);
        if(v==null)
            return retorno;
        Queue<Vertex<V>> cola= new ArrayDeque<>();
        cola.offer(v);
        v.visitados=true;

        //Mientras la cola no este vacia​
        while(!cola.isEmpty()){
            //Desencolar vertice
            v=cola.poll();
            //Mostrarlo​
            retorno.add(v.element);
            //Marcar como visitados los vertices adyacentes de v que no hayan sido ya visitados​
            //Encolarlos
            for(Edge<V> e: v.arcos){
                v=e.destino;
                if(!v.visitados){
                    cola.offer(v);
                    v.visitados=true;
                }
            }
        }
        resetearVisitados();
        return retorno;
    }

    /**
     * Recorrido en profundidad Depth-First Search
     * 
     * @param inicio vertice de inicio del recorrido
     * @return lista de vertices recorridos
     */
    public List<V> DFS(V inicio) {
        List<V> retorno = new LinkedList<>();
        Vertex<V> v = searchVertex(inicio);
        if (v == null)
            return retorno;
        resetearVisitados();
        
        ArrayDeque<Vertex<V>> pila = new ArrayDeque<>();
        pila.push(v);
        while (!pila.isEmpty()) {
            Vertex<V> actual = pila.pop();
            if (!actual.visitados) {
                actual.visitados = true;
                retorno.add(actual.element);
                ListIterator<Edge<V>> iterador = actual.arcos.listIterator(actual.arcos.size());
                while (iterador.hasPrevious()) {
                    Vertex<V> vecino = iterador.previous().destino;
                    if (!vecino.visitados) {
                        pila.push(vecino);
                    }
                }
            }
        }
        resetearVisitados();
        return retorno;
    }

    // Terminado
    public GrafoLista<V> invertir(){
        GrafoLista<V> invertido = new GrafoLista<>(true);
        for(Vertex<V> v : vertices){
            invertido.agregarVertice(v.element);
        }
        for(Vertex<V> v : vertices){
            for(Edge<V> e : v.arcos){
                invertido.agregarArco(e.destino.element, v.element);
            }
        }
        return invertido;
    }
    
    
    public Graph getGraphstream(){

        System.setProperty("org.graphstream.ui", "swing");
        Graph retorno = new SingleGraph("GrafoLista");

        for(Vertex<V> v : this.vertices){
                retorno.addNode(String.valueOf(v.element));
                retorno.getNode(String.valueOf(v.element)).setAttribute("ui.label", v); 
                retorno.getNode(String.valueOf(v.element)).setAttribute("ui.class", "personalizado"); 
        }

        for (Vertex<V> v : this.vertices) {
            for (Edge<V> e : v.arcos) {
                String edgeId = String.valueOf(v.element + "," + e.destino.element);

                if (!this.dirigido) {
                    String reverseEdgeId = String.valueOf(e.destino.element + "," + v.element);
                    if (retorno.getEdge(reverseEdgeId) == null) {
                        retorno.addEdge(edgeId, String.valueOf(v.element), String.valueOf(e.destino.element));
                    }

                } else {
                    retorno.addEdge(edgeId, String.valueOf(v.element), String.valueOf(e.destino.element), true);
                }
            }
        }
            return retorno;
        }
    
}