package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Axis3D;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import static java.lang.Math.*;

public class RotationMatrix extends Matrix{
    
    protected RotationMatrix(double[][] values) {
        super(values);
        Preconditions.checkArgument(values.length == 4);
        Preconditions.checkArgument(values[0].length == 4);
    }
    
    protected RotationMatrix(
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
     * Creates a 4x4 rotation matrix around the given axis for the given angle in radian 
     * @param axis The axis to produce the rotation around
     * @param angleRad The amount by which the transform rotates the object
     * @return A new rotation matrix
     */
    public static RotationMatrix around(Axis3D axis, double angleRad) {
        return switch (axis) {
            case X -> new RotationMatrix(
                    1, 0, 0, 0,
                    0, cos(angleRad), -sin(angleRad), 0,
                    0, sin(angleRad), cos(angleRad), 0,
                    0, 0, 0, 1
            );
            case Y -> throw new NotImplementedYet();
            case Z -> new RotationMatrix(
                    cos(angleRad), -sin(angleRad), 0, 0,
                    sin(angleRad), cos(angleRad), 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
            );
        };
    }
}
