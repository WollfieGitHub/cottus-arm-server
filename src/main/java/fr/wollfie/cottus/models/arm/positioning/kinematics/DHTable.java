package fr.wollfie.cottus.models.arm.positioning.kinematics;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.utils.Preconditions;
import org.ejml.simple.SimpleMatrix;

import static java.lang.Math.*;

/** Implementation of a
 * <a href="https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters">Denavit-Hartenberg Parameters Table</a> 
 * with the four parameters : d, a, alpha, theta 
 */
public class DHTable {
    
    /** d : Offset along the previous z to the common normal */
    @JsonIgnore
    private final double[] d;
    /** theta : Angle about the previous z, from old x to new x */
    @JsonIgnore private final double[] theta0;
    @JsonIgnore private final double[] currTheta;
    /** a : The length of the common normal. Assuming a revolute joint,
     * this is the radius about previous z */
    @JsonIgnore 
    private final double[] a;
    /** alpha : Angle about common normal, from old z to new z axis */
    @JsonIgnore 
    private final double[] alpha;
    
    /** virtual: If the joint exists only to change the referential between two set of joints
     * so that the arm can be modeled by DH parameters */
    @JsonIgnore
    private final boolean[] virtual;
    
    private final int size;
    

    public DHTable(double[] d, double[] a, double[] theta0, double[] alpha, boolean[] virtual) {
        this.size = d.length;
        Preconditions.checkArgument(a.length == size);
        Preconditions.checkArgument(theta0.length == size);
        Preconditions.checkArgument(alpha.length == size);
        Preconditions.checkArgument(virtual.length == size);
        
        this.d = d;
        this.a = a;
        this.theta0 = theta0;
        this.currTheta = new double[this.size];
        this.alpha = alpha;
        this.virtual = virtual;
    }

    /** @return The size of the DH Table, i.e., the number of articulations */
    @JsonGetter("size") public int size() { return this.size; }
    
    public double getD(int i) { return this.d[i]; }
    public double getA(int i) { return this.a[i]; }
    public double getTheta(int i) { return this.theta0[i]+this.currTheta[i]; }
    public double getAlpha(int i) { return this.alpha[i]; }
    public boolean isVirtual(int i) { return this.virtual[i]; }

    /** Because all are revolute join, only theta is able to vary */
    public void setTheta(int i, double value) { this.currTheta[i] = value; }

    /**
     * Return the transformation matrix which transforms 
     * articulation indexed i-1 to i
     * @param i The index of the articulation
     * @return The transformation matrix
     */
    public SimpleMatrix getTransformMatrix(int i) {
        return new SimpleMatrix(new double[][]{
                { cos(getTheta(i)),  -sin(getTheta(i))*cos(alpha[i]),  sin(getTheta(i)*sin(alpha[i])), a[i]*cos(getTheta(i)) },
                { sin(getTheta(i)),   cos(getTheta(i))*cos(alpha[i]), -cos(getTheta(i))*sin(alpha[i]), a[i]*sin(getTheta(i)) },
                {             0,                 sin(alpha[i]),                cos(alpha[i]),               d[i] },
                {             0,                             0,                            0,                  1 }
        });
    }
    
    /**
     * @param from The index of the articulation with the source space
     * @param to The index of the articulation with the destination space
     * @return A transformation matrix that transforms {@code from}'s space into {@code to}'s space
     */
    public SimpleMatrix getTransformMatrix(int from, int to) {
        Preconditions.checkArgument(from <= to);
        SimpleMatrix result = getTransformMatrix(from);
        for (int i = from+1; i <= to; i++) {
            result = result.mult(getTransformMatrix(i));
        }
        return result;
    }
    
    /** Returns a copy of the DH Table */
    public DHTable copy() {
        return new DHTable(d, a, theta0, alpha, virtual);
    }
}
