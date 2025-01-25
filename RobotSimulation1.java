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
