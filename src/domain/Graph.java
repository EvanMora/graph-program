
package domain;

import java.util.ArrayList;
import java.util.Collections;

public class Graph {
    private Node[] verteces;
    private Edge[][] connections; 

    /**
     * Creates a graph without connections just 
     * with the verteces 
     * 
     * @param verteces An array of the verteces names
     */
    public Graph(Node[] verteces) {
        this.verteces = verteces;
        this.connections = new Edge[verteces.length][verteces.length];
    }

    /* Get the list of verteces */
    public Node[] getVerteces() {
        return verteces;
    }

    public Edge[][] getConnections() {
        return connections;
    }

    /**
     * Gives the weight of a connection between
     * two verteces
     * 
     * @param vertex1
     * @param vertex2
     * @return The weight of the connection
     */
    public double getWeight(Node vertex1, Node vertex2){
        int vertex1Index = getVertexIndex(vertex1);
        int vertex2Index = getVertexIndex(vertex2);

        if (vertex1Index == - 1) {
            System.out.println("The vertex " + vertex1.getNombre() + " was not found in the graph");
            System.exit(1);
        }
        if (vertex2Index == - 1) {
            System.out.println("The vertex " + vertex2.getNombre() + " was not found in the Graph");
            System.exit(1);
        }

        return connections[vertex1Index][vertex2Index].getPeso();
    } 

    public int getVertexIndex(Node vertex) {
        for (int i = 0; i < verteces.length; i++) {
            if (verteces[i] == vertex) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Set a connection between two verteces whit an 
     * specific weight
     * 
     * @param vertex1
     * @param vertex2
     * @param weight
     */
    public void setConnection(Node vertex1, Node vertex2, double weight) {
        int vertex1Index = getVertexIndex(vertex1);
        int vertex2Index = getVertexIndex(vertex2);

        if (vertex1Index == - 1) {
            System.out.println("The vertex " + vertex1 + " was not found in the graph");
            System.exit(1);
        }
        
        if (vertex2Index == - 1) {
            System.out.println("The vertex " + vertex2 + " was not found in the Graph");
            System.exit(1);
        }
        
        if (vertex1 == vertex2) {
            System.out.println("Loops are not allowed on the Graph vertex1 and vertex2 must be diferent");
            System.exit(1);
        }
        
        connect(vertex1Index, vertex2Index, weight);
    }

    /**
     * Disable the connection of the verteces putting 
     * the weight on 0
     * 
     * @param vertex1
     * @param vertex2
     */
    public void disableConnection(Node vertex1, Node vertex2) {
        setConnection(vertex1, vertex2, 0);
    }

    /* Return de edge that conects vertex1 and vertex2 */
    public Edge getConnection(Node vertex1, Node vertex2) {
        int vertex1Index = getVertexIndex(vertex1);
        int vertex2Index = getVertexIndex(vertex2);

        if (vertex1Index == - 1) {
            System.out.println("The vertex " + vertex1 + " was not found in the graph");
            System.exit(1);
        }
        
        if (vertex2Index == - 1) {
            System.out.println("The vertex " + vertex2 + " was not found in the Graph");
            System.exit(1);
        }
        
        return connections[vertex1Index][vertex2Index];
    }

    private void connect(int x, int y, double weight) {
        Edge edge = new Edge(verteces[x], verteces[y], weight);
        connections[x][y] = edge;
        connections[y][x] = edge;
    }

    /* Calculate the shortest way and negative weights are allowed */
    public Node[] bellmanFord(Node start, Node end) {
        if (start == end) {
            Node[] nodes = {start};
            return nodes;
        }
        if (getConnection(start, end) != null) {
            Node[] nodes = {start, end};
            return nodes;
        }

        double[] distances = new double[verteces.length];
        Node[] predecessors = new Node[verteces.length];

        for (int i = 0; i < verteces.length; i++) {
            distances[i] = Double.MAX_VALUE;
        }

        int startIndex = getVertexIndex(start);

        distances[startIndex] = 0;

        for (int k = 0; k < verteces.length - 1; k++) {
            for (int i = 0; i < verteces.length; i++) {
                for (int j = 0; j < verteces.length; j++) {
                    Edge edge = connections[i][j];

                    if (edge != null) {
                        double newDistance = distances[i] + edge.getPeso();

                        if (distances[i] != Double.MAX_VALUE && newDistance < distances[j]) {
                            distances[j] = newDistance;
                            predecessors[j] = verteces[i];
                        }
                    }
                }
            }
        }

        ArrayList<Node> path = new ArrayList<>();
        Node current = end;

        while (current != null) {
            path.add(current);

            if (current == start) {
                break;
            }

            int currentIndex = getVertexIndex(current);
            current = predecessors[currentIndex]; 
        }

        if (path.get(path.size() - 1) != start) {
            return new Node[0];
        } 

        Collections.reverse(path);

        return path.toArray(new Node[0]);
    }
}
