package CaptainCommander;

/**
 * This class defines methods to be inherited by all objects that require rotational orientation.<br>
 * To use the functions within, extend your class with Orientation and store your position and direction values in eye, cen, and up.<br>
 * The move and rotate the object, call the forward, backward, roll, pitch, etc. functions.<br>
 * @author Ian Sohl
 *
 */
abstract class Orientation {
	protected double pitch = 0, roll = 0, yaw = 0; //Defines Euler angles for the object. (Yaw is unused)
	protected Double [][] eye = {{0.0},{0.0},{1.0}}; //The position of the object's center
	protected Double [][] cen = {{0.0},{0.0},{0.0}}; //The normalized position of the object's directional view.
	protected Double [][] up = {{0.0},{1.0},{0.0}}; //The normalized position of the object's upward's vector
	protected Double [][][] misc = null; //A list of other vectors for the program to track
	private Double [][] relcen, relup; //Internalized computational holders
	private Double [][][] relmisc; //Again...
	protected Double [][] temp = {{0.0},{0.0},{0.0}};
	protected boolean usemisc = false; //Set true if values are stored within misc

	Orientation(){
		temp[0][0] = Math.cos(pitch)*Math.cos(roll);
		temp[1][0] = Math.cos(pitch)*Math.sin(roll);
		temp[2][0] = -Math.sin(pitch);
//		cen[0][0] = Math.sin(0)*Math.sin(pitch)*Math.cos(roll) - Math.cos(0)*Math.sin(roll);
//        cen[1][0] = Math.cos(0)*Math.cos(roll) + Math.sin(0)*Math.sin(pitch)*Math.sin(roll);
//        cen[2][0] = Math.sin(0)*Math.cos(pitch);
//        up[0][0] = Math.sin(0)*Math.sin(roll) + Math.cos(0)*Math.sin(pitch)*Math.cos(roll);
//        up[1][0] = Math.cos(0)*Math.sin(pitch)*Math.sin(roll) - Math.sin(0)*Math.cos(roll);
//        up[2][0] = Math.cos(0)*Math.cos(pitch);
	}
	
	/**Square a double*/
	protected double sqr(double x){return x*x;}

	/**Quick method to move the object forward. (Cannibalized from Dr. Bargteil. Mostly.)*/
	protected void forward() {
		double dx = cen[0][0] - eye[0][0], dy = cen[1][0] - eye[1][0], dz = cen[2][0] - eye[2][0];
		double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
//		cen = new Double[][]{{cen[0][0]+dx/(m*100.0)},{cen[1][0]+dy/(m*100.0)},{cen[2][0]+dz/(m*100.0)}};
		eye = new Double[][]{{eye[0][0]+dx/(m*100.0)},{eye[1][0]+dy/(m*100.0)},{eye[2][0]+dz/(m*100.0)}};
	}
	
	/**Same as forward, except in the other direction...*/
	protected void back() {
		double dx = cen[0][0] - eye[0][0], dy = cen[1][0] - eye[1][0], dz = cen[2][0] - eye[2][0];
		double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
//		cen = new Double[][]{{cen[0][0]-dx/(m*100.0)},{cen[1][0]-dy/(m*100.0)},{cen[2][0]-dz/(m*100.0)}};
		eye = new Double[][]{{eye[0][0]-dx/(m*100.0)},{eye[1][0]-dy/(m*100.0)},{eye[2][0]-dz/(m*100.0)}};
	}

	/**
	 * Multiply a unitVector by a constant to create a non-unit vector pointing in the same direction.<br>
	 * Make sure that it is pointing *from* the origin.
	 * @param unitVector 3-vector to be multiplied
	 * @param coefficient Constant to multiply by
	 * @return 3-vector with new values
	 */
	protected Double[][] multiplyUnitVector(Double[][] unitVector, double coefficient){
		return new Double[][]{new Double[]{unitVector[0][0]*coefficient},new Double[]{unitVector[1][0]*coefficient},new Double[]{unitVector[2][0]*coefficient}};
	}

	/**
	 * Multiply a non-origin vector by a constant. Note, must note the location of the basis for the vector (usually 'eye')
	 * @param source The base of the vector to be multiplied
	 * @param vector The actual non-localized vector
	 * @param coefficient Constant to multiply by
	 * @return 3-vector with multiplied values at the source base
	 */
	protected Double[][] findMultiplyVectorfromSource(Double [][] source, Double [][] vector, double coefficient){
		//Translate the vector to the origin.
		Double [][] unitvector = {new Double[]{vector[0][0]-source[0][0]},new Double[]{vector[1][0]-source[1][0]},new Double[]{vector[2][0]-source[2][0]}};
		Double [][] multipliedVector = multiplyUnitVector(unitvector, coefficient); //Multiply the vector
		//ReTranslate back to the original location
		return new Double[][]{new Double[]{multipliedVector[0][0]+source[0][0]},new Double[]{multipliedVector[1][0]+source[1][0]},
				new Double[]{multipliedVector[2][0]+source[2][0]}};
	}

	/**Debugging function to print a vector with a specific name*/
	protected void printVector(Double[][] vector, String name){
		System.out.print(""+name+" : <"+vector[0][0]+","+vector[1][0]+","+vector[2][0]+">"+"\n");
	}

	/**
	 * Roll the object by a specific angle. This will cause the object to roll along it's translated z-axis.
	 * @param angle Angle to rotate around in radians.
	 */
	protected void roll(double angle){
		pitch+=-angle; //Reverse the angle because OpenGL has a horrid coordinate system.
		if(Math.abs(pitch)>10) pitch=(pitch+Math.PI*2)%(Math.PI*2); //If the angle is too far off, move back
		reevaluate(0.0,-angle); //Re-evaluate all vectors with the new angle
	}


	/**
	 * Pitch the object by a specific angle. This will cause the object to pitch along it's translated x-axis.
	 * @param angle Angle to rotate around in radians.
	 */
	protected void pitch(double angle){
		roll+=-angle; //Flip the angle, since OpenGL is stupid.
		if(Math.abs(roll)>10) roll=(roll+Math.PI*2)%(Math.PI*2); //Move the angle back into the realm of mere mortals
		reevaluate(-angle,0.0); //Re-evaluate all vectors with the new angle
	}
	
	/**
	 * Multiply all vectors by a specific rotation or translation matrix
	 * @param matrix 3x3 matrix to multiply vectors by
	 */
	protected void multiplyByMatrix(Double [][] matrix){
		Double [][] newcen1 = new Double[][]{new Double[]{0.0},new Double[]{0.0},new Double[]{0.0}}; //Create a holder for the result
		Double [][] newup1 = new Double[][]{new Double[]{0.0},new Double[]{0.0},new Double[]{0.0}}; //Note, holder is NOT identity
		Double [][][] misc1 = null; //Java suuuucks.
		if(usemisc){
			misc1 = new Double[misc.length][4][1]; //Reeeally sucks.
			for(int i = 0; i < misc.length; i++){ //Loop through all misc items
				misc1[i] = new Double[][]{new Double[]{0.0},new Double[]{0.0},new Double[]{0.0}}; //Create holders for those, too
			}
		}
		for(int i = 0; i < 3; i++) { //Loop through Columns in the matrix
			for(int k = 0; k < 3; k++) { //Loop through the Rows of the vector
				newcen1[i][0] += matrix[i][k] * relcen[k][0]; //Sum the matrix components
				newup1[i][0] += matrix[i][k] * relup[k][0]; //For everyone, of course
				if(usemisc){ //Note: using the relative (translated) angle, not the absolute
					for(int l = 0; l < misc.length; l++){
						misc1[l][i][0] += matrix[i][k] * relmisc[l][k][0];
					}
				}
			}
		}
		relcen = newcen1; //Update the relative angle with the new one
		relup = newup1;
		relmisc = misc1; //Will just write as null if no miscs
	}
	
	/**
	 * Normalize a vector and make it a unit-vector.<br>
	 * <i>Make sure the vector is based on the origin!</i>
	 * @param vector Vector to normalize.
	 * @return Unit-Vector
	 */
	protected Double[][] normalize(Double[][] vector){
		double mag = sqr(vector[0][0])+sqr(vector[1][0])+sqr(vector[2][0]); //Pythagoras!
		if(mag>2){ //Don't normalize it if it isn't too bad
			double norm = Math.sqrt(mag);//The real magnitude, I was lying before...
			vector[0][0]/=norm; //Multiply each vector by the proportion
			vector[1][0]/=norm;
			vector[2][0]/=norm;
		}
		return vector;
	}

	/**
	 * Re-Evaluate all attached vectors to the function with a specific roll or pitch
	 * @param roll The instantaneous roll of the object (Not total roll)
	 * @param pitch Instantaneous pitch of the object (Not total pitch)
	 */
	protected void reevaluate(double roll, double pitch){
		//Translate the vectors to the origin
//		relcen = new Double[][]{new Double[]{cen[0][0]-eye[0][0]},new Double[]{cen[1][0]-eye[1][0]},new Double[]{cen[2][0]-eye[2][0]}};
//		relup = new Double[][]{new Double[]{up[0][0]-eye[0][0]},new Double[]{up[1][0]-eye[1][0]},new Double[]{up[2][0]-eye[2][0]}};
//		if(usemisc){ //Note: This works because all vectors should be based on 'eye'
//			relmisc = new Double[misc.length][][]; //should...
//			for(int i = 0; i < misc.length; i++){
//				relmisc[i] = new Double[][]{new Double[]{misc[i][0][0]-eye[0][0]},new Double[]{misc[i][1][0]-eye[1][0]},new Double[]{misc[i][2][0]-eye[2][0]}};
//			}
//		}
		relcen = cen;
		relup = up;
		relmisc = misc;
		//Please kill me...
		Double [][] rollRotation = { //The rotation matrix for the instantaneous roll
				{1.0,0.0,0.0}, 		 //  This is how much we're actually changing the object
				{0.0,Math.cos(roll),Math.sin(roll)},
				{0.0,-Math.sin(roll),Math.cos(roll)}};
		Double [][] pitchRotation = { //Rotation matrix for the instantaneous pitch
				{Math.cos(pitch),Math.sin(pitch),0.0},
				{-Math.sin(pitch),Math.cos(pitch),0.0},
				{0.0,0.0,1.0}};
		Double [][] inverseExistingRoll = { //Rotation matrix to rotate the object back to the origin for further modifications
				{1.0,0.0,0.0},
				{0.0,Math.cos(-this.roll),Math.sin(-this.roll)},
				{0.0,-Math.sin(-this.roll),Math.cos(-this.roll)}};
		Double [][] existingRoll = { //Rotation matrix to revert the inverse roll and move the object back to it's proper place
				{1.0,0.0,0.0},
				{0.0,Math.cos(this.roll),Math.sin(this.roll)},
				{0.0,-Math.sin(this.roll),Math.cos(this.roll)}};
		Double [][] inverseExistingPitch = { //Inverse matrix for the pitch rotation
				{Math.cos(-this.pitch),Math.sin(-this.pitch),0.0},
				{-Math.sin(-this.pitch),Math.cos(-this.pitch),0.0},
				{0.0,0.0,1.0}};
		Double [][] existingPitch = { //Replacing pitch rotation. Note that this is total pitch, not instantaneous
				{Math.cos(this.pitch),Math.sin(this.pitch),0.0},
				{-Math.sin(this.pitch),Math.cos(this.pitch),0.0},
				{0.0,0.0,1.0}};
		multiplyByMatrix(inverseExistingRoll); //Apply the inverse Total Roll
		multiplyByMatrix(inverseExistingPitch); //Apply the inverse Total Pitch
		multiplyByMatrix(pitchRotation); //Apply the new instantaneous pitch
		multiplyByMatrix(rollRotation); //Apply the new instantaneous rotation (roll)
		multiplyByMatrix(existingPitch); //Revert the total pitch
		multiplyByMatrix(existingRoll); //Revert the total roll
		relcen = normalize(relcen); //Re-normalize the relative variables
		relup = normalize(relup); //Necessary because of computation error? Unsure.
		//Translate the unit vectors back to their normal positions
		cen = new Double[][]{new Double[]{relcen[0][0]+eye[0][0]},new Double[]{relcen[1][0]+eye[1][0]},new Double[]{relcen[2][0]+eye[2][0]}};
		up = new Double[][]{new Double[]{relup[0][0]+eye[0][0]},new Double[]{relup[1][0]+eye[1][0]},new Double[]{relup[2][0]+eye[2][0]}};
		if(usemisc){ //And reapply to the stored class variables
			Double [][][] relmisc2 = new Double[misc.length][][];
			for(int i = 0; i < misc.length; i++){
				relmisc[i] = normalize(relmisc[i]);
				relmisc2[i] = new Double[][]{new Double[]{relmisc[i][0][0]+eye[0][0]},new Double[]{relmisc[i][1][0]+eye[1][0]},new Double[]{relmisc[i][2][0]+eye[2][0]}};
			}
			misc = relmisc2;
		}
	}
	
	protected void reevaluate(double roll, double pitch, boolean disable){
        double tx = Math.cos(roll)*up[0][0] - Math.sin(roll)*temp[0][0];
        double ty = Math.cos(roll)*up[1][0] - Math.sin(roll)*temp[1][0];
        double tz = Math.cos(roll)*up[2][0] - Math.sin(roll)*temp[2][0];
		temp[0][0] = Math.sin(roll)*up[0][0] + Math.cos(roll)*temp[0][0];
		temp[1][0] = Math.sin(roll)*up[1][0] + Math.cos(roll)*temp[1][0];
		temp[2][0] = Math.sin(roll)*up[2][0] + Math.cos(roll)*temp[2][0];
        up[0][0] = tx;
        up[1][0] = ty;
        up[2][0] = tz;
        tx = Math.cos(pitch)*up[0][0] + Math.sin(pitch)*cen[0][0];
        ty = Math.cos(pitch)*up[1][0] + Math.sin(pitch)*cen[1][0];
        tz = Math.cos(pitch)*up[2][0] + Math.sin(pitch)*cen[2][0];
        cen[0][0] = -Math.sin(pitch)*up[0][0] + Math.cos(pitch)*cen[0][0];
        cen[1][0] = -Math.sin(pitch)*up[1][0] + Math.cos(pitch)*cen[1][0];
        cen[2][0] = -Math.sin(pitch)*up[2][0] + Math.cos(pitch)*cen[2][0];
        up[0][0] = tx;
        up[1][0] = ty;
        up[2][0] = tz;
//        eye[0][0] -= cen[0][0];
//        eye[1][0] -= cen[1][0];
//        eye[2][0] -= cen[2][0];
	}
}
