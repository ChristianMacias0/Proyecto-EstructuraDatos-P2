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

        // Añadir nodos y aristas para los obstáculos
        int obstacleCounter = 1;
        for (List<Point> rectangle : obstacles) {
            String prevNode = null;
            String firstNode = null;
            for (int i = 0; i < rectangle.size(); i++) {
                Point corner = rectangle.get(i);
                String nodeId = "Obstacle" + obstacleCounter + "_" + i;
                grafo.agregarVertice(nodeId);

                if (prevNode != null) {
                    grafo.agregarArco(prevNode, nodeId);
                }
                if (i == 0) {
                    firstNode = nodeId;
                }
                prevNode = nodeId;
            }
            // Conectar último punto con el primero para cerrar el rectángulo
            if (firstNode != null && prevNode != null) {
                grafo.agregarArco(prevNode, firstNode);
            }
            obstacleCounter++;
        }
    }

    public void display() {
        Graph graphstreamGraph = grafo.getGraphstream();
        graphstreamGraph.display();
    }

    public static void main(String[] args) {
        // Ruta del archivo de configuración
        String filePath = "config1.txt";

        RobotSimulation1 simulation = new RobotSimulation1(filePath);
        simulation.display();
    }
}
