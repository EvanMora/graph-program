package domain;

import javax.management.BadAttributeValueExpException;

public class Graph {
    private String[] verteces;
    private int[][] connections;

    public Graph(int numVerteces) {
        this.verteces = new String[numVerteces];
        this.connections = new int[numVerteces][numVerteces];
    }

    public Graph(String[] verteces) {
        this.verteces = verteces;
        this.connections = new int[verteces.length][verteces.length];
    }

    public String[] getVerteces() {
        return verteces;
    }

    public int getWeight(String vertex1, String vertex2) {
        int vertex1Index = - 1;
        int vertex2Index = - 1;
        for (int i = 0; i < verteces.length; i++) {
            if (verteces[i] == vertex1) 
                vertex1Index = i;

            if (verteces[i] == vertex2)
                vertex2Index = i;
        }

        if (vertex1Index == - 1) {
            try {
                throw new VertexNotFound("The vertex " + vertex1 + " was not found in the graph");
            } catch (Exception e) {
                System.exit(1);
            }
        }
        if (vertex2Index == - 1) {
            try {
                throw new VertexNotFound("The vertex " + vertex2 + " was not found in the Graph");
            } catch (Exception e) {
                System.exit(1);
            }
        }

        return connections[vertex1Index][vertex2Index];
    } 

    public void setConnection(String vertex1, String vertex2, int weight) {
        int vertex1Index = - 1;
        int vertex2Index = - 1;
        for (int i = 0; i < verteces.length; i++) {
            if (verteces[i] == vertex1) 
                vertex1Index = i;

            if (verteces[i] == vertex2)
                vertex2Index = i;
        }

        if (vertex1Index == - 1) {
            try {
                throw new VertexNotFound("The vertex " + vertex1 + " was not found in the graph");
            } catch (Exception e) {
                System.exit(1);
            }
        }
        if (vertex2Index == - 1) {
            try {
                throw new VertexNotFound("The vertex " + vertex2 + " was not found in the Graph");
            } catch (Exception e) {
                System.exit(1);
            }
        }
        if (vertex1 == vertex2) {
            try {
                throw new BadAttributeValueExpException("Loops are not allowed on the Graph vertex1 and vertex2 must be diferent");
            } catch (Exception e) {
                System.exit(1);
            }
        }
        
        connect(vertex1Index, vertex2Index, weight);
    }

    public void disableConnection(String vertex1, String vertex2) {
        if (vertex1 == vertex2) {
            return;
        }
        setConnection(vertex1, vertex2, 0);
    }

    private void connect(int x, int y, int weight) {
        connections[x][y] = weight;
        connections[y][x] = weight;
    }

}
