package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import java.util.List;

/** 
 * With analytical solver, we have all the solutions for the angles,
 * which means we have to "process" them and choose only one set
 * of angles, which is the role of this "wrapper"
 * */
public class AnalyticalIKSolutionProcessor {

    /** Process the recorded solutions and choose one */
    public List<Double> process() {
        return null;
    }
}
