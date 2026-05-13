package domain;

import javax.management.BadAttributeValueExpException;

public class Graph {
    private Node[] verteces;
    // Save the weight of the connections
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

    /**
     * Gives the weight of a connection between
     * two verteces
     * 
     * @param vertex1
     * @param vertex2
     * @return The weight of the connection
     */
    public double getWeight(Node vertex1, Node vertex2){
        int vertex1Index = - 1;
        int vertex2Index = - 1;
        for (int i = 0; i < verteces.length; i++) {
            if (verteces[i] == vertex1) 
                vertex1Index = i;

            if (verteces[i] == vertex2)
                vertex2Index = i;
        }

        // if (vertex1Index == - 1) {
        //     throw new VertexNotFound("The vertex " + vertex1 + " was not found in the graph");
        // }
        // if (vertex2Index == - 1) {
        //     throw new VertexNotFound("The vertex " + vertex2 + " was not found in the Graph");
        // }

        return connections[vertex1Index][vertex2Index].getPeso();
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
        int vertex1Index = - 1;
        int vertex2Index = - 1;
        for (int i = 0; i < verteces.length; i++) {
            if (verteces[i] == vertex1) 
                vertex1Index = i;

            if (verteces[i] == vertex2)
                vertex2Index = i;
        }

        // if (vertex1Index == - 1) 
        //     throw new VertexNotFound("The vertex " + vertex1 + " was not found in the graph");
        
        // if (vertex2Index == - 1) 
        //     throw new VertexNotFound("The vertex " + vertex2 + " was not found in the Graph");
        
        // if (vertex1 == vertex2) 
        //     throw new BadAttributeValueExpException("Loops are not allowed on the Graph vertex1 and vertex2 must be diferent");
        
        
        connect(vertex1Index, vertex2Index, weight);
    }

    /**
     * Disable the connection of the verteces putting 
     * the weight on 0
     * 
     * @param vertex1
     * @param vertex2
     * @throws VertexNotFound When a vertex doesn't exist on the verteces list
     */
    public void disableConnection(Node vertex1, Node vertex2) throws VertexNotFound {
        setConnection(vertex1, vertex2, 0);
    }

    private void connect(int x, int y, double weight) {
        Edge edge = new Edge(verteces[x], verteces[y], weight);
        connections[x][y] = edge;
        connections[y][x] = edge;
    }

}
