package domain;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private PanelGrafo panelGrafo;
    private JTextArea  areaTexto;
    private JComboBox<Node> cbOrigen;
    private JComboBox<Node> cbDestino;
    private JTextField tfDistancia;

    // Paleta sidebar
    private static final Color CARD_BG   = new Color(20, 26, 46);
    private static final Color CARD_BRD  = new Color(45, 60, 100);
    private static final Color ACCENT    = new Color(79, 109, 245);
    private static final Color ACCENT2   = new Color(255, 140, 30);
    private static final Color TEXT_DIM  = new Color(140, 160, 210);

    public VentanaPrincipal() {
        setTitle("Grafos — Parques de Bogotá");
        setSize(1260, 790);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panelGrafo = new PanelGrafo(this);

        areaTexto = new JTextArea("Bienvenido.\nUsa el menú Algoritmos para comenzar.");
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setMargin(new Insets(10, 10, 10, 10));
        areaTexto.setBackground(new Color(15, 20, 38));
        areaTexto.setForeground(new Color(200, 215, 255));

        buildLayout();
        buildMenuBar();
        actualizarCombosNodos();
        setVisible(true);
    }


    private void buildLayout() {
        setLayout(new BorderLayout(0, 0));

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.add(panelGrafo,           BorderLayout.CENTER);
        center.add(buildAlgorithmBar(),  BorderLayout.SOUTH);

        add(center,           BorderLayout.CENTER);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel(new BorderLayout(0, 10));
        side.setPreferredSize(new Dimension(310, 0));
        side.setBackground(new Color(15, 20, 38));
        side.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 10));

        side.add(buildModePanel(),       BorderLayout.NORTH);
        side.add(buildResultsPanel(),    BorderLayout.CENTER);
        side.add(buildNuevoCaminoPanel(), BorderLayout.SOUTH);

        return side;
    }

    private JPanel buildModePanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);

        JLabel title = new JLabel("MODO");
        title.setFont(new Font("SansSerif", Font.BOLD, 10));
        title.setForeground(TEXT_DIM);
        title.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        wrapper.add(title, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);

        JButton btnEd  = makeButton("✎  Edición",     ACCENT2);
        JButton btnExp = makeButton("⬡  Exploración", ACCENT);

        btnEd .addActionListener(e -> panelGrafo.setModoEdicion(true));
        btnExp.addActionListener(e -> panelGrafo.setModoEdicion(false));

        btnRow.add(btnEd);
        btnRow.add(btnExp);
        wrapper.add(btnRow, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildResultsPanel() {
        JPanel card = makeCard("Resultados");
        card.setLayout(new BorderLayout(0, 6));

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setBorder(BorderFactory.createLineBorder(CARD_BRD, 1));
        scroll.setBackground(new Color(15, 20, 38));
        card.add(scroll, BorderLayout.CENTER);

        JButton btnLimpiar = makeButton("Limpiar resaltado", new Color(60, 80, 140));
        btnLimpiar.addActionListener(e -> panelGrafo.limpiarResaltado());
        card.add(btnLimpiar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildNuevoCaminoPanel() {
        JPanel card = makeCard("Nuevo Camino");
        card.setLayout(new BorderLayout(0, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill   = GridBagConstraints.HORIZONTAL;

        cbOrigen  = new JComboBox<>();
        cbDestino = new JComboBox<>();
        tfDistancia = new JTextField(6);
        tfDistancia.setToolTipText("Distancia en km");

        addFormRow(form, g, 0, "Origen:",          cbOrigen);
        addFormRow(form, g, 1, "Destino:",          cbDestino);
        addFormRow(form, g, 2, "Distancia (km):",   tfDistancia);

        card.add(form, BorderLayout.CENTER);

        JButton btnCrear = makeButton("+ Crear Camino", new Color(60, 180, 110));
        btnCrear.addActionListener(e -> crearCaminoDesdePanel());
        card.add(btnCrear, BorderLayout.SOUTH);

        return card;
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row,
                            String label, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_DIM);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 1;
        p.add(comp, g);
    }

    private JPanel buildAlgorithmBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        bar.setBackground(new Color(12, 16, 32));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BRD));

        JLabel lbl = new JLabel("Algoritmos:");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_DIM);
        bar.add(lbl);

        // Agregado el botón de Bellman-Ford manteniendo la distribución visual uniforme
        bar.add(makeAlgoBtn("⬦ Dijkstra",       new Color( 67, 97, 238), e -> iniciarDijkstra()));
        bar.add(makeAlgoBtn("⬦ Bellman-Ford",    new Color( 38, 70, 180), e -> iniciarBellmanFord()));
        bar.add(makeAlgoBtn("◉ BFS",             new Color( 47,168, 104), e -> iniciarBFS()));
        bar.add(makeAlgoBtn("◎ DFS",             new Color( 30,160, 160), e -> iniciarDFS()));
        bar.add(algoSep());
        bar.add(makeAlgoBtn("✦ Prim",            new Color(200, 90,   0), e -> ejecutarPrim()));
        bar.add(makeAlgoBtn("✧ Kruskal",         new Color(140, 60, 200), e -> ejecutarKruskal()));
        bar.add(algoSep());
        bar.add(makeAlgoBtn("⊞ Floyd-Warshall",  new Color(180, 30,  60), e -> ejecutarFloydWarshall()));
        bar.add(algoSep());
        bar.add(makeAlgoBtn("✕ Limpiar",         new Color( 55, 70, 115), e -> panelGrafo.limpiarResaltado()));

        return bar;
    }

    private JButton makeAlgoBtn(String text, Color color, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(color.darker(), 5),
            BorderFactory.createEmptyBorder(5, 11, 5, 11)
        ));
        btn.setOpaque(true);
        btn.addActionListener(al);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(color); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(color.darker()); }
        });
        return btn;
    }

    private JSeparator algoSep() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(CARD_BRD);
        return sep;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bar.setBackground(new Color(10, 14, 28));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BRD));

        JLabel lbl = new JLabel("Grafos — Parques de Bogotá  |  Arrastra nodos en Modo Edición para ajustar distancias");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(new Color(90, 110, 170));
        bar.add(lbl);
        return bar;
    }


    private JPanel makeCard(String titulo) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(CARD_BRD, 8),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2, 4, 4, 4),
                titulo,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(130, 160, 240))
        ));
        return card;
    }

    private JButton makeButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(color, 6),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(color);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(color.darker());
            }
        });
        return btn;
    }


    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mAlg = new JMenu("Algoritmos");
        addMenuItem(mAlg, "Dijkstra (camino más corto)",       e -> iniciarDijkstra());
        addMenuItem(mAlg, "Bellman-Ford (camino alternativo)",   e -> iniciarBellmanFord()); // Agregado al menú desplegable
        addMenuItem(mAlg, "Prim (MST)",                        e -> ejecutarPrim());
        addMenuItem(mAlg, "Floyd-Warshall (todos los pares)",  e -> ejecutarFloydWarshall());
        addMenuItem(mAlg, "Kruskal (MST)",                     e -> ejecutarKruskal());
        mAlg.addSeparator();
        addMenuItem(mAlg, "BFS (recorrido en amplitud)",       e -> iniciarBFS());
        addMenuItem(mAlg, "DFS (recorrido en profundidad)",    e -> iniciarDFS());

        JMenu mEd = new JMenu("Edición");
        addMenuItem(mEd, "Modo Edición",     e -> panelGrafo.setModoEdicion(true));
        addMenuItem(mEd, "Modo Exploración", e -> panelGrafo.setModoEdicion(false));

        JMenu mVer = new JMenu("Ver");
        addMenuItem(mVer, "Limpiar resaltado", e -> panelGrafo.limpiarResaltado());

        bar.add(mAlg);
        bar.add(mEd);
        bar.add(mVer);
        setJMenuBar(bar);
    }

    private void addMenuItem(JMenu menu, String label,
                             java.awt.event.ActionListener al) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(al);
        menu.add(item);
    }


    public void actualizarCombosNodos() {
        if (cbOrigen == null || cbDestino == null) return;
        Node selO = (Node) cbOrigen.getSelectedItem();
        Node selD = (Node) cbDestino.getSelectedItem();

        cbOrigen.removeAllItems();
        cbDestino.removeAllItems();
        for (Node nd : panelGrafo.getNodos()) {
            cbOrigen.addItem(nd);
            cbDestino.addItem(nd);
        }
        if (selO != null && panelGrafo.getNodos().contains(selO)) cbOrigen.setSelectedItem(selO);
        if (selD != null && panelGrafo.getNodos().contains(selD)) cbDestino.setSelectedItem(selD);
    }

    private void crearCaminoDesdePanel() {
        Node origen  = (Node) cbOrigen.getSelectedItem();
        Node destino = (Node) cbDestino.getSelectedItem();
        String txt   = tfDistancia.getText().trim().replace(",", ".");

        if (origen == null || destino == null) {
            JOptionPane.showMessageDialog(this, "Selecciona origen y destino.",
                "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (origen == destino) {
            JOptionPane.showMessageDialog(this, "Origen y destino deben ser distintos.",
                "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (txt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa la distancia en km.",
                "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double km = Double.parseDouble(txt);
            if (km <= 0) {
                JOptionPane.showMessageDialog(this, "La distancia debe ser positiva.");
                return;
            }
            panelGrafo.agregarArista(origen, destino, km);
            tfDistancia.setText("");
            mostrar("Camino creado:\n  " + origen + " ↔ " + destino
                + "\n  Distancia: " + km + " km");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }


    private void iniciarDijkstra() {
        mostrar("Dijkstra activado.\nHaz clic en el nodo ORIGEN en el grafo.");
        panelGrafo.iniciarAlgoritmo(PanelGrafo.EstadoAlgoritmo.DIJKSTRA_ORIGEN);
    }

    // Nuevo método para disparar el flujo interactivo de Bellman-Ford
    private void iniciarBellmanFord() {
        mostrar("Bellman-Ford activado.\nHaz clic en el nodo ORIGEN en el grafo.");
        panelGrafo.iniciarAlgoritmo(PanelGrafo.EstadoAlgoritmo.BELLMAN_FORD_ORIGEN);
    }

    private void ejecutarPrim() {
        Graph g = panelGrafo.getGrafo();
        List<Edge> mst = Algoritmos.prim(g);
        if (mst.isEmpty()) { mostrarResultado("Prim", "El grafo no tiene aristas."); return; }

        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Edge e : mst) { sb.append("  ").append(e).append("\n"); total += e.getPeso(); }
        sb.append(String.format("\nPeso total MST: %.2f km", total));

        panelGrafo.resaltarAristas(mst);
        mostrarResultado("Prim — Árbol de Expansión Mínima", sb.toString());
    }

    private void ejecutarFloydWarshall() {
        Graph g = panelGrafo.getGrafo();
        double[][] dist = Algoritmos.floydWarshall(g);
        Node[] verts = g.getVerteces();
        double INF = Double.MAX_VALUE / 2;

        StringBuilder sb = new StringBuilder("Distancias mínimas (km):\n\n");
        int col = 10;
        sb.append(String.format("%-" + col + "s", ""));
        for (Node v : verts) sb.append(String.format("%-" + col + "s", abrev(v.getNombre(), col - 1)));
        sb.append("\n");
        for (int i = 0; i < verts.length; i++) {
            sb.append(String.format("%-" + col + "s", abrev(verts[i].getNombre(), col - 1)));
            for (int j = 0; j < verts.length; j++) {
                String val = dist[i][j] >= INF ? "∞" : String.format("%.1f", dist[i][j]);
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
        for (Edge e : mst) { sb.append("  ").append(e).append("\n"); total += e.getPeso(); }
        sb.append(String.format("\nPeso total MST: %.2f km", total));

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
        areaTexto.setText("══════════════════════\n" + titulo
            + "\n══════════════════════\n\n" + cuerpo);
        areaTexto.setCaretPosition(0);
    }

    public void mostrar(String mensaje) {
        areaTexto.setText(mensaje);
    }

    private String abrev(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }


    private static class RoundedBorder implements Border {
        private final Color color;
        private final int   radius;

        RoundedBorder(Color color, int radius) {
            this.color  = color;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public boolean isBorderOpaque() { return false; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
            g2.dispose();
        }
    }
}