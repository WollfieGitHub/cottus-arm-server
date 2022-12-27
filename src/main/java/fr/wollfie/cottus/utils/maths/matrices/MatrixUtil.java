package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import org.ejml.simple.SimpleMatrix;

import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.*;
import static java.lang.Math.atan2;

public class MatrixUtil {
    private MatrixUtil() {}
    
    public static Vector mult(SimpleMatrix matrix, Vector other) {
        Preconditions.checkArgument(matrix.numCols() == other.dim);
        double[] values = new double[matrix.numRows()];
        for (int i = 0; i < matrix.numRows(); i++) {
            double c = 0.0;
            for (int j = 0; j < matrix.numCols(); j++) { c += matrix.get(i, j) * other.get(j); }
            values[i] = c;
        }
        return new Vector(values);
    }

    public static Vector3D mult(SimpleMatrix matrix, Vector3D other) {
        return mult(matrix, new Vector(other.x, other.y, other.z)).to3D();
    }

    public static Vector3D multHt(SimpleMatrix htMatrix, Vector3D other) {
        Vector vector = new Vector(other.x, other.y, other.z, 1);
        Vector res = mult(htMatrix, vector);
        return Vector3D.of(res.get(0)/res.get(3), res.get(1)/res.get(3), res.get(2)/res.get(3));
    }

    /**
     * Applies an operation on all coefficient of the matrix
     * @param operator The operation to apply
     */
    public static SimpleMatrix apply(SimpleMatrix matrix, DoubleUnaryOperator operator) {
        double[][] values = new double[matrix.numRows()][matrix.numCols()];
        for (int i = 0; i < matrix.numRows(); i++) { for (int j = 0; j < matrix.numCols(); j++) {
            values[i][j] = operator.applyAsDouble(matrix.get(i,j));
        } }
        return new SimpleMatrix(values);
    }
    
    /** @return The matrix which's columns are the vectors given as argument */
    public static SimpleMatrix from(Vector[] columns) {
        // A cost for dimension here would be too costly
        int width = columns.length;
        int height = columns[0].dim;
        double[][] values = new double[height][width];
        for (int i = 0; i < height; i++) { for (int j = 0; j < width; j++) {
            values[i][j] = columns[j].get(i);
        } }
        return new SimpleMatrix(values);
    }
    
    /** @return The rotation component of the htMatrix */
    public static Vector3D extractRotation(SimpleMatrix htMatrix) {
        double x = atan2(htMatrix.get(1,2), htMatrix.get(2,2));
        double cosX = cos(x); double sinX = sin(x);
        double cosY = sqrt(1-htMatrix.get(0,2));
        double y = atan2(htMatrix.get(0,2), cosY);
        double sinZ = cosX*htMatrix.get(1,0) + sinX*htMatrix.get(2,0);
        double cosZ = cosX*htMatrix.get(1,1) + sinX*htMatrix.get(2,1);
        double z = atan2(cosZ, sinZ);
        return Vector3D.of(x,y,z);
    }

    /** @return The translation component of the htMatrix */
    public static Vector3D extractTranslation(SimpleMatrix htMatrix) {
        return multHt(htMatrix, Vector3D.Zero);
    }
}
