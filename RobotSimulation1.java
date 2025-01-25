import org.graphstream.graph.*;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RobotSimulation1 {
    private GrafoLista<String> grafo;
    private Point robotPosition;
    private Point goalPosition;
    private List<List<Point>> obstacles = new ArrayList<>();

    public RobotSimulation1(String filePath) {
        grafo = new GrafoLista<>(false); // Grafo no dirigido
        loadConfiguration(filePath);
        buildGraph();
    }

    private void loadConfiguration(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Leer posición inicial del robot
            String[] robotPos = reader.readLine().replace("(", "").replace(")", "").split(",");
            robotPosition = new Point(Integer.parseInt(robotPos[0]), Integer.parseInt(robotPos[1]));

            // Leer posición de la meta
            String[] goalPos = reader.readLine().replace("(", "").replace(")", "").split(",");
            goalPosition = new Point(Integer.parseInt(goalPos[0]), Integer.parseInt(goalPos[1]));

            // Leer obstáculos
            String line;
            while ((line = reader.readLine()) != null) {
                String[] obstaclePositions = line.split(";");
                List<Point> rectangle = new ArrayList<>();
                for (String obstacle : obstaclePositions) {
                    String[] coords = obstacle.replace("(", "").replace(")", "").split(",");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    rectangle.add(new Point(x, y));
                }
                obstacles.add(rectangle);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al leer el archivo de configuración: " + e.getMessage());
        }
    }

    private void buildGraph() {
        // Agregar el robot y la meta como nodos
        grafo.agregarVertice("Robot");
        grafo.agregarVertice("Goal");

        // Agregar los nodos de los obstáculos
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                String nodeId = "Obstacle" + obstacleCounter + "_" + i;
                grafo.agregarVertice(nodeId);
            }
            obstacleCounter++;
        }

        // Conectar las esquinas de los mismos obstáculos
        connectObstacleCorners();

        // Conectar nodos visibles entre sí
        connectVisibleNodes();
    }

    private void connectObstacleCorners() {
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                String nodeA = "Obstacle" + obstacleCounter + "_" + i;
                String nodeB = "Obstacle" + obstacleCounter + "_" + ((i + 1) % rectangle.size());
                grafo.agregarArco(nodeA, nodeB);
            }
            obstacleCounter++;
        }
    }

    private void connectVisibleNodes() {
        List<String> nodes = new ArrayList<>();

        // Agregar todos los nodos de los obstáculos
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                nodes.add("Obstacle" + obstacleCounter + "_" + i);
            }
            obstacleCounter++;
        }

        // Conectar los nodos visibles entre sí
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                String nodeA = nodes.get(i);
                String nodeB = nodes.get(j);
                if (isVisible(nodeA, nodeB)) {
                    grafo.agregarArco(nodeA, nodeB);
                }
            }
        }
    }

    private boolean isVisible(String nodeA, String nodeB) {
        Point posA = getNodePosition(nodeA);
        Point posB = getNodePosition(nodeB);
    
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                Point p1 = rectangle.get(i);
                Point p2 = rectangle.get((i + 1) % rectangle.size());
    
                if (linesIntersect(posA, posB, p1, p2)) {
                    return false;
                }
            }
        }
        System.out.println("Nodos visibles: " + nodeA + " y " + nodeB);
        return true;
    }

    private boolean linesIntersect(Point a1, Point a2, Point b1, Point b2) {
        // Verificar si los Bounding Boxes se superponen
        if (!boundingBoxesIntersect(a1, a2, b1, b2)) {
            return false;
        }
    
        // Cálculo del determinante para verificar si las líneas son paralelas o colineales
        double det = (a2.x - a1.x) * (b2.y - b1.y) - (a2.y - a1.y) * (b2.x - b1.x);
        if (det == 0) {
            // Líneas paralelas o colineales
            return colinearSegmentsOverlap(a1, a2, b1, b2);
        }
    
        // Cálculo de los parámetros t y u
        double t = ((b1.x - a1.x) * (b2.y - b1.y) - (b1.y - a1.y) * (b2.x - b1.x)) / det;
        double u = ((b1.x - a1.x) * (a2.y - a1.y) - (b1.y - a1.y) * (a2.x - a1.x)) / det;
    
        // Verificar si t y u están dentro de los límites [0, 1]
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
    
    private boolean colinearSegmentsOverlap(Point a1, Point a2, Point b1, Point b2) {
        // Verificar si los segmentos son colineales
        if (!isColinear(a1, a2, b1) || !isColinear(a1, a2, b2)) {
            return false;
        }
    
        // Verificar si los segmentos se solapan
        return Math.max(a1.x, a2.x) >= Math.min(b1.x, b2.x) &&
               Math.max(b1.x, b2.x) >= Math.min(a1.x, a2.x) &&
               Math.max(a1.y, a2.y) >= Math.min(b1.y, b2.y) &&
               Math.max(b1.y, b2.y) >= Math.min(a1.y, a2.y);
    }
    
    private boolean isColinear(Point a, Point b, Point c) {
        // Verificar colinealidad usando el área del triángulo formado por los puntos
        return (b.x - a.x) * (c.y - a.y) == (c.x - a.x) * (b.y - a.y);
    }

    private boolean boundingBoxesIntersect(Point a1, Point a2, Point b1, Point b2) {
        boolean intersect = Math.min(a1.x, a2.x) <= Math.max(b1.x, b2.x) &&
                            Math.max(a1.x, a2.x) >= Math.min(b1.x, b2.x) &&
                            Math.min(a1.y, a2.y) <= Math.max(b1.y, b2.y) &&
                            Math.max(a1.y, a2.y) >= Math.min(b1.y, b2.y);
    
        return intersect;
    }

    private Point getNodePosition(String node) {
        if (node.equals("Robot")) return robotPosition;
        if (node.equals("Goal")) return goalPosition;

        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                String nodeId = "Obstacle" + obstacleCounter + "_" + i;
                if (nodeId.equals(node)) {
                    return rectangle.get(i);
                }
            }
            obstacleCounter++;
        }
        return null;
    }

    public void display() {
        Graph graphstreamGraph = grafo.getGraphstream();

        // Configurar el CSS para visualización
        String cssFilePath = "style.css"; // Ruta al archivo CSS
        graphstreamGraph.setAttribute("ui.stylesheet", "url('" + cssFilePath + "')");

        // Mostrar el grafo en GraphStream
        graphstreamGraph.display();
    }

    public static void main(String[] args) {
        String filePath = "config1.txt"; // Ruta al archivo de configuración

        RobotSimulation1 simulation = new RobotSimulation1(filePath);
        simulation.display();
    }
}
