package gcode.previewer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import gcode.GcodeFileReader;
import gcode.Line;
import gcode.Point;

import java.util.ArrayList;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

public class SimpleGLCanvas implements GLEventListener {

    public static final Point DEFAULT_ROTATION = new Point(-90, 0, 0);
    public static final Point DEFAULT_EYE = new Point(-90, 0, 0);


    GcodeFileReader gcodeFileReader;
    ArrayList<Line> lines;
    private Point rotation = DEFAULT_EYE;

    public void setEye(Point eye) {
        this.eye = eye;
    }

    private Point eye = DEFAULT_ROTATION;
    float aspect = 1.0F;

    private GLU glu;
    private int xSize, ySize;
    boolean ortho = true;
    private double scaleFactor = 1.0f;
    private double zoomFactor = 1.0f;

    private int oneLayer = 0;
    private int [] rangeOfLayers = null;
    private boolean idleMoves = false;

    public Point getEye() {
        return new Point(eye.getX(), eye.getY(), eye.getZ());
    }

    public enum DRAW_MODE {
        ALL,
        ONE_LAYER,
        RANGE_OF_LAYERS
    }
    public void setDrawModeOneLayer(int layer) {
        this.drawMode = DRAW_MODE.ONE_LAYER;
        this.oneLayer = layer;
    }
    public void setIdleMoves(boolean idleMoves) {
        this.idleMoves = idleMoves;
    }

    public void setDrawModeRangeOfLayers(int [] layers) {
        this.drawMode = DRAW_MODE.RANGE_OF_LAYERS;
        this.rangeOfLayers = layers;
    }
    public void setDrawModeAll() {
        this.drawMode = DRAW_MODE.ALL;
    }


    private DRAW_MODE drawMode = DRAW_MODE.ALL;
    public SimpleGLCanvas(GcodeFileReader gcodeFileReader) {
        try {
            this.gcodeFileReader = gcodeFileReader;
            gcodeFileReader.parseAllLines();
            lines = gcodeFileReader.getLines();

            double x = Math.abs(gcodeFileReader.getMin().getX()) + Math.abs(gcodeFileReader.getMax().getX());
            double y = Math.abs(gcodeFileReader.getMin().getY()) + Math.abs(gcodeFileReader.getMax().getY());
            double z = Math.abs(gcodeFileReader.getMin().getZ()) + Math.abs(gcodeFileReader.getMax().getZ());
            scaleFactor =  Math.max(x, Math.max(y, z));

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

        if (ortho) {
            // Manual rotation
            gl.glRotated(this.rotation.getX(), 0.0, 1.0, 0.0);
            gl.glRotated(this.rotation.getY(), 1.0, 0.0, 0.0);
            gl.glTranslated(-eye.getX() - gcodeFileReader.getCenter().getX(), -eye.getY() - gcodeFileReader.getCenter().getY(), -eye.getZ() - gcodeFileReader.getCenter().getZ());
        }

        // Clear color and depth buffers
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Start drawing lines
        gl.glBegin(GL2.GL_LINES);
        if(drawMode == DRAW_MODE.ALL) {
            drawLines(gl, 0, lines.size());
        } else if (drawMode == DRAW_MODE.ONE_LAYER) {
            int layerStart = gcodeFileReader.getLayerIndex(oneLayer);
            int layerEnd = gcodeFileReader.getLayerIndex(oneLayer + 1);
            drawLines(gl, layerStart, layerEnd);
        } else if (drawMode == DRAW_MODE.RANGE_OF_LAYERS) {
            int layerStart = gcodeFileReader.getLayerIndex(rangeOfLayers[0]);
            int layerEnd = gcodeFileReader.getLayerIndex(rangeOfLayers[1]);
            drawLines(gl, layerStart, layerEnd);

        }
        gl.glEnd();  // End drawing
    }

    private void drawLines(GL2 gl, int layerStart, int layerEnd) {
        for (int i = layerStart; i < layerEnd; i++) {
            Line ls = lines.get(i);
            if (!ls.checkIfTheSameZ() || (!idleMoves && ls.isIdleMove()))
                continue;
            if(ls.isIdleMove())
                gl.glColor3f(0.0f, 1.0f, 0.0f);  // Set color (green)
            else
                gl.glColor3f(1.0f, 1.0f, 1.0f);  // Set color (white)
            gl.glVertex3f(ls.getStart().getX(), ls.getStart().getY(), ls.getStart().getZ());
            gl.glVertex3f(ls.getEnd().getX(), ls.getEnd().getY(), ls.getEnd().getZ());
        }
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


        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

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
        }
    }

    public void zoom(int z) {
        if (ortho) {

            this.zoomFactor += z * 0.2;
            System.out.println("Zoom factor: " + this.zoomFactor);
            if (this.zoomFactor < 1) {
                this.zoomFactor = 1;
            }

            if (this.zoomFactor > 30) {
                this.zoomFactor = 30;
            }

            scaleFactor = findScaleFactor(this.xSize, this.ySize, gcodeFileReader.getMin(), gcodeFileReader.getMax(), 0.9) * this.zoomFactor;

        } else {
            //this.eye.z += increments;
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
    public void setRotation(Point rotation) {
        this.rotation = rotation;
    }



}
