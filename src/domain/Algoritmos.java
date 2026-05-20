package domain;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public final class Algoritmos {

public static final double INF = Double.MAX_VALUE / 2;

private Algoritmos() {
}

public static List<Node> bfs(Graph g, Node inicio) {
    List<Node> orden = new ArrayList<>();
    if (g == null || inicio == null) return orden;

    Set<Node> visitados = new HashSet<>();
    Queue<Node> cola = new LinkedList<>();

    cola.offer(inicio);
    visitados.add(inicio);

    Node[] verts = g.getVerteces();

    while (!cola.isEmpty()) {
        Node actual = cola.poll();
        orden.add(actual);

        for (Node otro : verts) {
            if (otro == actual || visitados.contains(otro)) continue;
            if (sonAdyacentes(g, actual, otro)) {
                visitados.add(otro);
                cola.offer(otro);
            }
        }
    }
    return orden;
}

public static List<Node> dfs(Graph g, Node inicio) {
    List<Node> orden = new ArrayList<>();
    if (g == null || inicio == null) return orden;

    Set<Node> visitados = new HashSet<>();
    java.util.Deque<Node> pila = new java.util.ArrayDeque<>();
    pila.push(inicio);

    Node[] verts = g.getVerteces();

    while (!pila.isEmpty()) {
        Node actual = pila.pop();
        if (visitados.contains(actual)) continue;
        visitados.add(actual);
        orden.add(actual);

        for (int i = verts.length - 1; i >= 0; i--) {
            Node otro = verts[i];
            if (otro == actual || visitados.contains(otro)) continue;
            if (sonAdyacentes(g, actual, otro)) {
                pila.push(otro);
            }
        }
    }
    return orden;
}

public static List<Node> dijkstra(Graph g, Node origen, Node destino) {
    List<Node> camino = new ArrayList<>();
    if (g == null || origen == null || destino == null) return camino;

    Node[] verts = g.getVerteces();
    Map<Node, Double> dist = new HashMap<>();
    Map<Node, Node> previo = new HashMap<>();

    for (Node v : verts) dist.put(v, Double.POSITIVE_INFINITY);
    dist.put(origen, 0.0);

    PriorityQueue<Node> pq = new PriorityQueue<>(
        (a, b) -> Double.compare(dist.get(a), dist.get(b))
    );
    pq.offer(origen);

    Set<Node> cerrados = new HashSet<>();

    while (!pq.isEmpty()) {
        Node u = pq.poll();
        if (!cerrados.add(u)) continue;
        if (u == destino) break;

        double dU = dist.get(u);
        for (Node v : verts) {
            if (v == u || cerrados.contains(v)) continue;
            Edge e = g.getConnection(u, v);
            if (e == null || e.getPeso() <= 0) continue;

            double alt = dU + e.getPeso();
            if (alt < dist.get(v)) {
                dist.put(v, alt);
                previo.put(v, u);
                pq.offer(v);
            }
        }
    }

    if (dist.get(destino) == Double.POSITIVE_INFINITY) return camino;

    Node cur = destino;
    while (cur != null) {
        camino.add(cur);
        if (cur == origen) break;
        cur = previo.get(cur);
    }
    if (camino.isEmpty() || camino.get(camino.size() - 1) != origen) {
        return new ArrayList<>();
    }
    java.util.Collections.reverse(camino);
    return camino;
}

public static List<Edge> prim(Graph g) {
    List<Edge> mst = new ArrayList<>();
    if (g == null) return mst;

    Node[] verts = g.getVerteces();
    if (verts.length == 0) return mst;

    Set<Node> enMST = new HashSet<>();
    enMST.add(verts[0]);

    while (enMST.size() < verts.length) {
        Edge mejor = null;

        for (Node u : enMST) {
            for (Node v : verts) {
                if (enMST.contains(v)) continue;
                Edge e = g.getConnection(u, v);
                if (e == null || e.getPeso() <= 0) continue;
                if (mejor == null || e.getPeso() < mejor.getPeso()) {
                    mejor = e;
                }
            }
        }

        if (mejor == null) break;

        Node nuevo = enMST.contains(mejor.getOrigen())
                   ? mejor.getDestino()
                   : mejor.getOrigen();
        enMST.add(nuevo);
        mst.add(mejor);
    }
    return mst;
}

public static List<Edge> kruskal(Graph g) {
    List<Edge> mst = new ArrayList<>();
    if (g == null) return mst;

    Node[] verts = g.getVerteces();

    List<Edge> aristas = new ArrayList<>();
    for (int i = 0; i < verts.length; i++) {
        for (int j = i + 1; j < verts.length; j++) {
            Edge e = g.getConnection(verts[i], verts[j]);
            if (e != null && e.getPeso() > 0) aristas.add(e);
        }
    }
    aristas.sort((a, b) -> Double.compare(a.getPeso(), b.getPeso()));

    Map<Node, Node> parent = new HashMap<>();
    for (Node v : verts) parent.put(v, v);

    for (Edge e : aristas) {
        Node ra = find(parent, e.getOrigen());
        Node rb = find(parent, e.getDestino());
        if (ra != rb) {
            parent.put(ra, rb);
            mst.add(e);
            if (mst.size() == verts.length - 1) break;
        }
    }
    return mst;
}

private static Node find(Map<Node, Node> parent, Node x) {
    Node root = x;
    while (parent.get(root) != root) root = parent.get(root);
    Node cur = x;
    while (parent.get(cur) != root) {
        Node next = parent.get(cur);
        parent.put(cur, root);
        cur = next;
    }
    return root;
}

public static double[][] floydWarshall(Graph g) {
    Node[] verts = (g == null) ? new Node[0] : g.getVerteces();
    int n = verts.length;
    double[][] dist = new double[n][n];

    for (double[] fila : dist) Arrays.fill(fila, INF);

    for (int i = 0; i < n; i++) {
        dist[i][i] = 0;
        for (int j = 0; j < n; j++) {
            if (i == j) continue;
            Edge e = g.getConnection(verts[i], verts[j]);
            if (e != null && e.getPeso() > 0) {
                dist[i][j] = e.getPeso();
            }
        }
    }

    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            if (dist[i][k] >= INF) continue;
            for (int j = 0; j < n; j++) {
                if (dist[k][j] >= INF) continue;
                double alt = dist[i][k] + dist[k][j];
                if (alt < dist[i][j]) dist[i][j] = alt;
            }
        }
    }
    return dist;
}
    public static ArrayList<Node> bellmanFord(Graph g, Node start, Node end) {
        if (start == end) {
            ArrayList<Node> nodes = new ArrayList<>();
            nodes.add(start);
            return nodes;
        }
        if (g.getConnection(start, end) != null) {
            ArrayList<Node> nodes = new ArrayList<>();
            return nodes;
        }

        double[] distances = new double[g.getVerteces().length];
        Node[] predecessors = new Node[g.getVerteces().length];

        for (int i = 0; i < g.getVerteces().length; i++) {
            distances[i] = Double.MAX_VALUE;
        }

        int startIndex = g.getVertexIndex(start);

        distances[startIndex] = 0;

        for (int k = 0; k < g.getVerteces().length - 1; k++) {
            for (int i = 0; i < g.getVerteces().length; i++) {
                for (int j = 0; j < g.getVerteces().length; j++) {
                    Edge edge = g.getConnections()[i][j];

                    if (edge != null) {
                        double newDistance = distances[i] + edge.getPeso();

                        if (distances[i] != Double.MAX_VALUE && newDistance < distances[j]) {
                            distances[j] = newDistance;
                            predecessors[j] = g.getVerteces()[i];
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

            int currentIndex = g.getVertexIndex(current);
            current = predecessors[currentIndex]; 
        }

        if (path.get(path.size() - 1) != start) {
            return new ArrayList<>();
        } 

        Collections.reverse(path);

        return path;
    }

private static boolean sonAdyacentes(Graph g, Node a, Node b) {
    Edge e = g.getConnection(a, b);
    return e != null && e.getPeso() > 0;
}

}
