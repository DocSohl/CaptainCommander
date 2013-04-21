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
	protected Double [] eye = {0.0,0.0,1.0}; //The position of the object's center
	protected Double [] cen = {0.0,0.0,0.0}; //The normalized position of the object's directional view.
	protected Double [] up = {0.0,1.0,0.0}; //The normalized position of the object's upward's vector
	protected Double [][] misc = null; //A list of other vectors for the program to track
	private Double [] relcen, relup; //Internalized computational holders
	private Double [][] relmisc; //Again...
	protected Double [] temp = {0.0,0.0,0.0};
	protected boolean usemisc = false; //Set true if values are stored within misc
	double xx,xy,xz,yx,yy,yz,zx,zy,zz;

	Orientation(){
		pitch=Math.PI;
		xx = Math.cos(pitch)*Math.cos(roll);
        xy = Math.cos(pitch)*Math.sin(roll);
        xz = -Math.sin(pitch);
        yx = Math.sin(0.0)*Math.sin(pitch)*Math.cos(roll) - Math.cos(0.0)*Math.sin(roll);
        yy = Math.cos(0.0)*Math.cos(roll) + Math.sin(0.0)*Math.sin(pitch)*Math.sin(roll);
        yz = Math.sin(0.0)*Math.cos(pitch);
        zx = Math.sin(0.0)*Math.sin(roll) + Math.cos(0.0)*Math.sin(pitch)*Math.cos(roll);
        zy = Math.cos(0.0)*Math.sin(pitch)*Math.sin(roll) - Math.sin(0.0)*Math.cos(roll);
        zz = Math.cos(0.0)*Math.cos(pitch);
		temp[0] = Math.cos(pitch)*Math.cos(roll);
		temp[1] = Math.cos(pitch)*Math.sin(roll);
		temp[2] = -Math.sin(pitch);
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
//		double dx = cen[0] - eye[0], dy = cen[1] - eye[1], dz = cen[2] - eye[2];
//		double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
//		//		cen = new Double[][]{{cen[0][0]+dx/(m*100.0)},{cen[1][0]+dy/(m*100.0)},{cen[2][0]+dz/(m*100.0)}};
//		eye = new Double[]{eye[0]+dx/(m*100.0),eye[1]+dy/(m*100.0),eye[2]+dz/(m*100.0)};
		eye[0]-=zx*0.03;
		eye[1]-=zy*0.03;
		eye[2]-=zz*0.03;
	}

	/**Same as forward, except in the other direction...*/
	protected void back() {
//		double dx = cen[0] - eye[0], dy = cen[1] - eye[1], dz = cen[2] - eye[2];
//		double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
//		//		cen = new Double[][]{{cen[0][0]-dx/(m*100.0)},{cen[1][0]-dy/(m*100.0)},{cen[2][0]-dz/(m*100.0)}};
//		eye = new Double[]{eye[0]-dx/(m*100.0),eye[1]-dy/(m*100.0),eye[2]-dz/(m*100.0)};
		eye[0]+=zx*0.03;
		eye[1]+=zy*0.03;
		eye[2]+=zz*0.03;
	}

	/**
	 * Multiply a unitVector by a constant to create a non-unit vector pointing in the same direction.<br>
	 * Make sure that it is pointing *from* the origin.
	 * @param unitVector 3-vector to be multiplied
	 * @param coefficient Constant to multiply by
	 * @return 3-vector with new values
	 */
	protected Double[] multiplyUnitVector(Double[] unitVector, double coefficient){
		return new Double[]{unitVector[0]*coefficient,unitVector[1]*coefficient,unitVector[2]*coefficient};
	}

	/**
	 * Multiply a non-origin vector by a constant. Note, must note the location of the basis for the vector (usually 'eye')
	 * @param source The base of the vector to be multiplied
	 * @param vector The actual non-localized vector
	 * @param coefficient Constant to multiply by
	 * @return 3-vector with multiplied values at the source base
	 */
	protected Double[] findMultiplyVectorfromSource(Double [] source, Double [] vector, double coefficient){
		//Translate the vector to the origin.
		Double [] unitvector = {vector[0]-source[0],vector[1]-source[1],vector[2]-source[2]};
		Double [] multipliedVector = multiplyUnitVector(unitvector, coefficient); //Multiply the vector
		//ReTranslate back to the original location
		return new Double[]{multipliedVector[0]+source[0],multipliedVector[1]+source[1],multipliedVector[2]+source[2]};
	}

	/**Debugging function to print a vector with a specific name*/
	protected void printVector(Double[] vector, String name){
		System.out.print(""+name+" : <"+vector[0]+","+vector[1]+","+vector[2]+">"+"\n");
	}

	/**
	 * Roll the object by a specific angle. This will cause the object to roll along it's translated z-axis.
	 * @param angle Angle to rotate around in radians.
	 */
	protected void pitch(double angle){
		reevaluate(0.0,-angle); //Re-evaluate all vectors with the new angle
		pitch+=-angle; //Reverse the angle because OpenGL has a horrid coordinate system.
		pitch=(pitch+Math.PI*2)%(Math.PI*2); //If the angle is too far off, move back
	}


	/**
	 * Pitch the object by a specific angle. This will cause the object to pitch along it's translated x-axis.
	 * @param angle Angle to rotate around in radians.
	 */
	protected void roll(double angle){
		reevaluate(-angle,0.0); //Re-evaluate all vectors with the new angle
		roll+=-angle; //Flip the angle, since OpenGL is stupid.
		roll=(roll+Math.PI*2)%(Math.PI*2); //Move the angle back into the realm of mere mortals
	}

	/**
	 * Multiply all vectors by a specific rotation or translation matrix
	 * @param matrix 3x3 matrix to multiply vectors by
	 */
	protected void multiplyByMatrix(Double [][] matrix){
		Double [] newcen1 = {0.0,0.0,0.0}; //Create a holder for the result
		Double [] newup1 = {0.0,0.0,0.0}; //Note, holder is NOT identity
		Double [][] misc1 = null; //Java suuuucks.
		if(usemisc){
			misc1 = new Double[misc.length][4]; //Reeeally sucks.
			for(int i = 0; i < misc.length; i++){ //Loop through all misc items
				misc1[i] = new Double[]{0.0,0.0,0.0}; //Create holders for those, too
			}
		}
		for(int i = 0; i < 3; i++) { //Loop through Columns in the matrix
			for(int k = 0; k < 3; k++) { //Loop through the Rows of the vector
				newcen1[i] += matrix[i][k] * relcen[k]; //Sum the matrix components
				newup1[i] += matrix[i][k] * relup[k]; //For everyone, of course
				if(usemisc){ //Note: using the relative (translated) angle, not the absolute
					for(int l = 0; l < misc.length; l++){
						misc1[l][i] += matrix[i][k] * relmisc[l][k];
					}
				}
			}
		}
		relcen = normalize(newcen1); //Update the relative angle with the new one
		relup = normalize(newup1);
		if(usemisc){
			for(int i = 0; i<misc.length; i++){
				relmisc[i] = normalize(misc1[i]);
			}
		}
	}
	
	protected Double [][] multiplyMatrices(Double [][] matrix1, Double [][] matrix2){
		Double [][] output = {{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}};
		for(int i = 0; i < 3; i++) { // aRow
		    for(int j = 0; j < 3; j++) { // bColumn
		      for(int k = 0; k < 3; k++) { // aColumn
		        output[i][j] += matrix1[i][k] * matrix2[k][j];
		      }
		    }  
		  }
		return output;
	}

	/**
	 * Normalize a vector and make it a unit-vector.<br>
	 * <i>Make sure the vector is based on the origin!</i>
	 * @param vector Vector to normalize.
	 * @return Unit-Vector
	 */
	protected Double[] normalize(Double[] vector){
		double mag = sqr(vector[0])+sqr(vector[1])+sqr(vector[2]); //Pythagoras!
		if(mag!=1){ //Don't normalize it if it isn't too bad
			double norm = Math.sqrt(mag);//The real magnitude, I was lying before...
			vector[0]/=norm; //Multiply each vector by the proportion
			vector[1]/=norm;
			vector[2]/=norm;
		}
		return vector;
	}
	
	
	/**
	 * Re-Evaluate all attached vectors to the function with a specific roll or pitch
	 * @param roll The instantaneous roll of the object (Not total roll)
	 * @param pitch Instantaneous pitch of the object (Not total pitch)
	 */
	protected void reevaluate(double roll, double pitch, boolean disabled){
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
//		relcen = new Double[]{0.0,0.0,1.0};
//		relup = new Double[]{0.0,1.0,0.0};
//		double x=Math.sqrt(2)/2;
//		relmisc = new Double[][]{ 
//				new Double[]{-x,-x,0.0},
//				new Double[]{x,-x,0.0},
//				new Double[]{x,x,0.0},
//				new Double[]{-x,x,0.0}};
		//Please kill me...
		this.yaw = -Math.atan2(cen[0],cen[2]);
		System.out.println(this.yaw);
		Double [][] pitchRotation = { //The rotation matrix for the instantaneous roll
				{1.0,0.0,0.0}, 		 //  This is how much we're actually changing the object
				{0.0,Math.cos(pitch),Math.sin(pitch)},
				{0.0,-Math.sin(pitch),Math.cos(pitch)}};
		Double [][] rollRotation = { //Rotation matrix for the instantaneous pitch
				{Math.cos(roll),Math.sin(roll),0.0},
				{-Math.sin(roll),Math.cos(roll),0.0},
				{0.0,0.0,1.0}};
		Double [][] yawRotation = {
				{Math.cos(yaw),0.0,-Math.sin(yaw)},
				{0.0,1.0,0.0},
				{Math.sin(yaw),0.0,Math.cos(yaw)}};
		Double [][] combinedPitchRoll = {
				{Math.cos(roll), Math.sin(roll), 0.0}, 
				{-Math.cos(pitch)*Math.sin(roll), Math.cos(pitch)*Math.cos(roll), Math.sin(pitch)}, 
				{Math.sin(pitch)*Math.sin(roll), -Math.cos(roll)*Math.sin(pitch), Math.cos(pitch)}};
		Double [][] combinedRollPitch = {
				{Math.cos(roll), Math.cos(pitch)*Math.sin(roll), Math.sin(pitch)*Math.sin(roll)}, 
				{-Math.sin(roll), Math.cos(pitch)*Math.cos(roll), Math.cos(roll)*Math.sin(pitch)}, 
				{0.0, -Math.sin(pitch), Math.cos(pitch)}};
		Double [][] inverseExistingPitch = { //Rotation matrix to rotate the object back to the origin for further modifications
				{1.0,0.0,0.0},
				{0.0,Math.cos(-this.pitch),Math.sin(-this.pitch)},
				{0.0,-Math.sin(-this.pitch),Math.cos(-this.pitch)}};
		Double [][] existingPitch = { //Rotation matrix to revert the inverse roll and move the object back to it's proper place
				{1.0,0.0,0.0},
				{0.0,Math.cos(this.pitch),Math.sin(this.pitch)},
				{0.0,-Math.sin(this.pitch),Math.cos(this.pitch)}};
		Double [][] inverseExistingRoll = { //Inverse matrix for the pitch rotation
				{Math.cos(-this.roll),Math.sin(-this.roll),0.0},
				{-Math.sin(-this.roll),Math.cos(-this.roll),0.0},
				{0.0,0.0,1.0}};
		Double [][] existingRoll = { //Replacing pitch rotation. Note that this is total pitch, not instantaneous
				{Math.cos(this.roll),Math.sin(this.roll),0.0},
				{-Math.sin(this.roll),Math.cos(this.roll),0.0},
				{0.0,0.0,1.0}};
		Double [][] inverseExistingYaw = {
				{Math.cos(-this.yaw),0.0,-Math.sin(-this.yaw)},
				{0.0,1.0,0.0},
				{Math.sin(-this.yaw),0.0,Math.cos(-this.yaw)}};
		Double [][] existingYaw = {
				{Math.cos(this.yaw),0.0,-Math.sin(this.yaw)},
				{0.0,1.0,0.0},
				{Math.sin(this.yaw),0.0,Math.cos(this.yaw)}};
		Double [][] inversetotal = {
				{Math.cos(-this.yaw)*Math.cos(pitch),Math.cos(-this.roll)*Math.sin(-this.pitch)+Math.sin(-this.roll)*Math.sin(-this.yaw)*Math.cos(-this.pitch),Math.sin(-this.roll)*Math.sin(-this.pitch)-Math.cos(-this.roll)*Math.sin(-this.yaw)*Math.cos(-this.pitch)},
				{-Math.cos(-this.yaw)*Math.sin(-this.pitch),Math.cos(-this.roll)*Math.cos(-this.pitch)-Math.sin(-this.roll)*Math.sin(-this.yaw)*Math.sin(-this.pitch),Math.sin(-this.roll)*Math.cos(-this.pitch)+Math.cos(-this.roll)*Math.sin(-this.yaw)*Math.sin(-this.pitch)},
				{Math.sin(-this.yaw),-Math.sin(-this.roll)*Math.cos(-this.yaw),Math.cos(-this.roll)*Math.cos(-this.yaw)}};
		Double [][] combined = {
				{Math.cos(yaw)*Math.cos(pitch),Math.cos(roll)*Math.sin(pitch)+Math.sin(roll)*Math.sin(yaw)*Math.cos(pitch),Math.sin(roll)*Math.sin(pitch)-Math.cos(roll)*Math.sin(yaw)*Math.cos(pitch)},
				{-Math.cos(yaw)*Math.sin(pitch),Math.cos(roll)*Math.cos(pitch)-Math.sin(roll)*Math.sin(yaw)*Math.sin(pitch),Math.sin(roll)*Math.cos(pitch)+Math.cos(roll)*Math.sin(yaw)*Math.sin(pitch)},
				{Math.sin(yaw),-Math.sin(roll)*Math.cos(yaw),Math.cos(roll)*Math.cos(yaw)}};
		Double [][] reversetotal = {
				{Math.cos(this.yaw)*Math.cos(this.pitch),Math.cos(this.roll)*Math.sin(this.pitch)+Math.sin(this.roll)*Math.sin(this.yaw)*Math.cos(this.pitch),Math.sin(this.roll)*Math.sin(this.pitch)-Math.cos(this.roll)*Math.sin(this.yaw)*Math.cos(this.pitch)},
				{-Math.cos(this.yaw)*Math.sin(this.pitch),Math.cos(this.roll)*Math.cos(this.pitch)-Math.sin(this.roll)*Math.sin(this.yaw)*Math.sin(this.pitch),Math.sin(this.roll)*Math.cos(this.pitch)+Math.cos(this.roll)*Math.sin(this.yaw)*Math.sin(this.pitch)},
				{Math.sin(this.yaw),-Math.sin(this.roll)*Math.cos(this.yaw),Math.cos(this.roll)*Math.cos(this.yaw)}};
//		multiplyByMatrix(inverseExistingRoll); //Apply the inverse Total Roll
//		multiplyByMatrix(inverseExistingPitch); //Apply the inverse Total Pitch
//		multiplyByMatrix(inverseExistingYaw);
//		multiplyByMatrix(rollRotation); //Apply the new instantaneous rotation (roll)
//		multiplyByMatrix(pitchRotation); //Apply the new instantaneous pitch
//		multiplyByMatrix(yawRotation);
//		multiplyByMatrix(combinedRollPitch);
//		multiplyByMatrix(existingYaw);
//		multiplyByMatrix(existingPitch); //Revert the total pitch
//		multiplyByMatrix(existingRoll); //Revert the total roll
		multiplyByMatrix(inverseExistingRoll);
		multiplyByMatrix(inverseExistingYaw);
//		multiplyByMatrix(inverseExistingPitch);
		multiplyByMatrix(multiplyMatrices(pitchRotation,rollRotation));
//		multiplyByMatrix(existingPitch);
		multiplyByMatrix(existingYaw);
		multiplyByMatrix(existingRoll);
//		System.out.println(this.roll);
//		printVector(up,"up");
//		multiplyByMatrix(inversetotal);
//		multiplyByMatrix(combined);
//		multiplyByMatrix(reversetotal);
		relcen = normalize(relcen); //Re-normalize the relative variables
		relup = normalize(relup); //Necessary because of computation error? Unsure.
		//Translate the unit vectors back to their normal positions
		cen = relcen;
		up = relup;
		if(usemisc){
			misc = relmisc;
		}
	}

	protected void reevaluate(double roll, double pitch){
		double tx = Math.cos(roll)*yx - Math.sin(roll)*xx;
        double ty = Math.cos(roll)*yy - Math.sin(roll)*xy;
        double tz = Math.cos(roll)*yz - Math.sin(roll)*xz;
        xx = Math.sin(roll)*yx + Math.cos(roll)*xx;
        xy = Math.sin(roll)*yy + Math.cos(roll)*xy;
        xz = Math.sin(roll)*yz + Math.cos(roll)*xz;
        yx = tx;
        yy = ty;
        yz = tz;
        tx = Math.cos(pitch)*yx + Math.sin(pitch)*zx;
        ty = Math.cos(pitch)*yy + Math.sin(pitch)*zy;
        tz = Math.cos(pitch)*yz + Math.sin(pitch)*zz;
        zx = -Math.sin(pitch)*yx + Math.cos(pitch)*zx;
        zy = -Math.sin(pitch)*yy + Math.cos(pitch)*zy;
        zz = -Math.sin(pitch)*yz + Math.cos(pitch)*zz;
        yx = tx;
        yy = ty;
        yz = tz;
	}
}
