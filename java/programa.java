import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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

// 3. CLASE PRINCIPAL (Lógica de Interfaz y Reportes - Refactorizada a Dashboard)
public class programa extends JFrame {
    private JTextField txtDato1, txtDato2;
    private JComboBox<String> cmbTipoEntrada;
    private LienzoMulti panelGrafico;
    private ArrayList<VectorMulti> listaVectores;

    // Componentes del nuevo layout Dashboard
    private JSplitPane splitPane;
    private JTextArea areaReporte;

    // Banderas y Constructores Modulares del Reporte
    private StringBuilder reporteIngresos;
    private StringBuilder reporteSuma;
    private StringBuilder reporteProducto;

    private boolean flagSuma = false;
    private boolean flagProducto = false;
    // Nuevas variables para almacenar los últimos resultados del producto escalar
    private double lastDotProductResult = 0;
    private double lastDotProductAngle = 0;
    // Nuevas variables para almacenar los ángulos individuales respecto a la
    // resultante
    // Lista dinámica para guardar los ángulos de cada vector respecto a la
    // resultante
    private ArrayList<Double> listaAngulosConResultante = new ArrayList<>();

    public programa() {
        listaVectores = new ArrayList<>();
        reporteIngresos = new StringBuilder("--- HISTORIAL DE VECTORES AGREGADOS ---\n\n");
        reporteSuma = new StringBuilder();
        reporteProducto = new StringBuilder();

        setTitle("Analizador de Vectores");
        setSize(1200, 750); // Ligeramente más ancho para acomodar el menú lateral
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. ARQUITECTURA PRINCIPAL: BorderLayout
        setLayout(new BorderLayout());

        // =========================================================
        // 2. PANEL SUPERIOR (NORTH) - Ingreso de Datos
        // =========================================================
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelNorte.setBackground(new Color(245, 246, 248));
        panelNorte.setBorder(new EmptyBorder(5, 10, 5, 10));

        cmbTipoEntrada = new JComboBox<>(new String[] { "Rectangulares (X, Y)", "Polares (M, A)" });
        txtDato1 = new JTextField(7);
        txtDato2 = new JTextField(7);
        // Activamos el resaltado de enfoque para la navegación por TAB
        programarResaltadoEnfoque(txtDato1);
        programarResaltadoEnfoque(txtDato2);
        programarResaltadoEnfoque(cmbTipoEntrada);

        JButton btnAgregar = new JButton("Agregar Vector");
        btnAgregar.setBackground(new Color(40, 167, 69)); // Verde #28a745
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAgregar.setOpaque(true);
        btnAgregar.setBorderPainted(false);
        JButton btnLimpiar = new JButton("Limpiar Lienzo");
        btnLimpiar.setBackground(Color.WHITE);
        btnLimpiar.setForeground(new Color(220, 53, 69)); // Rojo #dc3545
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setFont(new Font("SansSerif", Font.BOLD, 12));

        panelNorte.add(new JLabel("Tipo:"));
        panelNorte.add(cmbTipoEntrada);
        panelNorte.add(new JLabel(" D1:"));
        panelNorte.add(txtDato1);
        panelNorte.add(new JLabel(" D2:"));
        panelNorte.add(txtDato2);
        panelNorte.add(btnAgregar);
        panelNorte.add(btnLimpiar);

        add(panelNorte, BorderLayout.NORTH);

        // =========================================================
        // 3. PANEL LATERAL IZQUIERDO (WEST) - Menú Principal
        // =========================================================
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setPreferredSize(new Dimension(220, 0));
        panelMenu.setBackground(new Color(33, 37, 41)); // Gris muy oscuro #212529
        panelMenu.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel lblMenu = new JLabel("MENÚ PRINCIPAL");
        lblMenu.setForeground(Color.WHITE);
        lblMenu.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblMenu.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Estilización y creación de los botones del menú (Flat Design)
        JButton btnSumar = crearBotonMenu("Sumar Vectores", new Color(23, 162, 184)); // Cyan/Celeste
        JButton btnPunto = crearBotonMenu("Producto Escalar", new Color(111, 66, 193)); // Morado
        JButton btnReporte = crearBotonMenu("Reporte Ejecutivo", new Color(52, 58, 64)); // Gris oscuro
        JButton btnGuardar = crearBotonMenu("Guardar PDF", new Color(40, 167, 69)); // Verde para destacar la acción de
                                                                                    // guardar

        panelMenu.add(lblMenu);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 30))); // Espaciador
        panelMenu.add(btnSumar);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMenu.add(btnPunto);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMenu.add(btnReporte);
        panelMenu.add(Box.createRigidArea(new Dimension(0, 10)));
        panelMenu.add(btnGuardar);

        add(panelMenu, BorderLayout.WEST);

        // =========================================================
        // 4. ÁREA CENTRAL (CENTER) - JSplitPane
        // =========================================================
        panelGrafico = new LienzoMulti(listaVectores); // Lienzo superior

        areaReporte = new JTextArea();
        areaReporte.setEditable(false);
        areaReporte.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaReporte.setLineWrap(true);
        areaReporte.setWrapStyleWord(true);
        areaReporte.setBackground(new Color(250, 250, 250));

        JScrollPane scrollReporte = new JScrollPane(areaReporte); // Text area inferior
        scrollReporte.setBorder(BorderFactory.createTitledBorder("Panel de Reportes"));

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelGrafico, scrollReporte);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        // Posicionar el divisor del JSplitPane casi al fondo una vez que la UI cargue
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.95));

        // =========================================================
        // EVENTOS (Listeners)
        // =========================================================
        btnAgregar.addActionListener(e -> procesarDatos());
        btnSumar.addActionListener(e -> realizarSuma());
        btnPunto.addActionListener(e -> calcularProductoEscalar());
        btnReporte.addActionListener(e -> mostrarReporteDinamico("TOP"));
        // btnReporte.addActionListener(e -> mostrarReporteDinamico());
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

            // Colapsar el panel de reportes suavemente
            splitPane.setDividerLocation(0.95);
        });
    }

    // Método auxiliar para estilizar botones laterales flat
    private JButton crearBotonMenu(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =========================================================
    // MÉTODOS ORIGINALES
    // =========================================================

    private void procesarDatos() {
        try {
            double d1 = Double.parseDouble(txtDato1.getText());
            double d2 = Double.parseDouble(txtDato2.getText());
            double x, y, r, ang;
            String procedimiento = "";
            int n = listaVectores.size() + 1;

            int modoSeleccionado = cmbTipoEntrada.getSelectedIndex();

            if (modoSeleccionado == 0) { // RECTANGULARES (X, Y)
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

            } else { // POLARES (M, A)
                double magnitudIngresada = d1;
                double anguloIngresado = d2;

                // NUEVA VALIDACIÓN: Bloquear magnitud negativa
                if (magnitudIngresada < 0) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede ingresar una magnitud negativa.\n\n" +
                                    "Físicamente, la magnitud representa la longitud o distancia del vector, " +
                                    "por lo que siempre debe ser un valor absoluto (positivo o cero).",
                            "Error de Ingreso",
                            JOptionPane.ERROR_MESSAGE);
                    return; // Detiene la ejecución aquí mismo
                }

                // Si pasa la validación, se asignan los valores directamente
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

                // REPORTE LIMPIO: Ya no incluye la variable "explicacionMagnitudNegativa"
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
        // Elimina suma anterior si la hubiese para no acumular en gráfico
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

        // 1. Ángulo de calculadora base (Tangente inversa pura)
        double anguloBruto;
        if (sumX == 0) {
            anguloBruto = (sumY > 0) ? 90 : (sumY < 0 ? -90 : 0);
        } else {
            anguloBruto = Math.toDegrees(Math.atan(sumY / sumX));
        }

        // 2. Ángulo real (0 a 360) usando atan2
        double resAng = Math.toDegrees(Math.atan2(sumY, sumX));
        if (resAng < 0) {
            resAng += 360;
        }

        // 3. Explicación de ajuste según el cuadrante
        String explicacionCuadrante = "";
        if (sumX > 0 && sumY >= 0) {
            explicacionCuadrante = "La resultante se ubica en el Cuadrante I. El ángulo real es equivalente al ángulo de referencia.";
        } else if (sumX < 0) {
            explicacionCuadrante = "La resultante se ubica en el Cuadrante II o III (ΣX es negativa). Se suman 180° al ángulo obtenido.\n"
                    +
                    "* Operación: 180° + (" + String.format("%.2f°", anguloBruto) + ")";
        } else if (sumX > 0 && sumY < 0) {
            explicacionCuadrante = "La resultante se ubica en el Cuadrante IV (ΣX positiva, ΣY negativa). Se suman 360° al ángulo obtenido.\n"
                    +
                    "* Operación: 360° + (" + String.format("%.2f°", anguloBruto) + ")";
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
            analisisAngulos
                    .append(String.format("* Magnitud V%d = √((%.2f)² + (%.2f)²) = %.2f\n", i + 1, v.x, v.y, magV));
            analisisAngulos.append(String.format("* Magnitud R = √((%.2f)² + (%.2f)²) = %.2f\n", sumX, sumY, magR));
            analisisAngulos
                    .append(String.format("* Resultado Denominador = %.2f * %.2f = %.4f\n\n", magV, magR, denom));

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
        // --- HASTA AQUÍ ---

        // === CAPTURA DINÁMICA DE ÁNGULOS CON LA RESULTANTE ===
        listaAngulosConResultante.clear(); // Limpiamos cálculos anteriores

        // Recorremos todos los vectores ingresados hasta el momento
        for (VectorMulti v : listaVectores) {
            // Calculamos la diferencia angular absoluta entre la resultante (resAng) y el
            // vector actual
            double diffAngulo = Math.abs(resAng - v.angulo);

            // Ajuste geométrico para obtener el menor ángulo de separación (máximo 180°)
            if (diffAngulo > 180) {
                diffAngulo = 360 - diffAngulo;
            }

            // Guardamos el ángulo calculado en nuestra lista dinámica
            listaAngulosConResultante.add(diffAngulo);
        }

        reporteSuma.setLength(0);
        reporteSuma.append(texto);
        flagSuma = true;

        // Se agrega la resultante a la lista
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
                        "Paso 1: El Numerador (u·v)\n" +
                        "Se multiplican las componentes de cada eje y se suman.\n" +
                        "* Eje X: (%.2f) * (%.2f) = %.4f\n" +
                        "* Eje Y: (%.2f) * (%.2f) = %.4f\n" +
                        "* Suma (Numerador) = %.4f + %.4f = %.4f\n\n" +
                        "Paso 2: El Denominador (||u||·||v||)\n" +
                        "Se calcula la longitud de cada vector por Pitágoras y se multiplican.\n" +
                        "* ||u|| = √((%.2f)² + (%.2f)²) = %.2f\n" +
                        "* ||v|| = √((%.2f)² + (%.2f)²) = %.2f\n" +
                        "* Producto (Denominador) = %.2f * %.2f = %.4f\n\n" +
                        "Paso 3: División y Ángulo Final\n" +
                        "* cos(A) = (%.4f) / (%.4f)\n" +
                        "* Ángulo exacto de separación: %.2f°\n\n" +
                        "*** CONCLUSIÓN GEOMÉTRICA ***\n%s\n" +
                        "---------------------------------------------------\n",
                v1.x, v2.x, prodX, v1.y, v2.y, prodY, prodX, prodY, productoPunto,
                v1.x, v1.y, magU, v2.x, v2.y, magV, magU, magV, denom,
                productoPunto, denom, anguloSep, interpretacion);

        // Guardamos los resultados numéricos en las variables globales para el PDF
        // simplificado
        lastDotProductResult = productoPunto;
        lastDotProductAngle = anguloSep;

        reporteProducto.setLength(0);
        reporteProducto.append(texto);
        flagProducto = true;

        // Se actualiza el reporte y viaja directamente a la sección del producto
        // escalar
        mostrarReporteDinamico("PRODUCTO");
    }

    private void mostrarReporteDinamico(String focusTarget) {
        StringBuilder reporteFinal = new StringBuilder();

        // 1. Siempre agregamos el historial de ingresos al inicio
        reporteFinal.append(reporteIngresos.toString());

        int focusIndex = 0;

        // 2. Si hay suma, la acoplamos
        if (flagSuma) {
            if (focusTarget.equals("SUMA")) {
                focusIndex = reporteFinal.length(); // Guarda la posición exacta donde inicia la suma
            }
            reporteFinal.append(reporteSuma.toString());
        }

        // 3. Si hay producto escalar, lo acoplamos
        if (flagProducto) {
            if (focusTarget.equals("PRODUCTO")) {
                focusIndex = reporteFinal.length(); // Guarda la posición exacta donde inicia el producto
            }
            reporteFinal.append(reporteProducto.toString());
        }

        // Actualizamos el JTextArea con todo el texto acumulado
        areaReporte.setText(reporteFinal.toString());

        // Aseguramos que el panel se abra lo suficiente para leer el texto
        splitPane.setDividerLocation(0.55);

        // Movemos automáticamente la barra de desplazamiento (Scroll) hacia el cálculo
        // indexado
        if (focusIndex > 0) {
            areaReporte.setCaretPosition(focusIndex);
            areaReporte.requestFocus();
        } else {
            areaReporte.setCaretPosition(0);
        }
    }
    // private void mostrarReporteDinamico() {
    // StringBuilder reporteFinal = new StringBuilder();

    // 1. Siempre muestra ingresos
    // reporteFinal.append(reporteIngresos.toString());

    // 2. Anexa Suma si fue calculada
    // if (flagSuma) {
    // reporteFinal.append(reporteSuma.toString());
    // }

    // 3. Anexa Producto si fue calculado
    // if (flagProducto) {
    // reporteFinal.append(reporteProducto.toString());
    // }

    // Cargar texto en el área inferior del Dashboard
    // areaReporte.setText(reporteFinal.toString());
    // areaReporte.setCaretPosition(0);

    // Animación sencilla para levantar el divisor (ocupa el 60% de la pantalla para
    // gráfica, 40% reporte)
    // splitPane.setDividerLocation(0.6);
    // }
    // Método para generar solo el resumen de respuestas finales
    // Método corregido para generar el resumen con las resultantes y ángulos
    // específicos
    // Método final optimizado para el reporte de respuestas
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

        // 1. Mostrar la resultante de la suma y la relación de todos los ángulos
        if (flagSuma) {
            shortReport.append("\n--- CÁLCULO DE VECTOR RESULTANTE (SUMA) ---\n");
            for (VectorMulti v : listaVectores) {
                if (v.tipoOriginal.equals("S")) {
                    shortReport.append(String.format(
                            "VECTOR RESULTANTE (VR) -> Componentes: (X: %.2f, Y: %.2f) | Módulo: %.2f | Ángulo: %.2f°\n",
                            v.x, v.y, v.r, v.angulo));
                }
            }

            shortReport.append("\n--- ÁNGULOS RESPECTO A LA RESULTANTE (VR) ---\n");
            // Imprimimos dinámicamente el ángulo de cada vector guardado respecto a VR
            for (int i = 0; i < listaAngulosConResultante.size(); i++) {
                shortReport.append(String.format("Ángulo interno entre V%d y la Resultante (VR) = %.2f°\n",
                        (i + 1), listaAngulosConResultante.get(i)));
            }
        }

        // 2. Mostrar el Producto Escalar
        if (flagProducto) {
            shortReport.append("\n--- CÁLCULO DE PRODUCTO ESCALAR --- \n");
            shortReport.append(String.format("Valor del Producto Punto = %.4f\n", lastDotProductResult));
            shortReport.append(String.format("Ángulo de Separación entre V1 y V2 = %.2f°\n", lastDotProductAngle));
        }

        shortReport.append("\n=========================================");
        return shortReport.toString();
    }

    // =========================================================
    // EXPORTACIÓN DE RESULTADOS A PDF
    // =========================================================
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

        // --- VENTANA DE SELECCIÓN DE DISEÑO DE PDF ---
        Object[] opcionesMenu = { "Procedimiento Completo", "Solo Resultados" };
        int seleccionUsuario = JOptionPane.showOptionDialog(this,
                "¿Cómo deseas guardar el texto del reporte en el PDF?",
                "Opciones de Exportación PDF",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesMenu,
                opcionesMenu[0]);

        // Decidimos qué texto inyectar en el documento PDF
        String contenidoTextoPdf;
        if (seleccionUsuario == 1) {
            contenidoTextoPdf = generateShortReport(); // Carga solo las respuestas limpias
        } else {
            contenidoTextoPdf = areaReporte.getText(); // Carga todo el texto detallado del JTextArea
        }

        try {
            // Captura de imagen idéntica a tu código original
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

            // Imprimimos el texto seleccionado por el usuario (Completo o Simplificado)
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

    // Método para resaltar visualmente cualquier componente cuando gana el foco
    // (con TAB o clic)
    private void programarResaltadoEnfoque(JComponent componente) {
        // Guardamos el borde original que tiene el componente por defecto
        javax.swing.border.Border bordeOriginal = componente.getBorder();

        // Creamos un nuevo borde de color azul suave con un grosor de 2 píxeles
        // (No es exagerado, resalta la silueta exterior sin tapar el texto)
        Color colorEnfoque = new Color(0, 120, 215);
        javax.swing.border.Border bordeConEnfoque = BorderFactory.createLineBorder(colorEnfoque, 2);

        // Agregamos el escuchador al componente
        componente.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Cuando el usuario llega con TAB, cambiamos el borde al resaltado
                componente.setBorder(bordeConEnfoque);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Cuando el usuario se va a otro componente, regresa a la normalidad
                componente.setBorder(bordeOriginal);
            }
        });
    }

    public static void main(String[] args) {
        // Establecer un look and feel más moderno si está disponible
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new programa().setVisible(true));
    }
}