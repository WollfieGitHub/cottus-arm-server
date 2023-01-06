package fr.wollfie.cottus.dto.specification;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;

import java.util.List;

/**
 * A set of parameters that fully specify the state of the arm at a given
 * point in time. It should remove all degrees of freedom
 */
public interface ArmSpecification {

    /**
     * Return the list of #{@link CottusArm#getNbOfJoints()} angles for the arm
     * for the given specification
     * @param cottusArm A reference to the arm
     * @return The set of angles that satisfy this specification for the 
     */
    List<Double> getAnglesFor(CottusArm cottusArm) throws NoSolutionException;

    /** @return True if the specification is actually realisable by the arm given the joints' limits */
    default boolean isValidGiven(CottusArm arm) {
        List<JointBounds> bounds = arm.joints().stream()
                .filter(joint -> !joint.isVirtual())
                .map(Joint::getBounds).toList();
        
        try {
            List<Double> angles = getAnglesFor(arm);
            for (int i = 0; i < bounds.size(); i++) {
                JointBounds b = bounds.get(i);
                double angle = angles.get(i);

                if (b.isOutOfBounds(angle)) {
                    Log.errorf("Joint %d : %5.2fpi is out of bounds for %s", i, angle/Math.PI, b);
                    return false;
                }
            }
            return true;
        
        } catch (NoSolutionException e) { return false; }
    }
}
