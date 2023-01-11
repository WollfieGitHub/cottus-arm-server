package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.wollfie.cottus.utils.maths.MathUtils.*;
import static fr.wollfie.cottus.utils.maths.matrices.MatrixUtil.isNan;
import static java.lang.Math.*;

public class Analytical7DOFsIK implements IKSolver {

    private DHTable table;
    private double psi, cPsi, sPsi;
    
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
    private SimpleMatrix rD;
    private SimpleMatrix R03_0D;
    private SimpleMatrix R34;
    private SimpleMatrix R47_D;

//=========   ====  == =
//      JOINT ANALYTIC INFO
//=========   ====  == =
    
    public interface JointAnalyticInfo { }
    public record CosineJointInfo(int r, int c, int f) implements JointAnalyticInfo {} 
    public record TangentJointInfo(int r1, int c1, int f1, int r2, int c2, int f2) implements JointAnalyticInfo {} 
    
    private static final Map<Integer, JointAnalyticInfo> JOINT_ANALYTIC_INFO_MAP = new HashMap<>();
    
//=========   ====  == =
//      SOLVING ALGORITHM
//=========   ====  == =
    
    public Analytical7DOFsIK( ) { }
    
    /**
     * Note : This algorithm only exposes one solution when some joints have multiple possible angles 
     * @implNote Adaptation of <a href="https://ieeexplore.ieee.org/document/4631505">M. Shimizu, H. Kakuya, W. -K.
     * Yoon, K. Kitagaki and K. Kosuge, "Analytical Inverse Kinematic Computation for 7-DOF Redundant Manipulators
     * With Joint Limits and Its Application to Redundancy Resolution," in IEEE Transactions on Robotics, vol. 24, 
     * no. 5, pp. 1131-1142, Oct. 2008, doi: 10.1109/TRO.2008.2003266.</a>
     */
    @Override
    public List<Double> startIKSolve(
            CottusArm arm, AbsoluteEndEffectorSpecification specification,
            double maxPosError, double maxRotError
    ) throws NoSolutionException {
        Map<Integer, JointBounds> bounds = getBounds(arm);
        
        table = arm.dhTable().copy();
        table.setVarThetas( Vector.Zero(table.size()) );
        
        // Rotate the Z axis around the Y axis so that default rotation is end effector pointing
        // towards X axis
        rD = specification.getEndEffectorOrientation().getMatrix();
        // Set vectors for reference frame
        // From base to shoulder
        lBs = MatrixUtil.multHt(table.getTransformMatrix(0, 1), Vector3D.Zero);
        // From shoulder to elbow
        lSe = MatrixUtil.multHt(table.getTransformMatrix(0, 4), Vector3D.Zero).minus(lBs);
        // From elbow to Wrist
        lEw = MatrixUtil.multHt(table.getTransformMatrix(0, 5), Vector3D.Zero).minus(lBs).minus(lSe);
        // From wrist to end effector tip
        lWt = MatrixUtil.multHt(table.getTransformMatrix(0, 8), Vector3D.Zero).minus(lBs).minus(lSe).minus(lEw);
        
        // Compute stuff relative to the axis that connects the Shoulder to the Wrist 
        xSw = specification.getEndEffectorPosition().minus(lBs).minus( MatrixUtil.mult(rD, lWt) );
        uSw = xSw.normalized();
        uSwX = uSw.extract3D().skewSymmetric();
        uSwT = uSw.toMatrix().mult(uSw.toMatrix().transpose());
        
//=========   ====  == =
//      COMPUTE DESIRED ANGLES (FARTHEST ANGLES FROM JOINT LIMITS
//=========   ====  == =

        JointBounds boundsI;
        for (int i = 0; i < 7; i++) {
            boundsI = bounds.get(i);
            // Middle value between the two bounds
            table.setVarTheta(0, boundsI.getLowerBound() + abs(boundsI.getUpperBound() - boundsI.getLowerBound()) / 2.0);
        }

        // Now we have the desired rotation matrices for angles furthest from the bounds
        R03_0D = table.getRotationMatrix(0, 3);
        R47_D = table.getRotationMatrix(4, 7);
        
//=========   ====  == =
//      COMPUTE THETA 4
//=========   ====  == =
        
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

        // Compute aS, bS, ... cW
        this.computeMatrices();

        if (isNan(aS) || isNan(bS) || isNan(cS) || isNan(aW) || isNan(bW) || isNan(cW)) { throw new NoSolutionException(); }
        
        try {
            
            // Compute set of feasible arm angles
            FeasibleArmAngles feasibleArmAngles
                    = new FeasibleArmAngles( JOINT_ANALYTIC_INFO_MAP, bounds, aS, bS, cS, aW, bW, cW );
            // Compute arm angle avoiding joint limits
            JointLimitAvoidance jointLimitAvoidance
                    = new JointLimitAvoidance( feasibleArmAngles, aS, bS, cS, R03_0D, aW, bW, cW, R47_D);

            // Compute the optimal arm angle that avoids joints' bounds
            double optPsi = jointLimitAvoidance.getFeasiblePsiOpt();
            
            // Recompute the angles for the optimal arm angle
            this.computeRemainingAnglesGiven(optPsi);
            
        } catch (Exception e) { e.printStackTrace(); }

        List<Double> angles = List.of(theta1, theta2, theta3, theta4, theta5, theta6, theta7);
        if (angles.stream().anyMatch(a -> a.isNaN())) { throw new NoSolutionException(); }
        
        return angles;
    }
    
    /** @return Formats the joints' bounds as a map of integer where the integer is the index of the joint*/
    private Map<Integer, JointBounds> getBounds(CottusArm arm) {
        List<JointBounds> bounds = arm.joints().stream().map(Joint::getBounds).toList();
        Map<Integer, JointBounds> result = new HashMap<>();
        for (int i = 0; i < bounds.size(); i++) {
            result.put(i, bounds.get(i));
        }
        return result;
    }
    
    /** Compute Theta1, Theta2, Theta3, Theta5, Theta6, Theta7, given the Matrices aS, ..., cW and psi */
    private void computeRemainingAnglesGiven(double psi) {
        // Compute cos and sin of arm angle
        this.psi = psi;
        this.cPsi = cos(psi);
        this.sPsi = sin(psi);
        
        theta1 = computeTheta(0, aS, bS, cS);
        theta2 = computeTheta(1, aS, bS, cS);
        theta3 = computeTheta(2, aS, bS, cS);

        table.setVarTheta(0, theta1);
        table.setVarTheta(1, theta2);
        table.setVarTheta(2, theta3);
        
        theta5 = computeTheta(4, aW, bW, cW);
        theta6 = computeTheta(5, aW, bW, cW);
        theta7 = computeTheta(6, aW, bW, cW);

        table.setVarTheta(4, theta5);
        table.setVarTheta(5, theta6);
        table.setVarTheta(6, theta7);
    }

    private void computeMatrices() {
        
        // Given that theta3_0 = 0
        R03_0 = table.getRotationMatrix(0, 3);
        
        aS = uSwX.mult(R03_0);
        bS = uSwX.mult(uSwX).scale(-1).mult(R03_0);
        cS = uSwT.mult(R03_0);
        
        R34 = table.getRotationMatrix(3,4);

        aW = R34.transpose().mult( aS.transpose() ).mult( rD );
        bW = R34.transpose().mult( bS.transpose() ).mult( rD );
        cW = R34.transpose().mult( cS.transpose() ).mult( rD );
    }

    /** @return The value of (a_ij * sinPsi + b_ij * cosPsi + c_ij */
    private double getCoefficient(int i, int j, SimpleMatrix a, SimpleMatrix b, SimpleMatrix c, double factor) {
        return factor * (a.get(i,j)*sPsi + b.get(i, j)*cPsi + c.get(i,j));
    }

//=========   ====  == =
//      ELBOW ANGLE
//=========   ====  == =
    
    /** Compute the Elbow's joint */
    private void computeTheta4() {
        double dSe = lSe.norm();
        double dEw = lEw.norm();
        
        theta4 = acos(( xSw.normSquared() - dSe*dSe - dEw*dEw )/( 2*dSe*dEw ));
    }
    
//=========   ====  == =
//      REF ANGLES
//=========   ====  == =
    
    private void computeTheta2_0() {
        // Radius: Because lSw3 and lSw0 have the same origin
        if (isZero(lSw3.z+lSw0.z)) { theta2_0 = -PI; }
        else {
            theta2_0 = 2 * atan2(
                    sqrt( lSw3.z*lSw3.z + lSw3.x*lSw3.x - lSw0.z*lSw0.z ) - lSw3.x,
                    lSw3.z+lSw0.z
            );
        }
    }

    private void computeTheta1_0() {
        double s1_0 = lSw0.y / (lSw3.x*cos(theta2_0) + lSw3.z*sin(theta2_0));
        double c1_0 = lSw0.x / (lSw3.x*cos(theta2_0) + lSw3.z*sin(theta2_0));
        
        // Handle first joint's singularity
        if (isZero(s1_0) && isZero(c1_0)) { theta1_0 = 0; }
        else { theta1_0 = atan2(s1_0, c1_0); }
    }

//=========   ====  == =
//      COMPUTE ANGLES
//=========   ====  == =

    // Initialized on first reference to class
    static {
        // Shoulder
        JOINT_ANALYTIC_INFO_MAP.put(0, new TangentJointInfo(1,1, -1, 0,1, -1));
        JOINT_ANALYTIC_INFO_MAP.put(1, new CosineJointInfo(2,1, -1));
        JOINT_ANALYTIC_INFO_MAP.put(2, new TangentJointInfo(2,2, 1, 2,0, -1));
        
        // Wrist
        JOINT_ANALYTIC_INFO_MAP.put(4, new TangentJointInfo(1,2, 1, 0,2, 1));
        JOINT_ANALYTIC_INFO_MAP.put(5, new CosineJointInfo(2,2, 1));
        JOINT_ANALYTIC_INFO_MAP.put(6, new TangentJointInfo(2,1, 1,2,0,-1));
    }
    
    private double computeTheta(int i, SimpleMatrix a, SimpleMatrix b, SimpleMatrix c) { 
        JointAnalyticInfo jointAnalyticInfo = JOINT_ANALYTIC_INFO_MAP.get(i);
        
        // Tangent joint type
        if (jointAnalyticInfo instanceof TangentJointInfo tJ) {
            return atan2( 
                    getCoefficient(tJ.r1,tJ.c1,a,b,c, tJ.f1), 
                    getCoefficient(tJ.r2,tJ.c2,a,b,c, tJ.f2)
            );
        // Cosine joint type
        } else if (jointAnalyticInfo instanceof CosineJointInfo cJ){
            return acos( getCoefficient(cJ.r,cJ.c,a,b,c, cJ.f));
        // Not possible
        } else { throw new IllegalStateException("Joint should be either of type Cosine or Tangent"); }
    }
    


}
