package fr.wollfie.cottus.models.arm.positioning;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public class InverseKinematics {
    // Static class, cannot be instantiated
    private InverseKinematics() {}

    /**
     * TODO: 12/8/2022 DOCUMENT THIS FUNCTION 
     * @param arm
     * @param endEffectorPosition
     * @param endEffectorOrientation
     * @return
     */
    public static double[] solve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorOrientation
    ) {
        return new double[0];
    }
}
