package CaptainCommander;




/**
 * This class defines methods to be inherited by all objects that require rotational orientation.<br>
 * To use the functions within, extend your class with Orientation and store your position and direction values in eye, cen, and up.<br>
 * The move and rotate the object, call the forward, backward, roll, pitch, etc. functions.<br>
 * @author Ian Sohl
 *
 */
abstract class Orientation {
	protected Double [] c = {0.0,0.0,0.0}; //The position of the object's center
	protected Double [][] misc = null;
	private Double [][] omisc = null; //A list of other vectors for the program to track
	private boolean usemisc = false; //Set true if values are stored within misc
	protected Double [] n, v;

	/**
	 * Create a new Orientation object with given scene coordinate Euler rotations
	 * @param roll Roll amount in radians (Around z axis)
	 * @param pitch Pitch around x axis in radians
	 * @param yaw Yaw around y axis in radians
	 */
	Orientation(double roll, double pitch, double yaw){
		v = new Double[]{0.0,0.0,0.0};
        v[0] = Math.sin(yaw)*Math.sin(pitch)*Math.cos(roll) - Math.cos(yaw)*Math.sin(roll);
        v[1] = Math.cos(yaw)*Math.cos(roll) + Math.sin(yaw)*Math.sin(pitch)*Math.sin(roll);
        v[2] = Math.sin(yaw)*Math.cos(pitch);
        n = new Double []{0.0,0.0,0.0};
        n[0] = -Math.sin(yaw)*Math.sin(roll) - Math.cos(yaw)*Math.sin(pitch)*Math.cos(roll);
        n[1] = -Math.cos(yaw)*Math.sin(pitch)*Math.sin(roll) + Math.sin(yaw)*Math.cos(roll);
        n[2] = -Math.cos(yaw)*Math.cos(pitch);
	}
	
	/**
	 * Set Orientation to track an array of vectors in the camera system
	 * @param newmisc Double Array of 3-Vector Arrays
	 */
	protected void setUseMisc(Double [][] newmisc){
		usemisc = true;
		misc = newmisc.clone();
		omisc = misc.clone();
    	for(int i=0; i<misc.length; i++)
    		misc[i]=normalize(misc[i]);
	}

	/**Square a double*/
	protected double sqr(double x){return x*x;}
	
	protected int sign(double x){return x>=0? 1:-1;}

	/**Move the object forward
	 * @param multiplier Amount to move forward by in screen units*/
	protected void forward(double multiplier) {
		c[0]+=n[0]*multiplier;
		c[1]+=n[1]*multiplier;
		c[2]+=n[2]*multiplier;
	}

	/**Same as forward, except in the other direction...
	 * @param multiplier Amount to move backward in screen units*/
	protected void back(double multiplier) {
		c[0]-=n[0]*multiplier;
		c[1]-=n[1]*multiplier;
		c[2]-=n[2]*multiplier;
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

	/**Debugging function to print a vector with a specific name*/
	protected void printVector(Double[] vector, String name){
		System.out.print(""+name+" : <"+vector[0]+","+vector[1]+","+vector[2]+">"+"\n");
	}
	
	/**Debugging function to print a matrix with a specific name*/
	protected void printMatrix(Double [][] matrix, String name){
		System.out.println(name+":");
		for(int i=0; i<matrix.length;i++){
			System.out.print("<");
			for(int j=0; j<matrix[i].length; j++){
				System.out.print(matrix[i][j]+",");
			}
			System.out.println(">");
		}
	}
	
	/**Add two 3-vectors together */
	protected Double [] addVec(Double [] vec1, Double [] vec2){
		return new Double[]{vec1[0]+vec2[0],vec1[1]+vec2[1],vec1[2]+vec2[2]};
	}
	
	/**Subtract two 3-vectors from another*/
	protected Double [] subVec(Double [] vec1, Double [] vec2){
		return new Double[]{vec1[0]-vec2[0],vec1[1]-vec2[1],vec1[2]-vec2[2]};
	}
	
	/**
	 * Returned the squared magnitude of a vector. (No slow square root)
	 * @param vec Column 3-vector
	 */
	protected double sqrmag(Double [] vec){
		return sqr(vec[0])+sqr(vec[1])+sqr(vec[2]);
	}
	
	/**
	 * Returned the absolute magnitude of a vector. (with slow square root)
	 * @param vec Column 3-vector
	 */
	protected double mag(Double [] vec){
		return Math.sqrt(sqr(vec[0])+sqr(vec[1])+sqr(vec[2]));
	}

	/**
	 * Roll the object by a specific angle. This will cause the object to roll along it's translated z-axis.
	 * @param angle Angle to rotate around in radians.
	 */
	protected void pitch(double angle){
		reevaluate(0.0,-angle,0.0); //Re-evaluate all vectors with the new angle
	}


	/**
	 * Pitch the object by a specific angle. This will cause the object to pitch along it's translated x-axis.
	 * @param angle Angle to rotate around in radians.
	 */
	protected void roll(double angle){
		reevaluate(angle,0.0,0.0); //Re-evaluate all vectors with the new angle
	}
	
	protected void yaw(double angle){
		reevaluate(0.0,0.0,angle);
	}

	/**
	 * Multiply all vectors by a specific rotation or translation matrix
	 * @param matrix 3x3 matrix to multiply vectors by
	 */
	protected void multiplyByMatrix(Double [][] matrix){
		Double [] newv = {0.0,0.0,0.0}; //Create a holder for the result
		Double [] newn = {0.0,0.0,0.0}; //Note, holder is NOT identity
		for(int i = 0; i < 3; i++) { //Loop through Columns in the matrix
			for(int k = 0; k < 3; k++) { //Loop through the Rows of the vector
				newv[i] += matrix[i][k] * v[k]; //Sum the matrix components
				newn[i] += matrix[i][k] * n[k]; //For everyone, of course
			}
		}
		v = normalize(newv); //Update the relative angle with the new one
		n = normalize(newn);
	}
	
	protected Double [][] multiplyMatrices(Double [][] m1, Double [][] m2){
		Double [][] output = new Double[][]{{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}};
		for(int i = 0; i < 3; i++) { //Loop through Columns in the matrix
			for(int k = 0; k < 3; k++) { //Loop through the Rows of the vector
				for(int l = 0; l < 3; l++){
					output[i][l] += m1[i][k] * m2[k][l]; //Sum the matrix components
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
	 * Take the cross product of two vectors
	 * @param vector1 First vector to cross
	 * @param vector2 Second vector to cross
	 * @return Normalized cross product from the v1,v2 plane
	 */
	protected Double [] cross(Double [] vector1, Double [] vector2){
		Double [] output = {0.0,0.0,0.0};
		output[0]=vector1[1]*vector2[2]-vector1[2]*vector2[1];
		output[1]=vector1[2]*vector2[0]-vector1[0]*vector2[2];
		output[2]=vector1[0]*vector2[1]-vector1[1]*vector2[0];
		return output;
	}
	
	/**
	 * Transpose a matrix
	 * @param matrix Double [][] to transpose
	 * @return Double [][] transposed matrix
	 */
	private Double [][] transpose(Double [][] matrix) {
	    Double [][] transposedMatrix = new Double[matrix[0].length][matrix.length];
	    for (int i=0;i<matrix.length;i++) {
	        for (int j=0;j<matrix[0].length;j++) {
	            transposedMatrix[j][i] = matrix[i][j];
	        }
	    }
	    return transposedMatrix;
	}
	
	/**
	 * Determinant alternation function
	 * @param i Integer for the count
	 * @return -1 if i is odd, 1 if i is even 
	 */
	private int changeSign(int i){
		return ((i+1)%2)*2-1;
	}
	
	/**
	 * Determinant function that removes a single row and column from the matrix
	 * @param matrix Double [m][n] original matrix
	 * @param excluding_row The row to remove (starting at 0)
	 * @param excluding_col The column to remove
	 * @return Double [m-1][n-1] matrix with with removed rows and columns
	 */
	private Double [][] createSubMatrix(Double [][] matrix, int excluding_row, int excluding_col) {
	    Double [][] mat = new Double[matrix.length-1][matrix[0].length-1];
	    int r = -1;
	    for (int i=0;i<matrix.length;i++) {
	        if (i==excluding_row)
	            continue;
            r++;
            int c = -1;
	        for (int j=0;j<matrix[0].length;j++) {
	            if (j==excluding_col)
	                continue;
	            mat[r][++c] = matrix[i][j];
	        }
	    }
	    return mat;
	} 
	
	/**
	 * Calculate the determinant of a matrix recursively
	 * @param matrix Double [][] This MUST be a square matrix
	 * @return double determinant
	 */
	protected double determinant(Double [][] matrix){
	    double sum = 0.0;
	    for (int i=0; i<matrix.length; i++)
	        sum += changeSign(i) * matrix[0][i] * determinant(createSubMatrix(matrix, 0, i));
	    return sum;
	}
	
	/**
	 * Optimized determinant for 3x3 matrices
	 * @param m Double [3][3] matrix
	 * @return double determinant
	 */
	private double threeDeterminant(Double [][] m){
		return m[0][0]*m[1][1]*m[2][2]+m[0][1]*m[1][2]*m[2][0]+m[0][2]*m[1][0]*m[2][1]-m[0][2]*m[1][1]*m[2][0]-m[0][1]*m[1][0]*m[2][2]-m[0][0]*m[1][2]*m[2][1];
	}
	
	/**
	 * Optimized determinant for 2x2 matrices
	 * @param m Double [2][2] matrix
	 * @return double determinant
	 */
	private double twoDeterminant(Double [][] m){
		double d = m[0][0]*m[1][1]-m[0][1]*m[1][0];
		return d;
	}
	
	/**
	 * Find the cofactor of a matrix
	 * @param matrix Double [][] MUST be a square matrix
	 * @return Double [][] cofactored
	 */
	private Double [][] cofactor(Double [][] matrix){
	    Double [][] mat = new Double[matrix.length][matrix[0].length];
	    for (int i=0;i<matrix.length;i++) {
	        for (int j=0; j<matrix[0].length;j++) {
	            mat[i][j] = changeSign(i) * changeSign(j) * twoDeterminant(createSubMatrix(matrix, i, j));
	        }
	    }
	    return mat;
	}
	
	/**
	 * Find the inverse of a matrix
	 * @param matrix Double [][] MUST be a square matrix
	 * @return Double [][] inverted matrix
	 */
	protected Double [][] inverse(Double [][] matrix){
		Double [][] mat = transpose(cofactor(matrix));
	    double c = 1.0/threeDeterminant(matrix);
	    for (int i=0;i<matrix.length;i++) {
	        for (int j=0; j<matrix[0].length;j++) {
	            mat[i][j] = mat[i][j]*c;
	        }
	    }
	    return mat;
	}

	/**
	 * Recalculate all associated values with this object
	 * @param roll The instantaneous roll of the object
	 * @param pitch The instantaneous pitch of the object
	 */
	private void reevaluate(double roll, double pitch, double yaw){
		Double [] u = cross(v,n);
		Double [] t = {0.0,0.0,0.0};
		t[0] = Math.cos(roll)*v[0] - Math.sin(roll)*u[0];
		t[1] = Math.cos(roll)*v[1] - Math.sin(roll)*u[1];
		t[2] = Math.cos(roll)*v[2] - Math.sin(roll)*u[2];
		v=t.clone();
		t[0] = Math.cos(pitch)*v[0] - Math.sin(pitch)*n[0];
        t[1] = Math.cos(pitch)*v[1] - Math.sin(pitch)*n[1];
        t[2] = Math.cos(pitch)*v[2] - Math.sin(pitch)*n[2];
        n[0] = Math.sin(pitch)*v[0] + Math.cos(pitch)*n[0];
        n[1] = Math.sin(pitch)*v[1] + Math.cos(pitch)*n[1];
        n[2] = Math.sin(pitch)*v[2] + Math.cos(pitch)*n[2];
        v=t.clone();
        u = cross(v,n);
        Double [][] uvn = new Double[][]{u,v,n};
        Double [][] yawMatrix1 = {
        		{1.0,0.0,0.0},
        		{0.0,Math.cos(yaw),-Math.sin(yaw)},
        		{0.0,Math.sin(yaw),Math.cos(yaw)}};
        Double [][] yawMatrix2 = {
        		{1.0,0.0,0.0},
        		{0.0,Math.cos(yaw),-Math.sin(yaw)},
        		{0.0,Math.sin(yaw),Math.cos(yaw)}};
        Double [][] yawMatrix3 = {
        		{Math.cos(yaw),-Math.sin(yaw),0.0},
        		{Math.sin(yaw),Math.cos(yaw),0.0},
        		{0.0,0.0,1.0}};
        uvn = multiplyMatrices(uvn,yawMatrix3);
        v = uvn[1];
        n = uvn[2];
        if(usemisc){
        	Double [][] matrix = inverse(uvn);
        	for(int l = 0; l<misc.length; l++){
        		Double [] tempmisc = {0.0,0.0,0.0};
        		for(int i = 0; i < 3; i++) //Loop through Columns in the matrix
        			for(int k = 0; k < 3; k++) //Loop through the Rows of the vector
						tempmisc[i] += matrix[i][k] * omisc[l][k];
        		misc[l]=tempmisc;
        	}
        }
	}
	
	/**
	 * Convert a screen coordinate vector into camera coordinates
	 * @param vector Unit Vector to transform
	 * @return Unit vector in the camera system
	 */
	protected Double [] convertToCamera(Double [] vector){
		Double [] u = cross(v,n);
    	Double [][] uvn = new Double[][]{u,v,n};
    	Double [][] matrix = inverse(uvn);
		Double [] output = {0.0,0.0,0.0};
		for(int i = 0; i < 3; i++) //Loop through Columns in the matrix
			for(int k = 0; k < 3; k++) //Loop through the Rows of the vector
				output[i] += matrix[i][k] * vector[k];
		return output;
	}
}
