package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector3D;

/**
 * Homogeneous Transform Matrix
 */
public class HTMatrix extends Matrix
{
    public HTMatrix(double[][] values) {
        super(values);
        Preconditions.checkArgument(values.length == 4);
        Preconditions.checkArgument(values[0].length == 4);
    }
    
    /** @return The position vector of the transform */
    public Vector3D getPosition() {
        return Vector3D.of(get(0, 3), get(1, 3), get(2, 3));
    }
    
    /** Multiplies matrix while keeping the HTMatrix type */
    public HTMatrix multipliedBy(HTMatrix that) {
        return new HTMatrix(super.multipliedBy(that).values);
    }
}
