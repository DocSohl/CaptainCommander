package CaptainCommander;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;


/**
 * Sample ship class for a ship controlled by the mouse. You can use this for reference for your individual classes.
 * (Or copy it directly, if you're lazy!)
 * @author Ian Sohl
 */
public class Ship extends Orientation{	
	/**Create a new ship at <1,0,0>, pointing in <1,0,0>.*/
	public Ship(){
		super(); //In case I decide to add anything to the Orientation constructor
		eye[0] = 0.0; //The actual physical location of the ship.
		eye[1] = 0.0; //Technically, vectors here are column vectors so they look like this:
		eye[2] = 1.0; //                    {{x},
		n[0] = 0.0; //                     {y},
		n[1] = 0.0; //                     {z}}
		n[2] = 1.0; //
		v[0] = 0.0;  //eye (Eye) is the location of the ship/camera
		v[1] = 1.0;  //cen (Center) is the unit vector that the ship points
		v[2] = 0.0;  //up  (Up) is the unit vector indicating which direction is up for the ship
		misc = new Double[][]{ //Create 4 3-vectors to represent the 4 corners of the base of the ship
				new Double[]{-minorLength,-minorLength,0.0},
				new Double[]{minorLength,-minorLength,0.0},
				new Double[]{minorLength,minorLength,0.0},
				new Double[]{-minorLength,minorLength,0.0}
		}; //These are created in order for the rotating drawing
		setUseMisc(); //Tell the superclass to use these misc vectors
	}
	
	/**
	 * Map the mouse movement to an angle via an arbitrary constant.<br>
	 * (General: edge of screen = pi/2)
	 * @param pixdiff
	 * @param winWidth
	 */
	public void roll(int pixdiff, int winWidth){
		double changeangle = pixdiff/(winWidth*0.5)*Math.PI*0.5; //This may need to be tweaked later for best effect
		super.roll(changeangle); //Remember, this angle is in radians
	}
	
	/**
	 * Map mouse movement to an angle in the pitch direction.
	 * @param pixdiff
	 * @param winHeight
	 */
	public void pitch(int pixdiff, int winHeight){
		double changeangle = pixdiff/(winHeight*0.5)*Math.PI*0.5;
		super.pitch(changeangle); //radians
	}
	
	double majorLength = 0.5; //The length of the major edges of the pyramid
	double minorLength = 0.2; //The length of each of the square sides of the base
	
	/**
	 * Draw the ship. (Just a pyramid here)
	 * @param gld GLAutoDrawable passed from MainApp
	 */
	public void display(GLAutoDrawable gld){
		double nosedistance = Math.sqrt(sqr(majorLength)-sqr(minorLength/2)); //Finding the distance between the center of the ship and the nose.
		Double [] noselocation = super.multiplyUnitVector(n, nosedistance); //Adjusting the cen vector to point to the location of the nose
		final GL2 gl = gld.getGL().getGL2(); //Fetch the drawing environment from the gld
		gl.glBegin(GL2.GL_TRIANGLE_FAN); //Drawing the pyramidal part in one sheet, which is more efficient since less edges are calculated
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Make it shiny!
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, new float[]{1.0f,0.0f,0.0f}, 0); //And Blue...
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, new float[]{0.0f,0.0f,1.0f}, 0); //Shiny blue
	    gl.glVertex3d(eye[0]+noselocation[0],eye[1]+noselocation[1],eye[2]+noselocation[2]);   //Place the vertex for the nose of the ship
	    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, new float[]{0.0f,1.0f,0.0f}, 0); //Fade to green
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, new float[]{0.0f,1.0f,0.0f}, 0); //Shiny green
	    gl.glVertex3d(eye[0]+(minorLength*misc[0][0]),eye[1]+(minorLength*misc[0][1]),eye[2]+(minorLength*misc[0][2]));   //Draw the remaining edges in this order:
	    gl.glVertex3d(eye[0]+(minorLength*misc[1][0]),eye[1]+(minorLength*misc[1][1]),eye[2]+(minorLength*misc[1][2]));   //			1,2,3,4,1
	    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, new float[]{0.0f,0.0f,1.0f}, 0); //Fade to green
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, new float[]{0.0f,0.0f,1.0f}, 0); //Shiny green
	    gl.glVertex3d(eye[0]+(minorLength*misc[2][0]),eye[1]+(minorLength*misc[2][1]),eye[2]+(minorLength*misc[2][2]));   //Which mark the four corners of the base
	    gl.glVertex3d(eye[0]+(minorLength*misc[3][0]),eye[1]+(minorLength*misc[3][1]),eye[2]+(minorLength*misc[3][2]));   //		Note: reversing the drawing order causes the normal to point inwards
	    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, new float[]{0.0f,1.0f,0.0f}, 0); //Fade to green
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, new float[]{0.0f,1.0f,0.0f}, 0); //Shiny green
	    gl.glVertex3d(eye[0]+(minorLength*misc[0][0]),eye[1]+(minorLength*misc[0][1]),eye[2]+(minorLength*misc[0][2]));   //		Also: eye+(minorLength*misc is a sequence of 4 3-vectors pointing to corners
		gl.glEnd(); //Finish the the main pyramid part
		gl.glBegin(GL2.GL_QUADS); //Draw the base of the ship as a square
		gl.glVertex3d(eye[0]+(minorLength*misc[0][0]),eye[1]+(minorLength*misc[0][1]),eye[2]+(minorLength*misc[0][2])); //Note, same order as the pyramid, which causes the normal to appear outside.
	    gl.glVertex3d(eye[0]+(minorLength*misc[1][0]),eye[1]+(minorLength*misc[1][1]),eye[2]+(minorLength*misc[1][2]));
	    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, new float[]{0.0f,0.0f,1.0f}, 0); //Fade to green
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, new float[]{0.0f,0.0f,1.0f}, 0); //Shiny green
	    gl.glVertex3d(eye[0]+(minorLength*misc[2][0]),eye[1]+(minorLength*misc[2][1]),eye[2]+(minorLength*misc[2][2]));
	    gl.glVertex3d(eye[0]+(minorLength*misc[3][0]),eye[1]+(minorLength*misc[3][1]),eye[2]+(minorLength*misc[3][2]));
		gl.glEnd(); //Finish the base
	}
}
