package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.AnalyticalIKSolutionProcessor;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKFuture;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import org.ejml.simple.SimpleMatrix;

import static java.lang.Math.*;

public class Analytical7DOFsIK implements IKSolver {
    
    private DHTable table;
    private final KinematicsModule kinematics;
    private final IKFuture ikFuture;
    private final AnalyticalIKSolutionProcessor solutionProcessor = new AnalyticalIKSolutionProcessor();
    
    // Translation vectors
    private Vector lBs, lSe, lEw, lWt;
    // Length of the different parts
    private double dBs, dSe, dEw, dWt;
    
    // Vector from should to wrist, unit vector
    private Vector xSw, uSw;
    // Skew-Symmetric matrix of uSw, Matrix generated from "uSw*uSw.Transposed"
    private SimpleMatrix uSwX, uSwT;
    
    private Vector lSw0, lSw3;
    private SimpleMatrix R03_0;
    
    private SimpleMatrix aS, bS, cS;
    private SimpleMatrix aW, bW, cW;
    
    private double theta1_0, theta2_0, theta3_0;

    /**
     * Create a new instance of this solver (necessary for each new computation)
     * @param kinematicsModule A reference to the kinematics module
     * @param ikFuture The future to feed during the solving
     * @param armAngle The arm angle (phi) 
     */
    public Analytical7DOFsIK(KinematicsModule kinematicsModule, IKFuture ikFuture, double armAngle) {
        this.kinematics = kinematicsModule;
        this.ikFuture = ikFuture;
    }
    
    /** @implNote 
     * <a href="https://ieeexplore.ieee.org/document/4631505">M. Shimizu, H. Kakuya, W. -K. Yoon, K. Kitagaki
     * and K. Kosuge, "Analytical Inverse Kinematic Computation for 7-DOF Redundant Manipulators With Joint Limits
     * and Its Application to Redundancy Resolution," in IEEE Transactions on Robotics, vol. 24, no. 5, pp. 1131-1142,
     * Oct. 2008, doi: 10.1109/TRO.2008.2003266.</a>
     */
    @Override
    public void startIKSolve(
            CottusArm arm,
            Vector3D xD, Rotation rotation,
            double maxPosError, double maxRotError
    ) {
        table = arm.dhTable().copy();
        // Rotate the Z axis of the base frame
        SimpleMatrix rD = Axis3D.Z.rotatedAtOriginUsing(rotation.getEulerAngles());
        
        // Compute stuff relative to the axis that connects the Shoulder to the Wrist 
        xSw = xD.minus(lBs).minus( MatrixUtil.mult(rD, lWt) );
        uSw = xSw.normalized();
        uSwX = uSw.extract3D().skewSymmetric();
        uSwT = uSw.toMatrix().mult(uSw.toMatrix().transpose());

        // lSw0 = pW1 - pS 
        lSw0 = arm.getJoint(5).getTransform().getOrigin().minus(
                arm.getJoint(2).getTransform().getOrigin()
        );
        // To verify : lSw3 = lSe + R34 * lSw
        lSw3 = lSe.plus( MatrixUtil.mult(table.getRotationMatrix(3,4), lEw) );

        // We must compute theta2_0 before theta1_0
        computeTheta2_0();
        computeTheta1_0();
        theta3_0 = 0;
        
        // solutionProcessor.add(computeTheta4(), 4);

        // Given that theta3_0 = 0
        R03_0 = new SimpleMatrix(new double[][] {
                { cos(theta1_0)*cos(theta2_0),  -sin(theta1_0), cos(theta1_0)*sin(theta2_0), },
                { sin(theta1_0)*cos(theta2_0),   cos(theta1_0), sin(theta1_0)*sin(theta2_0), },
                {              -sin(theta2_0),               0,               cos(theta2_0), },
        });
        
        aW = table.getRotationMatrix(3,4).mult( aS.transpose() ).mult(rD);
        bW = table.getRotationMatrix(3,4).mult( bS.transpose() ).mult(rD);
        cW = table.getRotationMatrix(3,4).mult( cS.transpose() ).mult(rD);
        
        computeRefShoulderAngles();
    }

    /** Compute the Elbow's joint */
    private double computeTheta4() {
        double cos4 = ( xSw.normSquared() - dSe*dSe - dEw*dEw )/( 2*dSe*dEw );
        return Math.acos(cos4);
    }

    private void computeRefShoulderAngles() {
        
    }

    private void computeTheta1_0() {
        double s1_0 = lSw0.get(2) / (lSw3.get(0)*cos(theta2_0) + lSw3.get(2)*sin(theta2_0));
        double c1_0 = lSw0.get(0) / (lSw3.get(0)*cos(theta2_0) + lSw3.get(2)*sin(theta2_0));
        
        theta1_0 = atan2(s1_0, c1_0);
    }

    private void computeTheta2_0() {
        double lSw3z = lSw3.get(2);
        double lSw3x = lSw3.get(0);
        double lSw0z = lSw0.get(2);
        
        theta2_0 = 2 * atan2(lSw3z+lSw0z, sqrt(lSw3z*lSw3z + lSw3x*lSw3x - lSw0z*lSw0z) - lSw3x);
    }
}
