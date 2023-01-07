package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;

import javax.xml.crypto.dsig.keyinfo.PGPData;
import java.util.List;

/**
 * The arm represented as an object.
 * 
 * It has a list of {@link Joint} where
 * the first articulation will be the root of the arm 
 * and the last element will be the end effector.
 */
public interface CottusArm {

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       FIND JOINTS                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /** @return The articulations of the arm */
    @JsonGetter("joints")
    List<Joint> joints();
    
    /** @return The root of the arm, the first articulation */
    @JsonIgnore
    default Joint getRoot() { return joints().get(0); }
    
    /** @return The elbow of the arm, the third articulation from the root.
     * Because it has 3 degrees of freedom before it, its position can be set using I.K. */
    @JsonIgnore
    default Joint getElbow() { return joints().get(3); }
    
    /** @return The end effector of the arm, the last articulation,
     * because it has 6 degrees of freedom before it, its position and rotation 
     * can be set using I.K.*/
    @JsonIgnore
    default Joint getEndEffector() { 
        return joints().get(joints().size()-1);
    }

    /**
     * The joint corresponding to the given index. Joints are indexed from 0 (base) to n (end-effector).
     * @param jointIndex The index of the joint to return
     * @param includeVirtual Specify if indexing should take into account virtual joints
     * @return A joint 
     */
    default Joint getJoint(int jointIndex, boolean includeVirtual) {
        return joints().stream()
                .filter(joint -> includeVirtual || !joint.isVirtual())
                .toList().get(jointIndex);
    }

    /** @return The joint corresponding to the given index. Joints are indexed from 0 (base) to n (end-effector) */
    default Joint getJoint(int jointIndex) { return getJoint(jointIndex, true); }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       GENERAL ARM INFO                               ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /** @return The reach of the arm in millimeters 
     * @implNote Works only for this arm */
    @JsonIgnore
    default double getReachMm() {
        double length = 0;
        for (int i = 0; i < getNbOfJoints(); i++) {
            length += dhTable().getA(i) + dhTable().getD(i);
        }
        return length;
    }
    
    /** @return The DH Parameters Table of the arm, used for inverse and forward kinematics */
    @JsonIgnore
    DHTable dhTable();
    
    /** @return The number of articulations (virtual joints included) */
    @JsonGetter("nbJoints")
    default int getNbOfJoints() { return joints().size(); }

    /** @return The number of articulations, i.e., Degrees of freedom */
    @JsonGetter("nbNonVirtualJoints")
    default int getNbOfNonVirtualJoints() {
        return joints().stream().filter(joint -> !joint.isVirtual()).toList().size();
    }

    /**
     * Finds the articulation in this arm with the specified name
     * @param articulationName The name of the articulation to find
     * @return The actual articulation with the given name
     */
    default Joint getJointUsing(String articulationName) {
        return this.joints()
                .stream()
                .filter(articulation -> articulationName.equals(articulation.getName()))
                .findFirst().orElse(null);
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       END EFFECTOR INFO                              ||
// ||                                                                                      ||
// \\======================================================================================//

    /** @return The position of the end effector */
    @JsonGetter("endEffectorPosition")
    default Vector3D getEndEffectorPosition() {
        return this.getEndEffector().getTransform().getOrigin();
    }

    /**
     * @return The orientation of the end effector
     */
    @JsonGetter("endEffectorOrientation")
    default Rotation getEndEffectorOrientation() {
        return Rotation.from(MatrixUtil.extractRotation(this.dhTable().getRotationMatrix(0, this.getNbOfJoints()-1)));
    }

    /** @return The current arm angle. It is the angle formed between the plane_0 and plane_phi where 
     * <ul>
     *     <li>plane_0 is the plane formed by The base, The Shoulder, The Wrist</li>
     *     <li>plane_0 is the plane formed by The base, The Elbow, The Wrist</li>
     * </ul>
     */
    @JsonGetter("armAngle")
    default double getArmAngle() {
        Vector3D base = this.getJoint(0).getTransform().getOrigin();
        Vector3D shoulder = this.getJoint(2).getTransform().getOrigin();
        Vector3D elbow = this.getJoint(3).getTransform().getOrigin();
        Vector3D wrist = this.getJoint(5).getTransform().getOrigin();
        
        Vector3D plane0Normal = shoulder.minus(base).cross( wrist.minus(base) );
        Vector3D planePhiNormal = elbow.minus(base).cross( wrist.minus(base) );

        if (true) { return 0; }
        
        if (plane0Normal.isNan() || planePhiNormal.isNan()) {
            return Double.NaN;
        }
        else if (plane0Normal.isZero() || planePhiNormal.isZero()) { return 0; }
        
        return plane0Normal.angleTo(planePhiNormal);
    }

    /** @return The specification of the arm based on the end effector's position, 
     * orientation and the arm's angle */
    @JsonIgnore
    default AbsoluteEndEffectorSpecification getEndEffectorSpecification() {
        return new AbsoluteEndEffectorSpecification(
                this.getEndEffectorPosition(),
                this.getEndEffectorOrientation(),
                this.getArmAngle()
        );
    }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       JOINT ANGLES                                   ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /**
     * Sets the angle in radian for each articulation, from root to nbOfArticulations
     * @param anglesRad The angles to set in radian
     * @throws AngleOutOfBoundsException If one of the angle is not in the bounds of its joint
     */
    default void setAngles(List<Double> anglesRad) throws AngleOutOfBoundsException {
        Preconditions.checkArgument(anglesRad.size() == this.getNbOfNonVirtualJoints());
        int i = 0;
        for (Joint j : joints()) { if (!j.isVirtual()) { j.setAngleRad(anglesRad.get(i++)); } }
    }
    
    /** Sets the given joint the specified angle rotation */
    default void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        getJoint(jointIndex, false).setAngleRad(angleRad);
    }

    /**
     * Return the angle of the joint specified by the index
     * @param jointIndex The index of the joint
     * @return The angle of the joint
     */
    default double getAngle(int jointIndex) {
        return getJoint(jointIndex, false).getAngleRad();
    }
    
}
