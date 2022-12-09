package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector3D;

public class Matrix {
    
    protected final double[][] values;

    /** Constructor for all values without specific dimensions*/
    public Matrix(double[][] values) { this.values = values; }
    
    /** Constructor for 3x3 matrix */
    public Matrix(
            double a00, double a01, double a02,
            double a10, double a11, double a12,
            double a20, double a21, double a22
    ) {
        this(new double[][]{ {a00, a01, a02}, {a10, a11, a12}, {a20, a21, a22} });
    }
    
    /** @return The coefficient at line {@code i} and column {@code j} */
    public double get(int i, int j) { return this.values[i][j]; }
    
    /** @return The width of the matrix */
    public int getWidth() { return getHeight() == 0 ? 0 : this.values[0].length; }
    /** @return The height of the matrix */
    public int getHeight() { return this.values.length; }
    
    /** @return The matrix resulting of the multiplication of {@code this} by {@code that} matrix */
    public Matrix multipliedBy(Matrix that) {
        Preconditions.checkNotNull(that);
        Preconditions.checkArgument(this.getWidth() == that.getHeight());
        double[][] result = new double[this.getHeight()][that.getWidth()];

        for (int j = 0; j < that.getWidth(); j++) {
            for (int i = 0; i < this.getHeight(); i++) {
                double coeffIJ = 0;
                for (int k = 0; k < this.getWidth(); k++) {
                    coeffIJ += this.values[i][k] * that.values[k][j];
                }
                result[i][j] = coeffIJ;
            }
        }
        return new Matrix(result);
    }
    
    /** @return The 3D vector resulting of the multiplication of {@code this} by {@code that} vector 
     * @apiNote Can only be 3D Vector out, so the matrix should be 3x3 
     */
    public Vector3D multipliedBy(Vector3D that) {
        Preconditions.checkNotNull(that);
        Preconditions.checkArgument(this.getWidth() == 3);
        Preconditions.checkArgument(this.getHeight() == 3);
        
        double x = values[0][0]*that.x + values[0][1]*that.y + values[0][1]*that.x;
        double y = values[1][0]*that.x + values[1][1]*that.y + values[1][1]*that.x;
        double z = values[2][0]*that.x + values[2][1]*that.y + values[2][1]*that.x;
        return Vector3D.of(x, y, z);
    }
}
