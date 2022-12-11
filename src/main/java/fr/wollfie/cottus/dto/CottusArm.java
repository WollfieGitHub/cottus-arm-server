package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

import java.util.List;

/**
 * The arm represented as an object.
 * 
 * It has a list of {@link Joint} where
 * the first articulation will be the root of the arm 
 * and the last element will be the end effector.
 */
public interface CottusArm {

    /** @return The articulations of the arm */
    @JsonGetter("articulations")
    List<Joint> joints();
    
    /** @return The root of the arm, the first articulation */
    default Joint getRoot() { return joints().get(0); }
    
    /** @return The elbow of the arm, the third articulation from the root.
     * Because it has 3 degrees of freedom before it, its position can be set using I.K. */
    default Joint getElbow() { return joints().get(3); }
    
    /** @return The end effector of the arm, the last articulation,
     * because it has 6 degrees of freedom before it, its position and rotation 
     * can be set using I.K.*/
    default Joint getEndEffector() { 
        List<Joint> joints = joints();
        return joints.get(joints.size()-1);
    }
    
    /** @return The DH Parameters Table of the arm, used for inverse and forward kinematics */
    @JsonSetter("dhTable")
    DHTable getDHTable();
    
    /** @return The number of articulations, i.e., Degrees of freedom */
    default int getNbOfJoints() { return joints().size(); }

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

    /**
     * Sets the angle in radian for each articulation, from root to nbOfArticulations
     * @param anglesRad The angles to set in radian
     * @throws AngleOutOfBoundsException If one of the angle is not in the bounds of its joint
     */
    void setAngles(List<Double> anglesRad) throws AngleOutOfBoundsException;
}
