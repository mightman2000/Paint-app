import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MyFrame extends JFrame {

    private List<Line2D> vectors = new ArrayList<>();
    private List<Rectangle> squares = new ArrayList<>();
    private List<Polygon> triangles = new ArrayList<>();
    private List<Rectangle> ellipses = new ArrayList<>();
    List<Object> selectedPrimitives = new ArrayList<>();

    List<Object> getIntersectingPrimitives(Rectangle selectionRect) {

        // Check intersection with vectors
        for (Line2D vector : vectors) {
            if (selectionRect.intersectsLine(vector)) {
                selectedPrimitives.add(vector);
            }
        }

        // Check intersection with squares
        for (Rectangle square : squares) {
            if (selectionRect.intersects(square)) {
                selectedPrimitives.add(square);
            }
        }

        // Check intersection with triangles
        for (Polygon triangle : triangles) {
            if (selectionRect.intersects(triangle.getBounds())) {
                selectedPrimitives.add(triangle);
            }
        }

        // Check intersection with ellipses
        for (Rectangle ellipse : ellipses) {
            if (selectionRect.intersects(ellipse)) {
                selectedPrimitives.add(ellipse);
            }
        }

        return selectedPrimitives;
    }

    Color currentColor = Color.ORANGE; // Default drawing color
    int currentThickness = 2; // Default line thickness

    //mouse stuff
    private int startX, startY, endX, endY;
    private int prevX, prevY;
    private boolean isDrawing;
    private boolean lineDrawn = false; // Track if a line is already drawn

    private JButton selectionButton;
    private JToolBar fileJToolBar;
    private JPanel mainPanel;
    private JButton moveButton;
    private JButton primitivesButton;
    private JButton fillColorButton;
    private JButton borderColorButton;
    private JButton groupButton;
    private JButton unGroupButton;
    private JButton eraseButton;
    private JButton copyButton;
    private JButton pasteButton;
    private JPanel drawingPanel;
    private JButton rotateButton;
    private JButton horizontalFlipButton;
    private JButton verticalFlipButton;
    private JButton redrawPrimBTN;
    private JButton colorButton;
    private JButton thicknessButton;

    // Create dropdown button
    private JButton dropDownButton = new JButton("File");

    // Create popup menu and menu items for files
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem newItem = new JMenuItem("New");
    JMenuItem openItem = new JMenuItem("Open");
    JMenuItem saveItem = new JMenuItem("Save");

    // Create popup menu and items for primitives
    JPopupMenu popupPrimitivesMenu = new JPopupMenu();
    JMenuItem squareItem = new JMenuItem("Square");
    JMenuItem triangleItem = new JMenuItem("Triangle");
    JMenuItem ellipseItem = new JMenuItem("Ellipse");
    JMenuItem vectorItem = new JMenuItem("Vector");

    public MyFrame() {
        setContentPane(mainPanel);
        setTitle("Paint");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //selectButton
        selectionButton.addActionListener(new SelectionButtonAction());


        //FILE MENU
        fileJToolBar.add(dropDownButton);

        // Add items to popup menu
        popupMenu.add(newItem);
        popupMenu.add(openItem);
        popupMenu.add(saveItem);

        // Add action listener to dropdown button to show popup menu (FILE)
        dropDownButton.addActionListener(new DropDownFileAction());

        // Add action listeners to menu items
        newItem.addActionListener(new AddNewItemAction());
        openItem.addActionListener(new OpenItemAction());
        saveItem.addActionListener(new SaveItemAction());

        //selection button
        selectionButton.addActionListener(new SelectionButtonAction());

        //rotate button //todo
        rotateButton.addActionListener(new RotateButtonAction());

        //flip horizontal
        horizontalFlipButton.addActionListener(new HorizontalFlipButtonAction());

        //flip vertical
        verticalFlipButton.addActionListener(new VerticalFlipButtonAction());

        //color
        fileJToolBar.add(colorButton);
        colorButton.addActionListener(new ColorButtonAction());

        //thickness
        fileJToolBar.add(thicknessButton);
        thicknessButton.addActionListener(new ThicknessButtonAction());


        // PRIMITIVES MENU
        // Add action listener to dropdown button to show popup menu
        primitivesButton.addActionListener(new AddPrimitiveAction());

        //Add items to popup primitives menu
        popupPrimitivesMenu.add(squareItem);
        popupPrimitivesMenu.add(triangleItem);
        popupPrimitivesMenu.add(ellipseItem);
        popupPrimitivesMenu.add(vectorItem);

        // Add action listeners to menu items
        squareItem.addActionListener(new AddSquareAction());
        triangleItem.addActionListener(new AddTriangleActon());
        ellipseItem.addActionListener(new AddEllipseActon());
        vectorItem.addActionListener(new AddVectorActon());

        //erase button
        eraseButton.addActionListener(new EraseButtonAction());

        //copy button
        copyButton.addActionListener(new CopyButtonAction());

        //erase button
        eraseButton.addActionListener(new EraseButtonAction());

        //drawing panel properties
        drawingPanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        drawingPanel.setBackground(Color.GRAY);

        //TEST TODO
        redrawPrimBTN.addActionListener(new RedrawTestAction());

        //Component listener for redrawing the vectors whenever the app window is moved or minimised.

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                redrawPrimitives(); // Redraw vectors when the frame is resized
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                redrawPrimitives(); // Redraw vectors when the frame is moved
            }

            @Override
            public void componentShown(ComponentEvent e) {
                redrawPrimitives(); // Redraw vectors when the frame is shown
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                //redrawPrimitives();
            }
        });

        setVisible(true);
    }

//inside classes    -----------------------------------------------------------------------------


    class RedrawTestAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            redrawPrimitives();
        }
    }

    class DropDownFileAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            popupMenu.show(dropDownButton, 0, dropDownButton.getHeight());
        }
    }

    class AddNewItemAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            MyFrame frame = new MyFrame();
        }
    }

    class OpenItemAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Choose a file to open
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    // Open the selected file
                    BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                    String line;
                    vectors.clear(); // Clear existing vectors
                    // Read each line (vector) from the file
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts.length == 4) {
                            // Parse coordinates and create a Line2D object
                            double x1 = Double.parseDouble(parts[0]);
                            double y1 = Double.parseDouble(parts[1]);
                            double x2 = Double.parseDouble(parts[2]);
                            double y2 = Double.parseDouble(parts[3]);
                            vectors.add(new Line2D.Double(x1, y1, x2, y2));
                        }
                    }
                    reader.close();
                    redrawPrimitives(); // Redraw vectors on the drawing panel
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    class SaveItemAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            // Choose a file to save the vectors
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    // Open a file writer
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()));
                    // Write each vector to the file
                    for (Line2D vector : vectors) {
                        writer.write(vector.getX1() + "," + vector.getY1() + "," + vector.getX2() + "," + vector.getY2() + "\n");
                    }
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    class SelectionButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            drawingPanel.addMouseListener(new DrawSelectionRectangle());
            drawingPanel.addMouseMotionListener(new DrawSelectionRectangle());
        }
    }

    class RotateButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            Rectangle selectionRect = new Rectangle(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
            rotate90Degrees(selectionRect);
            redrawPrimitives();
        }
    }

    class HorizontalFlipButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the selected primitives
            List<Object> selectedPrimitives = getIntersectingPrimitives(new Rectangle(drawingPanel.getWidth(), drawingPanel.getHeight()));

            // Flip each selected primitive horizontally
            for (Object primitive : selectedPrimitives) {
                if (primitive instanceof Line2D) {
                    Line2D line = (Line2D) primitive;
                    double centerX = (line.getX1() + line.getX2()) / 2;
                    double newX1 = 2 * centerX - line.getX1();
                    double newX2 = 2 * centerX - line.getX2();
                    Line2D flippedLine = new Line2D.Double(newX1, line.getY1(), newX2, line.getY2());
                    int index = vectors.indexOf(line);
                    if (index != -1) {
                        vectors.set(index, flippedLine);
                    }
                } else if (primitive instanceof Rectangle) {
                    Rectangle rect = (Rectangle) primitive;
                    double centerX = rect.getCenterX();
                    Rectangle flippedRect = new Rectangle((int) (2 * centerX - rect.getMaxX()), rect.y, rect.width, rect.height);
                    int index = squares.indexOf(rect);
                    if (index != -1) {
                        squares.set(index, flippedRect);
                    }
                } else if (primitive instanceof Polygon) {
                    Polygon poly = (Polygon) primitive;
                    double centerX = 0;
                    for (int i = 0; i < poly.npoints; i++) {
                        centerX += poly.xpoints[i];
                    }
                    centerX /= poly.npoints;
                    int[] newXPoints = new int[poly.npoints];
                    for (int i = 0; i < poly.npoints; i++) {
                        newXPoints[i] = (int) (2 * centerX - poly.xpoints[i]);
                    }
                    Polygon flippedPoly = new Polygon(newXPoints, poly.ypoints, poly.npoints);
                    int index = triangles.indexOf(poly);
                    if (index != -1) {
                        triangles.set(index, flippedPoly);
                    }
                } else if (primitive instanceof Rectangle) {
                    Rectangle rect = (Rectangle) primitive;
                    double centerX = rect.getCenterX();
                    Rectangle flippedRect = new Rectangle((int) (2 * centerX - rect.getMaxX()), rect.y, rect.width, rect.height);
                    int index = ellipses.indexOf(rect);
                    if (index != -1) {
                        ellipses.set(index, flippedRect);
                    }
                }
            }
            drawingPanel.repaint(); // Repaint the drawing panel
        }
    }

    class VerticalFlipButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the selected primitives
            List<Object> selectedPrimitives = getIntersectingPrimitives(new Rectangle(drawingPanel.getWidth(), drawingPanel.getHeight()));

            // Flip each selected primitive vertically
            for (Object primitive : selectedPrimitives) {
                if (primitive instanceof Line2D) {
                    Line2D line = (Line2D) primitive;
                    double centerY = (line.getY1() + line.getY2()) / 2;
                    double newY1 = 2 * centerY - line.getY1();
                    double newY2 = 2 * centerY - line.getY2();
                    Line2D flippedLine = new Line2D.Double(line.getX1(), newY1, line.getX2(), newY2);
                    int index = vectors.indexOf(line);
                    if (index != -1) {
                        vectors.set(index, flippedLine);
                    }
                } else if (primitive instanceof Rectangle) {
                    Rectangle rect = (Rectangle) primitive;
                    double centerY = rect.getCenterY();
                    Rectangle flippedRect = new Rectangle(rect.x, (int) (2 * centerY - rect.getMaxY()), rect.width, rect.height);
                    int index = squares.indexOf(rect);
                    if (index != -1) {
                        squares.set(index, flippedRect);
                    }
                } else if (primitive instanceof Polygon) {
                    Polygon poly = (Polygon) primitive;
                    double centerY = 0;
                    for (int i = 0; i < poly.npoints; i++) {
                        centerY += poly.ypoints[i];
                    }
                    centerY /= poly.npoints;
                    int[] newYPoints = new int[poly.npoints];
                    for (int i = 0; i < poly.npoints; i++) {
                        newYPoints[i] = (int) (2 * centerY - poly.ypoints[i]);
                    }
                    Polygon flippedPoly = new Polygon(poly.xpoints, newYPoints, poly.npoints);
                    int index = triangles.indexOf(poly);
                    if (index != -1) {
                        triangles.set(index, flippedPoly);
                    }
                } else if (primitive instanceof Rectangle) {
                    Rectangle rect = (Rectangle) primitive;
                    double centerY = rect.getCenterY();
                    Rectangle flippedRect = new Rectangle(rect.x, (int) (2 * centerY - rect.getMaxY()), rect.width, rect.height);
                    int index = ellipses.indexOf(rect);
                    if (index != -1) {
                        ellipses.set(index, flippedRect);
                    }
                }
            }
            drawingPanel.repaint();
        }
    }


    class AddPrimitiveAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            popupPrimitivesMenu.show(primitivesButton, 0, primitivesButton.getHeight());
        }
    }

    class AddSquareAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            drawingPanel.addMouseListener(new DrawingSquareMouseListener());
            drawingPanel.addMouseMotionListener(new DrawingSquareMouseListener());
        }
    }

    class AddTriangleActon implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            drawingPanel.addMouseListener(new DrawingTriangleMouseListener());
            drawingPanel.addMouseMotionListener(new DrawingTriangleMouseListener());
        }
    }

    class AddEllipseActon implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            drawingPanel.addMouseListener(new DrawingEllipseMouseListener());
            drawingPanel.addMouseMotionListener(new DrawingEllipseMouseListener());
        }
    }

    class AddVectorActon implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            drawingPanel.addMouseListener(new DrawingVectorMouseListener());
            drawingPanel.addMouseMotionListener(new DrawingVectorMouseListener());
        }
    }

    class ColorButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Color selectedColor = JColorChooser.showDialog(MyFrame.this, "Select Drawing Color", currentColor);
            if (selectedColor != null) {
                // Update the current color
                currentColor = selectedColor;
            }
        }
    }

    class ThicknessButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] thicknessOptions = {"1", "2", "3", "4", "5"};
            String selectedThickness = (String) JOptionPane.showInputDialog(
                    MyFrame.this,
                    "Select Line Thickness:",
                    "Thickness Selection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    thicknessOptions,
                    Integer.toString(currentThickness));

            if (selectedThickness != null) {
                currentThickness = Integer.parseInt(selectedThickness);
            }
        }
    }

    class EraseButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshMouseListeners();
            eraseSelectedPrimitives(selectedPrimitives);
            redrawPrimitives();
        }
    }

    class CopyButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
    class PasteButtonAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    //MOUSE LISTENERS for drawing

    class DrawSelectionRectangle extends MouseAdapter {
        private int startX, startY, endX, endY;

        @Override
        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            endX = e.getX();
            endY = e.getY();

            // Calculate the coordinates and dimensions of the selection rectangle
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);
            Rectangle selectionRect = new Rectangle(x, y, width, height);

            // Get the Graphics2D object
            Graphics g = drawingPanel.getGraphics();
            Graphics2D g2d = (Graphics2D) g;

            // Set the dashed stroke
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 10}, 0)); // Set dashed stroke with more space
            g2d.setColor(Color.BLACK); // Set the color for the square

            // Draw the dashed rectangle
            g2d.drawRect(x, y, width, height);

            // Detect intersections and add primitives to the list
            List<Object> selectedPrimitives = getIntersectingPrimitives(selectionRect);

            //list of selected primitives
            //print the selected primitives
            System.out.println("Selected Primitives:");
            for (Object primitive : selectedPrimitives) {
                System.out.println(primitive);
            }
            refreshMouseListeners();

        }
    }

    class DrawingVectorMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            lineDrawn = false;
            if (!lineDrawn) { // Only allow drawing if no line is already drawn
                startX = e.getX();
                startY = e.getY();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!lineDrawn) { // Only draw the line if no line is already drawn
                Graphics g = drawingPanel.getGraphics();
                g.setColor(currentColor);
                Graphics2D g2d = (Graphics2D) g;

                // Set the stroke thickness based on the current thickness
                g2d.setStroke(new BasicStroke(currentThickness));
                // Draw the line from the starting point to the mouse release point
                g.drawLine(startX, startY, e.getX(), e.getY());
                vectors.add(new Line2D.Double(startX, startY, e.getX(), e.getY()));
                lineDrawn = true; // Set lineDrawn to true after drawing the line
            }
        }
    }

    class DrawingSquareMouseListener extends MouseAdapter {
        private int startX, startY, endX, endY;

        @Override
        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            endX = e.getX();
            endY = e.getY();

            // Calculate the width and height of the square
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);
            int size = Math.min(width, height);

            // Calculate the coordinates for drawing the square
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);

            // Draw the square
            Graphics g = drawingPanel.getGraphics();
            g.setColor(currentColor);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(currentThickness));
            g.drawRect(x, y, size, size);

            Rectangle square = new Rectangle(x, y, size ,size);

            squares.add(square);
        }
    }

    class DrawingTriangleMouseListener extends MouseAdapter {
        private int startX, startY, endX, endY;

        @Override
        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            endX = e.getX();
            endY = e.getY();

            // Calculate the coordinates for drawing the triangle
            int x1 = startX;
            int y1 = startY;
            int x2 = endX;
            int y2 = endY;
            int x3 = startX + (endX - startX) / 2;
            int y3 = startY;

            // Draw the triangle
            Graphics g = drawingPanel.getGraphics();
            g.setColor(currentColor);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(currentThickness));
            int[] xPoints = {x1, x2, x3};
            int[] yPoints = {y1, y2, y3};
            g.drawPolygon(xPoints, yPoints, 3);

            Polygon triangle = new Polygon(xPoints, yPoints, 3);

            triangles.add(triangle);
        }
    }

    class DrawingEllipseMouseListener extends MouseAdapter {
        private int startX, startY, endX, endY;

        @Override
        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            endX = e.getX();
            endY = e.getY();

            // Calculate the coordinates for drawing the ellipse
            int x = Math.min(startX, endX);
            int y = Math.min(startY, endY);
            int width = Math.abs(endX - startX);
            int height = Math.abs(endY - startY);

            Graphics g = drawingPanel.getGraphics();
            g.setColor(currentColor);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(currentThickness));
            g.drawOval(x, y, width, height);

            // Create the ellipse object (as a Rectangle representing its bounding box)
            Rectangle ellipse = new Rectangle(x, y, width, height);

            // Add the ellipse to the list
            ellipses.add(ellipse);
        }
    }

    //refresh listeners
    private void refreshMouseListeners(){
        MouseListener[] mouseListeners = drawingPanel.getMouseListeners();
        for (MouseListener listener : mouseListeners) {
            drawingPanel.removeMouseListener(listener);
        }

        MouseMotionListener[] mouseMotionListeners = drawingPanel.getMouseMotionListeners();
        for (MouseMotionListener listener : mouseMotionListeners) {
            drawingPanel.removeMouseMotionListener(listener);
        }
    }

    public void redrawPrimitives() {
        Graphics g = drawingPanel.getGraphics();
        g.setColor(currentColor);
        for (Line2D vector : vectors) {
            g.drawLine((int)vector.getX1(), (int)vector.getY1(), (int)vector.getX2(), (int)vector.getY2());
        }
        for (Rectangle square : squares) {
            g.drawRect(square.x, square.y, square.width, square.height);
        }
        for (Polygon triangle : triangles) {
            g.drawPolygon(triangle);
        }
        for (Rectangle ellipse : ellipses) {
            g.drawOval(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
        }
        //drawingPanel.revalidate();

    }

    private void rotateSelectedPrimitives(List<Object> selectedPrimitives) {
        for (Object primitive : selectedPrimitives) {
            if (primitive instanceof Line2D) {
                Line2D line = (Line2D) primitive;
                double centerX = (line.getX1() + line.getX2()) / 2;
                double centerY = (line.getY1() + line.getY2()) / 2;

                double newX1 = centerX - (line.getY1() - centerY);
                double newY1 = centerY + (line.getX1() - centerX);
                double newX2 = centerX - (line.getY2() - centerY);
                double newY2 = centerY + (line.getX2() - centerX);

                Line2D rotatedLine = new Line2D.Double(newX1, newY1, newX2, newY2);
                int index = vectors.indexOf(line);
                if (index != -1) {
                    vectors.set(index, rotatedLine);
                }
            } else if (primitive instanceof Rectangle) {
                Rectangle rect = (Rectangle) primitive;
                int newWidth = rect.height;
                int newHeight = rect.width;
                int newX = rect.x + (rect.width - rect.height) / 2;
                int newY = rect.y + (rect.height - rect.width) / 2;

                Rectangle rotatedRect = new Rectangle(newX, newY, newWidth, newHeight);
                int index = squares.indexOf(rect);
                if (index != -1) {
                    squares.set(index, rotatedRect);
                }
            } else if (primitive instanceof Polygon) {
                Polygon poly = (Polygon) primitive;
                int[] newXPts = new int[poly.npoints];
                int[] newYPts = new int[poly.npoints];
                int centerX = 0, centerY = 0;

                for (int i = 0; i < poly.npoints; i++) {
                    centerX += poly.xpoints[i];
                    centerY += poly.ypoints[i];
                }

                centerX /= poly.npoints;
                centerY /= poly.npoints;

                for (int i = 0; i < poly.npoints; i++) {
                    newXPts[i] = centerX - (poly.ypoints[i] - centerY);
                    newYPts[i] = centerY + (poly.xpoints[i] - centerX);
                }

                Polygon rotatedPoly = new Polygon(newXPts, newYPts, poly.npoints);
                int index = triangles.indexOf(poly);
                if (index != -1) {
                    triangles.set(index, rotatedPoly);
                }
            } else if (primitive instanceof Rectangle) {
                // rotation for ellipses
                Rectangle ellipse = (Rectangle) primitive;
                int newWidth = ellipse.height;
                int newHeight = ellipse.width;
                int newX = ellipse.x + (ellipse.width - ellipse.height) / 2;
                int newY = ellipse.y + (ellipse.height - ellipse.width) / 2;

                Rectangle rotatedEllipse = new Rectangle(newX, newY, newWidth, newHeight);
                int index = ellipses.indexOf(ellipse);
                if (index != -1) {
                    ellipses.set(index, rotatedEllipse);
                }
            }
        }
    }

    private void rotate90Degrees(Rectangle selectionRect) {
        List<Object> selectedPrimitives = getIntersectingPrimitives(selectionRect);
        rotateSelectedPrimitives(selectedPrimitives);
        drawingPanel.repaint();
    }

    void eraseSelectedPrimitives(List selectedPrimitives) {
        for (Object primitive : selectedPrimitives) {
            if (primitive instanceof Line2D) {
                vectors.remove(primitive);
            } else if (primitive instanceof Rectangle) {
                squares.remove(primitive);
                ellipses.remove(primitive); // ellipse?
            } else if (primitive instanceof Polygon) {
                triangles.remove(primitive);
            }
        }
        selectedPrimitives.clear();
        drawingPanel.repaint();
    }
}