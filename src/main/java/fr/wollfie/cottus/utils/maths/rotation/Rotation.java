package fr.wollfie.cottus.utils.maths.rotation;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.maths.Vector3D;

public class Rotation {

    @JsonProperty("eulerAngles") private final Vector3D eulerAngles;

    public static Rotation Identity = Rotation.from(Vector3D.Zero);

    /**
     * Build a rotation object using euler angles
     * @param eulerAngles The euler angles for the rotation
     */
    private Rotation(
            @JsonProperty("eulerAngles") Vector3D eulerAngles
    ) { this.eulerAngles = eulerAngles; }

    /** @return A new rotation object from euler angles */
    public static Rotation from(Vector3D eulerAngles) { return new Rotation(eulerAngles); }

    /** @return A new rotation object from quaternion */
    public static Rotation from(Quaternion quaternion) { return new Rotation(quaternion.toEulerAngles()); }

    /** @return The rotation as euler angles */
    @JsonGetter("eulerAngles") public Vector3D getEulerAngles() { return this.eulerAngles; }

    @Override
    public String toString() {
        return "Rotation{" +
                "eulerAngles=" + eulerAngles +
                '}';
    }
}
