package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Axis3D;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

public class TranslationMatrix extends Matrix {

    public TranslationMatrix(double[][] values) {
        super(values);
        Preconditions.checkArgument(values.length == 4);
        Preconditions.checkArgument(values[0].length == 4);
    }

    public TranslationMatrix(
            double a00, double a01, double a02, double a03,
            double a10, double a11, double a12, double a13,
            double a20, double a21, double a22, double a23,
            double a30, double a31, double a32, double a33
    ) {
        this(new double[][]{
                {a00, a01, a02, a03}, {a10, a11, a12, a13}, {a20, a21, a22, a23}, {a30, a31, a32, a33}
        });
    }

    /**
     * Creates a new translation matrix which translates the object along the given
     * axis by the specified amount
     * @param axis3D The axis along which the translation occurs
     * @param distance The distance by which the object is translated
     * @return A new translation matrix
     */
    public static TranslationMatrix along(Axis3D axis3D, double distance) {
        return switch (axis3D) {
            case X -> new TranslationMatrix(
                    1, 0, 0, distance,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            );
            case Y -> throw new NotImplementedYet();
            case Z -> new TranslationMatrix(
                    1, 0, 0, 0,
                    0, 1, 1, 0,
                    0, 1, 1, distance,
                    0, 0, 0, 1
            );
        };
    }
}
