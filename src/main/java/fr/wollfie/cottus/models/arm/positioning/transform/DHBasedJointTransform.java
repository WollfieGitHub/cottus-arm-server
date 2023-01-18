package fr.wollfie.cottus.models.arm.positioning.transform;

import fr.wollfie.cottus.dto.JointTransform;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

/**
 * Transform based on the arm's {@link DHTable}
 */
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
        return MatrixUtil.multHt(dhTable.getTransformMatrix(0, jointIndex), localPosition);
    }

    @Override
    public Vector3D inverseTransform(Vector3D globalPosition) { throw new NotImplementedYet(); }

    @Override
    public void setAngle(double angleRad) { this.dhTable.setVarTheta(jointIndex, angleRad); }

    @Override
    public double getAngle() { return this.dhTable.getVarTheta(jointIndex); }
}
