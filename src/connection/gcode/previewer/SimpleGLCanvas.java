package connection.gcode.previewer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import connection.gcode.GcodeFileReader;
import connection.gcode.Line;
import connection.gcode.Point;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

public class SimpleGLCanvas implements GLEventListener {
    GcodeFileReader gcodeFileReader;
    ArrayList<Line> lines;
    private Point rotation = new Point(0, 0, 0);
    private Point eye = new Point(0, 0, 10);
    float aspect = 1.0F;

    private GLU glu;
    private int xSize, ySize;
    boolean ortho = true;
    private double scaleFactor = 1.0f;

    public SimpleGLCanvas() {
        try {
            gcodeFileReader = new GcodeFileReader("C:\\Users\\Szymon\\Desktop\\3DBenchy.gcode");
            gcodeFileReader.parseAllLines();
            lines = gcodeFileReader.getLines();

            double x = Math.abs(gcodeFileReader.getMin().getX()) + Math.abs(gcodeFileReader.getMax().getX());
            double y = Math.abs(gcodeFileReader.getMin().getY()) + Math.abs(gcodeFileReader.getMax().getY());
            double z = Math.abs(gcodeFileReader.getMin().getZ()) + Math.abs(gcodeFileReader.getMax().getZ());
            scaleFactor =  Math.max(x, Math.max(y, z));

//            System.out.println(lines);

            //save to file
            try {
                File file = new File("C:\\Users\\Szymon\\Desktop\\3DBenchy.out");
                FileWriter fileWriter = new FileWriter(file);
                for (Line line : lines) {
                    fileWriter.write(line.getStart().getX() + " " + line.getStart().getY() + " " + line.getStart().getZ() + " " + line.getEnd().getX() + " " + line.getEnd().getY() + " " + line.getEnd().getZ() + "\n");
                }
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void init(GLAutoDrawable drawable) {
        // Inicjalizacja kontekstu OpenGL
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Get GL2 context

        setupPerpective(this.xSize, this.ySize, drawable, ortho);
        GL2 gl = drawable.getGL().getGL2();

        gl.glScaled(this.scaleFactor, this.scaleFactor, this.scaleFactor);

        // Rotate prior to translating so that rotation happens from middle of
        // object.
        if (ortho) {
            // Manual rotation
            gl.glRotated(this.rotation.getX(), 0.0, 1.0, 0.0);
            gl.glRotated(this.rotation.getY(), 1.0, 0.0, 0.0);
            gl.glTranslated(-eye.getX() - gcodeFileReader.getCenter().getX(), -eye.getY() - gcodeFileReader.getCenter().getY(), -eye.getZ() - gcodeFileReader.getCenter().getZ());
        } else {
            // Shift model to center of window.
            gl.glTranslated(-gcodeFileReader.getCenter().getX(), -gcodeFileReader.getCenter().getY(), 0);
        }

        // Clear color and depth buffers
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Start drawing lines
        gl.glBegin(GL2.GL_LINES);
        gl.glLineWidth(1.0f);

        for (Line ls : lines) {
            gl.glColor3f(1.0f, 1.0f, 1.0f);  // Set color (white)
            gl.glVertex3f(ls.getStart().getX(), ls.getStart().getY(), ls.getStart().getZ());
            gl.glVertex3f(ls.getEnd().getX(), ls.getEnd().getY(), ls.getEnd().getZ());
        }

        gl.glEnd();  // End drawing
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Get GL2 context
        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) height = 1;  // Prevent divide by zero
        this.xSize = width;
        this.ySize = height;
        aspect = (float) xSize / ySize;

        scaleFactor = findScaleFactor(this.xSize, this.ySize,gcodeFileReader.getMin(), gcodeFileReader.getMax(), 0.9);

        // Set viewport to new window size
        gl.glViewport(0, 0, width, height);

        // Switch to Projection matrix to set up orthographic view
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();  // Load identity matrix

        // Set up orthographic projection (scaling with aspect ratio)
//        double orthoSize = 1.0;
//        gl.glOrtho(-orthoSize * aspect, orthoSize * aspect, -orthoSize, orthoSize, -10.0, 20.0);

        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Czyszczenie zasobów (jeśli potrzebne)
    }

    private void setupPerpective(int x, int y, GLAutoDrawable drawable, boolean ortho) {
        final GL2 gl = drawable.getGL().getGL2();

        if (ortho) {
            gl.glDisable(GL_DEPTH_TEST);
            //gl.glDisable(GL_LIGHTING);
            gl.glMatrixMode(GL_PROJECTION);
            gl.glLoadIdentity();
            // Object's longest dimension is 1, make window slightly larger.
            gl.glOrtho(-0.51*this.aspect,0.51*this.aspect,-0.51,0.51,-10,10);
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity();
        } else {
            gl.glEnable(GL.GL_DEPTH_TEST);

            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
            gl.glLoadIdentity();             // reset projection matrix

            glu.gluPerspective(45.0, this.aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
            // Move camera out and point it at the origin
            glu.gluLookAt(this.eye.getX(),  this.eye.getY(),  this.eye.getZ(),
                    0, 0, 0,
                    0, 1, 0);

            // Enable the model-view transform
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity(); // reset

        }
    }

    public static double findScaleFactor(double x, double y, Point min, Point max, double bufferFactor) {

        if (y == 0 || x == 0 || min == null || max == null) {
            return 1;
        }
        double xObj = Math.abs(min.getX()) + Math.abs(max.getY());
        double yObj = Math.abs(min.getY()) + Math.abs(max.getY());
        double windowRatio = x / y;
        double objRatio = xObj / yObj;
        if (windowRatio < objRatio) {
            return (1.0 / xObj) * windowRatio * bufferFactor;
        } else {
            return (1.0 / yObj) * bufferFactor;
        }
    }

    public Point getRotation() {
        return rotation;
    }



}
