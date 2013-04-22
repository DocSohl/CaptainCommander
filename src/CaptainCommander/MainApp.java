package CaptainCommander;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.JApplet;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.fixedfunc.GLLightingFunc;

/**
 * Main OpenGL app for CaptainCommander. Ripped quite heartily from Dr. Bargteil's example.<br>
 * Creates a JApplet viewer and attaches a JoGL canvas to it.
 * @author Ian Sohl
 */
public class MainApp extends JApplet implements GLEventListener, KeyListener
{
	private static final long serialVersionUID = 1L;

	/**Square Doubles*/
	double sqr( double x) { return x*x;}


	int winWidth=800, winHeight=600; //Arbitrary values
	FPSAnimator animator; //FPSAnimator to define framerate
	GLU glu;
	GLUT glut;
	TextRenderer renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));		
	TextRenderer smallRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 18));
	boolean startGame = false;
	boolean pauseGame = false;
	boolean restartGame = false;
	//will be used to control when the player ship can fire
	boolean canFire = false;
	Thread updateThread;
	Cursor noCursor;
	GLProfile glp;
	GLCapabilities caps;
	GLCanvas canvas;
	Camera camera; //Camera to view scene with
	Ship ship; //Sample ship for testing
	int lastx=winWidth/2, lasty=winHeight/2; //The last recorded location of the mouse
	boolean robotmoved = false;

	public MainApp() {
		double x = Math.sqrt(2)/2;
		camera = new Camera(0,0,0,0,0,1,0,1,0); //Create a camera at <0,0,0> pointing into the z-axis
		//		camera = new Camera(0,0,0,0,x,x,0,x,-x);
		//		camera.roll+=Math.PI;
		//		camera.pitch+=Math.PI/2;
		ship = new Ship(); //Create a test ship for testing.
		//		ship.roll(200,winWidth);
		//		ship.pitch(-200,winHeight);

	}

	/**
	 * Run all periodic items in the scene
	 */
	public synchronized void update () {
	
		//Unused for now
		//System.out.println("pauseGame state: " + pauseGame);
		//		ship.roll(1,winWidth*1000);
	}
	public synchronized void pauseScreen(GLAutoDrawable gld){
		final GL2 gl = gld.getGL().getGL2();
		gl.glVertex3d(camera.eye[0]-1.0,camera.eye[1]- 1.0,camera.eye[2]+ .1);
		gl.glVertex3d(camera.eye[0]-1.0,camera.eye[1]+ 1.0,camera.eye[2]+ .1);
		gl.glVertex3d(camera.eye[0]+1.0,camera.eye[1]+ 1.0,camera.eye[2]+ .1);
		gl.glVertex3d(camera.eye[0]+1.0,camera.eye[1]- 1.0,camera.eye[2]+ .1);
		gl.glEnd();
	}
	public synchronized void startScreen(GLAutoDrawable gld){
		final GL2 gl = gld.getGL().getGL2();
		gl.glVertex3d(-1.0,- 1.0,+ 1.0);
		gl.glVertex3d(-1.0,+ 1.0,+ 1.0);
		gl.glVertex3d(+1.0,+ 1.0,+ 1.0);
		gl.glVertex3d(+1.0,- 1.0,+ 1.0);
		gl.glEnd();
	}

	/**
	 * Redraw the scene
	 */
	public synchronized void display (GLAutoDrawable gld)
	{

		if (pauseGame == true || startGame == false){
			canvas.setCursor(getCursor());
		}
		else{
			canvas.setCursor(noCursor);
		}
		final GL2 gl = gld.getGL().getGL2();
		final GL2 gl1 = gld.getGL().getGL2();
		// Clear the buffer, need to do both the color and the depth buffers
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		// Load the identity into the Modelview matrix
		gl.glLoadIdentity();
		// Setup the camera.  The camera is located at the origin, looking along the positive z-axis, with y-up
		camera.setLookAt(glu);

		if (startGame == false){
			//startScreen(gld);
		}
		else{
			// set the position and diffuse/ambient terms of the light
			float [] pos = {1, 1, -1, 0};
			gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, pos, 0);
			float [] diffuse = {0.7f, 0.7f, 0.7f, 1.0f};
			gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
			float [] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
			gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, ambient, 0);
			float [] specular = {1.0f, 1.0f, 1.0f, 1.0f};
			gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, specular, 0);
			ship.display(gld);

			// draw a GIANT ASS RECTANGLE (for science)
			gl.glBegin(GL2.GL_QUADS); //Use quads to define vertices
			float [] color2 = {0.0f, 0.0f, 1.0f, 1.0f}; //Make one side blue
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color2, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color2, 0);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //And Shiny!
			gl.glNormal3d(0,0,-1); //Point the normal in the right direction
			gl.glVertex3d(-2.0, -2.0, 4.0); //Define two corners
			gl.glVertex3d(-2.0, 2.0, 4.0);
			float [] color4 = {1.0f, 0.0f, 0.0f, 1.0f}; //Make the other side red
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color4, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color4, 0);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
			gl.glVertex3d(2.0, 2.0, 4.0); //Define the remaining two corners
			gl.glVertex3d(2.0, -2.0, 4.0); 
			if (pauseGame == true){
				//pauseScreen(gld);
			}
			gl.glEnd(); //Done with rectangle
		}
	}

	public void reshape (GLAutoDrawable gld, int x, int y, int width, int height){ //JoGL resizing function
		GL gl = gld.getGL(); 
		winWidth = width;
		winHeight = height;
		gl.glViewport(0,0, width, height);
	}

	public void init (GLAutoDrawable gld){ //Initialize the JoGL components
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
		//		gl.glEnable(GL.GL_CULL_FACE); // cull back faces (DISABLED)
		gl.glEnable(GL.GL_DEPTH_TEST); // turn on z-buffer
		gl.glEnable(GLLightingFunc.GL_LIGHTING); // turn on lighting
		gl.glEnable(GLLightingFunc.GL_LIGHT0); // turn on light
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH); // smooth normals
	}
	/**Initialize scene based JoGL components*/
	public void init() {
		setLayout(new FlowLayout());
		// create a gl drawing canvas
		glp = GLProfile.getDefault();
		caps = new GLCapabilities(glp);
		canvas = new GLCanvas(caps);
		canvas.setPreferredSize(new Dimension(winWidth, winHeight));
		//Mask cursor
		Toolkit t = Toolkit.getDefaultToolkit();
		Image i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		noCursor = t.createCustomCursor(i, new Point(0, 0), "none");

		canvas.setCursor(noCursor);


		// add gl event listener
		canvas.addGLEventListener(this);
		add(canvas);
		setSize(winWidth, winHeight);
		canvas.addKeyListener(this);
		canvas.addMouseListener(new MouseAdapter(){ //Listen for the Mouse
			public void mouseExited(MouseEvent e) { //When the mouse exits the canvas, move it back to the center

				if (pauseGame == true || startGame == false){
				}
				else{
					int screenx = e.getLocationOnScreen().x-e.getPoint().x; //Determine canvas position based on cursor position in the canvas
					int screeny = e.getLocationOnScreen().y-e.getPoint().y; //     and in the OS
					int middleX = screenx+(winWidth/ 2); //New position to move to
					int middleY = screeny+(winHeight / 2);
					try{
						new Robot().mouseMove(middleX, middleY); //Use Robot to move the cursor
					}catch(Exception ex){System.out.println(ex);}
				}

			};
			public void mouseEntered(MouseEvent e){ //When the user first interacts, store the mouse position
				lastx = e.getX();
				lasty = e.getY();
			}
			public void mouseClicked(MouseEvent e){
				System.out.println(e.getX() + ", " + e.getY());
				if (startGame == false){
					//NOT SURE WHY THISE ISN'T WORKING YET
//					if (Math.abs(e.getX()-winWidth/2)<25 && Math.abs(e.getY()-winHeight/2)<25){
//						startGame = true;
//					}
					if (e.getX()  <= winWidth/2 + 25 && e.getX() >= winWidth/2-25 && 
							e.getY() <=winHeight/2 + 25 && e.getY() >= winHeight/2-25){
						startGame = true;
					}
				}
				else if(startGame == true && pauseGame == false){
					if (canFire == true){
						
					}
					//fire weapon code
				}
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter(){ //Actually use the mouse to move the camera
			public void mouseMoved(MouseEvent e){
				int screenx = e.getLocationOnScreen().x-e.getPoint().x; //Determine canvas position based on cursor position in the canvas
				int screeny = e.getLocationOnScreen().y-e.getPoint().y; //     and in the OS
				int middleX = screenx+(winWidth/ 2); //New position to move to
				int middleY = screeny+(winHeight / 2);
				int x = e.getX();
				int y = e.getY();//Get the current coordinates...
				if (pauseGame == true || startGame == false){

				}
				else{
					camera.roll(x-lastx,winWidth); //And compare with the last ones to make a roll
					camera.pitch(y-lasty,winHeight); //And pitch
				}
				lastx=x; //Store for the next time
				lasty=y;
				if(Math.abs(x-(winWidth/2))>40|| Math.abs(y-(winHeight/2))>40){
					try{
						if (pauseGame == true || startGame == false){

						}
						else{
							lastx=winWidth/2;
							lasty=winHeight/2;
							new Robot().mouseMove(middleX, middleY); //Use Robot to move the cursor
						}
					}catch(Exception ex){System.out.println(ex);}
				}
			}
		});
		// add the canvas to the frame
		animator = new FPSAnimator(canvas, 60); //Set to run at 60 Frames per Second
		updateThread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					update(); //Run the update thread at the same time
				}
			}
		});
	}

	public void start() { //Applet start
		animator.start();
		updateThread.start();
	} 
	public void stop() {animator.stop();} //Applet Stop
	public void dispose (GLAutoDrawable arg0){} //JoGL yells at me if I remove this
	@Override
	public synchronized void keyPressed (KeyEvent e){ //Allow for forwards and backwards movement
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) camera.forward();
		if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
			camera.back();
		}

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
			if (pauseGame == true){
				pauseGame = false;
				return;
			}
			pauseGame = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			startGame = true;
		}
	}
	@Override
	public void keyReleased (KeyEvent e){}
	@Override
	public void keyTyped (KeyEvent e){}
}
