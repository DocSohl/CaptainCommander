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
import java.util.ArrayList;
import java.util.Iterator;

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
 * Main OpenGL app for CaptainCommander<br>
 * Creates a JApplet viewer and attaches a JoGL canvas to it.
 * @author Ian Sohl & Greg Lewis
 */
public class MainApp extends JApplet implements GLEventListener, KeyListener
{
	private static final long serialVersionUID = 1L;

	/**Square Doubles*/
	double sqr( double x) { return x*x;}


	int winWidth=800, winHeight=600; //Arbitrary values
	FPSAnimator animator; //FPSAnimator to define framerate
	GLU glu;

	int enemyCount = 10;
	String enemiesRemaining = "" + enemyCount;
	GLUT glut;
	TextRenderer renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 36));		
	TextRenderer smallRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 18));
	boolean startGame = false;
	boolean pauseGame = false;
	boolean restartGame = false;
	boolean invertControls = false;
	boolean canFire = false;
	Thread updateThread;
	Cursor noCursor;
	GLProfile glp;
	GLCapabilities caps;
	GLCanvas canvas;
	Camera camera; //Camera to view scene with
	int lastx=winWidth/2, lasty=winHeight/2; //The last recorded location of the mouse
	ArrayList<Ship> ships = new ArrayList<Ship>();

	public MainApp() {
		camera = new Camera(0,0,0,0,0,1,0,1,0); //Create a camera at <0,0,0> pointing into the z-axis
		Ship player = new Ship();
		camera.setPlayer(player);
		ships.add(player);
		for(int i=1; i<10; i++){
			ships.add(new Ship(0.0,0.0,i));
		}

		//FEAR MY MAGIC NUMBERS
		Double [][] plane = {
				{-2.0,-2.0,.1},//middle of screen button0
				{-2.0,2.0,.1},//""1
				{2.0,2.0,.1},//""2
				{2.0,-2.0,.1},//""3
				{-4.0,-4.04,.1},//bottom right of screen overlay4
				{-4.0,-0.04,.1},//""5
				{0.0, -0.04,.1},//""6
				{0.0,-4.04,.1},//""7
				{-0.043,-.031,.1},//bototm middle pause button8
				{-0.043, -.02,.1},//""9
				{.03, -.02,.1},//""10
				{.03,-.031,0.1},//""11
				{-0.0765,.026,.1},//top right pause button 12
				{-0.0765, 0.035,.1},//""13
				{-.0275, 0.035,.1},//""14
				{-.0275,.026,0.1},//""15
				{-0.0, -.012,.1},//BEGINNING COCKPIT, TOP LEFT BAR TO CENTER 16
				{-0.0, -0.01,.1},//""17
				{0.08, 0.058,.1},//""18
				{0.08,.0525,0.1},//""19
				{-0.0, -.01,.1},//COCKPIT, TOP right BAR TO CENTER 20
				{-0.0, -0.012,.1},//""21
				{-0.28, .21,.1},//""22
				{-0.28,.226,0.1},//""23
				{-0.00028, -.003,.1},//COCKPIT, vertical crossheir, 24
				{-0.00028, 0.001,.1},//""25
				{0.00029, 0.001,.1},//""26
				{0.00029,-.003,0.1},//""27				
				{-.002, -.001, .1},//cockpit, horizontal corsshair, 28
				{0.002, -.001, .1},//""29
				{0.002, -0.0005, .1},//""30
				{-.002, -0.0005,.1},//""31
				{-0.0, -.082,.1},//cockpit, bottomleft LEFT BAR TO CENTER 32
				{-0.0, -0.01,.1},//""33
				{0.08, -0.0825,.1},//""34			
				{0.08,-.048,0.1},//""35			
				{-0.0, -.01,.1},//COCKPIT, bottom right bar to center 36
				{-0.0, -0.082,.1},//""37
				{-0.28, -.138,.1},//""38  			
				{-0.28,-.125,0.1},//""39
		};
		camera.setUseMisc(plane);

	}

	/**
	 * Run all periodic items in the scene
	 */
	public synchronized void update () {
		//Unused for now
		//System.out.println("pauseGame state: " + pauseGame);
		//		ship.roll(1,winWidth*1000);
	}
	public synchronized void gameInstructions(GLAutoDrawable gld){
		smallRenderer.beginRendering(gld.getWidth(), gld.getHeight());
		smallRenderer.draw("Number of enemies remaining:" + enemiesRemaining, winWidth/2 +100, winHeight-574);
		smallRenderer.draw("escape to pause", winWidth/2 - 50, winHeight-554);
		smallRenderer.draw("Shift to invert controls", winWidth/2-260, winHeight-574);
		//smallRenderer.draw("shift to restart", winWidth/2 + 225, winHeight-554);
		smallRenderer.endRendering();
	}
	public synchronized void pauseInstructions(GLAutoDrawable gld){
		smallRenderer.beginRendering(gld.getWidth(),gld.getHeight());
		smallRenderer.draw("Or escape to resume", winWidth/2+50, winHeight-554);
		smallRenderer.draw("Shift to invert controls", winWidth/2+100, winHeight-574);
		smallRenderer.endRendering();
	}
	public synchronized void pauseScreen(GLAutoDrawable gld){
		final GL2 gl = gld.getGL().getGL2();


		//button in middle of pause screen
		Double [] bar1 = camera.multiplyUnitVector(camera.misc[8], 0.01);
		Double [] bar2 = camera.multiplyUnitVector(camera.misc[9], 0.01);
		Double [] bar3 = camera.multiplyUnitVector(camera.misc[10], 0.01);
		Double [] bar4 = camera.multiplyUnitVector(camera.misc[11], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color3 = {0.0f, 1.0f, 0.00f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color3, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color3, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar1[0],camera.c[1]+bar1[1],camera.c[2]+bar1[2]);
		gl.glVertex3d(camera.c[0]+bar2[0],camera.c[1]+bar2[1],camera.c[2]+bar2[2]);
		gl.glVertex3d(camera.c[0]+bar3[0],camera.c[1]+bar3[1],camera.c[2]+bar3[2]);
		gl.glVertex3d(camera.c[0]+bar4[0],camera.c[1]+bar4[1],camera.c[2]+bar4[2]);
		gl.glEnd();



		Double [] bar5 = camera.multiplyUnitVector(camera.misc[4], 0.01);
		Double [] bar6 = camera.multiplyUnitVector(camera.misc[5], 0.01);
		Double [] bar7 = camera.multiplyUnitVector(camera.misc[6], 0.01);
		Double [] bar8 = camera.multiplyUnitVector(camera.misc[7], 0.01);
		//area at bottom right of pause screen
		gl.glBegin(GL2.GL_QUADS);
		float [] color4 = {0.0f, 0.0f, 0.0f, 0.0f}; 
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color4, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color4, 0);	
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar5[0],camera.c[1]+bar5[1],camera.c[2]+bar5[2]);
		gl.glVertex3d(camera.c[0]+bar6[0],camera.c[1]+bar6[1],camera.c[2]+bar6[2]);
		gl.glVertex3d(camera.c[0]+bar7[0],camera.c[1]+bar7[1],camera.c[2]+bar7[2]);
		gl.glVertex3d(camera.c[0]+bar8[0],camera.c[1]+bar8[1],camera.c[2]+bar8[2]);
		gl.glEnd();


		Double [] bar9 = camera.multiplyUnitVector(camera.misc[12], 0.01);
		Double [] bar10 = camera.multiplyUnitVector(camera.misc[13], 0.01);
		Double [] bar11 = camera.multiplyUnitVector(camera.misc[14], 0.01);
		Double [] bar12= camera.multiplyUnitVector(camera.misc[15], 0.01);
		//button at top right of pause screen
		gl.glBegin(GL2.GL_QUADS);
		float [] color5 = {0.0f, 1.0f, 0.00f, 1.0f}; 
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color5, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color5, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar9[0],camera.c[1]+bar9[1],camera.c[2]+bar9[2]);
		gl.glVertex3d(camera.c[0]+bar10[0],camera.c[1]+bar10[1],camera.c[2]+bar10[2]);
		gl.glVertex3d(camera.c[0]+bar11[0],camera.c[1]+bar11[1],camera.c[2]+bar11[2]);
		gl.glVertex3d(camera.c[0]+bar12[0],camera.c[1]+bar12[1],camera.c[2]+bar12[2]);
		gl.glEnd();

		pauseInstructions(gld);
	}
	public synchronized void startScreen(GLAutoDrawable gld){
		final GL2 gl = gld.getGL().getGL2();

		//actual backround
		gl.glBegin(GL2.GL_QUADS);
		float [] color2 = {0.0f, 0.0f, 1.0f, 1.0f}; 
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color2, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color2, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+camera.misc[0][0],camera.c[1]+camera.misc[0][1],camera.c[2]+camera.misc[0][2]);
		gl.glVertex3d(camera.c[0]+camera.misc[1][0],camera.c[1]+camera.misc[1][1],camera.c[2]+camera.misc[1][2]);
		gl.glVertex3d(camera.c[0]+camera.misc[2][0],camera.c[1]+camera.misc[2][1],camera.c[2]+camera.misc[2][2]);
		gl.glVertex3d(camera.c[0]+camera.misc[3][0],camera.c[1]+camera.misc[3][1],camera.c[2]+camera.misc[3][2]);
		gl.glEnd();

		//button in the middle
		gl.glBegin(GL2.GL_QUADS);
		float [] color3 = {0.0f, 1.0f, .5f, 1.0f}; 
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color3, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color3, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+camera.misc[0][0]*.0018,camera.c[1]+camera.misc[0][1]*.0018,camera.c[2]+camera.misc[0][2]-.01);
		gl.glVertex3d(camera.c[0]+camera.misc[1][0]*.0018,camera.c[1]+camera.misc[1][1]*.0018,camera.c[2]+camera.misc[1][2]-.01);
		gl.glVertex3d(camera.c[0]+camera.misc[2][0]*.0018,camera.c[1]+camera.misc[2][1]*.0018,camera.c[2]+camera.misc[2][2]-.01);
		gl.glVertex3d(camera.c[0]+camera.misc[3][0]*.0018,camera.c[1]+camera.misc[3][1]*.0018,camera.c[2]+camera.misc[3][2]-.01);
		gl.glEnd();



	}
	public synchronized void inGameOverLay(GLAutoDrawable gld){
		final GL2 gl = gld.getGL().getGL2();

		//top left bar
		Double [] bar1 = camera.multiplyUnitVector(camera.misc[16], 0.01);
		Double [] bar2 = camera.multiplyUnitVector(camera.misc[17], 0.01);
		Double [] bar3 = camera.multiplyUnitVector(camera.misc[18], 0.01);
		Double [] bar4 = camera.multiplyUnitVector(camera.misc[19], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color1 = {0.0f, 0.0f, 0.50f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color1, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color1, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar1[0],camera.c[1]+bar1[1],camera.c[2]+bar1[2]);
		gl.glVertex3d(camera.c[0]+bar2[0],camera.c[1]+bar2[1],camera.c[2]+bar2[2]);
		gl.glVertex3d(camera.c[0]+bar3[0],camera.c[1]+bar3[1],camera.c[2]+bar3[2]);
		gl.glVertex3d(camera.c[0]+bar4[0],camera.c[1]+bar4[1],camera.c[2]+bar4[2]);
		gl.glEnd();

		//top right bar
		Double [] bar5 = camera.multiplyUnitVector(camera.misc[20], 0.01);
		Double [] bar6 = camera.multiplyUnitVector(camera.misc[21], 0.01);
		Double [] bar7 = camera.multiplyUnitVector(camera.misc[22], 0.01);
		Double [] bar8 = camera.multiplyUnitVector(camera.misc[23], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color2 ={0.0f, 0.0f, 0.50f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color2, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color2, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar5[0],camera.c[1]+bar5[1],camera.c[2]+bar5[2]);
		gl.glVertex3d(camera.c[0]+bar6[0],camera.c[1]+bar6[1],camera.c[2]+bar6[2]);
		gl.glVertex3d(camera.c[0]+bar7[0],camera.c[1]+bar7[1],camera.c[2]+bar7[2]);
		gl.glVertex3d(camera.c[0]+bar8[0],camera.c[1]+bar8[1],camera.c[2]+bar8[2]);
		gl.glEnd();
		// vertical crosshair
		Double [] bar9 = camera.multiplyUnitVector(camera.misc[24], 0.01);
		Double [] bar10 = camera.multiplyUnitVector(camera.misc[25], 0.01);
		Double [] bar11 = camera.multiplyUnitVector(camera.misc[26], 0.01);
		Double [] bar12= camera.multiplyUnitVector(camera.misc[27], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color4 ={1.0f, 1.0f, 1.0f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color4, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color4, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar9[0],camera.c[1]+bar9[1],camera.c[2]+bar9[2]);
		gl.glVertex3d(camera.c[0]+bar10[0],camera.c[1]+bar10[1],camera.c[2]+bar10[2]);
		gl.glVertex3d(camera.c[0]+bar11[0],camera.c[1]+bar11[1],camera.c[2]+bar11[2]);
		gl.glVertex3d(camera.c[0]+bar12[0],camera.c[1]+bar12[1],camera.c[2]+bar12[2]);
		gl.glEnd();
		//horizontal crosshair
		Double [] bar13 = camera.multiplyUnitVector(camera.misc[28], 0.01);
		Double [] bar14 = camera.multiplyUnitVector(camera.misc[29], 0.01);
		Double [] bar15 = camera.multiplyUnitVector(camera.misc[30], 0.01);
		Double [] bar16= camera.multiplyUnitVector(camera.misc[31], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color5 = {1.0f, 1.0f, 1.50f, 1.0f}; 
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color5, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color5, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar13[0],camera.c[1]+bar13[1],camera.c[2]+bar13[2]);
		gl.glVertex3d(camera.c[0]+bar14[0],camera.c[1]+bar14[1],camera.c[2]+bar14[2]);
		gl.glVertex3d(camera.c[0]+bar15[0],camera.c[1]+bar15[1],camera.c[2]+bar15[2]);
		gl.glVertex3d(camera.c[0]+bar16[0],camera.c[1]+bar16[1],camera.c[2]+bar16[2]);
		gl.glEnd();

		//bottom left to center crossbar
		Double [] bar17 = camera.multiplyUnitVector(camera.misc[32], 0.01);
		Double [] bar18 = camera.multiplyUnitVector(camera.misc[33], 0.01);
		Double [] bar19 = camera.multiplyUnitVector(camera.misc[34], 0.01);
		Double [] bar20 = camera.multiplyUnitVector(camera.misc[35], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color6 = {0.0f, 0.0f, 0.50f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color6, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color6, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar17[0],camera.c[1]+bar17[1],camera.c[2]+bar17[2]);
		gl.glVertex3d(camera.c[0]+bar18[0],camera.c[1]+bar18[1],camera.c[2]+bar18[2]);
		gl.glVertex3d(camera.c[0]+bar19[0],camera.c[1]+bar19[1],camera.c[2]+bar19[2]);
		gl.glVertex3d(camera.c[0]+bar20[0],camera.c[1]+bar20[1],camera.c[2]+bar20[2]);
		gl.glEnd();

		//bottom right to center crossbar
		Double [] bar21 = camera.multiplyUnitVector(camera.misc[36], 0.01);
		Double [] bar22 = camera.multiplyUnitVector(camera.misc[37], 0.01);
		Double [] bar23 = camera.multiplyUnitVector(camera.misc[38], 0.01);
		Double [] bar24 = camera.multiplyUnitVector(camera.misc[39], 0.01);
		gl.glBegin(GL2.GL_QUADS);
		float [] color7 ={0.0f, 0.0f, 0.50f, 1.0f};
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color7, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color7, 0);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Also shiny!
		gl.glVertex3d(camera.c[0]+bar21[0],camera.c[1]+bar21[1],camera.c[2]+bar21[2]);
		gl.glVertex3d(camera.c[0]+bar22[0],camera.c[1]+bar22[1],camera.c[2]+bar22[2]);
		gl.glVertex3d(camera.c[0]+bar23[0],camera.c[1]+bar23[1],camera.c[2]+bar23[2]);
		gl.glVertex3d(camera.c[0]+bar24[0],camera.c[1]+bar24[1],camera.c[2]+bar24[2]);
		gl.glEnd();

		gameInstructions(gld);
	}
	
	int counter = 0; //added

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
		// Clear the buffer, need to do both the color and the depth buffers
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		// Load the identity into the Modelview matrix
		gl.glLoadIdentity();
		// Setup the camera.  The camera is located at the origin, looking along the positive z-axis, with y-up
		camera.forward();
		camera.setLookAt(glu);

		if (startGame == false){
			startScreen(gld);
			renderer.beginRendering(gld.getWidth(), gld.getHeight());
			renderer.draw("escape to pause", winWidth/2, winHeight/2-150);
			renderer.draw("click to begin", winWidth/2, winHeight/2-150+36);
			renderer.endRendering();
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
		Iterator<Ship> siter = ships.iterator();
		while(siter.hasNext()){
			Ship ship = siter.next();
			ship.update(gld,glut,ships); //added
			ship.display(gld);
		}

			if (pauseGame == true){

				pauseScreen(gld);	
				renderer.beginRendering(gld.getWidth(), gld.getHeight());
				renderer.draw("click to restart",  winWidth/2+150, winHeight/2+150);
				renderer.draw("click to resume game", winWidth/2-150, winHeight/2-150);
				renderer.endRendering();

			}
			else{
				inGameOverLay(gld);
			}
		}
		// draw a GIANT ASS RECTANGLE (for science)
		gl.glBegin(GL2.GL_QUADS); //Use quads to define vertices
		float [] color2 = {0.0f, 0.0f, 1.0f, 1.0f}; //Make one side blue
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color2, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color2, 0);
		gl.glNormal3d(0,0,-1); //Point the normal in the right direction
		gl.glVertex3d(camera.c[0]-20, camera.c[1]-200, camera.c[2]-200);
		gl.glVertex3d(camera.c[0]-20, camera.c[1]-200, camera.c[2]+200);
		float [] color4 = {0.5f, 0.5f, 1.0f, 1.0f}; //Make the other side red
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color4, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color4, 0);
		gl.glVertex3d(camera.c[0]-20, camera.c[1]+200, camera.c[2]+200);
		gl.glVertex3d(camera.c[0]-20, camera.c[1]+200, camera.c[2]-200);
		gl.glEnd(); //Done with rectangle
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
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); 
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
				else if(startGame == true && pauseGame == true){
					if (e.getX() <=624 && e.getX() >=244 && e.getY()<=460 && e.getY()>=404){
						pauseGame = false;
					}
					if (e.getX() <=797 && e.getX() >= 543 && e.getY() >=118 && e.getY() <= 164){
						//RESTART GAME SHIIIIT
					}

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
					if (invertControls == true){
						camera.roll(x-lastx,winWidth);
						camera.pitch((y-lasty)*-1,winHeight);
					}
					else{
						camera.roll(x-lastx,winWidth); //And compare with the last ones to make a roll
						camera.pitch(y-lasty,winHeight); //And pitch
					}
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
			if (startGame == true){
				pauseGame = !pauseGame;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			startGame = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT){
			invertControls = !invertControls;
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE){
			camera.shoot();
		}
	}
	@Override
	public void keyReleased (KeyEvent e){}
	@Override
	public void keyTyped (KeyEvent e){}
}
