package fr.wollfie.cottus.utils.maths.rotation;

import fr.wollfie.cottus.utils.maths.Vector3D;

public interface Quaternion {

    /** @return The rotation in euler angles */
    Vector3D toEulerAngles();
}
