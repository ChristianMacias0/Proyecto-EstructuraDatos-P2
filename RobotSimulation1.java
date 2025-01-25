import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class RobotSimulation1 {
    private GrafoLista<String> grafo;
    private Point robotPosition;
    private Point goalPosition;
    private List<List<Point>> obstacles = new ArrayList<>();

    public RobotSimulation1(String filePath) {
        grafo = new GrafoLista<>(true); // Grafo dirigido
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
        // Añadir nodos para robot y meta
        grafo.agregarVertice("Robot");
        grafo.agregarVertice("Goal");

        // Añadir nodos para las esquinas de los obstáculos
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                Point corner = rectangle.get(i);
                String nodeId = "Obstacle" + obstacleCounter + "_" + i;
                grafo.agregarVertice(nodeId);
                grafo.getGraphstream().getNode(nodeId).setAttribute("ui.class", "obstacleNode");
            }
            obstacleCounter++;
        }

        System.out.println("Nodos: " + grafo.getGraphstream().getNodeCount());
System.out.println("Aristas: " + grafo.getGraphstream().getEdgeCount());

        // Conectar las esquinas de los mismos obstáculos
        connectObstacleCorners();
        System.out.println("Aristas: " + grafo.getGraphstream().getEdgeCount());

        // Conectar nodos visibles entre obstáculos
        connectVisibleNodes();
    }

    private void connectObstacleCorners() {
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                String nodeA = "Obstacle" + obstacleCounter + "_" + i;
                String nodeB = "Obstacle" + obstacleCounter + "_" + ((i + 1) % rectangle.size());
    
                // Verificar si los nodos existen
                if (grafo.getGraphstream().getNode(nodeA) != null && grafo.getGraphstream().getNode(nodeB) != null) {
                    // Crear identificador único para la arista
                    String edgeId = nodeA + "-" + nodeB;
    
                    // Añadir la arista si no existe
                    if (grafo.getGraphstream().getEdge(edgeId) == null) {
                        Edge edge = grafo.getGraphstream().addEdge(edgeId, nodeA, nodeB);
                        if (edge != null) {
                            edge.setAttribute("ui.class", "obstacleEdge");
                        }
                    }
                }
            }
            obstacleCounter++;
        }
    }

    private void connectVisibleNodes() {
        List<String> nodes = new ArrayList<>();
    
        // Agregar nodos de obstáculos
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                nodes.add("Obstacle" + obstacleCounter + "_" + i);
            }
            obstacleCounter++;
        }
    
        // Crear aristas entre nodos visibles
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                String nodeA = nodes.get(i);
                String nodeB = nodes.get(j);
    
                // Verificar si los nodos existen
                if (grafo.getGraphstream().getNode(nodeA) != null && grafo.getGraphstream().getNode(nodeB) != null) {
                    if (isVisible(nodeA, nodeB)) {
                        String edgeId = nodeA + "-" + nodeB;
    
                        // Crear la arista si no existe
                        if (grafo.getGraphstream().getEdge(edgeId) == null) {
                            Edge edge = grafo.getGraphstream().addEdge(edgeId, nodeA, nodeB);
                            if (edge != null) {
                                edge.setAttribute("ui.class", "visibleEdge");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isVisible(String nodeA, String nodeB) {
        // Obtener posiciones de los nodos
        Point posA = getNodePosition(nodeA);
        Point posB = getNodePosition(nodeB);

        // Verificar si la línea entre A y B interseca algún obstáculo
        for (List<Point> rectangle : obstacles) {
            for (int i = 0; i < rectangle.size(); i++) {
                Point p1 = rectangle.get(i);
                Point p2 = rectangle.get((i + 1) % rectangle.size());
                if (linesIntersect(posA, posB, p1, p2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Point scalePosition(Point original, double scaleFactor, int offsetX, int offsetY) {
        int x = (int) (original.x * scaleFactor) + offsetX;
        int y = (int) (original.y * scaleFactor) + offsetY;
        return new Point(x, y);
    }

    private Point getNodePosition(String node) {
        double scaleFactor = 0.5; // Factor para reducir aún más las posiciones
        int offsetX = 100; // Desplazamiento horizontal
        int offsetY = 100; // Desplazamiento vertical

        if (node.equals("Robot")) 
            return scalePosition(robotPosition, scaleFactor, offsetX, offsetY);
        if (node.equals("Goal")) 
            return scalePosition(goalPosition, scaleFactor, offsetX, offsetY);

        // Obtener posición de las esquinas de obstáculos
        for (int i = 0; i < obstacles.size(); i++) {
            List<Point> rectangle = obstacles.get(i);
            for (int j = 0; j < rectangle.size(); j++) {
                String expectedNode = "Obstacle" + (i + 1) + "_" + j;
                if (node.equals(expectedNode)) {
                    return scalePosition(rectangle.get(j), scaleFactor, offsetX, offsetY);
                }
            }
        }
        return null;
    }

    private boolean linesIntersect(Point a1, Point a2, Point b1, Point b2) {
        double det = (a2.x - a1.x) * (b2.y - b1.y) - (a2.y - a1.y) * (b2.x - b1.x);
        if (det == 0) return false; // Líneas paralelas

        double t = ((b1.x - a1.x) * (b2.y - b1.y) - (b1.y - a1.y) * (b2.x - b1.x)) / det;
        double u = ((b1.x - a1.x) * (a2.y - a1.y) - (b1.y - a1.y) * (a2.x - a1.x)) / det;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    public void display() {
        Graph graphstreamGraph = grafo.getGraphstream();

        // Cargar el archivo CSS
        String cssFilePath = "style.css"; // Asegúrate de que esta ruta sea correcta
        graphstreamGraph.setAttribute("ui.stylesheet", "url('" + cssFilePath + "')");

        // Asignar posiciones a los nodos
        graphstreamGraph.getNode("Robot").setAttribute("xy", getNodePosition("Robot").x, getNodePosition("Robot").y);
        graphstreamGraph.getNode("Goal").setAttribute("xy", getNodePosition("Goal").x, getNodePosition("Goal").y);

        graphstreamGraph.display();
    }

    public static void main(String[] args) {
        // Ruta del archivo de configuración
        String filePath = "config1.txt";

        RobotSimulation1 simulation = new RobotSimulation1(filePath);
        simulation.display();
    }
}
