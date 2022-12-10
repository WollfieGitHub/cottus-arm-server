package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
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
    List<Joint> getArticulations();
    
    /** @return The root of the arm, the first articulation */
    default Joint getRoot() { return getArticulations().get(0); }
    
    /** @return The elbow of the arm, the third articulation from the root.
     * Because it has 3 degrees of freedom before it, its position can be set using I.K. */
    default Joint getElbow() { return getArticulations().get(3); }
    
    /** @return The end effector of the arm, the last articulation,
     * because it has 6 degrees of freedom before it, its position and rotation 
     * can be set using I.K.*/
    default Joint getEndEffector() { 
        List<Joint> joints = getArticulations();
        return joints.get(joints.size()-1);
    }
    
    /** @return The DH Parameters Table of the arm, used for inverse and forward kinematics */
    @JsonSetter("dhTable")
    DHTable getDHTable();
    
    /** @return The number of articulations, i.e., Degrees of freedom */
    default int getNbOfArticulations() { return getArticulations().size(); }

    /**
     * Finds the articulation in this arm with the specified name
     * @param articulationName The name of the articulation to find
     * @return The actual articulation with the given name
     */
    default Joint getArticulationUsing(String articulationName) {
        return this.getArticulations()
                .stream()
                .filter(articulation -> articulationName.equals(articulation.getName()))
                .findFirst().orElse(null);
    }
}
