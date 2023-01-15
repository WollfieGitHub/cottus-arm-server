package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.utils.maths.Vector3D;

import java.util.Set;


public interface WorkspaceSample {
    
    /** A sample of the points of the workspace of the robot */
    @JsonGetter("points") Set<Vector3D> points();
}
