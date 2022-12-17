package fr.wollfie.cottus.models.arm.positioning.transform;

import fr.wollfie.cottus.dto.JointTransform;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.utils.maths.Vector3D;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

public class DHBasedJointTransform implements JointTransform {
    
    private final DHTable dhTable;
    /** The position this transform occupies in a chain represented by the dh table */
    private final int jointIndex;
    
    public DHBasedJointTransform(DHTable dhTable, int jointIndex) {
        this.dhTable = dhTable;
        this.jointIndex = jointIndex;
    }

    @Override
    public Vector3D transform(Vector3D localPosition) {
        return dhTable.getTransformMatrix(0, jointIndex).multipliedBy(localPosition);
    }

    @Override
    public Vector3D inverseTransform(Vector3D globalPosition) {
        throw new NotImplementedYet();
    }

    @Override
    public void setAngle(double angleRad) { this.dhTable.setTheta(jointIndex, angleRad); }

    @Override
    public double getAngle() { return this.dhTable.getTheta(jointIndex); }
}
