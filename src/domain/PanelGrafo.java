package domain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PanelGrafo extends JPanel implements MouseListener, MouseMotionListener {

    public enum EstadoAlgoritmo {
        NINGUNO, DIJKSTRA_ORIGEN, DIJKSTRA_DESTINO, BFS_INICIO, DFS_INICIO
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

    // Drag state
    private Node dragCandidate  = null;
    private Node nodeArrastrado = null;
    private int  dragOffsetX, dragOffsetY;
    private int  prevDragX, prevDragY;
    // pesos originales al inicio del arrastre (para escalar en tiempo real)
    private final Map<Edge, Double> originalPesos = new HashMap<>();

    private static final int R = 28;

    // Paleta de colores
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
            // Resplandor naranja
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

        // Etiqueta de peso
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

        // Sombra
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(x - R + 4, y - R + 4, d, d);

        // Resplandor para nodos seleccionados / en camino
        if (seleccionado || enCamino) {
            Color glow = enCamino
                ? new Color(80, 210, 140, 55)
                : new Color(255, 140, 30, 55);
            g2.setColor(glow);
            g2.fillOval(x - R - 8, y - R - 8, d + 16, d + 16);
        }

        // Relleno degradado
        Color top = enCamino ? C_PATH_TOP : seleccionado ? C_SEL_TOP : C_NODO_TOP;
        Color bot = enCamino ? C_PATH_BOT : seleccionado ? C_SEL_BOT : C_NODO_BOT;
        g2.setPaint(new GradientPaint(x - R, y - R, top, x + R, y + R, bot));
        g2.fillOval(x - R, y - R, d, d);

        // Borde
        Color borde = enCamino
            ? new Color(110, 245, 165)
            : seleccionado
                ? new Color(255, 185, 80)
                : C_BORDE;
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x - R, y - R, d, d);

        // Brillo (efecto glassy)
        g2.setColor(new Color(255, 255, 255, 45));
        g2.fillOval(x - R + 5, y - R + 4, d / 2, d / 3);

        // ID del nodo
        g2.setColor(C_TEXTO);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        String id = String.valueOf(nd.getId());
        g2.drawString(id, x - fm.stringWidth(id) / 2, y + fm.getAscent() / 2 - 1);

        // Nombre debajo
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
        // Píldora de modo (arriba a la derecha)
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

        // Pista de algoritmo (arriba a la izquierda)
        if (estado != EstadoAlgoritmo.NINGUNO) {
            String hint = switch (estado) {
                case DIJKSTRA_ORIGEN  -> "Clic en nodo ORIGEN";
                case DIJKSTRA_DESTINO -> "Origen: " + origenSeleccionado + "  •  Clic en DESTINO";
                case BFS_INICIO       -> "Clic en nodo de INICIO  (BFS)";
                case DFS_INICIO       -> "Clic en nodo de INICIO  (DFS)";
                default               -> "";
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
        ventana.mostrar(modo
            ? "Modo edición:\n• Clic en espacio vacío → nuevo nodo\n"
              + "• Clic en nodo → seleccionar para arista\n"
              + "• Arrastrar nodo → mover (km se actualizan)\n"
              + "• Clic derecho → eliminar nodo"
            : "Modo exploración activado.");
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
        aristas.removeIf(e ->
            (e.getOrigen() == a && e.getDestino() == b) ||
            (e.getOrigen() == b && e.getDestino() == a));
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

        // Capturar pesos originales de las aristas conectadas
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

        // Actualizar pesos EN TIEMPO REAL según la distancia al punto de origen del arrastre
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
        if (nodeArrastrado != null) {
            reconstruirGrafo();   // sincroniza el grafo con los pesos finales
            nodeArrastrado = null;
        }
        dragCandidate = null;
        originalPesos.clear();
    }

    @Override
    public void mouseMoved(MouseEvent e) {}


    @Override
    public void mouseClicked(MouseEvent e) {
        // mouseClicked no se dispara si hubo movimiento (drag), así que es seguro procesarlo
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
                ventana.mostrar("Origen: " + clicked.getNombre() + "\nAhora clic en el nodo destino.");
                repaint();
            }
            case DIJKSTRA_DESTINO -> {
                if (clicked == origenSeleccionado) { ventana.mostrar("Elige un nodo diferente como destino."); return; }
                ejecutarDijkstra(origenSeleccionado, clicked);
                estado = EstadoAlgoritmo.NINGUNO;
                origenSeleccionado = null;
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
        if (clicked == null) {
            crearNodo(x, y);
        } else if (nodoParaArista == null) {
            nodoParaArista = clicked;
            ventana.mostrar("Seleccionado: " + clicked.getNombre()
                + "\nClic en otro nodo para crear arista.");
            repaint();
        } else if (nodoParaArista == clicked) {
            nodoParaArista = null;
            ventana.mostrar("Selección cancelada.");
            repaint();
        } else {
            crearArista(nodoParaArista, clicked);
            nodoParaArista = null;
        }
    }


    private void ejecutarDijkstra(Node origen, Node destino) {
        List<Node> camino = Algoritmos.dijkstra(grafo, origen, destino);
        if (camino.isEmpty()) {
            ventana.mostrarResultado("Dijkstra",
                "No existe camino entre " + origen + " y " + destino + ".");
            return;
        }
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++)
            total += grafo.getWeight(camino.get(i), camino.get(i + 1));

        StringBuilder sb = new StringBuilder("Camino más corto:\n  ");
        for (int i = 0; i < camino.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(camino.get(i).getNombre());
        }
        sb.append(String.format("\n\nDistancia total: %.2f km", total));
        resaltarCamino(camino);
        ventana.mostrarResultado("Dijkstra  " + origen + " → " + destino, sb.toString());
    }

    private void ejecutarBFS(Node inicio) {
        List<Node> rec = Algoritmos.bfs(grafo, inicio);
        StringBuilder sb = new StringBuilder("Orden de visita:\n  ");
        for (int i = 0; i < rec.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(rec.get(i).getNombre());
        }
        resaltarCamino(rec);
        ventana.mostrarResultado("BFS desde " + inicio, sb.toString());
    }

    private void ejecutarDFS(Node inicio) {
        List<Node> rec = Algoritmos.dfs(grafo, inicio);
        StringBuilder sb = new StringBuilder("Orden de visita:\n  ");
        for (int i = 0; i < rec.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(rec.get(i).getNombre());
        }
        resaltarCamino(rec);
        ventana.mostrarResultado("DFS desde " + inicio, sb.toString());
    }


    private void crearNodo(int x, int y) {
        String nombre = JOptionPane.showInputDialog(this,
            "Nombre del nuevo nodo:", "Nuevo Nodo", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) return;
        nodos.add(new Node(nodos.size(), nombre.trim(), x, y));
        reconstruirGrafo();
        ventana.mostrar("Nodo creado: " + nombre.trim());
    }

    private void crearArista(Node a, Node b) {
        String input = JOptionPane.showInputDialog(this,
            "Distancia entre " + a + " y " + b + " (km):",
            "Nueva Arista", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) { repaint(); return; }
        try {
            double peso = Double.parseDouble(input.trim().replace(",", "."));
            if (peso <= 0) { JOptionPane.showMessageDialog(this, "La distancia debe ser positiva."); return; }
            agregarArista(a, b, peso);
            ventana.mostrar("Arista creada: " + a + " ↔ " + b + " = " + peso + " km");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarNodo(Node nd) {
        int r = JOptionPane.showConfirmDialog(this,
            "¿Eliminar el nodo \"" + nd.getNombre() + "\" y sus aristas?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        aristas.removeIf(e -> e.getOrigen() == nd || e.getDestino() == nd);
        nodos.remove(nd);
        reconstruirGrafo();
        ventana.mostrar("Nodo eliminado: " + nd.getNombre());
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e)  {}
}
