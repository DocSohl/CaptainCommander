
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.JApplet;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.fixedfunc.GLLightingFunc;


public class MainApp extends JApplet implements GLEventListener, KeyListener
{
    double sqr( double x) { return x*x;}
    public class Camera {
        private double ex, ey, ez, cx, cy, cz, ux, uy, uz;
        private double ctheta = 0.0087266463;
        private double roll = 0, pitch = 0, yaw = 0;
        private double X=0,Y=1,Z=0;
//        private Double [][] rotationmatrix = {{1.0,0.0,0.0},{0.0,1.0,0.0},{0.0,0.0,1.0}};
            
        Camera(double ex, double ey, double ez, double cx, double cy, double cz, double ux, double uy, double uz) {
	    this.ex = ex;
	    this.ey = ey;
	    this.ez = ez;
	    this.cx = cx;
	    this.cy = cy;
	    this.cz = cz;
	    this.ux = ux;
	    this.uy = uy; 
	    this.uz = uz;
        }
            
        void setLookAt(GLU glu) {
	    glu.gluLookAt(ex, ey, ez, cx, cy, cz, ux, uy, uz);
        }
            
        void forward() {
	    double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	    double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	    dx /= m*1000.0;
	    dy /= m*1000.0;
	    dz /= m*1000.0;
                
	    cx += dx;
	    cy += dy;
	    cz += dz;
	    ex += dx;
	    ey += dy;
	    ez += dz;
        }
            
        void back() {
	    double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	    double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	    dx /= m*1000.0;
	    dy /= m*1000.0;
	    dz /= m*1000.0;
                
	    cx -= dx;
	    cy -= dy;
	    cz -= dz;
	    ex -= dx;
	    ey -= dy;
	    ez -= dz;
        }
            
        void left() {
	    double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	    double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	    dx /= m;
	    dy /= m;
	    dz /= m;
	    double nx = Math.cos(ctheta)*dx + Math.sin(ctheta)*dz;
	    double ny = dy;
	    double nz = -Math.sin(ctheta)*dx + Math.cos(ctheta)*dz;
	    cx = ex+nx;
	    cy = ey+ny;
	    cz = ez+nz;
        }

        void right() {
	    double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	    double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	    dx /= m;
	    dy /= m;
	    dz /= m;
	    double nx = Math.cos(ctheta)*dx - Math.sin(ctheta)*dz;
	    double ny = dy;
	    double nz = Math.sin(ctheta)*dx + Math.cos(ctheta)*dz;
	    cx = ex+nx;
	    cy = ey+ny;
	    cz = ez+nz;
        }
        
        void pitch(int pixdiff){
        	//Map the mousemovement to an angle via an arbitrary constant (General: edge of screen = pi/2)
        	double changeangle = pixdiff/(winWidth*0.5)*Math.PI*0.5;
        	pitch+=changeangle;
        	reevaluate();
        }
        
        void roll(int pixdiff){
        	double changeangle = pixdiff/(winWidth*0.5)*Math.PI*0.5;
        	roll+=changeangle;
        	reevaluate();
        }
        
        void reevaluate(){
        	//Please kill me...
        	Double [][] rotationMatrix = {
        			{(1-Math.cos(theta))*X*X+Math.cos(theta),(1-Math.cos(theta))*X*Y+Math.sin(theta)*Z,(1-Math.cos(theta))*X*Z-Math.sin(theta)*Y},
        			{(1-Math.cos(theta))*X*Y-Math.sin(theta)*Z,(1-Math.cos(theta))*Y*Y+Math.cos(theta),(1-Math.cos(theta))*Y*Z+Math.sin(theta)*X},
        			{(1-Math.cos(theta))*X*Z+Math.sin(theta)*Y,(1-Math.cos(theta))*Y*Z-Math.sin(theta)*X,(1-Math.cos(theta))*Z*Z+Math.cos(theta)}};
        }
    }
    
    int winWidth=800, winHeight=600;
    FPSAnimator animator;
    GLU glu;
    GLUT glut;
    double sx, sy, sz=1;
    double theta;
    long lastTime;
    Thread updateThread;
    Camera camera;
    int lastx=winWidth/2, lasty=winHeight/2;
    
    public MainApp() {
        lastTime = System.nanoTime();
        theta = 0;
        camera = new Camera(0,0,0,0,0,1,0,1,0);
    }
    
    public synchronized void update () {
//        long dt = System.nanoTime() - lastTime;
//        lastTime = System.nanoTime();
//        theta += dt/10000000000.0;
//        sx = 0.5*Math.sin(theta);
//        sy = 0;
//        sz = 0.5*Math.cos(theta);
    }
    
    public synchronized void display (GLAutoDrawable gld)
    {
        final GL2 gl = gld.getGL().getGL2();

        // Clear the buffer, need to do both the color and the depth buffers
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        // Load the identity into the Modelview matrix
        gl.glLoadIdentity();
        // Setup the camera.  The camera is located at the origin, looking along the positive z-axis, with y-up
        camera.setLookAt(glu);
        
        // set the position and diffuse/ambient terms of the light
        float [] pos = {1, 1, -1, 0};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, pos, 0);
        float [] diffuse = {0.7f, 0.7f, 0.7f, 1.0f};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
        float [] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, ambient, 0);
        float [] specular = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, specular, 0);
        
        // set the color
        float [] color1 = {0.0f, 1.0f, 0.0f, 1.0f};
        float [] color3 = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color1, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color3, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 64.0f);

        // draw a sphere
        gl.glPushMatrix();
        gl.glTranslated(sx, sy, sz);
        gl.glRotated(360*theta, 0, 1, 0);
        glut.glutSolidTorus(0.05, 0.1, 20, 20);
        gl.glPopMatrix();
        

        // draw a quad
        gl.glBegin(GL2.GL_QUADS);
        float [] color2 = {0.0f, 0.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color2, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color2, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f);

        //gl.glNormal3d(0,0,-1);
        //gl.glVertex3d(-1.0, -1.0, 1.0);
        //gl.glVertex3d(-1.0, 1.0, 1.0);
        //gl.glVertex3d(1.0, 1.0, 1.0);
        //gl.glVertex3d(1.0, -1.0, 1.0);
             
        gl.glEnd();
    }
    
    public void displayChanged (GLAutoDrawable gld, boolean arg1, boolean arg2)
    {
    }
    
    public void reshape (GLAutoDrawable gld, int x, int y, int width, int height)
    {
        GL gl = gld.getGL();
        winWidth = width;
        winHeight = height;
        gl.glViewport(0,0, width, height);
    }

    public void init (GLAutoDrawable gld)
    {
        glu = new GLU();
        glut = new GLUT();
        final GL2 gl = gld.getGL().getGL2();
        gl.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        // setup the projection matrix
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60.0, 1.33, 0.001, 20);
        
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glEnable(GLLightingFunc.GL_NORMALIZE); // automatically normalizes stuff
        gl.glEnable(GL.GL_CULL_FACE); // cull back faces
        gl.glEnable(GL.GL_DEPTH_TEST); // turn on z-buffer
        gl.glEnable(GLLightingFunc.GL_LIGHTING); // turn on lighting
        gl.glEnable(GLLightingFunc.GL_LIGHT0); // turn on light
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH); // smooth normals
    }
    public void init() {
        setLayout(new FlowLayout());
        // create a gl drawing canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        canvas.setPreferredSize(new Dimension(winWidth, winHeight));
        // add gl event listener
        canvas.addGLEventListener(this);
        add(canvas);
        setSize(winWidth, winHeight);
        canvas.addKeyListener(this);
        canvas.addMouseListener(new MouseAdapter(){
        	public void mouseExited(MouseEvent e) {
        	    int screenx = e.getLocationOnScreen().x-e.getPoint().x;
        	    int screeny = e.getLocationOnScreen().y-e.getPoint().y;
        	    int middleX = screenx+(winWidth/ 2);
        	    int middleY = screeny+(winHeight / 2);
        	    try{
        	      new Robot().mouseMove(middleX, middleY);
        	    }catch(Exception ex){System.out.println(ex);}
        	};
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter(){
        	public void mouseMoved(MouseEvent e){
        		int x = e.getX();
        		int y = e.getY();
        		camera.pitch(x-lastx);
        		camera.roll(y-lasty);
        		lastx=x;
        		lasty=y;
        	}
        });
        // add the canvas to the frame
        animator = new FPSAnimator(canvas, 60);
        updateThread = new Thread(new Runnable() {
		public void run() {
		    while(true) {
			update();
		    }
		}
	    });
    }
    
    public void start() {
        animator.start();
        updateThread.start();
	} 
    public void stop() {animator.stop();}
    public void dispose (GLAutoDrawable arg0){}
    @Override
	public synchronized void keyPressed (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            camera.forward();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            camera.back();
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            camera.left();
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            camera.right();
        }
    }
    @Override
	public void keyReleased (KeyEvent e){}
    @Override
	public void keyTyped (KeyEvent e){}
}
