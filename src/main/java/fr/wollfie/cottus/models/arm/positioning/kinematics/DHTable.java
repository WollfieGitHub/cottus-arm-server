package fr.wollfie.cottus.models.arm.positioning.kinematics;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.matrices.HTMatrix;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;

import static java.lang.Math.*;

/** Implementation of a
 * <a href="https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters">Denavit-Hartenberg Parameters Table</a> 
 * with the four parameters : d, a, alpha, theta 
 */
public class DHTable {
    
    /** d : Offset along the previous z to the common normal */
    private final double[] d;
    /** theta : Angle about the previous z, from old x to new x */
    private final double[] theta;
    /** a : The length of the common normal. Assuming a revolute joint,
     * this is the radius about previous z */
    private final double[] a;
    /** alpha : Angle about common normal, from old z to new z axis */
    private final double[] alpha;
    
    private final int size;

    public DHTable(int size, double[] d, double[] theta, double[] a, double[] alpha) {
        Preconditions.checkArgument(d.length == size);
        Preconditions.checkArgument(a.length == size);
        Preconditions.checkArgument(alpha.length == size);
        Preconditions.checkArgument(theta.length == size);
        
        this.d = d;this.theta = theta;this.a = a;this.alpha = alpha;
        this.size = size;
    }

    /** @return The size of the DH Table, i.e., the number of articulations */
    public int size() { return this.size; }
    
    public double getD(int i) { return this.d[i]; }
    public double getA(int i) { return this.a[i]; }
    public double getTheta(int i) { return this.theta[i]; }
    public double getAlpha(int i) { return this.alpha[i]; }

    public void setD(int i, double value) { this.d[i] = value; }
    public void setA(int i, double value) { this.a[i] = value; }
    public void setTheta(int i, double value) { this.theta[i] = value; }
    public void setAlpha(int i, double value) { this.alpha[i] = value; }

    /**
     * Return the transformation matrix which transforms 
     * articulation indexed i-1 to i
     * @param i The index of the articulation
     * @return The transformation matrix
     */
    public HTMatrix getTransformMatrix(int i) {
        return new HTMatrix(new double[][]{
                {               cos(theta[i]),                  -sin(theta[i]),                0,               a[i-1] },
                { cos(alpha[i-1])*sin(theta[i]), cos(alpha[i-1])*cos(theta[i]), -sin(alpha[i-1]), -d[i]*sin(alpha[i-1])},
                { sin(alpha[i-1])*sin(theta[i]), sin(alpha[i-1])*cos(theta[i]),  cos(alpha[i-1]),  d[i]*cos(alpha[i-1])},
                {                           0,                               0,                0,                    1 }
        });
    }

    /**
     * @param from The index of the articulation with the source space
     * @param to The index of the articulation with the destination space
     * @return A transformation matrix that transforms {@code from}'s space into {@code to}'s space
     */
    public HTMatrix getTransformMatrix(int from, int to) {
        Preconditions.checkArgument(from < to);
        HTMatrix result = getTransformMatrix(from);
        for (int i = from+1; i <= to; i++) {
            result = result.multipliedBy(getTransformMatrix(i));
        }
        return result;
    }
    
    /** Returns a copy of the DH Table */
    public DHTable copy() {
        return new DHTable(size, d, theta, a, alpha);
    }
}
