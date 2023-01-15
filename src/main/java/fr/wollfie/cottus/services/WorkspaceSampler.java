package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.WorkspaceSample;
import fr.wollfie.cottus.utils.maths.intervals.ConvexInterval;

import java.util.List;

/** Creates a sample of the feasible workspace of the arm */
public interface WorkspaceSampler {
    
    /**
     * Compute a sample of the feasible workspace of the arm using forward kinematics
     * using the number of points specified for each joints
     * @param nbPoints The number of points for each joint. The size of the array must be equal to the number of joints
     * @return The sample of the workspace
     */
    WorkspaceSample computeFromJointSpace(int... nbPoints);
    
    /**
     * Compute a sample of the feasible workspace of the arm using inverse kinematics
     * on the 3D Space specified by the parameters of this function
     * @param xAxis The interval on which to compute feasible points for the X Axis 
     * @param nbPointsX The number of points to take for the X axis
     * @param yAxis The interval on which to compute feasible points for the Y Axis 
     * @param nbPointsY The number of points to take for the Y axis
     * @param zAxis The interval on which to compute feasible points for the Z Axis 
     * @param nbPointsZ The number of points to take for the Z axis
     * @return The sample of the workspace
     */
    WorkspaceSample computeFrom3DSpace(
            ConvexInterval xAxis, int nbPointsX,
            ConvexInterval yAxis, int nbPointsY,
            ConvexInterval zAxis, int nbPointsZ
    );
}
