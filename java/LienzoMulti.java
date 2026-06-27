import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class LienzoMulti extends JPanel {
    private ArrayList<VectorMulti> vectores;
    private Color[] colores = {
            new Color(0, 102, 204, 250), // Azul
            new Color(220, 53, 69, 250), // Rojo
            new Color(40, 167, 69, 250), // Verde
            new Color(111, 66, 193, 250), // Morado
            new Color(253, 126, 20, 250) // Naranja
    };

    public LienzoMulti(ArrayList<VectorMulti> v) {
        this.vectores = v;
        setBackground(Color.WHITE);
    }

    private void dibujarTextoConFondo(Graphics2D g2, String texto, int x, int y, Color colorTexto) {
        FontMetrics fm = g2.getFontMetrics();
        int ancho = fm.stringWidth(texto);
        int alto = fm.getHeight();
        int ascent = fm.getAscent();

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(x - 2, y - ascent - 2, ancho + 4, alto + 2, 4, 4);

        g2.setColor(colorTexto);
        g2.drawString(texto, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int cx = getWidth() / 2, cy = getHeight() / 2;

        // --- Dibujo de Ejes Cartesianos ---
        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawLine(0, cy, getWidth(), cy);
        g2.drawLine(cx, 0, cx, getHeight());

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        dibujarTextoConFondo(g2, "x (0° / 360°)", getWidth() - 95, cy - 10, Color.DARK_GRAY);
        dibujarTextoConFondo(g2, "y (90°)", cx + 10, 20, Color.DARK_GRAY);
        dibujarTextoConFondo(g2, "-x (180°)", 10, cy - 10, Color.DARK_GRAY);
        dibujarTextoConFondo(g2, "-y (270°)", cx + 10, getHeight() - 15, Color.DARK_GRAY);
        dibujarTextoConFondo(g2, "0", cx - 12, cy + 15, Color.DARK_GRAY);

        if (vectores.isEmpty())
            return;

        // --- Cálculo de Escala (Zoom) y Marcas de Graduación ---
        double max = 1;
        for (VectorMulti v : vectores) {
            max = Math.max(max, Math.max(Math.abs(v.x), Math.abs(v.y)));
        }
        double zoom = (Math.min(cx, cy) * 0.75) / max;

        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        int numMarcas = (int) Math.ceil(max);
        int paso = 1;
        if (numMarcas > 15)
            paso = numMarcas / 10;
        if (paso == 0)
            paso = 1;

        for (int i = paso; i <= numMarcas; i += paso) {
            int pos = (int) (i * zoom);
            int tickSize = 4;
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(cx + pos, cy - tickSize, cx + pos, cy + tickSize);
            dibujarTextoConFondo(g2, String.valueOf(i), cx + pos - 5, cy + 18, Color.GRAY);
            g2.drawLine(cx - pos, cy - tickSize, cx - pos, cy + tickSize);
            dibujarTextoConFondo(g2, "-" + i, cx - pos - 8, cy + 18, Color.GRAY);
            g2.drawLine(cx - tickSize, cy - pos, cx + tickSize, cy - pos);
            dibujarTextoConFondo(g2, String.valueOf(i), cx + 8, cy - pos + 4, Color.GRAY);
            g2.drawLine(cx - tickSize, cy + pos, cx + tickSize, cy + pos);
            dibujarTextoConFondo(g2, "-" + i, cx + 8, cy + pos + 4, Color.GRAY);
        }

        // --- Estilo de Línea Punteada para Proyecciones y Arcos ---
        Stroke trazoPunteado = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                new float[] { 4f }, 0.0f);
        // --- Polígono: Línea discontinua conectando las cabezas de los vectores ---
        if (vectores.size() > 1) {
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                    new float[] { 6f, 5f }, 0.0f));
            g2.setColor(new Color(130, 130, 130, 200));

            for (int i = 0; i < vectores.size(); i++) {
                VectorMulti vA = vectores.get(i);
                VectorMulti vB = vectores.get((i + 1) % vectores.size());

                int xA = cx + (int) (vA.x * zoom);
                int yA = cy - (int) (vA.y * zoom);
                int xB = cx + (int) (vB.x * zoom);
                int yB = cy - (int) (vB.y * zoom);

                g2.drawLine(xA, yA, xB, yB);
            }
        }
        // --- Dibujo de Vectores y sus Componentes ---
        for (int i = 0; i < vectores.size(); i++) {
            VectorMulti v = vectores.get(i);
            Color colorVector = colores[i % colores.length];

            int vx = (int) (v.x * zoom);
            int vy = (int) (v.y * zoom); // Componente Y en pantalla (invertida)

            // 1. Proyecciones punteadas hacia los ejes X e Y
            g2.setColor(new Color(colorVector.getRed(), colorVector.getGreen(), colorVector.getBlue(), 130)); // Tono
                                                                                                              // semi-transparente
            g2.setStroke(trazoPunteado);
            g2.drawLine(cx + vx, cy - vy, cx + vx, cy); // Proyección vertical al eje X
            g2.drawLine(cx + vx, cy - vy, cx, cy - vy); // Proyección horizontal al eje Y

            // 2. Arco del ángulo (Radio incremental según el índice)
            g2.setColor(colorVector);
            int radioArco = 40 + (i * 20);
            g2.drawArc(cx - radioArco / 2, cy - radioArco / 2, radioArco, radioArco, 0, (int) v.angulo);

            // 3. Vector principal (Línea continua y extremo circular)
            float grosorVector = 3.5f - (i * 0.3f);
            if (grosorVector < 1.8f)
                grosorVector = 1.8f; // Evita que se vuelva excesivamente delgado
            g2.setStroke(new BasicStroke(grosorVector));
            g2.drawLine(cx, cy, cx + vx, cy - vy);
            g2.fillOval(cx + vx - 5, cy - vy - 5, 10, 10);

            // 4. Etiquetas informativas en la punta del vector
            g2.setFont(new Font("Arial", Font.BOLD, 11));

            String txtCartesiano = String.format("V%d (X:%.1f, Y:%.1f)", i + 1, v.x, v.y);
            String txtPolar = String.format("M:%.1f, A:%.1f°", v.r, v.angulo);

            // Desplazamiento dinámico para evitar colisiones entre textos del mismo vector
            int offsetBaseY = cy - vy - 8;

            dibujarTextoConFondo(g2, txtCartesiano, cx + vx + 8, offsetBaseY, colorVector);
            dibujarTextoConFondo(g2, txtPolar, cx + vx + 8, offsetBaseY + 14, colorVector);
        }
    }
}