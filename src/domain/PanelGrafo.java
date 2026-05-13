import domain.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class PanelGrafo extends JPanel implements MouseListener {

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

    private static final int R = 25;
    private static final Color C_NODO      = new Color(52, 101, 164);
    private static final Color C_BORDE     = new Color(114, 159, 207);
    private static final Color C_SEL       = new Color(245, 121, 0);
    private static final Color C_CAMINO    = new Color(255, 200, 0);
    private static final Color C_ARISTA    = new Color(100, 100, 110);
    private static final Color C_HIGHLIGHT = new Color(252, 132, 0);
    private static final Color C_TEXTO     = Color.WHITE;

    public PanelGrafo(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setBackground(new Color(28, 28, 36));
        setPreferredSize(new Dimension(780, 680));
        addMouseListener(this);
        cargarDatos();
    }

    private void cargarDatos() {
        ParkData data = new ParkData();
        nodos = new ArrayList<>(Arrays.asList(data.parkArray));
        grafo = data.parkGraph;
        aristas = new ArrayList<>();
        Node[] verts = grafo.getVerteces();
        for (int i = 0; i < verts.length; i++)
            for (int j = i + 1; j < verts.length; j++) {
                Edge e = grafo.getConnection(verts[i], verts[j]);
                if (e != null && e.getPeso() > 0) aristas.add(e);
            }
    }

    // ── Painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Edge e : aristas)
            dibujarArista(g2, e, esResaltada(e));

        for (Node nd : nodos)
            dibujarNodo(g2, nd,
                nd == origenSeleccionado || nd == nodoParaArista,
                caminoResaltado.contains(nd));

        dibujarEstado(g2);
    }

    private boolean esResaltada(Edge e) {
        if (aristasResaltadas.contains(e)) return true;
        if (caminoResaltado.size() < 2) return false;
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
            g2.setColor(C_HIGHLIGHT);
            g2.setStroke(new BasicStroke(3.5f));
        } else {
            g2.setColor(C_ARISTA);
            g2.setStroke(new BasicStroke(1.5f));
        }
        g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());

        // Weight label at midpoint
        int mx = (a.getX() + b.getX()) / 2;
        int my = (a.getY() + b.getY()) / 2;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String label = String.format("%.1f", e.getPeso());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(mx - fm.stringWidth(label) / 2 - 2, my - fm.getAscent(), fm.stringWidth(label) + 4, fm.getHeight(), 4, 4);
        g2.setColor(resaltada ? C_HIGHLIGHT : new Color(190, 190, 190));
        g2.drawString(label, mx - fm.stringWidth(label) / 2, my);
    }

    private void dibujarNodo(Graphics2D g2, Node nd, boolean seleccionado, boolean enCamino) {
        int x = nd.getX() - R, y = nd.getY() - R, d = R * 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(x + 3, y + 3, d, d);

        // Fill
        g2.setColor(enCamino ? C_CAMINO : seleccionado ? C_SEL : C_NODO);
        g2.fillOval(x, y, d, d);

        // Border
        g2.setColor(enCamino || seleccionado ? C_HIGHLIGHT : C_BORDE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, d, d);

        // ID inside
        g2.setColor(C_TEXTO);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        String id = String.valueOf(nd.getId());
        g2.drawString(id, nd.getX() - fm.stringWidth(id) / 2, nd.getY() + fm.getAscent() / 2 - 2);

        // Name below with dark background
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        String nombre = nd.getNombre();
        if (fm.stringWidth(nombre) > 90) nombre = nombre.substring(0, 10) + "…";
        int tx = nd.getX() - fm.stringWidth(nombre) / 2;
        int ty = nd.getY() + R + fm.getAscent() + 3;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(tx - 2, ty - fm.getAscent(), fm.stringWidth(nombre) + 4, fm.getHeight() + 1, 3, 3);
        g2.setColor(enCamino ? C_CAMINO : C_TEXTO);
        g2.drawString(nombre, tx, ty);
    }

    private void dibujarEstado(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        String modo = modoEdicion ? "✎ MODO EDICIÓN" : "⬡ MODO EXPLORACIÓN";
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(6, 6, 200, 20, 6, 6);
        g2.setColor(modoEdicion ? new Color(252, 175, 62) : new Color(114, 159, 207));
        g2.drawString(modo, 10, 21);

        if (estado != EstadoAlgoritmo.NINGUNO) {
            String hint = switch (estado) {
                case DIJKSTRA_ORIGEN  -> "Clic en nodo ORIGEN";
                case DIJKSTRA_DESTINO -> "Clic en nodo DESTINO  (origen: " + origenSeleccionado + ")";
                case BFS_INICIO       -> "Clic en nodo de INICIO (BFS)";
                case DFS_INICIO       -> "Clic en nodo de INICIO (DFS)";
                default               -> "";
            };
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(0, 0, 0, 130));
            g2.fillRoundRect(6, 30, fm.stringWidth(hint) + 8, 18, 5, 5);
            g2.setColor(new Color(238, 238, 236));
            g2.drawString(hint, 10, 43);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
        repaint();
    }

    // ── Public API for VentanaPrincipal ───────────────────────────────────────

    public Graph getGrafo() { return grafo; }

    public void setModoEdicion(boolean modo) {
        modoEdicion = modo;
        estado = EstadoAlgoritmo.NINGUNO;
        origenSeleccionado = null;
        nodoParaArista = null;
        repaint();
        ventana.mostrar(modo
            ? "Modo edición: clic en espacio vacío → nuevo nodo\n"
              + "Clic en nodo → seleccionar para arista\n"
              + "Clic derecho en nodo → eliminar"
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

    // ── Mouse events ──────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
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
            default -> { /* nothing */ }
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
            ventana.mostrar("Seleccionado: " + clicked.getNombre() + "\nClic en otro nodo para crear arista.");
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

    // ── Algorithm execution ───────────────────────────────────────────────────

    private void ejecutarDijkstra(Node origen, Node destino) {
        List<Node> camino = Algoritmos.dijkstra(grafo, origen, destino);
        if (camino.isEmpty()) {
            ventana.mostrarResultado("Dijkstra", "No existe camino entre " + origen + " y " + destino + ".");
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
        sb.append(String.format("\n\nDistancia total: %.2f Km", total));
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

    // ── Edit operations ───────────────────────────────────────────────────────

    private void crearNodo(int x, int y) {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo nodo:", "Nuevo Nodo", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) return;
        Node nuevo = new Node(nodos.size(), nombre.trim(), x, y);
        nodos.add(nuevo);
        reconstruirGrafo();
        ventana.mostrar("Nodo creado: " + nombre.trim());
    }

    private void crearArista(Node a, Node b) {
        String input = JOptionPane.showInputDialog(this,
            "Distancia entre " + a + " y " + b + " (Km):", "Nueva Arista", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) { repaint(); return; }
        try {
            double peso = Double.parseDouble(input.trim().replace(",", "."));
            if (peso <= 0) { JOptionPane.showMessageDialog(this, "La distancia debe ser positiva."); return; }
            aristas.add(new Edge(a, b, peso));
            reconstruirGrafo();
            ventana.mostrar("Arista creada: " + a + " ↔ " + b + " = " + peso + " Km");
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

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}

