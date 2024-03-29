package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import org.ejml.simple.SimpleMatrix;

import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.*;
import static java.lang.Math.atan2;

public class MatrixUtil {
    private MatrixUtil() {}
    
    public static boolean isNan(SimpleMatrix matrix) {
        for (int i = 0; i < matrix.numRows(); i++) { for (int j = 0; j < matrix.numCols(); j++) {
            if (Double.isNaN(matrix.get(i, j))) { return true; }
        } }
        return false;
    }
    
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
        return mult(matrix, new Vector(other.x, other.y, other.z)).extract3D();
    }

    public static Vector3D multHt(SimpleMatrix htMatrix, Vector3D other) {
        Vector vector = new Vector(other.x, other.y, other.z, 1);
        Vector res = mult(htMatrix, vector);
        return Vector3D.of(res.get(0), res.get(1), res.get(2));
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
        // https://learnopencv.com/rotation-matrix-to-euler-angles/
        double sy = sqrt(htMatrix.get(0,0) * htMatrix.get(0,0)
                +  htMatrix.get(1,0) * htMatrix.get(1,0));
        
        boolean singular = sy < 1e-6;
        double x,y,z;
        
        if(singular) {
            x = atan2(htMatrix.get(2, 1), htMatrix.get(2, 2));
            y = atan2(-htMatrix.get(2, 0), sy);
            z = atan2(htMatrix.get(1, 0), htMatrix.get(0, 0));
        } else {
            x = atan2(-htMatrix.get(1, 2), htMatrix.get(1, 1));
            y = atan2(-htMatrix.get(2, 0), sy);
            z = 0;
        }
        return Vector3D.of(x,y,z);
    }

    /** @return The translation component of the htMatrix */
    public static Vector3D extractTranslation(SimpleMatrix htMatrix) {
        return multHt(htMatrix, Vector3D.Zero);
    }
    
    /** @return The rotation matrix corresponding to the given rotation */
    public static SimpleMatrix rotationFrom(Vector3D eulerAngles) {
        double c1 = cos(eulerAngles.x), s1 = sin(eulerAngles.x);
        double c2 = cos(eulerAngles.y), s2 = sin(eulerAngles.y);
        double c3 = cos(eulerAngles.z), s3 = sin(eulerAngles.z);
        
        return new SimpleMatrix(new double[][] {
                {          c2*c3,         -c2*s3,     s2 },
                { c1*s3+c3*s1*s2, c1*c3-s1*s2*s3, -c2*s1 },
                { s1*s3-c1*c3*s2, c3*s1+c1*s2*s3,  c1*c2 },
        });
    }
}
