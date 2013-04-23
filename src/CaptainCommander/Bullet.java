package CaptainCommander;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import com.jogamp.opengl.util.gl2.GLUT;

public class Bullet extends Orientation{
	public float [] color;
	public double velocity;
	private Double [] start;
	public double range = 100.0;
	public boolean alive = true;
	public Bullet(Double [] c, Double [] v, Double [] n, float [] color, double velocity){
		super(0.0,0.0,0.0);
		this.c=c;
		this.v=v;
		this.n=n;
		this.velocity = velocity;
		this.color = color;
		start=c.clone();
	}
	
	public void update(ArrayList<Ship> ships){
		forward(velocity);
		if(sqrmag(subVec(c,start))>sqr(range)){
			alive=false;
		}
		else{
			//Loop through all ships, determine if colliding
			Iterator<Ship> siter = ships.iterator();
			while(siter.hasNext()){
				Ship ship = siter.next();
				if(sqrmag(subVec(ship.c,this.c))<sqr(ship.minorLength)){
					ship.dead=true;
				}
			}
		}
	}
	
	public void display(GLAutoDrawable gld, GLUT glut){
		final GL2 gl = gld.getGL().getGL2(); //Fetch the drawing environment from the gld
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, 1.0f); //Make it shiny!
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE, color, 0); //And Blue...
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, color, 0); //Shiny blue
        gl.glPushMatrix();
        gl.glTranslated(c[0], c[1], c[2]);
        glut.glutSolidSphere(0.05, 16, 16);
        gl.glPopMatrix();
	}
}
