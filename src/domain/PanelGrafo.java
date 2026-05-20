package domain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PanelGrafo extends JPanel implements MouseListener, MouseMotionListener {

    public enum EstadoAlgoritmo {
        NINGUNO, DIJKSTRA_ORIGEN, DIJKSTRA_DESTINO, 
        BELLMAN_FORD_ORIGEN, BELLMAN_FORD_DESTINO,
        BFS_INICIO, DFS_INICIO
    }

    private ArrayList<Node> nodos = new ArrayList<>();
    private ArrayList<Edge> aristas = new ArrayList<>();
    private Graph grafo;
    private final VentanaPrincipal ventana;

    private Node origenSeleccionado;
    private Node nodoParaArista;
    private List<Node> caminoResaltado = new ArrayList<>();
    private List<Edge> aristasResaltadas = new ArrayList<>();
    private boolean modoEdicion = false;
    private EstadoAlgoritmo estado = EstadoAlgoritmo.NINGUNO;

    private Node dragCandidate  = null;
    private Node nodeArrastrado = null;
    private int  dragOffsetX, dragOffsetY;
    private int  prevDragX, prevDragY;
    private final Map<Edge, Double> originalPesos = new HashMap<>();

    private static final int R = 28;

    private static final Color C_BG          = new Color(13, 17, 30);
    private static final Color C_GRID_LINE   = new Color(25, 32, 55);
    private static final Color C_GRID_DOT    = new Color(40, 52, 88);
    private static final Color C_NODO_TOP    = new Color(79, 109, 245);
    private static final Color C_NODO_BOT    = new Color(50,  75, 200);
    private static final Color C_BORDE       = new Color(120, 150, 255);
    private static final Color C_SEL_TOP     = new Color(255, 140,  30);
    private static final Color C_SEL_BOT     = new Color(220,  95,   0);
    private static final Color C_PATH_TOP    = new Color( 80, 210, 140);
    private static final Color C_PATH_BOT    = new Color( 45, 160, 100);
    private static final Color C_ARISTA      = new Color( 55,  70, 110);
    private static final Color C_HIGHLIGHT   = new Color(255, 140,  30);
    private static final Color C_PESO_BG     = new Color( 18,  24,  45, 210);
    private static final Color C_TEXTO       = Color.WHITE;

    public PanelGrafo(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setBackground(C_BG);
        setPreferredSize(new Dimension(820, 700));
        addMouseListener(this);
        addMouseMotionListener(this);
        cargarDatos();
    }

    private void cargarDatos() {
        ParkData data = new ParkData();
        nodos = new ArrayList<>(Arrays.asList(data.parkArray));
        grafo  = data.parkGraph;
        aristas = new ArrayList<>();
        Node[] verts = grafo.getVerteces();
        for (int i = 0; i < verts.length; i++)
            for (int j = i + 1; j < verts.length; j++) {
                Edge e = grafo.getConnection(verts[i], verts[j]);
                if (e != null && e.getPeso() > 0) aristas.add(e);
            }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        dibujarGrid(g2);

        for (Edge e : aristas)
            dibujarArista(g2, e, esResaltada(e));

        for (Node nd : nodos)
            dibujarNodo(g2, nd,
                nd == origenSeleccionado || nd == nodoParaArista,
                caminoResaltado.contains(nd));

        dibujarEstado(g2);
    }

    private void dibujarGrid(Graphics2D g2) {
        int step = 45;
        g2.setColor(C_GRID_LINE);
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < getWidth();  x += step) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += step) g2.drawLine(0, y, getWidth(), y);

        g2.setColor(C_GRID_DOT);
        for (int x = 0; x < getWidth(); x += step)
            for (int y = 0; y < getHeight(); y += step)
                g2.fillOval(x - 2, y - 2, 4, 4);
    }

    private boolean esResaltada(Edge e) {
        if (aristasResaltadas.contains(e)) return true;
        if (caminoResaltado.size() < 2)   return false;
        for (int i = 0; i < caminoResaltado.size() - 1; i++) {
            Node a = caminoResaltado.get(i), b = caminoResaltado.get(i + 1);
            if ((e.getOrigen() == a && e.getDestino() == b)
             || (e.getOrigen() == b && e.getDestino() == a)) return true;
        }
        return false;
    }

    private void dibujarArista(Graphics2D g2, Edge e, boolean resaltada) {
        Node a = e.getOrigen(), b = e.getDestino();

        if (resaltada) {
            g2.setColor(new Color(255, 140, 30, 35));
            g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
            g2.setColor(C_HIGHLIGHT);
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        } else {
            g2.setColor(C_ARISTA);
            float[] dash = {9f, 6f};
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
        }
        g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());

        int mx = (a.getX() + b.getX()) / 2;
        int my = (a.getY() + b.getY()) / 2;
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String label = String.format("%.1f km", e.getPeso());
        FontMetrics fm = g2.getFontMetrics();
        int lw = fm.stringWidth(label);
        int lh = fm.getHeight();

        g2.setColor(C_PESO_BG);
        g2.fillRoundRect(mx - lw / 2 - 5, my - fm.getAscent() - 3, lw + 10, lh + 2, 7, 7);

        g2.setColor(resaltada ? new Color(255, 195, 100) : new Color(150, 175, 230));
        g2.drawString(label, mx - lw / 2, my);
    }

    private void dibujarNodo(Graphics2D g2, Node nd, boolean seleccionado, boolean enCamino) {
        int x = nd.getX(), y = nd.getY(), d = R * 2;

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(x - R + 4, y - R + 4, d, d);

        if (seleccionado || enCamino) {
            Color glow = enCamino ? new Color(80, 210, 140, 55) : new Color(255, 140, 30, 55);
            g2.setColor(glow);
            g2.fillOval(x - R - 8, y - R - 8, d + 16, d + 16);
        }

        Color top = enCamino ? C_PATH_TOP : seleccionado ? C_SEL_TOP : C_NODO_TOP;
        Color bot = enCamino ? C_PATH_BOT : seleccionado ? C_SEL_BOT : C_NODO_BOT;
        g2.setPaint(new GradientPaint(x - R, y - R, top, x + R, y + R, bot));
        g2.fillOval(x - R, y - R, d, d);

        Color borde = enCamino ? new Color(110, 245, 165) : seleccionado ? new Color(255, 185, 80) : C_BORDE;
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x - R, y - R, d, d);

        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillOval(x - R + 5, y - R + 4, d / 2, d / 3);

        g2.setColor(C_TEXTO);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        String id = String.valueOf(nd.getId());
        g2.drawString(id, x - fm.stringWidth(id) / 2, y + fm.getAscent() / 2 - 1);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        String nombre = nd.getNombre();
        if (fm.stringWidth(nombre) > 105) nombre = nombre.substring(0, 11) + "…";
        int tx = x - fm.stringWidth(nombre) / 2;
        int ty = y + R + fm.getAscent() + 5;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(tx - 3, ty - fm.getAscent() - 1, fm.stringWidth(nombre) + 6, fm.getHeight() + 2, 5, 5);
        g2.setColor(enCamino ? new Color(160, 255, 190) : C_TEXTO);
        g2.drawString(nombre, tx, ty);
    }

    private void dibujarEstado(Graphics2D g2) {
        String modoTxt = modoEdicion ? "✎  EDICIÓN" : "⬡  EXPLORACIÓN";
        Color modeColor = modoEdicion ? new Color(255, 140, 30) : new Color(80, 210, 140);

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int pw = fm.stringWidth(modoTxt) + 22;
        int ph = 26;
        int px = getWidth() - pw - 12;
        int py = 12;

        g2.setColor(new Color(modeColor.getRed(), modeColor.getGreen(), modeColor.getBlue(), 28));
        g2.fillRoundRect(px - 3, py - 3, pw + 6, ph + 6, 18, 18);
        g2.setColor(new Color(modeColor.getRed(), modeColor.getGreen(), modeColor.getBlue(), 110));
        g2.fillRoundRect(px, py, pw, ph, 14, 14);
        g2.setColor(modeColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(px, py, pw, ph, 14, 14);
        g2.drawString(modoTxt, px + 11, py + ph - 7);

        if (estado != EstadoAlgoritmo.NINGUNO) {
            String hint = switch (estado) {
                case DIJKSTRA_ORIGEN      -> "Clic en nodo ORIGEN (Dijkstra)";
                case DIJKSTRA_DESTINO     -> "Origen: " + origenSeleccionado + "  •  Clic en DESTINO";
                case BELLMAN_FORD_ORIGEN  -> "Clic en nodo ORIGEN (Bellman-Ford)";
                case BELLMAN_FORD_DESTINO -> "Origen: " + origenSeleccionado + "  •  Clic en DESTINO";
                case BFS_INICIO           -> "Clic en nodo de INICIO  (BFS)";
                case DFS_INICIO           -> "Clic en nodo de INICIO  (DFS)";
                default                   -> "";
            };
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            int hw = fm.stringWidth(hint) + 18;
            g2.setColor(new Color(20, 28, 55, 225));
            g2.fillRoundRect(10, 12, hw, 26, 10, 10);
            g2.setColor(new Color(120, 160, 255));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(10, 12, hw, 26, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(hint, 19, 29);
        }
    }

    private Node nodoEnPosicion(int x, int y) {
        for (Node nd : nodos) {
            int dx = nd.getX() - x, dy = nd.getY() - y;
            if (dx * dx + dy * dy <= R * R) return nd;
        }
        return null;
    }

    public void reconstruirGrafo() {
        Node[] arr = nodos.toArray(new Node[0]);
        grafo = new Graph(arr);
        for (Edge e : aristas)
            grafo.setConnection(e.getOrigen(), e.getDestino(), e.getPeso());
        ventana.actualizarCombosNodos();
        repaint();
    }

    public Graph getGrafo()       { return grafo; }
    public List<Node> getNodos()  { return nodos; }

    public void setModoEdicion(boolean modo) {
        modoEdicion = modo;
        estado = EstadoAlgoritmo.NINGUNO;
        origenSeleccionado = null;
        nodoParaArista = null;
        repaint();
        ventana.mostrar(modo ? "Modo edición activo." : "Modo exploración activo.");
    }

    public void iniciarAlgoritmo(EstadoAlgoritmo est) {
        modoEdicion = false;
        estado = est;
        origenSeleccionado = null;
        caminoResaltado.clear();
        aristasResaltadas.clear();
        repaint();
    }

    public void limpiarResaltado() {
        caminoResaltado.clear();
        aristasResaltadas.clear();
        origenSeleccionado = null;
        estado = EstadoAlgoritmo.NINGUNO;
        repaint();
    }

    public void resaltarCamino(List<Node> camino) {
        caminoResaltado = new ArrayList<>(camino);
        aristasResaltadas.clear();
        repaint();
    }

    public void resaltarAristas(List<Edge> edges) {
        aristasResaltadas = new ArrayList<>(edges);
        caminoResaltado.clear();
        repaint();
    }

    public void agregarArista(Node a, Node b, double peso) {
        aristas.removeIf(e -> (e.getOrigen() == a && e.getDestino() == b) || (e.getOrigen() == b && e.getDestino() == a));
        aristas.add(new Edge(a, b, peso));
        reconstruirGrafo();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!modoEdicion || !SwingUtilities.isLeftMouseButton(e)) return;
        Node clicked = nodoEnPosicion(e.getX(), e.getY());
        if (clicked == null) return;
        dragCandidate = clicked;
        dragOffsetX   = e.getX() - clicked.getX();
        dragOffsetY   = e.getY() - clicked.getY();
        prevDragX     = clicked.getX();
        prevDragY     = clicked.getY();
        originalPesos.clear();
        for (Edge edge : aristas)
            if (edge.getOrigen() == clicked || edge.getDestino() == clicked)
                originalPesos.put(edge, edge.getPeso());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragCandidate == null || !modoEdicion) return;
        nodeArrastrado = dragCandidate;
        int nx = Math.max(R + 5, Math.min(getWidth()  - R - 5,  e.getX() - dragOffsetX));
        int ny = Math.max(R + 5, Math.min(getHeight() - R - 30, e.getY() - dragOffsetY));
        nodeArrastrado.setX(nx);
        nodeArrastrado.setY(ny);
        for (Edge edge : aristas) {
            if (edge.getOrigen()  != nodeArrastrado && edge.getDestino() != nodeArrastrado) continue;
            Double pesoOrig = originalPesos.get(edge);
            if (pesoOrig == null) continue;
            Node other    = (edge.getOrigen() == nodeArrastrado) ? edge.getDestino() : edge.getOrigen();
            double distOrig = Math.hypot(prevDragX - other.getX(), prevDragY - other.getY());
            double distNow  = Math.hypot(nx - other.getX(), ny - other.getY());
            if (distOrig > 0.5)
                edge.setPeso(Math.max(0.1, Math.round(pesoOrig * (distNow / distOrig) * 10.0) / 10.0));
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (nodeArrastrado != null) { reconstruirGrafo(); nodeArrastrado = null; }
        dragCandidate = null;
    }

    @Override public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        if (nodeArrastrado != null) return;
        int x = e.getX(), y = e.getY();
        Node clicked = nodoEnPosicion(x, y);
        if (modoEdicion) handleEditClick(e, x, y, clicked);
        else             handleExplorationClick(clicked);
    }

    private void handleExplorationClick(Node clicked) {
        if (clicked == null) return;
        switch (estado) {
            case DIJKSTRA_ORIGEN -> {
                origenSeleccionado = clicked;
                estado = EstadoAlgoritmo.DIJKSTRA_DESTINO;
                ventana.mostrar("Origen: " + clicked.getNombre() + "\nAhora clic en el destino.");
                repaint();
            }
            case DIJKSTRA_DESTINO -> {
                if (clicked == origenSeleccionado) return;
                ejecutarDijkstra(origenSeleccionado, clicked);
                estado = EstadoAlgoritmo.NINGUNO;
            }
            case BELLMAN_FORD_ORIGEN -> {
                origenSeleccionado = clicked;
                estado = EstadoAlgoritmo.BELLMAN_FORD_DESTINO;
                ventana.mostrar("Origen: " + clicked.getNombre() + "\nAhora clic en el destino.");
                repaint();
            }
            case BELLMAN_FORD_DESTINO -> {
                if (clicked == origenSeleccionado) return;
                ejecutarBellmanFord(origenSeleccionado, clicked);
                estado = EstadoAlgoritmo.NINGUNO;
            }
            case BFS_INICIO -> { ejecutarBFS(clicked); estado = EstadoAlgoritmo.NINGUNO; }
            case DFS_INICIO -> { ejecutarDFS(clicked); estado = EstadoAlgoritmo.NINGUNO; }
            default -> {}
        }
    }

    private void handleEditClick(MouseEvent e, int x, int y, Node clicked) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (clicked != null) eliminarNodo(clicked);
            return;
        }
        if (clicked == null) crearNodo(x, y);
        else if (nodoParaArista == null) {
            nodoParaArista = clicked;
            repaint();
        } else if (nodoParaArista == clicked) {
            nodoParaArista = null;
            repaint();
        } else {
            crearArista(nodoParaArista, clicked);
            nodoParaArista = null;
        }
    }

    private void ejecutarDijkstra(Node origen, Node destino) {
        List<Node> camino = Algoritmos.dijkstra(grafo, origen, destino);
        if (camino.isEmpty()) { ventana.mostrar("No hay camino."); return; }
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++)
            total += grafo.getWeight(camino.get(i), camino.get(i + 1));
        resaltarCamino(camino);
        ventana.mostrarResultado("Dijkstra", "Distancia: " + total + " km");
    }

    private void ejecutarBellmanFord(Node origen, Node destino) {
        List<Node> camino = Algoritmos.bellmanFord(grafo, origen, destino);
        if (camino == null || camino.isEmpty()) { ventana.mostrar("No hay camino."); return; }
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++)
            total += grafo.getWeight(camino.get(i), camino.get(i + 1));
        resaltarCamino(camino);
        ventana.mostrarResultado("Bellman-Ford", "Distancia: " + total + " km");
    }

    private void ejecutarBFS(Node inicio) {
        List<Node> rec = Algoritmos.bfs(grafo, inicio);
        resaltarCamino(rec);
        ventana.mostrarResultado("BFS", rec.toString());
    }

    private void ejecutarDFS(Node inicio) {
        List<Node> rec = Algoritmos.dfs(grafo, inicio);
        resaltarCamino(rec);
        ventana.mostrarResultado("DFS", rec.toString());
    }

    private void crearNodo(int x, int y) {
        String input = JOptionPane.showInputDialog(this, "Nombre:");
        if (input == null || input.trim().isEmpty()) return;
        String nombreLimpio = input.trim();
        nodos.add(new Node(nodos.size(), nombreLimpio, x, y));
        reconstruirGrafo();
    }

    private void crearArista(Node a, Node b) {
        String input = JOptionPane.showInputDialog(this, "Distancia:");
        if (input == null) return;
        try {
            double peso = Double.parseDouble(input.trim());
            agregarArista(a, b, peso);
        } catch (Exception ex) {}
    }

    private void eliminarNodo(Node nd) {
        nodos.remove(nd);
        aristas.removeIf(e -> e.getOrigen() == nd || e.getDestino() == nd);
        reconstruirGrafo();
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e)  {}
}