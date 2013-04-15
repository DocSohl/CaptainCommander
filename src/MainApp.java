
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
		private double pitch = 0, roll = 0, yaw = 0;
		private double X=0,Y=1,Z=0;
		private Double [][] eye = {{0.0},{1.0},{0.0}};
		private Double [][] cen = {{0.0},{0.0},{0.0}};
		private Double [][] up = {{0.0},{0.0},{1.0}};
		//        private Double [][] rotationmatrix = {{1.0,0.0,0.0},{0.0,1.0,0.0},{0.0,0.0,1.0}};

		Camera(double ex, double ey, double ez, double cx, double cy, double cz, double ux, double uy, double uz) {
			this.eye[0] = new Double[]{ex};
			this.eye[1] = new Double[]{ey};
			this.eye[2] = new Double[]{ez};
			this.cen[0] = new Double[]{cx};
			this.cen[1] = new Double[]{cy};
			this.cen[2] = new Double[]{cz};
			this.up[0] = new Double[]{ux};
			this.up[1] = new Double[]{uy}; 
			this.up[2] = new Double[]{uz};
		}

		void setLookAt(GLU glu) {
			glu.gluLookAt(eye[0][0], eye[1][0], eye[2][0], cen[0][0], cen[1][0], cen[2][0], up[0][0], up[1][0], up[2][0]);
		}

		void forward() {
			double dx = cen[0][0] - eye[0][0], dy = cen[1][0] - eye[1][0], dz = cen[2][0] - eye[2][0];
			double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
			dx /= m*100.0;
			dy /= m*100.0;
			dz /= m*100.0;

			cen[0][0] += dx;
			cen[1][0] += dy;
			cen[2][0] += dz;
			eye[0][0] += dx;
			eye[1][0] += dy;
			eye[2][0] += dz;
		}

		void back() {
			double dx = cen[0][0] - eye[0][0], dy = cen[1][0] - eye[1][0], dz = cen[2][0] - eye[2][0];
			double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
			dx /= m*100.0;
			dy /= m*100.0;
			dz /= m*100.0;

			cen[0][0] -= dx;
			cen[1][0] -= dy;
			cen[2][0] -= dz;
			eye[0][0] -= dx;
			eye[1][0] -= dy;
			eye[2][0] -= dz;
		}

		void left() {
			double dx = cen[0][0] - eye[0][0], dy = cen[1][0] - eye[1][0], dz = cen[2][0] - eye[2][0];
			double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
			dx /= m;
			dy /= m;
			dz /= m;
			double nx = Math.cos(ctheta)*dx + Math.sin(ctheta)*dz;
			double ny = dy;
			double nz = -Math.sin(ctheta)*dx + Math.cos(ctheta)*dz;
			cen[0][0] = eye[0][0]+nx;
			cen[1][0] = eye[1][0]+ny;
			cen[2][0] = eye[2][0]+nz;
		}

		void right() {
			double dx = cen[0][0] - eye[0][0], dy = cen[1][0] - eye[1][0], dz = cen[2][0] - eye[2][0];
			double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
			dx /= m;
			dy /= m;
			dz /= m;
			double nx = Math.cos(ctheta)*dx - Math.sin(ctheta)*dz;
			double ny = dy;
			double nz = Math.sin(ctheta)*dx + Math.cos(ctheta)*dz;
			cen[0][0] = eye[0][0]+nx;
			cen[1][0] = eye[1][0]+ny;
			cen[2][0] = eye[2][0]+nz;
		}

		void roll(int pixdiff){
			//Map the mousemovement to an angle via an arbitrary constant (General: edge of screen = pi/2)
			double changeangle = pixdiff/(winWidth*0.5)*Math.PI*0.5;
			roll(changeangle);
		}

		void roll(double angle){
			pitch=-angle;
			reevaluate();
		}

		void pitch(int pixdiff){
			double changeangle = pixdiff/(winHeight*0.5)*Math.PI*0.5;
			pitch(changeangle);
		}

		void pitch(double angle){
			roll=-angle;
			reevaluate();
		}

		void reevaluate(){
			//Please kill me...
			//        	Double [][] rotationMatrix = {
			//        			{(1-Math.cos(theta))*X*X+Math.cos(theta),(1-Math.cos(theta))*X*Y+Math.sin(theta)*Z,(1-Math.cos(theta))*X*Z-Math.sin(theta)*Y},
			//        			{(1-Math.cos(theta))*X*Y-Math.sin(theta)*Z,(1-Math.cos(theta))*Y*Y+Math.cos(theta),(1-Math.cos(theta))*Y*Z+Math.sin(theta)*X},
			//        			{(1-Math.cos(theta))*X*Z+Math.sin(theta)*Y,(1-Math.cos(theta))*Y*Z-Math.sin(theta)*X,(1-Math.cos(theta))*Z*Z+Math.cos(theta)}};
			//			Double [][] rotationMatrix = {
			//					{Math.cos(pitch)*Math.cos(yaw), Math.cos(pitch)*Math.sin(yaw), -Math.sin(pitch), 0.0}, 
			//					{Math.cos(yaw)*Math.sin(roll)*Math.sin(pitch)-Math.cos(roll)*Math.sin(yaw), Math.cos(roll)*Math.cos(yaw)+Math.sin(roll)*Math.sin(pitch)*Math.sin(yaw), Math.cos(pitch)*Math.sin(roll), 0.0}, 
			//					{Math.cos(roll)*Math.cos(yaw)*Math.sin(pitch)+Math.sin(roll)*Math.sin(yaw), -Math.cos(yaw)*Math.sin(roll)+Math.cos(roll)*Math.sin(pitch)*Math.sin(yaw), Math.cos(roll)*Math.cos(pitch), 0.0}, 
			//					{0.0, 0.0, 0.0, 1.0}};
//			Double [][] rotationMatrix = {
//					{Math.cos(roll), Math.cos(pitch)*Math.sin(roll), Math.sin(roll)*Math.sin(pitch)}, 
//					{-Math.sin(roll), Math.cos(roll)*Math.cos(pitch), Math.cos(roll)*Math.sin(pitch)}, 
//					{0.0, -Math.sin(pitch), Math.cos(pitch)}};
//			Double [][] rotationMatrix = {
//					{Math.cos(roll), Math.sin(roll), 0.0}, 
//					{-Math.cos(pitch)*Math.sin(roll), Math.cos(roll)*Math.cos(pitch), Math.sin(pitch)}, 
//					{Math.sin(roll)*Math.sin(pitch), -Math.cos(roll)*Math.sin(pitch), Math.cos(pitch)}};
			Double [][] rollRotation = {
					{1.0,0.0,0.0},
					{0.0,Math.cos(roll),Math.sin(roll)},
					{0.0,-Math.sin(roll),Math.cos(roll)}};
			Double [][] pitchRotation = {
					{Math.cos(pitch),Math.sin(pitch),0.0},
					{-Math.sin(pitch),Math.cos(pitch),0.0},
					{0.0,0.0,1.0}};
			Double [][] rotationMatrix = new Double[][]{new Double[]{1.0,0.0,0.0},new Double[]{0.0,1.0,0.0},new Double[]{0.0,0.0,1.0}};
			Double [][] temprotationMatrix = new Double[][]{new Double[]{0.0,0.0,0.0},new Double[]{0.0,0.0,0.0},new Double[]{0.0,0.0,0.0}};
			for(int i = 0; i < 3; i++)
				for(int j = 0; j < 3; j++)
					for(int k = 0; k < 3; k++)
						temprotationMatrix[i][j] += rotationMatrix[i][k] * pitchRotation[k][j];
			Double [][] temprotationMatrix2 = new Double[][]{new Double[]{0.0,0.0,0.0},new Double[]{0.0,0.0,0.0},new Double[]{0.0,0.0,0.0}};
//			System.out.println("Matrix: ");
			for(int i = 0; i < 3; i++){
				for(int j = 0; j < 3; j++){
					for(int k = 0; k < 3; k++)
						temprotationMatrix2[i][j] += temprotationMatrix[i][k] * rollRotation[k][j];
//					System.out.print(temprotationMatrix2[i][j]+" ");
				}
//				System.out.println();
			}
			rotationMatrix = temprotationMatrix2;
			Double [][] newcen = new Double[][]{new Double[]{0.0},new Double[]{0.0},new Double[]{0.0}};
			Double [][] newup = new Double[][]{new Double[]{0.0},new Double[]{0.0},new Double[]{0.0}};
			for(int i = 0; i < 3; i++) {
				for(int k = 0; k < 3; k++) {
					newcen[i][0] += rotationMatrix[i][k] * cen[k][0];
					newup[i][0] += rotationMatrix[i][k] * up[k][0];
				}
			}
			cen = newcen;
			up = newup;
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
//				camera.pitch(0.000001);
//				camera.roll(0.000001);
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

		// draw a shiny donut
		//		gl.glPushMatrix();
		//		gl.glTranslated(0.2, sy, sz);
		//		gl.glRotated(360*theta, 0, 1, 0);
		//		glut.glutSolidTorus(0.05, 0.1, 20, 20);
		//		gl.glPopMatrix();


		// draw a GIANT ASS BOX
		gl.glBegin(GL2.GL_QUADS);
		float [] color2 = {0.0f, 0.0f, 1.0f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color2, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color2, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f);

		gl.glNormal3d(0,0,-1);
		gl.glVertex3d(-1.0, -1.0, 1.0);
		gl.glVertex3d(-1.0, 1.0, 1.0);
		float [] color4 = {1.0f, 0.0f, 0.0f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color4, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color4, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f);
		gl.glVertex3d(1.0, 1.0, 1.0);
		gl.glVertex3d(1.0, -1.0, 1.0);

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
			public void mouseEntered(MouseEvent e){
				lastx = e.getX();
				lasty = e.getY();
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				int x = e.getX();
				int y = e.getY();
				camera.roll(x-lastx);
				camera.pitch(y-lasty);
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
