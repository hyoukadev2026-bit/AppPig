import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

// Imports para exportación a PDF (Requiere la librería OpenPDF o iText)
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class programa extends JFrame {
    
    // =========================================================
    // VARIABLES ORIGINALES DE LÓGICA (INTACTAS)
    // =========================================================
    private JTextField txtDato1, txtDato2;
    private JComboBox<String> cmbTipoEntrada;
    private LienzoMulti panelGrafico;
    private ArrayList<VectorMulti> listaVectores;

    private JSplitPane splitPane;
    private JTextArea areaReporte;

    private StringBuilder reporteIngresos;
    private StringBuilder reporteSuma;
    private StringBuilder reporteProducto;

    private boolean flagSuma = false;
    private boolean flagProducto = false;
    private double lastDotProductResult = 0;
    private double lastDotProductAngle = 0;
    private ArrayList<Double> listaAngulosConResultante = new ArrayList<>();

    // =========================================================
    // VARIABLES DE UI/UX MODERNAS
    // =========================================================
    private JPanel panelMenuWrapper;
    private CardPanel panelReporteCard;
    
    // Banderas de estado visual
    private boolean menuOpen = true;
    private boolean reportOpen = false;

    // Paleta de colores moderna
    public static final Color BG_COLOR = new Color(245, 247, 250);
    public static final Color CARD_COLOR = new Color(255, 255, 255);
    public static final Color BORDER_COLOR = new Color(230, 232, 236);
    public static final Color TEXT_COLOR = new Color(30, 41, 59);
    
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);
    public static final Color ACCENT_GREEN = new Color(34, 197, 94);
    public static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    public static final Color ACCENT_RED = new Color(239, 68, 68);
    public static final Color ACCENT_CYAN = new Color(6, 182, 212);
    public static final Color ACCENT_DARK = new Color(51, 65, 85);

    public static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public programa() {
        listaVectores = new ArrayList<>();
        reporteIngresos = new StringBuilder("--- HISTORIAL DE VECTORES AGREGADOS ---\n\n");
        reporteSuma = new StringBuilder();
        reporteProducto = new StringBuilder();

        setTitle("Analizador de Vectores 2026");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        JPanel mainWrapper = new JPanel(new BorderLayout(15, 15));
        mainWrapper.setBackground(BG_COLOR);
        mainWrapper.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainWrapper);

        // =========================================================
        // 1. PANEL SUPERIOR (NORTH)
        // =========================================================
        CardPanel panelNorte = new CardPanel();
        panelNorte.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));

        cmbTipoEntrada = new ModernComboBox(new String[] { "Rectangulares (X, Y)", "Polares (M, A)" });
        txtDato1 = new ModernTextField(8);
        txtDato2 = new ModernTextField(8);
        
        ModernButton btnAgregar = new ModernButton("Agregar Vector", ACCENT_GREEN);
        ModernButton btnLimpiar = new ModernButton("Limpiar Lienzo", ACCENT_RED);
        btnLimpiar.setOutlined(true);

        JLabel lblTipo = new JLabel("Tipo de Entrada:");
        lblTipo.setFont(BOLD_FONT);
        lblTipo.setForeground(TEXT_COLOR);
        
        JLabel lblD1 = new JLabel("Dato 1:");
        lblD1.setFont(BOLD_FONT);
        lblD1.setForeground(TEXT_COLOR);
        
        JLabel lblD2 = new JLabel("Dato 2:");
        lblD2.setFont(BOLD_FONT);
        lblD2.setForeground(TEXT_COLOR);

        panelNorte.add(lblTipo);
        panelNorte.add(cmbTipoEntrada);
        panelNorte.add(lblD1);
        panelNorte.add(txtDato1);
        panelNorte.add(lblD2);
        panelNorte.add(txtDato2);
        panelNorte.add(btnAgregar);
        panelNorte.add(btnLimpiar);

        mainWrapper.add(panelNorte, BorderLayout.NORTH);

        // =========================================================
        // 2. PANEL LATERAL (WEST) - Con botón Toggle
        // =========================================================
        JPanel menuArea = new JPanel(new BorderLayout(10, 0));
        menuArea.setOpaque(false);

        ModernButton btnToggleMenu = new ModernButton("☰", ACCENT_DARK);
        btnToggleMenu.setPreferredSize(new Dimension(45, 45)); 
        btnToggleMenu.setToolTipText("Mostrar/Ocultar Menú");
        btnToggleMenu.addActionListener(e -> animarVistas(!menuOpen, reportOpen));

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        togglePanel.setOpaque(false);
        togglePanel.add(btnToggleMenu);
        menuArea.add(togglePanel, BorderLayout.WEST);

        panelMenuWrapper = new JPanel(new BorderLayout());
        panelMenuWrapper.setOpaque(false);
        // Ajuste de ancho inicial a 255 para evitar recortes
        panelMenuWrapper.setPreferredSize(new Dimension(255, 0)); 

        CardPanel panelMenu = new CardPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel lblMenu = new JLabel("MENÚ");
        lblMenu.setForeground(TEXT_COLOR);
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMenu.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton btnSumar = new ModernButton("Sumar Vectores", ACCENT_CYAN);
        ModernButton btnPunto = new ModernButton("Producto Escalar", ACCENT_PURPLE);
        ModernButton btnReporte = new ModernButton("Reporte Ejecutivo", ACCENT_DARK);
        ModernButton btnGuardar = new ModernButton("Guardar PDF", ACCENT_GREEN);

        panelMenu.add(lblMenu);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 30)));
        panelMenu.add(btnSumar);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnPunto);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 15)));
        panelMenu.add(btnReporte);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 40)));
        panelMenu.add(btnGuardar);

        panelMenuWrapper.add(panelMenu, BorderLayout.CENTER);
        menuArea.add(panelMenuWrapper, BorderLayout.CENTER);

        mainWrapper.add(menuArea, BorderLayout.WEST);

        // =========================================================
        // 3. ÁREA CENTRAL (CENTER) - Gráfica y Reporte
        // =========================================================
        CardPanel panelGraficoCard = new CardPanel();
        panelGraficoCard.setLayout(new BorderLayout());
        panelGrafico = new LienzoMulti(listaVectores); 
        panelGraficoCard.add(panelGrafico, BorderLayout.CENTER);

        panelReporteCard = new CardPanel();
        panelReporteCard.setLayout(new BorderLayout());
        panelReporteCard.setVisible(false); 
        
        areaReporte = new JTextArea();
        areaReporte.setEditable(false);
        areaReporte.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaReporte.setLineWrap(true);
        areaReporte.setWrapStyleWord(true);
        areaReporte.setBackground(CARD_COLOR);
        areaReporte.setForeground(TEXT_COLOR);
        areaReporte.setBorder(new EmptyBorder(15, 15, 15, 15));

        ModernScrollPane scrollReporte = new ModernScrollPane(areaReporte);
        
        JLabel lblTituloReporte = new JLabel("  Panel de Reportes");
        lblTituloReporte.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTituloReporte.setForeground(TEXT_COLOR);
        lblTituloReporte.setBorder(new EmptyBorder(10, 5, 10, 5));
        
        panelReporteCard.add(lblTituloReporte, BorderLayout.NORTH);
        panelReporteCard.add(scrollReporte, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelGraficoCard, panelReporteCard);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        // Garantiza que la gráfica obtenga el espacio adicional al maximizar ventana
        splitPane.setResizeWeight(0.68); 

        mainWrapper.add(splitPane, BorderLayout.CENTER);

        // =========================================================
        // EVENTOS ORIGINALES
        // =========================================================
        btnAgregar.addActionListener(e -> procesarDatos());
        btnSumar.addActionListener(e -> realizarSuma());
        btnPunto.addActionListener(e -> calcularProductoEscalar());
        btnReporte.addActionListener(e -> mostrarReporteDinamico("TOP"));
        btnGuardar.addActionListener(e -> guardarPdf());

        btnLimpiar.addActionListener(e -> {
            listaVectores.clear();
            reporteIngresos = new StringBuilder("--- HISTORIAL DE VECTORES AGREGADOS ---\n\n");
            reporteSuma.setLength(0);
            reporteProducto.setLength(0);
            flagSuma = false;
            flagProducto = false;
            areaReporte.setText("");
            panelGrafico.repaint();

            animarVistas(true, false);
        });
    }

    // =========================================================
    // MOTOR DE ANIMACIÓN UI/UX MEJORADO
    // =========================================================
    private void animarVistas(boolean targetMenu, boolean targetReport) {
        if (menuOpen == targetMenu && reportOpen == targetReport) return;

        if (targetReport && !reportOpen) {
            panelReporteCard.setVisible(true);
            splitPane.setDividerLocation(splitPane.getWidth()); 
        }

        int startMenuW = panelMenuWrapper.getWidth();
        // Ancho máximo del menú ajustado a 255
        int endMenuW = targetMenu ? 255 : 0; 

        int widthSplitPane = splitPane.getWidth();
        int startDiv = splitPane.getDividerLocation();
        
        // La gráfica ocuperá el 68% (dejando 32% para reportes) para evitar recortes en textos laterales
        int endDiv = targetReport ? (int)(widthSplitPane * 0.68) : widthSplitPane;

        long duration = 350; 
        long startTime = System.currentTimeMillis();

        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);
            
            float ease = 1 - (float) Math.pow(1 - progress, 3);

            if (menuOpen != targetMenu) {
                int currMenuW = (int) (startMenuW + (endMenuW - startMenuW) * ease);
                panelMenuWrapper.setPreferredSize(new Dimension(currMenuW, 0));
                panelMenuWrapper.revalidate();
            }

            if (reportOpen != targetReport) {
                int currDiv = (int) (startDiv + (endDiv - startDiv) * ease);
                splitPane.setDividerLocation(currDiv);
            }

            if (progress >= 1f) {
                timer.stop();
                menuOpen = targetMenu;
                reportOpen = targetReport;
                if (!targetReport) {
                    panelReporteCard.setVisible(false);
                }
            }
        });
        timer.start();
    }

    // =========================================================
    // MÉTODOS DE LÓGICA ORIGINALES (INTACTOS)
    // =========================================================

    private void procesarDatos() {
        try {
            double d1 = Double.parseDouble(txtDato1.getText());
            double d2 = Double.parseDouble(txtDato2.getText());
            double x, y, r, ang;
            String procedimiento = "";
            int n = listaVectores.size() + 1;

            int modoSeleccionado = cmbTipoEntrada.getSelectedIndex();

            if (modoSeleccionado == 0) { 
                x = d1;
                y = d2;
                r = Math.sqrt((x * x) + (y * y));

                double anguloCalculadoraBruto;
                if (x == 0) {
                    anguloCalculadoraBruto = (y > 0) ? 90 : (y < 0 ? -90 : 0);
                } else {
                    anguloCalculadoraBruto = Math.toDegrees(Math.atan(y / x));
                }

                ang = Math.toDegrees(Math.atan2(y, x));
                if (ang < 0)
                    ang += 360;

                String explicacionCuadrante = "";
                if (x > 0 && y >= 0) {
                    explicacionCuadrante = "El vector se ubica en el Cuadrante I. El ángulo real es equivalente al ángulo de referencia.";
                } else if (x < 0) {
                    explicacionCuadrante = "El vector se ubica en el Cuadrante II o III (X es negativa). Se suman 180° al ángulo obtenido.\n"
                            +
                            "* Operación: 180° + (" + String.format("%.2f°", anguloCalculadoraBruto) + ")";
                } else if (x > 0 && y < 0) {
                    explicacionCuadrante = "El vector se ubica en el Cuadrante IV (X positiva, Y negativa). Se suman 360° al ángulo obtenido.\n"
                            +
                            "* Operación: 360° + (" + String.format("%.2f°", anguloCalculadoraBruto) + ")";
                } else {
                    explicacionCuadrante = "El vector se encuentra posicionado directamente sobre uno de los ejes coordenados.";
                }

                procedimiento = String.format(
                        "--- VECTOR V%d (Conversión: Rectangulares a Polares) ---\n\n" +
                                "Datos de entrada:\n" +
                                "* Coordenada horizontal (X) = %.2f\n" +
                                "* Coordenada vertical (Y) = %.2f\n\n" +
                                "Paso 1: Cálculo de la Magnitud (M)\n" +
                                "Se aplica el Teorema de Pitágoras para determinar la longitud del vector:\n" +
                                "* Operación: M = √((%.2f)² + (%.2f)²) = %.4f\n\n" +
                                "Paso 2: Cálculo del Ángulo de Referencia (A)\n" +
                                "Se utiliza la función de Tangente Inversa evaluando la proporción Y/X:\n" +
                                "* Operación: A base = tan⁻¹(%.2f / %.2f)\n" +
                                "* Ángulo de referencia obtenido: %.2f°\n\n" +
                                "Paso 3: Determinación del Ángulo en Posición Estándar\n" +
                                "%s\n" +
                                "* Ángulo resultante (0° a 360°): %.2f°\n" +
                                "---------------------------------------------------\n\n",
                        n, x, y, x, y, r, y, x, anguloCalculadoraBruto, explicacionCuadrante, ang);

                listaVectores.add(new VectorMulti(d1, d2, x, y, r, ang, "R"));

            } else { 
                double magnitudIngresada = d1;
                double anguloIngresado = d2;

                if (magnitudIngresada < 0) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede ingresar una magnitud negativa.\n\n" +
                                    "Físicamente, la magnitud representa la longitud o distancia del vector, " +
                                    "por lo que siempre debe ser un valor absoluto (positivo o cero).",
                            "Error de Ingreso",
                            JOptionPane.ERROR_MESSAGE);
                    return; 
                }

                r = magnitudIngresada;
                ang = anguloIngresado;

                double radOriginal = Math.toRadians(ang);
                x = r * Math.cos(radOriginal);
                y = r * Math.sin(radOriginal);

                double angNorm = ang % 360;
                if (angNorm < 0)
                    angNorm += 360;

                String textoPaso3 = "";
                if (ang < 0) {
                    textoPaso3 = String.format(
                            "Paso 3: Determinación del Ángulo Equivalente Positivo\n" +
                                    "Como el ángulo de cálculo es negativo, se le suman 360° para encontrar su \n" +
                                    "posición equivalente en el plano estándar (0° a 360°).\n" +
                                    "* Operación: 360° + (%.2f°) = %.2f°\n",
                            ang, angNorm);
                } else if (ang >= 360) {
                    double resta = ang - angNorm;
                    textoPaso3 = String.format(
                            "Paso 3: Determinación del Ángulo Equivalente Positivo\n" +
                                    "Como el ángulo supera una vuelta completa (360°), se le restan \n" +
                                    "múltiplos de 360° para encontrar su posición base en el plano estándar.\n" +
                                    "* Operación: %.2f° - %.0f° = %.2f°\n",
                            ang, resta, angNorm);
                } else {
                    textoPaso3 = String.format(
                            "Paso 3: Determinación del Ángulo Equivalente Positivo\n" +
                                    "El ángulo de cálculo (%.2f°) ya se encuentra en el rango estándar de \n" +
                                    "0° a 360°, por lo que no requiere ajuste matemático adicional.\n",
                            ang);
                }

                procedimiento = String.format(
                        "--- VECTOR V%d (Conversión: Polares a Rectangulares) ---\n\n" +
                                "Datos ingresados por el usuario:\n" +
                                "* Magnitud ingresada (M) = %.2f\n" +
                                "* Ángulo ingresado (A) = %.2f°\n\n" +
                                "Paso 1: Cálculo de la Coordenada Horizontal (X)\n" +
                                "Se determina multiplicando la magnitud por el coseno del ángulo.\n" +
                                "* Operación: X = %.2f * cos(%.2f°) = %.4f\n\n" +
                                "Paso 2: Cálculo de la Coordenada Vertical (Y)\n" +
                                "Se determina multiplicando la magnitud por el seno del ángulo.\n" +
                                "* Operación: Y = %.2f * sin(%.2f°) = %.4f\n\n" +
                                "%s" +
                                "---------------------------------------------------\n\n",
                        n, magnitudIngresada, anguloIngresado,
                        r, ang, x, r, ang, y, textoPaso3);

                listaVectores.add(new VectorMulti(d1, d2, x, y, r, angNorm, "P"));
            }

            reporteIngresos.append(procedimiento);
            panelGrafico.repaint();
            txtDato1.setText("");
            txtDato2.setText("");
            txtDato1.requestFocus();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Por favor, introduzca valores numéricos válidos.");
        }
    }

    private void realizarSuma() {
        listaVectores.removeIf(v -> "S".equals(v.tipoOriginal));

        if (listaVectores.size() < 2) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos 2 vectores a la pantalla para poder sumarlos.");
            return;
        }

        double sumX = 0;
        double sumY = 0;
        StringBuilder ecuacionX = new StringBuilder();
        StringBuilder ecuacionY = new StringBuilder();

        for (int i = 0; i < listaVectores.size(); i++) {
            VectorMulti v = listaVectores.get(i);
            sumX += v.x;
            sumY += v.y;
            ecuacionX.append(String.format("(%.2f)", v.x));
            ecuacionY.append(String.format("(%.2f)", v.y));
            if (i < listaVectores.size() - 1) {
                ecuacionX.append(" + ");
                ecuacionY.append(" + ");
            }
        }

        double resR = Math.sqrt((sumX * sumX) + (sumY * sumY));
        double anguloBruto;
        if (sumX == 0) {
            anguloBruto = (sumY > 0) ? 90 : (sumY < 0 ? -90 : 0);
        } else {
            anguloBruto = Math.toDegrees(Math.atan(sumY / sumX));
        }

        double resAng = Math.toDegrees(Math.atan2(sumY, sumX));
        if (resAng < 0) {
            resAng += 360;
        }

        String explicacionCuadrante = "";
        if (sumX > 0 && sumY >= 0) {
            explicacionCuadrante = "La resultante se ubica en el Cuadrante I. El ángulo real es equivalente al ángulo de referencia.";
        } else if (sumX < 0) {
            explicacionCuadrante = "La resultante se ubica en el Cuadrante II o III (ΣX es negativa). Se suman 180° al ángulo obtenido.\n"
                    + "* Operación: 180° + (" + String.format("%.2f°", anguloBruto) + ")";
        } else if (sumX > 0 && sumY < 0) {
            explicacionCuadrante = "La resultante se ubica en el Cuadrante IV (ΣX positiva, ΣY negativa). Se suman 360° al ángulo obtenido.\n"
                    + "* Operación: 360° + (" + String.format("%.2f°", anguloBruto) + ")";
        } else {
            explicacionCuadrante = "La resultante se encuentra posicionada directamente sobre uno de los ejes coordenados.";
        }

        StringBuilder analisisAngulos = new StringBuilder();
        analisisAngulos.append("\n--- ANÁLISIS DE ÁNGULOS CON LA RESULTANTE ---\n");
        analisisAngulos.append("Para encontrar el ángulo de separación aplicamos: A = cos⁻¹((V·R) / (|V|·|R|))\n\n");

        for (int i = 0; i < listaVectores.size(); i++) {
            VectorMulti v = listaVectores.get(i);
            double num = (v.x * sumX) + (v.y * sumY);
            double magV = Math.sqrt((v.x * v.x) + (v.y * v.y));
            double magR = resR;
            double denom = magV * magR;
            double anguloSep = Math.toDegrees(Math.acos(num / denom));

            analisisAngulos.append(String.format("Ángulo entre V%d y la Resultante (R):\n", i + 1));
            analisisAngulos.append("Paso A: El Numerador (V·R)\n");
            analisisAngulos.append(String.format("* Operación: (%.2f * %.2f) + (%.2f * %.2f)\n", v.x, sumX, v.y, sumY));
            analisisAngulos.append(String.format("* Resultado Numerador = %.4f\n\n", num));
            analisisAngulos.append("Paso B: El Denominador (|V|·|R|)\n");
            analisisAngulos.append(String.format("* Magnitud V%d = √((%.2f)² + (%.2f)²) = %.2f\n", i + 1, v.x, v.y, magV));
            analisisAngulos.append(String.format("* Magnitud R = √((%.2f)² + (%.2f)²) = %.2f\n", sumX, sumY, magR));
            analisisAngulos.append(String.format("* Resultado Denominador = %.2f * %.2f = %.4f\n\n", magV, magR, denom));
            analisisAngulos.append("Paso C: División y Ángulo Final\n");
            analisisAngulos.append(String.format("* cos(A) = (%.4f) / (%.4f)\n", num, denom));
            if (Double.isNaN(anguloSep)) {
                analisisAngulos.append("* Ángulo de separación = NaN°\n\n");
            } else {
                analisisAngulos.append(String.format("* Ángulo de separación = %.2f°\n\n", anguloSep));
            }
        }

        String texto = String.format(
                "--- SUMA VECTORIAL (CÁLCULO DEL VECTOR RESULTANTE) ---\n\n" +
                        "Se sumaron todos los vectores ingresados utilizando sus componentes rectangulares.\n\n" +
                        "Paso 1: Sumatoria de Componentes Horizontales (ΣX)\n" +
                        "* Operación: ΣX = %s\n" +
                        "* Total X (Resultante) = %.4f\n\n" +
                        "Paso 2: Sumatoria de Componentes Verticales (ΣY)\n" +
                        "* Operación: ΣY = %s\n" +
                        "* Total Y (Resultante) = %.4f\n\n" +
                        "Paso 3: Formación del Vector Resultante\n" +
                        "Se calcula la nueva magnitud por Pitágoras y el ángulo aplicando Tangente Inversa.\n" +
                        "* Magnitud (M) = √((%.2f)² + (%.2f)²) = %.4f\n\n" +
                        "Cálculo del Ángulo de Referencia:\n" +
                        "* Operación base = tan⁻¹(ΣY / ΣX) = tan⁻¹(%.2f / %.2f)\n" +
                        "* Ángulo de referencia obtenido = %.2f°\n\n" +
                        "Determinación del Ángulo en Posición Estándar:\n" +
                        "%s\n" +
                        "* Ángulo resultante final (0° a 360°): %.2f°\n%s" +
                        "---------------------------------------------------\n\n",
                ecuacionX.toString(), sumX, ecuacionY.toString(), sumY,
                sumX, sumY, resR,
                sumY, sumX, anguloBruto,
                explicacionCuadrante, resAng,
                analisisAngulos.toString());

        listaAngulosConResultante.clear();
        for (VectorMulti v : listaVectores) {
            double diffAngulo = Math.abs(resAng - v.angulo);
            if (diffAngulo > 180) {
                diffAngulo = 360 - diffAngulo;
            }
            listaAngulosConResultante.add(diffAngulo);
        }

        reporteSuma.setLength(0);
        reporteSuma.append(texto);
        flagSuma = true;

        listaVectores.add(new VectorMulti(resR, resAng, sumX, sumY, resR, resAng, "S"));
        panelGrafico.repaint();

        mostrarReporteDinamico("SUMA");
    }

    private void calcularProductoEscalar() {
        ArrayList<VectorMulti> vectoresBases = new ArrayList<>();
        for (VectorMulti v : listaVectores) {
            if (!v.tipoOriginal.equals("S"))
                vectoresBases.add(v);
        }

        if (vectoresBases.size() != 2) {
            JOptionPane.showMessageDialog(this,
                    "El Producto Escalar (Producto Punto) se calcula exactamente entre 2 vectores.");
            return;
        }

        VectorMulti v1 = vectoresBases.get(0);
        VectorMulti v2 = vectoresBases.get(1);

        double prodX = v1.x * v2.x;
        double prodY = v1.y * v2.y;
        double productoPunto = prodX + prodY;

        double magU = Math.sqrt((v1.x * v1.x) + (v1.y * v1.y));
        double magV = Math.sqrt((v2.x * v2.x) + (v2.y * v2.y));
        double denom = magU * magV;
        double anguloSep = Math.toDegrees(Math.acos(productoPunto / denom));

        String interpretacion = "";
        if (Math.abs(productoPunto) < 0.0001) {
            interpretacion = "El resultado es CERO (0). Los vectores son PERPENDICULARES (90° entre sí).";
        } else if (productoPunto > 0) {
            interpretacion = "El resultado es POSITIVO. Los vectores apuntan en una dirección general similar (ángulo agudo).";
        } else {
            interpretacion = "El resultado es NEGATIVO. Los vectores apuntan en direcciones generales opuestas (ángulo obtuso).";
        }

        String texto = String.format(
                "--- PRODUCTO ESCALAR Y ÁNGULO ENTRE VECTORES ---\n\n" +
                        "Aplicando la fórmula: A = cos⁻¹((u·v) / (||u||·||v||))\n\n" +
                        "Paso 1: Producto Escalar - El Numerador (u·v)\n" +
                        "Se multiplican las componentes de cada eje y se suman.\n" +
                        "* Eje X: (%.2f) * (%.2f) = %.4f\n" +
                        "* Eje Y: (%.2f) * (%.2f) = %.4f\n" +
                        "* Suma = %.4f + %.4f = %.4f\n\n" +
                        "Paso 2: Magnitudes de los Vectores - El Denominador (||u||·||v||)\n" +
                        "Se calcula la longitud de cada vector por Pitágoras y se multiplican.\n" +
                        "* ||u|| = √((%.2f)² + (%.2f)²) = %.2f\n" +
                        "* ||v|| = √((%.2f)² + (%.2f)²) = %.2f\n" +
                        "* Multiplica = %.2f * %.2f = %.4f\n\n" +
                        "Paso 3: División y Ángulo Final\n" +
                        "* cos(A) = (%.4f) / (%.4f)\n" +
                        "* Ángulo exacto de separación: %.2f°\n\n" +
                        "*** CONCLUSIÓN GEOMÉTRICA ***\n%s\n" +
                        "---------------------------------------------------\n",
                v1.x, v2.x, prodX, v1.y, v2.y, prodY, prodX, prodY, productoPunto,
                v1.x, v1.y, magU, v2.x, v2.y, magV, magU, magV, denom,
                productoPunto, denom, anguloSep, interpretacion);

        lastDotProductResult = productoPunto;
        lastDotProductAngle = anguloSep;

        reporteProducto.setLength(0);
        reporteProducto.append(texto);
        flagProducto = true;

        mostrarReporteDinamico("PRODUCTO");
    }

    private void mostrarReporteDinamico(String focusTarget) {
        StringBuilder reporteFinal = new StringBuilder();
        reporteFinal.append(reporteIngresos.toString());

        int focusIndex = 0;

        if (flagSuma) {
            if (focusTarget.equals("SUMA")) {
                focusIndex = reporteFinal.length(); 
            }
            reporteFinal.append(reporteSuma.toString());
        }

        if (flagProducto) {
            if (focusTarget.equals("PRODUCTO")) {
                focusIndex = reporteFinal.length();
            }
            reporteFinal.append(reporteProducto.toString());
        }

        areaReporte.setText(reporteFinal.toString());

        animarVistas(false, true);

        if (focusIndex > 0) {
            areaReporte.setCaretPosition(focusIndex);
            areaReporte.requestFocus();
        } else {
            areaReporte.setCaretPosition(0);
        }
    }

    private String generateShortReport() {
        StringBuilder shortReport = new StringBuilder();
        shortReport.append("=========================================\n");
        shortReport.append("       REPORTE SIMPLIFICADO DE RESULTADOS\n");
        shortReport.append("=========================================\n\n");

        shortReport.append("--- VECTORES REGISTRADOS ---\n");
        int vectorCount = 1;
        for (VectorMulti v : listaVectores) {
            if (v.tipoOriginal.equals("R") || v.tipoOriginal.equals("P")) {
                shortReport.append(String.format(
                        "V%d -> Componentes: (X: %.2f, Y: %.2f) | Magnitud (M): %.2f | Ángulo (A): %.2f°\n",
                        vectorCount, v.x, v.y, v.r, v.angulo));
                vectorCount++;
            }
        }

        if (flagSuma) {
            shortReport.append("\n--- CÁLCULO DE VECTOR RESULTANTE (SUMA) ---\n");
            for (VectorMulti v : listaVectores) {
                if (v.tipoOriginal.equals("S")) {
                    shortReport.append(String.format(
                            "VECTOR RESULTANTE (VR) -> Componentes: (X: %.2f, Y: %.2f) | Magnitud: %.2f | Ángulo: %.2f°\n",
                            v.x, v.y, v.r, v.angulo));
                }
            }

            shortReport.append("\n--- ÁNGULOS RESPECTO A LA RESULTANTE (VR) ---\n");
            for (int i = 0; i < listaAngulosConResultante.size(); i++) {
                shortReport.append(String.format("Ángulo interno entre V%d y la Resultante (VR) = %.2f°\n",
                        (i + 1), listaAngulosConResultante.get(i)));
            }
        }

        if (flagProducto) {
            shortReport.append("\n--- CÁLCULO DE PRODUCTO ESCALAR --- \n");
            shortReport.append(String.format("Valor del Producto Punto = %.4f\n", lastDotProductResult));
            shortReport.append(String.format("Ángulo de Separación entre V1 y V2 = %.2f°\n", lastDotProductAngle));
        }

        shortReport.append("\n=========================================");
        return shortReport.toString();
    }

    private void guardarPdf() {
        if (areaReporte.getText().trim().isEmpty() || listaVectores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Área Vacía",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte como PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }

        Object[] opcionesMenu = { "Procedimiento Completo", "Solo Resultados" };
        int seleccionUsuario = JOptionPane.showOptionDialog(this,
                "¿Cómo deseas guardar el texto del reporte en el PDF?",
                "Opciones de Exportación PDF",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesMenu,
                opcionesMenu[0]);

        String contenidoTextoPdf;
        if (seleccionUsuario == 1) {
            contenidoTextoPdf = generateShortReport(); 
        } else {
            contenidoTextoPdf = areaReporte.getText(); 
        }

        try {
            int width = panelGrafico.getWidth();
            int height = panelGrafico.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            panelGrafico.paint(g2d);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            Image pdfImage = Image.getInstance(baos.toByteArray());
            pdfImage.scaleToFit(500, 400);
            pdfImage.setAlignment(Element.ALIGN_CENTER);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Paragraph titulo = new Paragraph("Analizador de Vectores - Reporte Ejecutivo\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(pdfImage);
            document.add(new Paragraph("\n\n"));

            Paragraph textoReporte = new Paragraph(contenidoTextoPdf,
                    FontFactory.getFont(FontFactory.COURIER, 11));
            document.add(textoReporte);

            document.close();

            JOptionPane.showMessageDialog(this, "El PDF se guardó exitosamente en:\n" + filePath, "Guardado Exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF:\n" + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.font", MAIN_FONT);
            UIManager.put("Label.font", MAIN_FONT);
            UIManager.put("TextField.font", MAIN_FONT);
            UIManager.put("ComboBox.font", MAIN_FONT);
            UIManager.put("ToolTip.font", MAIN_FONT);
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new programa().setVisible(true));
    }

    // =========================================================
    // COMPONENTES UI/UX PERSONALIZADOS (PURE SWING)
    // =========================================================

    class CardPanel extends JPanel {
        private int radius = 15;

        public CardPanel() {
            setOpaque(false);
            setBackground(CARD_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, radius, radius);
            g2.setColor(new Color(0, 0, 0, 4));
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, radius, radius);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private Color pressedColor;
        private boolean isOutlined = false;
        private boolean hovered = false;
        private boolean pressed = false;
        private int radius = 10;

        public ModernButton(String text, Color baseColor) {
            super(text);
            this.normalColor = baseColor;
            this.hoverColor = brighten(baseColor, 0.15f);
            this.pressedColor = darken(baseColor, 0.1f);

            setFont(BOLD_FONT);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hovered = false; pressed = false; repaint(); }
                @Override
                public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override
                public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        public void setOutlined(boolean outlined) {
            this.isOutlined = outlined;
            setForeground(normalColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentBg = pressed ? pressedColor : (hovered ? hoverColor : normalColor);

            if (isOutlined) {
                if (hovered || pressed) {
                    g2.setColor(new Color(currentBg.getRed(), currentBg.getGreen(), currentBg.getBlue(), 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                }
                g2.setColor(normalColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);
            } else {
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        private Color brighten(Color c, float fraction) {
            int r = Math.min(255, (int)(c.getRed() + (255 - c.getRed()) * fraction));
            int g = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * fraction));
            int b = Math.min(255, (int)(c.getBlue() + (255 - c.getBlue()) * fraction));
            return new Color(r, g, b);
        }

        private Color darken(Color c, float fraction) {
            int r = Math.max(0, (int)(c.getRed() * (1 - fraction)));
            int g = Math.max(0, (int)(c.getGreen() * (1 - fraction)));
            int b = Math.max(0, (int)(c.getBlue() * (1 - fraction)));
            return new Color(r, g, b);
        }
    }

    class ModernTextField extends JTextField {
        private boolean isFocused = false;
        private int radius = 8;

        public ModernTextField(int columns) {
            super(columns);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setFont(MAIN_FONT);
            setForeground(TEXT_COLOR);
            setCaretColor(ACCENT_BLUE);
            
            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) { isFocused = true; repaint(); }
                public void focusLost(java.awt.event.FocusEvent evt) { isFocused = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isFocused ? ACCENT_BLUE : BORDER_COLOR);
            g2.setStroke(new BasicStroke(isFocused ? 2f : 1f));
            g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 3, getHeight() - 3, radius, radius));
            g2.dispose();
        }
    }

    class ModernComboBox extends JComboBox<String> {
        public ModernComboBox(String[] items) {
            super(items);
            setFont(MAIN_FONT);
            setForeground(TEXT_COLOR);
            setBackground(Color.WHITE);
            setOpaque(false);
            
            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton btn = new JButton("▼");
                    btn.setBorder(new EmptyBorder(0, 5, 0, 5));
                    btn.setContentAreaFilled(false);
                    btn.setForeground(TEXT_COLOR);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    return btn;
                }
            });
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }

    class ModernScrollPane extends JScrollPane {
        public ModernScrollPane(Component view) {
            super(view);
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(CARD_COLOR);
            
            getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = new Color(200, 204, 212);
                    this.trackColor = CARD_COLOR;
                }
                @Override
                protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
                @Override
                protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
                private JButton createZeroButton() {
                    JButton jbutton = new JButton();
                    jbutton.setPreferredSize(new Dimension(0, 0));
                    jbutton.setMinimumSize(new Dimension(0, 0));
                    jbutton.setMaximumSize(new Dimension(0, 0));
                    return jbutton;
                }
                @Override
                protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(thumbColor);
                    g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
                    g2.dispose();
                }
            });
            getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        }
    }
}