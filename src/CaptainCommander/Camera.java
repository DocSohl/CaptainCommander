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
			super(); //Just in case. You never know when I might want to stick one in.
			eye[0] = new Double[]{ex};
			eye[1] = new Double[]{ey};
			eye[2] = new Double[]{ez};
			cen[0] = new Double[]{cx};
			cen[1] = new Double[]{cy};
			cen[2] = new Double[]{cz};
			up[0] = new Double[]{ux}; //Space efficiency!
			up[1] = new Double[]{uy}; 
			up[2] = new Double[]{uz};
		}
		
		/**
		 * Change the camera orientation to look in the pre-defined direction
		 * @param glu GL device passed from MainApp
		 */
		void setLookAt(GLU glu) {
			glu.gluLookAt(eye[0][0], eye[1][0], eye[2][0], cen[0][0], cen[1][0], cen[2][0], up[0][0], up[1][0], up[2][0]);
		}
		
		/**Tell the camera to move forward. I'll make this better at some point.*/
		public void forward(){
			super.forward();
		}
		
		/**Tell the camera to move backwards. I'll make this better at some point.*/
		public void back(){
			super.back();
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