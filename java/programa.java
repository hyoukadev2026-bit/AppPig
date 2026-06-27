import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// 1. CLASE ESTRUCTURAL DE DATOS
class VectorMulti {
    double d1, d2, x, y, r, angulo;
    String tipoOriginal;

    public VectorMulti(double d1, double d2, double x, double y, double r, double angulo, String tipoOriginal) {
        this.d1 = d1;
        this.d2 = d2;
        this.x = x;
        this.y = y;
        this.r = r;
        this.angulo = angulo;
        this.tipoOriginal = tipoOriginal;
    }
}

// 3. CLASE PRINCIPAL (Lógica de Interfaz y Reportes)
public class programa extends JFrame {
    private JTextField txtDato1, txtDato2;
    private JComboBox<String> cmbTipoEntrada;
    private LienzoMulti panelGrafico;
    private ArrayList<VectorMulti> listaVectores;

    // Banderas y Constructores Modulares del Reporte
    private StringBuilder reporteIngresos;
    private StringBuilder reporteSuma;
    private StringBuilder reporteProducto;

    private boolean flagSuma = false;
    private boolean flagProducto = false;

    public programa() {
        listaVectores = new ArrayList<>();
        reporteIngresos = new StringBuilder("--- HISTORIAL DE VECTORES AGREGADOS ---\n\n");
        reporteSuma = new StringBuilder();
        reporteProducto = new StringBuilder();

        setTitle("Calculador y Analizador de Vectores");
        setSize(1050, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelControl = new JPanel();
        panelControl.setBackground(new Color(245, 246, 248));
        panelControl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cmbTipoEntrada = new JComboBox<>(new String[] { "Rectangulares (X, Y)", "Polares (M, A)" });
        txtDato1 = new JTextField(5);
        txtDato2 = new JTextField(5);

        JButton btnAgregar = new JButton("Ingreso / Gráfica");
        btnAgregar.setBackground(new Color(40, 167, 69));
        btnAgregar.setForeground(Color.WHITE);

        JButton btnSumar = new JButton("Sumar Vectores");
        btnSumar.setBackground(new Color(23, 162, 184));
        btnSumar.setForeground(Color.WHITE);

        JButton btnPunto = new JButton("Producto Escalar");
        btnPunto.setBackground(new Color(111, 66, 193));
        btnPunto.setForeground(Color.WHITE);

        JButton btnReporte = new JButton("Reporte Ejecutivo");
        btnReporte.setBackground(new Color(0, 123, 255));
        btnReporte.setForeground(Color.WHITE);

        JButton btnLimpiar = new JButton("Limpiar Lienzo");
        btnLimpiar.setBackground(Color.WHITE);
        btnLimpiar.setForeground(Color.RED);

        panelControl.add(new JLabel("Tipo:"));
        panelControl.add(cmbTipoEntrada);
        panelControl.add(new JLabel(" D1:"));
        panelControl.add(txtDato1);
        panelControl.add(new JLabel(" D2:"));
        panelControl.add(txtDato2);

        panelControl.add(btnAgregar);
        panelControl.add(btnSumar);
        panelControl.add(btnPunto);
        panelControl.add(btnReporte);
        panelControl.add(btnLimpiar);

        add(panelControl, BorderLayout.NORTH);
        panelGrafico = new LienzoMulti(listaVectores);
        add(panelGrafico, BorderLayout.CENTER);

        // Eventos
        btnAgregar.addActionListener(e -> procesarDatos());
        btnSumar.addActionListener(e -> realizarSuma());
        btnPunto.addActionListener(e -> calcularProductoEscalar());
        btnReporte.addActionListener(e -> mostrarReporteDinamico());

        btnLimpiar.addActionListener(e -> {
            listaVectores.clear();

            // Reiniciar banderas y constructores
            reporteIngresos = new StringBuilder("--- HISTORIAL DE VECTORES AGREGADOS ---\n\n");
            reporteSuma.setLength(0);
            reporteProducto.setLength(0);
            flagSuma = false;
            flagProducto = false;

            panelGrafico.repaint();
        });
    }

    private void procesarDatos() {
        try {
            double d1 = Double.parseDouble(txtDato1.getText());
            double d2 = Double.parseDouble(txtDato2.getText());
            double x, y, r, ang;
            String procedimiento = "";
            int n = listaVectores.size() + 1;

            int modoSeleccionado = cmbTipoEntrada.getSelectedIndex();
            // panelGrafico.setModoVisualizacion(modoSeleccionado);// esta linea no es
            // necesaria

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
                String explicacionMagnitudNegativa = "";

                if (magnitudIngresada < 0) {
                    r = Math.abs(magnitudIngresada);
                    ang = anguloIngresado + 180;

                    explicacionMagnitudNegativa = String.format(
                            "Paso 0: Corrección de Magnitud Negativa\n" +
                                    "Físicamente una magnitud (distancia) no puede ser negativa. El signo negativo \n" +
                                    "indica que el vector apunta en la dirección exactamente opuesta. Por lo tanto, \n"
                                    +
                                    "la magnitud se vuelve positiva y se le suman 180° al ángulo.\n" +
                                    "* Nueva Magnitud (M) = %.2f\n" +
                                    "* Nuevo Ángulo (A) = %.2f° + 180° = %.2f°\n\n",
                            r, anguloIngresado, ang);
                } else {
                    r = magnitudIngresada;
                    ang = anguloIngresado;
                }

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
                                "%s" +
                                "Paso 1: Cálculo de la Coordenada Horizontal (X)\n" +
                                "Se determina multiplicando la magnitud corregida por el coseno del ángulo.\n" +
                                "* Operación: X = %.2f * cos(%.2f°) = %.4f\n\n" +
                                "Paso 2: Cálculo de la Coordenada Vertical (Y)\n" +
                                "Se determina multiplicando la magnitud corregida por el seno del ángulo.\n" +
                                "* Operación: Y = %.2f * sin(%.2f°) = %.4f\n\n" +
                                "%s" +
                                "---------------------------------------------------\n\n",
                        n, magnitudIngresada, anguloIngresado, explicacionMagnitudNegativa,
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
        double resAng = Math.toDegrees(Math.atan2(sumY, sumX));
        if (resAng < 0)
            resAng += 360;

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
            analisisAngulos.append("Paso A: El Numerador (Producto Punto V·R)\n");
            analisisAngulos.append(String.format("* Operación: (%.2f * %.2f) + (%.2f * %.2f)\n", v.x, sumX, v.y, sumY));
            analisisAngulos.append(String.format("* Resultado Numerador = %.4f\n\n", num));

            analisisAngulos.append("Paso B: El Denominador (Producto de Magnitudes |V|·|R|)\n");
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
                        "* Magnitud (M) = √((%.2f)² + (%.2f)²) = %.4f\n" +
                        "* Dirección (A) = tan⁻¹(ΣY / ΣX) = tan⁻¹(%.2f / %.2f) = %.2f°\n%s" +
                        "---------------------------------------------------\n\n",
                ecuacionX.toString(), sumX, ecuacionY.toString(), sumY, sumX, sumY, resR, sumY, sumX, resAng,
                analisisAngulos.toString());

        // Actualizar Bandera y Almacenar
        reporteSuma.setLength(0); // Limpia cálculos previos
        reporteSuma.append(texto);
        flagSuma = true;

        listaVectores.add(new VectorMulti(resR, resAng, sumX, sumY, resR, resAng, "S"));
        panelGrafico.repaint();
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
                        "Paso 1: El Numerador (Producto Punto u·v)\n" +
                        "Se multiplican las componentes de cada eje y se suman.\n" +
                        "* Eje X: (%.2f) * (%.2f) = %.4f\n" +
                        "* Eje Y: (%.2f) * (%.2f) = %.4f\n" +
                        "* Suma (Numerador) = %.4f + %.4f = %.4f\n\n" +
                        "Paso 2: El Denominador (Producto de Magnitudes ||u||·||v||)\n" +
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

        // Actualizar Bandera y Almacenar
        reporteProducto.setLength(0);
        reporteProducto.append(texto);
        flagProducto = true;

        // Mensaje breve indicando al usuario que abra el reporte
        JOptionPane.showMessageDialog(this,
                "Producto escalar calculado.\nPor favor, abra el 'Reporte Ejecutivo' para ver los detalles.",
                "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método de Compilación Modular
    private void mostrarReporteDinamico() {
        StringBuilder reporteFinal = new StringBuilder();

        // 1. Siempre muestra ingresos
        reporteFinal.append(reporteIngresos.toString());

        // 2. Anexa Suma si fue calculada
        if (flagSuma) {
            reporteFinal.append(reporteSuma.toString());
        }

        // 3. Anexa Producto si fue calculado
        if (flagProducto) {
            reporteFinal.append(reporteProducto.toString());
        }

        mostrarVentanaIndependiente("Reporte Ejecutivo Consolidado", reporteFinal.toString());
    }

    private void mostrarVentanaIndependiente(String titulo, String contenido) {
        JTextArea area = new JTextArea(contenido);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JDialog win = new JDialog(this, titulo, false);
        win.setSize(750, 500);
        win.add(scroll);
        win.setLocationRelativeTo(this);
        win.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new programa().setVisible(true));
    }
}