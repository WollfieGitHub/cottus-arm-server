package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKFuture;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
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
    private double phi, cPhi, sPhi;
    
    // Translation vectors
    private Vector3D lBs, lSe, lEw, lWt;
    
    // Vector from should to wrist, unit vector
    private Vector3D xSw, uSw;
    // Skew-Symmetric matrix of uSw, Matrix generated from "uSw*uSw.Transposed"
    private SimpleMatrix uSwX, uSwT;
    
    private Vector3D lSw0, lSw3;
    private SimpleMatrix R03_0;
    
    private SimpleMatrix aS, bS, cS;
    private SimpleMatrix aW, bW, cW;
    
    private double theta1_0, theta2_0;
    private double theta1, theta2, theta3, theta4, theta5, theta6, theta7;

    public Analytical7DOFsIK( ) { }
    
    /** @implNote Adaptation of <a href="https://ieeexplore.ieee.org/document/9734617">Y. Wang, L. Li and G. Li, "An Analytical 
     * Solution for Inverse Kinematics of 7-DOF Redundant Manipulators with Offsets at Elbow and Wrist," 2022 IEEE 6th 
     * Information Technology and Mechatronics Engineering Conference (ITOEC), 2022, pp. 1096-1103,
     * doi: 10.1109/ITOEC53115.2022.9734617.</a>
     */
    @Override
    public List<Double> startIKSolve(
            CottusArm arm, AbsoluteEndEffectorSpecification specification,
            double maxPosError, double maxRotError
    ) throws NoSolutionException {
        table = arm.dhTable().copy();
        table.setThetas( Vector.Zero(table.size()) );
        
        // Compute cos and sin of arm angle
        this.phi = specification.getPreferredArmAngle();
        this.cPhi = cos(phi);
        this.sPhi = sin(phi);
        
        // Rotate the Z axis around the Y axis so that default rotation is end effector pointing
        // towards X axis
        SimpleMatrix rD = MatrixUtil.rotationFrom(specification.getEndEffectorOrientation());
        // Set vectors for reference frame
        // From base to shoulder
        lBs = MatrixUtil.multHt(table.getTransformMatrix(0, 1), Vector3D.Zero);
        // From shoulder to elbow
        lSe = MatrixUtil.multHt(table.getTransformMatrix(0, 4), Vector3D.Zero).minus(lBs);
        // From elbow to Wrist
        lEw = MatrixUtil.multHt(table.getTransformMatrix(0, 5), Vector3D.Zero).minus(lBs).minus(lSe);
        // From wrist to end effector tip
        lWt = MatrixUtil.multHt(table.getTransformMatrix(0, 7), Vector3D.Zero).minus(lBs).minus(lSe).minus(lEw);
        
        // Compute stuff relative to the axis that connects the Shoulder to the Wrist 
        xSw = specification.getEndEffectorPosition().minus(lBs).minus( MatrixUtil.mult(rD, lWt) );
        uSw = xSw.normalized();
        uSwX = uSw.extract3D().skewSymmetric();
        uSwT = uSw.toMatrix().mult(uSw.toMatrix().transpose());
        
        computeTheta4();
        table.setVarTheta(3, theta4);

//=========   ====  == =
//      COMPUTE REFERENCE ANGLES (THETA3 = 0)
//=========   ====  == =
        
        // Set theta3_0 to 0
        table.setVarTheta(2, -table.getTheta0(2));

        // lSw0 = pW1 - pS = xSw (Coherence of notation between the two papers used)
        lSw0 = xSw;
        // lSw3 = lSe + R34 * lSw
        lSw3 = lSe.plus( MatrixUtil.mult(table.getRotationMatrix(0,4), lEw) );

        // We must compute theta2_0 before theta1_0
        computeTheta2_0();
        computeTheta1_0();
        
        table.setVarTheta(0, theta1_0);
        table.setVarTheta(1, theta2_0);
        
        // Given that theta3_0 = 0
        R03_0 = table.getRotationMatrix(0, 2);
        
//=========   ====  == =
//      COMPUTE SHOULDER ANGLES
//=========   ====  == =
        
        aS = uSwX.mult(R03_0);
        bS = uSwX.mult(uSwX).scale(-1).mult(R03_0);
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
        
        aW = table.getRotationMatrix(2,4).transpose().mult( aS.transpose() ).mult( rD );
        bW = table.getRotationMatrix(2,4).transpose().mult( bS.transpose() ).mult( rD );
        cW = table.getRotationMatrix(2,4).transpose().mult( cS.transpose() ).mult( rD );
        
        computeTheta5();
        computeTheta6();
        computeTheta7();

        table.setVarTheta(4, theta5);
        table.setVarTheta(5, theta6);
        table.setVarTheta(6, theta7);
        
        Vector solution = new Vector(theta1, theta2, theta3, theta4, theta5, theta6, theta7);
        Vector clampedSolution = IKSolver.getBoundedAngles(
                solution, arm.joints().stream().map(Joint::getBounds).toList() 
        );
        // TODO ONLY VALID ANGLES
        List<Double> angles = solution.toList();
        
        if (angles.stream().anyMatch(a -> a.isNaN())) { throw new NoSolutionException(); } 
        else { return angles; }
    }

    /** @return The value of (a_ij * sinPhi + b_ij * cosPhi + c_ij */
    private double getShoulderCoefficients(int i, int j) {
        return aS.get(i,j)*sPhi + bS.get(i, j)*cPhi + cS.get(i,j);
    }

    /** @return The value of (a_ij * sinPhi + b_ij * cosPhi + c_ij */
    private double getWristCoefficients(int i, int j) {
        return aW.get(i,j)*sPhi + bW.get(i, j)*cPhi + cW.get(i,j);
    }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       ELBOW ANGLE                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /** Compute the Elbow's joint */
    private void computeTheta4() {
        double dSe = lSe.norm();
        double dEw = lEw.norm();
        
        theta4 = acos(( xSw.normSquared() - dSe*dSe - dEw*dEw )/( 2*dSe*dEw ));
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       REF ANGLES                                     ||
// ||                                                                                      ||
// \\======================================================================================//

    private void computeTheta2_0() {
        // Radius: Because lSw3 and lSw0 have the same origin
        theta2_0 = 2 * atan2(
                sqrt( lSw3.z*lSw3.z + lSw3.x*lSw3.x - lSw0.z*lSw0.z ) - lSw3.x,
                lSw3.z+lSw0.z
        );
    }

    private void computeTheta1_0() {
        double s1_0 = lSw0.y / (lSw3.x*cos(theta2_0) + lSw3.z*sin(theta2_0));
        double c1_0 = lSw0.x / (lSw3.x*cos(theta2_0) + lSw3.z*sin(theta2_0));
        
        theta1_0 = atan2(s1_0, c1_0);
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       SHOULDER ANGLES                                ||
// ||                                                                                      ||
// \\======================================================================================//

    private void computeTheta1() { theta1 = atan2(getShoulderCoefficients(1,2),  getShoulderCoefficients(0,2)); }
    private void computeTheta2() { theta2 = acos( getShoulderCoefficients(2,2)); }
    private void computeTheta3() { theta3 = atan2(getShoulderCoefficients(2,1), -getShoulderCoefficients(2,0)); }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       WRIST ANGLES                                   ||
// ||                                                                                      ||
// \\======================================================================================//
    
    private void computeTheta5() { theta5 = atan2(getWristCoefficients(1,2), getWristCoefficients(0,2)); }
    private void computeTheta6() { theta6 = acos( getWristCoefficients(2,2)); }
    private void computeTheta7() { theta7 = atan2(getWristCoefficients(2,1), -getWristCoefficients(2,0)); }
}
