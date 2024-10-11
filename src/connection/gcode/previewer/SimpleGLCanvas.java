package connection.gcode.previewer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;


import javax.swing.JFrame;
import java.awt.*;

public class SimpleGLCanvas implements GLEventListener {

    @Override
    public void init(GLAutoDrawable drawable) {
        // Inicjalizacja kontekstu OpenGL
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Ustawienie czarnego tła
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Rysowanie na ekranie
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);  // Czyszczenie buforów
        gl.glBegin(GL2.GL_TRIANGLES);  // Rysowanie trójkąta
        gl.glColor3f(1.0f, 0.0f, 0.0f);  // Ustawienie koloru (czerwony)
        gl.glVertex2f(-0.5f, -0.5f);  // Wierzchołek 1
        gl.glColor3f(0.0f, 1.0f, 0.0f);  // Ustawienie koloru (zielony)
        gl.glVertex2f(0.5f, -0.5f);   // Wierzchołek 2
        gl.glColor3f(0.0f, 0.0f, 1.0f);  // Ustawienie koloru (niebieski)
        gl.glVertex2f(0.0f, 0.5f);    // Wierzchołek 3
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Obsługa zmiany rozmiaru okna
        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) height = 1;  // Zapobieganie dzieleniu przez zero
        float aspect = (float) width / height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-1.0 * aspect, 1.0 * aspect, -1.0, 1.0, -1.0, 1.0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Czyszczenie zasobów (jeśli potrzebne)
    }

    public static void main(String[] args) {
        // Konfiguracja profilu OpenGL
        GLProfile profile = GLProfile.getDefault();
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setDepthBits(0); // or 24
        capabilities.setAccumRedBits(0);
        capabilities.setAccumGreenBits(0);
        capabilities.setAccumBlueBits(0);
        capabilities.setAccumAlphaBits(0);
        System.out.println("Version: " + capabilities);

        // Tworzenie obiektu GLCanvas
        GLCanvas glCanvas = new GLCanvas(capabilities);
        SimpleGLCanvas renderer = new SimpleGLCanvas();
        glCanvas.addGLEventListener(renderer);

        // Tworzenie okna aplikacji
//        JFrame frame = new JFrame("Simple JOGL Example");
        final Frame frame = new Frame();
        frame.setSize( 500, 500 );
        frame.add( glCanvas );

        frame.setVisible( true );

//        frame.getContentPane().add(glCanvas);
//        frame.setSize(400, 400);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setVisible(true);
//
//        // Animacja o stałej liczbie klatek
//        FPSAnimator animator = new FPSAnimator(glCanvas, 60);
//        animator.start();  // Start animacji
    }


}
