package domain;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    PanelGrafo panelGrafo;
    JTextArea  areaTexto;

    public VentanaPrincipal() {
        setTitle("Grafo - Estructuras de Datos");
        setSize(1100, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panelGrafo = new PanelGrafo(this);
        areaTexto  = new JTextArea("Bienvenido.\nUsa el menú Algoritmos para comenzar.");
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setMargin(new Insets(8, 8, 8, 8));

        buildLayout();
        buildMenuBar();
        setVisible(true);
    }


    private void buildLayout() {
        setLayout(new BorderLayout(4, 0));
        add(panelGrafo, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setPreferredSize(new Dimension(300, 0));
        scroll.setBorder(BorderFactory.createTitledBorder("Resultados"));
        add(scroll, BorderLayout.EAST);
    }


    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mAlg = new JMenu("Algoritmos");

        JMenuItem iDijkstra = new JMenuItem("Dijkstra (camino más corto)");
        JMenuItem iPrim     = new JMenuItem("Prim (MST)");
        JMenuItem iFloyd    = new JMenuItem("Floyd-Warshall (todos los pares)");
        JMenuItem iKruskal  = new JMenuItem("Kruskal (MST)");
        JMenuItem iBFS      = new JMenuItem("BFS (recorrido en amplitud)");
        JMenuItem iDFS      = new JMenuItem("DFS (recorrido en profundidad)");

        iDijkstra.addActionListener(e -> iniciarDijkstra());
        iPrim    .addActionListener(e -> ejecutarPrim());
        iFloyd   .addActionListener(e -> ejecutarFloydWarshall());
        iKruskal .addActionListener(e -> ejecutarKruskal());
        iBFS     .addActionListener(e -> iniciarBFS());
        iDFS     .addActionListener(e -> iniciarDFS());

        mAlg.add(iDijkstra); mAlg.add(iPrim); mAlg.add(iFloyd);
        mAlg.add(iKruskal);  mAlg.addSeparator();
        mAlg.add(iBFS);      mAlg.add(iDFS);

        JMenu mEd = new JMenu("Edición");

        JMenuItem iModoEd  = new JMenuItem("Modo Edición");
        JMenuItem iModoExp = new JMenuItem("Modo Exploración");

        iModoEd .addActionListener(e -> panelGrafo.setModoEdicion(true));
        iModoExp.addActionListener(e -> panelGrafo.setModoEdicion(false));

        mEd.add(iModoEd);
        mEd.add(iModoExp);

        JMenu mVer = new JMenu("Ver");

        JMenuItem iLimpiar = new JMenuItem("Limpiar resaltado");
        iLimpiar.addActionListener(e -> panelGrafo.limpiarResaltado());
        mVer.add(iLimpiar);

        bar.add(mAlg);
        bar.add(mEd);
        bar.add(mVer);
        setJMenuBar(bar);
    }

    private void iniciarDijkstra() {
        mostrar("Dijkstra activado.\nHaz clic en el nodo ORIGEN en el grafo.");
        panelGrafo.iniciarAlgoritmo(PanelGrafo.EstadoAlgoritmo.DIJKSTRA_ORIGEN);
    }

    private void ejecutarPrim() {
        Graph g = panelGrafo.getGrafo();
        List<Edge> mst = Algoritmos.prim(g);

        if (mst.isEmpty()) { mostrarResultado("Prim", "El grafo no tiene aristas."); return; }

        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Edge e : mst) {
            sb.append("  ").append(e).append("\n");
            total += e.getPeso();
        }
        sb.append(String.format("\nPeso total MST: %.2f Km", total));

        panelGrafo.resaltarAristas(mst);
        mostrarResultado("Prim — Árbol de Expansión Mínima", sb.toString());
    }

    private void ejecutarFloydWarshall() {
        Graph g = panelGrafo.getGrafo();
        double[][] dist = Algoritmos.floydWarshall(g);
        Node[] verts = g.getVerteces();
        double INF = Double.MAX_VALUE / 2;

        StringBuilder sb = new StringBuilder("Distancias mínimas entre todos los pares (Km):\n\n");
        int col = 10;
        sb.append(String.format("%-" + col + "s", ""));
        for (Node v : verts)
            sb.append(String.format("%-" + col + "s", abrev(v.getNombre(), col - 1)));
        sb.append("\n");

        for (int i = 0; i < verts.length; i++) {
            sb.append(String.format("%-" + col + "s", abrev(verts[i].getNombre(), col - 1)));
            for (int j = 0; j < verts.length; j++) {
                String val = (dist[i][j] >= INF) ? "∞" : String.format("%.1f", dist[i][j]);
                sb.append(String.format("%-" + col + "s", val));
            }
            sb.append("\n");
        }

        mostrarResultado("Floyd-Warshall", sb.toString());
    }

    private void ejecutarKruskal() {
        Graph g = panelGrafo.getGrafo();
        List<Edge> mst = Algoritmos.kruskal(g);

        if (mst.isEmpty()) { mostrarResultado("Kruskal", "El grafo no tiene aristas."); return; }

        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Edge e : mst) {
            sb.append("  ").append(e).append("\n");
            total += e.getPeso();
        }
        sb.append(String.format("\nPeso total MST: %.2f Km", total));

        panelGrafo.resaltarAristas(mst);
        mostrarResultado("Kruskal — Árbol de Expansión Mínima", sb.toString());
    }

    private void iniciarBFS() {
        mostrar("BFS activado.\nHaz clic en el nodo de INICIO en el grafo.");
        panelGrafo.iniciarAlgoritmo(PanelGrafo.EstadoAlgoritmo.BFS_INICIO);
    }

    private void iniciarDFS() {
        mostrar("DFS activado.\nHaz clic en el nodo de INICIO en el grafo.");
        panelGrafo.iniciarAlgoritmo(PanelGrafo.EstadoAlgoritmo.DFS_INICIO);
    }


    public void mostrarResultado(String titulo, String cuerpo) {
        areaTexto.setText("══════════════════════════\n" + titulo + "\n══════════════════════════\n\n" + cuerpo);
        areaTexto.setCaretPosition(0);
    }

    public void mostrar(String mensaje) {
        areaTexto.setText(mensaje);
    }

    private String abrev(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
