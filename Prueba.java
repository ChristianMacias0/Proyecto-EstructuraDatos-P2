public class Prueba {
    public static void main(String[] args) {
    GrafoLista<String> grafoDirigido = new GrafoLista<>(true);
        grafoDirigido.agregarVertice("A");
        grafoDirigido.agregarVertice("B");
        grafoDirigido.agregarVertice("C");
        grafoDirigido.agregarVertice("D");

        grafoDirigido.agregarArco("A", "B", 1);
        grafoDirigido.agregarArco("A", "C", 3);
        grafoDirigido.agregarArco("B", "C", 1);
        grafoDirigido.agregarArco("B", "D", 4);
        grafoDirigido.agregarArco("C", "D", 2);
        
        grafoDirigido.getGraphstream().display();
    }
}
