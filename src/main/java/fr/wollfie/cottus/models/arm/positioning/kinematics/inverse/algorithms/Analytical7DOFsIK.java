package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.AnalyticalIKSolutionProcessor;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKFuture;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;

import java.util.List;

import static java.lang.Math.*;

public class Analytical7DOFsIK implements IKSolver {

    private DHTable table;
    private final KinematicsModule kinematics;
    private final IKFuture ikFuture;
    private final AnalyticalIKSolutionProcessor solutionProcessor = new AnalyticalIKSolutionProcessor();
    private final double phi, cPhi, sPhi;
    
    // Translation vectors
    private Vector lBs, lSe, lEw, lWt;
    
    // Vector from should to wrist, unit vector
    private Vector xSw, uSw;
    // Skew-Symmetric matrix of uSw, Matrix generated from "uSw*uSw.Transposed"
    private SimpleMatrix uSwX, uSwT;
    
    private Vector lSw0, lSw3;
    private SimpleMatrix R03_0;
    
    private SimpleMatrix aS, bS, cS;
    private SimpleMatrix aW, bW, cW;
    
    private double theta1_0, theta2_0, theta3_0;
    private double theta1, theta2, theta3, theta4, theta5, theta6, theta7;

    /**
     * Create a new instance of this solver (necessary for each new computation)
     * @param kinematicsModule A reference to the kinematics module
     * @param ikFuture The future to feed during the solving
     * @param armAngle The arm angle (phi) 
     */
    public Analytical7DOFsIK(KinematicsModule kinematicsModule, IKFuture ikFuture, double armAngle) {
        this.kinematics = kinematicsModule;
        this.ikFuture = ikFuture;
        this.phi = armAngle;
        // Pre compute cos and sine
        this.cPhi = cos(phi);
        this.sPhi = sin(phi);
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
        SimpleMatrix rD = MatrixUtil.rotationFrom(rotation);
        // From base to shoulder
        lBs = table.getTranslationVector(0, 1);
        lSe = table.getTranslationVector(1, 3);
        lEw = table.getTranslationVector(3, 5);
        lWt = table.getTranslationVector(5, 6);
        
        
        // Compute stuff relative to the axis that connects the Shoulder to the Wrist 
        xSw = xD.minus(lBs).minus( MatrixUtil.mult(rD, lWt) );
        uSw = xSw.normalized();
        uSwX = uSw.extract3D().skewSymmetric();
        uSwT = uSw.toMatrix().mult(uSw.toMatrix().transpose());
        
        computeTheta4();
        
        table.setVarTheta(3, theta4);

//=========   ====  == =
//      COMPUTE REFERENCE ANGLES (THETA3 = 0)
//=========   ====  == =
        
        theta3_0 = 0;

        // lSw0 = pW1 - pS 
        lSw0 = arm.getJoint(5).getTransform().getOrigin().minus(
                arm.getJoint(2).getTransform().getOrigin()
        );
        // To verify : lSw3 = lSe + R34 * lSw
        lSw3 = lSe.plus( MatrixUtil.mult(table.getRotationMatrix(3,4), lEw) );

        // We must compute theta2_0 before theta1_0
        computeTheta2_0();
        computeTheta1_0();
        
        // solutionProcessor.add(computeTheta4(), 4);

        // Given that theta3_0 = 0
        R03_0 = new SimpleMatrix(new double[][] {
                { cos(theta1_0)*cos(theta2_0),  -sin(theta1_0), cos(theta1_0)*sin(theta2_0), },
                { sin(theta1_0)*cos(theta2_0),   cos(theta1_0), sin(theta1_0)*sin(theta2_0), },
                {              -sin(theta2_0),               0,               cos(theta2_0), },
        });
        
//=========   ====  == =
//      COMPUTE SHOULDER ANGLES
//=========   ====  == =
        
        aS = uSwX.mult(R03_0);
        bS = uSwX.scale(-1).mult(R03_0);
        cS = uSwT.mult(R03_0);
        
        computeTheta1();
        computeTheta2();
        computeTheta3();
        
        table.setVarTheta(0, theta1);
        table.setVarTheta(1, theta2);
        table.setVarTheta(2, theta3);
        
//=========   ====  == =
//      COMPUTE WRIST ANGLES
//=========   ====  == =
        
        aW = table.getRotationMatrix(3,4).mult( aS.transpose() ).mult(rD);
        bW = table.getRotationMatrix(3,4).mult( bS.transpose() ).mult(rD);
        cW = table.getRotationMatrix(3,4).mult( cS.transpose() ).mult(rD);
        
        computeTheta5();
        computeTheta6();
        computeTheta7();

        List<Double> solution = List.of(
                theta1, theta2, theta3, theta4, theta5, theta6, theta7
        );
        Log.info(solution);
        // TODO ONLY VALID ANGLES
        ikFuture.completeWith(solution);
    }

    /** @return The value of (a_ij * sinPhi + b_ij * cosPhi + c_ij */
    private double getCoefficients(int i, int j, SimpleMatrix a, SimpleMatrix b, SimpleMatrix c) {
        return a.get(i,j)*sPhi + b.get(i, j)*cPhi + c.get(i,j);
    }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       ELBOW ANGLE                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /**
     * Compute the Elbow's joint
     */
    private void computeTheta4() {
        double dSe = lSe.norm();
        double dEw = lEw.norm();
        
        theta4 = acos (( xSw.normSquared() - dSe*dSe - dEw*dEw )/( 2*dSe*dEw ));
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       REF ANGLES                                     ||
// ||                                                                                      ||
// \\======================================================================================//

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
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       SHOULDER ANGLES                                ||
// ||                                                                                      ||
// \\======================================================================================//

    private void computeTheta1() { theta1 = atan2(-getCoefficients(1,1,aS,bS,cS), -getCoefficients(0,1,aS,bS,cS)); }
    private void computeTheta2() { theta2 = acos(-getCoefficients(2,1,aS,bS,cS)); }
    private void computeTheta3() { theta3 = atan2(-getCoefficients(2,0,aS,bS,cS), +getCoefficients(2,2,aS,bS,cS)); }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       WRIST ANGLES                                   ||
// ||                                                                                      ||
// \\======================================================================================//

    private void computeTheta5() { theta5 = atan2(+getCoefficients(0,2,aW,bW,cW),+getCoefficients(1,2,aW,bW,cW)); }
    private void computeTheta6() { theta6 = acos(+getCoefficients(2,2,aW,bW,cW)); }
    private void computeTheta7() { theta7 = atan2(+getCoefficients(2,1,aW,bW,cW), -getCoefficients(2,0,aW,bW,cW)); }
}
