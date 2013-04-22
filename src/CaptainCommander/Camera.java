package CaptainCommander;

import javax.media.opengl.glu.GLU;

/**
 * Quick and dirt Camera class adapted from code by Dr. Bargteil. <br>
 * Kinda... <br>
 * If you look at it with a squint.<br>
 * @author Ian Sohl
 */
public class Camera extends Orientation{
		/**
		 * Create a new camera at the positions defined.
		 * @param ex Eye(x) The position of the camera itself
		 * @param ey Eye(y)
		 * @param ez Eye(z)
		 * @param cx Center(x) The direction the camera is looking
		 * @param cy Center(y) Note: Based on the origin!
		 * @param cz Center(z)
		 * @param ux Up(x) The direction the camera would point if you said "sky"
		 * @param uy Up(y) If it had arms...
		 * @param uz Up(z) Or sentience...
		 */
		Camera(double ex, double ey, double ez, double cx, double cy, double cz, double ux, double uy, double uz) {
			super(0.0,0.0,0.0);
			c[0] = ex;
			c[1] = ey;
			c[2] = ez;
			n[0] = cx;
			n[1] = cy;
			n[2] = cz;
			v[0] = ux; //Space efficiency!
			v[1] = uy; 
			v[2] = uz;
		}
		
		/**
		 * Change the camera orientation to look in the pre-defined direction
		 * @param glu GL device passed from MainApp
		 */
		void setLookAt(GLU glu) {
			glu.gluLookAt(c[0],c[1],c[2],c[0]+n[0],c[1]+n[1],c[2]+n[2],v[0],v[1],v[2]);
		}
		
		/**Tell the camera to move forward. I'll make this better at some point.*/
		public void forward(){
			super.forward(0.03);
		}
		
		/**Tell the camera to move backwards. I'll make this better at some point.*/
		public void back(){
			super.back(0.03);
		}
		
		/**
		 * Map the mouse movement to an angle via an arbitrary constant (General: edge of screen = pi/2)
		 * @param pixdiff The number of pixels between this movement and the last recorded location
		 * @param winWidth The width of the window. Passed from MainApp
		 */
		public void roll(int pixdiff, int winWidth){
			double changeangle = pixdiff/(winWidth*0.5)*Math.PI*0.5; //moving the mouse from the middle to the edge is 90 degrees
			super.roll(changeangle); //Radians!
		}
		/**
		 * Map the mouse movement to an angle via an arbitrary constant (General: edge of screen = pi/2)
		 * @param pixdiff The number of pixels between this movement and the last recorded location
		 * @param winHeight The height of the window. Passed from MainApp
		 */
		public void pitch(int pixdiff, int winHeight){
			double changeangle = pixdiff/(winHeight*0.5)*Math.PI*0.5;
			super.pitch(changeangle); //Radians
		}
	}