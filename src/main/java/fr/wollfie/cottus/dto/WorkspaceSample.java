package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.utils.maths.Vector3D;

import java.util.Set;

/**
 * A sample of the workspace which the arm can reach. It contains a
 * set of {@link Vector3D} which are points that are guaranteed to be reachable 
 * by the arm's end effector
 */
public interface WorkspaceSample {
    
    /** A sample of the points of the workspace of the robot */
    @JsonGetter("points") Set<Vector3D> points();
}
