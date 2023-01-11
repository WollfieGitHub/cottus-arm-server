package fr.wollfie.cottus.utils.maths.rotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import org.ejml.simple.SimpleMatrix;

public class Rotation {

    public static Rotation Identity = Rotation.from(Vector3D.Zero);
    
    private final SimpleMatrix rotationMatrix;
    
    /** @return The rotation as a rotation matrix */
    @JsonGetter("rotationMatrix") public SimpleMatrix getMatrix() { return this.rotationMatrix; }

    /**
     * Build a rotation object using a rotation matrix
     * @param rotationMatrix The rotation matrix for this rotation
     */
    private Rotation(SimpleMatrix rotationMatrix) { this.rotationMatrix = rotationMatrix; }

    /** @return A new rotation object from euler angles */
    @JsonCreator
    public static Rotation from(
            @JsonProperty("eulerAngles") Vector3D eulerAngles
    ) { return new Rotation(MatrixUtil.rotationFrom(eulerAngles)); }

    /** @return A new rotation object from a rotation matrix */
    public static Rotation from(SimpleMatrix rotationMatrix) { return new Rotation(rotationMatrix); }

    /** @return A new rotation object from quaternion */
    public static Rotation from(Quaternion quaternion) { return from(quaternion.toEulerAngles()); }
    
    @Override
    public String toString() {
        return "Rotation{" + rotationMatrix + '}';
    }

    /** @return The rotation resulting of {@code this} rotation composed with {@code that} rotation */
    public Rotation plus(Rotation that) {
        return Rotation.from(this.rotationMatrix.mult(that.rotationMatrix));
    }

    /** @return The inverse rotation of this rotation */
    public Rotation inverted() {
        return Rotation.from(this.rotationMatrix.transpose());
    }
}
