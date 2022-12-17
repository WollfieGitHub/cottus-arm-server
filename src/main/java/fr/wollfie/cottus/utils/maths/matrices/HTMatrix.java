package fr.wollfie.cottus.utils.maths.matrices;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.Utils;
import fr.wollfie.cottus.utils.maths.Vector3D;

import static java.lang.Math.*;

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

    /** @return The 3D vector resulting of the multiplication of {@code this} by {@code that} vector 
     * @apiNote Can only be 3D Vector out, so the matrix should be 3x3 
     */
    public Vector3D multipliedBy(Vector3D that) {
        Preconditions.checkNotNull(that);

        double x = values[0][0]*that.x + values[0][1]*that.y + values[0][2]*that.z + values[0][3];
        double y = values[1][0]*that.x + values[1][1]*that.y + values[1][2]*that.z + values[1][3];
        double z = values[2][0]*that.x + values[2][1]*that.y + values[2][2]*that.z + values[2][3];
        double w = values[3][0]*that.x + values[3][1]*that.y + values[3][2]*that.z + values[3][3];
        
        if (Utils.isZero(w)) { return Vector3D.Zero(); }
        return Vector3D.of(x/w, y/w, z/w);
    }
    
    /** @return The translation vector from the Transform matrix */
    public Vector3D extractTranslation() {
        return multipliedBy(Vector3D.Zero());
    }
    
    /** @return The euler angles from the Transform matrix
     * @implNote : From <a href="https://gamedev.stackexchange.com/questions/50963/how-to-extract-euler-angles-from-transformation-matrix">
     *      StackOverflow
     *     </a> */
    public Vector3D extractRotation() {
        double x = atan2(get(1,2), get(2,2));
        double cosX = cos(x); double sinX = sin(x);
        double cosY = sqrt(1-get(0,2));
        double y = atan2(get(0,2), cosY);
        double sinZ = cosX*get(1,0) + sinX*get(2,0);
        double cosZ = cosX*get(1,1) + sinX*get(2,1);
        double z = atan2(cosZ, sinZ);
        return Vector3D.of(x,y,z);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < values[0].length; j++) {
                stringBuilder.append(String.format("%4.2f", values[i][j]));
                stringBuilder.append(", ");
            }
            stringBuilder.append("\n");
        }
        
        return String.format("HTMatrix{\n%s}", stringBuilder.toString());
    }
}
