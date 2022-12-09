package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.dto.Articulation;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;

import java.util.List;

/**
 * The arm represented as an object.
 * 
 * It has a list of {@link Articulation} where
 * the first articulation will be the root of the arm 
 * and the last element will be the end effector.
 */
public interface CottusArm {

    /** @return The articulations of the arm */
    @JsonGetter("articulations")
    List<Articulation> getArticulations();
    
    /** @return The root of the arm, the first articulation */
    default Articulation getRoot() { return getArticulations().get(0); }
    
    /** @return The elbow of the arm, the third articulation from the root.
     * Because it has 3 degrees of freedom before it, its position can be set using I.K. */
    default Articulation getElbow() { return getArticulations().get(3); }
    
    /** @return The end effector of the arm, the last articulation,
     * because it has 6 degrees of freedom before it, its position and rotation 
     * can be set using I.K.*/
    default Articulation getEndEffector() { 
        List<Articulation> articulations = getArticulations();
        return articulations.get(articulations.size()-1);
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
    default Articulation getArticulationUsing(String articulationName) {
        return this.getArticulations()
                .stream()
                .filter(articulation -> articulationName.equals(articulation.getName()))
                .findFirst().orElse(null);
    }
}
