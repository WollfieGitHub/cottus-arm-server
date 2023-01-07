package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.MathUtils;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.intervals.ContinuousInterval;
import fr.wollfie.cottus.utils.maths.intervals.Interval;
import fr.wollfie.cottus.utils.maths.intervals.trigonometric.TrigonometricInterval;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//=========   ====  == =
//      JOINT ANALYTIC INFO
//=========   ====  == =
    
    private enum JointAnalyticType { COSINE, TANGENT }
    private interface JointAnalyticInfo { }
    private record CosineJointInfo(int r, int c, int f) implements JointAnalyticInfo {} 
    private record TangentJointInfo(int r1, int c1, int f1, int r2, int c2, int f2) implements JointAnalyticInfo {} 
    
    private static final Map<Integer, JointAnalyticInfo> JOINT_ANALYTIC_INFO_MAP = new HashMap<>();
    
//=========   ====  == =
//      SOLVING ALGORITHM
//=========   ====  == =
    
    public Analytical7DOFsIK( ) { }
    
    /**
     * Note : This algorithm only exposes one solution when some joints have multiple possible angles 
     * @implNote Adaptation of <a href="https://ieeexplore.ieee.org/document/9734617">Y. Wang, L. Li and G. Li, "An Analytical 
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
        this.psi = specification.getPreferredArmAngle();
        this.cPsi = cos(psi);
        this.sPsi = sin(psi);
        
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
        
        theta1 = computeTheta(0, aS, bS, cS);
        theta2 = computeTheta(1, aS, bS, cS);
        theta3 = computeTheta(2, aS, bS, cS);

        table.setVarTheta(0, theta1);
        table.setVarTheta(1, theta2);
        table.setVarTheta(2, theta3);

//=========   ====  == =
//      COMPUTE WRIST ANGLES
//=========   ====  == =
        
        aW = table.getRotationMatrix(2,4).transpose().mult( aS.transpose() ).mult( rD );
        bW = table.getRotationMatrix(2,4).transpose().mult( bS.transpose() ).mult( rD );
        cW = table.getRotationMatrix(2,4).transpose().mult( cS.transpose() ).mult( rD );

        theta5 = computeTheta(4, aW, bW, cW);
        theta6 = computeTheta(5, aW, bW, cW);
        theta7 = computeTheta(6, aW, bW, cW);

        table.setVarTheta(4, theta5);
        table.setVarTheta(5, theta6);
        table.setVarTheta(6, theta7);
        
        List<JointBounds> bounds = arm.joints().stream().map(Joint::getBounds).toList();

        Vector solution = new Vector(theta1, theta2, theta3, theta4, theta5, theta6, theta7);
        Vector clampedSolution = IKSolver.getBoundedAngles(
                solution, bounds
        );
        // TODO ONLY VALID ANGLES
        List<Double> angles = solution.toList();
        
        Interval feasibleArmAngles = getFeasibleAngles(bounds);
        
        if (angles.stream().anyMatch(a -> a.isNaN())) { throw new NoSolutionException(); } 
        else { return angles; }
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
        theta2_0 = 2 * atan2(
                sqrt( lSw3.z*lSw3.z + lSw3.x*lSw3.x - lSw0.z*lSw0.z ) - lSw3.x,
                lSw3.z+lSw0.z
        );
    }

    private void computeTheta1_0() {
        double s1_0 = lSw0.y / (lSw3.x*cos(theta2_0) + lSw3.z*sin(theta2_0));
        double c1_0 = lSw0.x / (lSw3.x*cos(theta2_0) + lSw3.z*sin(theta2_0));
        
        // Handle first joint's singularity
        if (MathUtils.isZero(s1_0) && MathUtils.isZero(c1_0)) { theta1_0 = 0; }
        else { theta1_0 = atan2(s1_0, c1_0); }
    }

//=========   ====  == =
//      COMPUTE ANGLES
//=========   ====  == =

    // Initialized on first reference to class
    static {
        // Shoulder
        JOINT_ANALYTIC_INFO_MAP.put(0, new TangentJointInfo(1,2, 1, 0,2, 1));
        JOINT_ANALYTIC_INFO_MAP.put(1, new CosineJointInfo(2,2, 1));
        JOINT_ANALYTIC_INFO_MAP.put(2, new TangentJointInfo(2,1, 1, 2,0, -1));
        
        // Wrist
        JOINT_ANALYTIC_INFO_MAP.put(4, new TangentJointInfo(1,2, 1, 0,2, -1));
        JOINT_ANALYTIC_INFO_MAP.put(5, new CosineJointInfo(2,2, 1));
        JOINT_ANALYTIC_INFO_MAP.put(6, new TangentJointInfo(2,1, 1,2,0, -1));
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
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                             FEASIBLE ARM ANGLES GIVEN JOINT LIMITS                   ||
// ||                                                                                      ||
// \\======================================================================================//
    
    
//=========   ====  == =
//      COMPUTE ARM ANGLE DEPENDING ON JOINT TYPE
//=========   ====  == =
    
    /** @return The result of the equation c = a*sinx + b*cosx, solved for x */
    private double solveBase(double a, double b, double c) {
        return 2 * atan2(a - sqrt(a*a + b*b - c*c), b+c);
    }
    
    /** @return psi = f-1(theta_i) */
    private double getPsiForCosTypeTheta(
            CosCoefficients t, double theta
    ) {
        // We have "cos(theta) = asin(psi) + bsin(psi) + c" => Derive "psi = f-1(theta)"
        double k = cos(theta)-t.c;
        return this.solveBase(t.a, t.b, k);
    }

    /** @return psi = f-1(theta_i). Two solutions : result[0] < result[1] */
    private double[] getPsiForTanTypeTheta(TanCoefficients t, double theta) {
        // TODO VERIFY THIS BIT
        // We only need to solve for the cos, but it must respect the sin
        double k1 = cos(theta)-t.c1;
        double psi1 = this.solveBase(t.a1, t.a2, k1);
        
        if (MathUtils.isZero(abs(
                tan(theta) - (t.a1 * sin(psi1) + t.b1 * cos(psi1) + t.c1) /
                (t.a2 * sin(psi1) + t.b2 * cos(psi1) + t.c2)
        ))) {
            // Two solutions
            return new double[] { min(psi1, PI-psi1), max(psi1, PI-psi1) };
        // No solution : Theta is a singularity
        } else { return new double[0]; }
    }
    
    /** @return Theta_i = f(psi) */
    private double getTanTypeThetaForPsi(TanCoefficients t, double psi) {
        return atan2(
                t.a1 * sin(psi) + t.b1 * cos(psi) + t.c1,
                t.a2 * sin(psi) + t.b2 * cos(psi) + t.c2
        );
    }
    
    private double[] getGlobalExtremesForCyclicFunction(TanCoefficients t) {
        double at = t.b2*t.c1 - t.b1*t.c2;
        double bt = t.a1*t.c2 - t.a2*t.c1;
        double ct = t.a1*t.b2 - t.a2*t.b1;
        
        double delta = at*at + bt*bt - ct*ct;
        if (delta < 0) { return new double[0]; }
        else if (MathUtils.isZero(delta)) { return new double[] { 2*atan2(at, bt-ct) }; }
        else { return new double[] {
                2 * atan2(at - sqrt(delta), bt-ct),
                2 * atan2(at + sqrt(delta), bt-ct),
        }; }
    }

//=========   ====  == =
//      COMPUTE INTERVAL OF FEASIBLE ARM ANGLES
//=========   ====  == =
    private record CosCoefficients(double a, double b, double c) {};
    
    private record TanCoefficients(
            double a1, double b1, double c1,
            double a2, double b2, double c2
    ) {}

    /** Compute the interval of feasible arm angles */
    private Interval getFeasibleArmAngle(
            JointAnalyticInfo jointInfo, JointBounds bounds,
            SimpleMatrix a, SimpleMatrix b, SimpleMatrix c
    ) {
        if (jointInfo instanceof CosineJointInfo cJ) { 
            CosCoefficients coefficients = new CosCoefficients(
                    cJ.f * a.get(cJ.r, cJ.c), cJ.f * b.get(cJ.r, cJ.c), cJ.f * c.get(cJ.r, cJ.c)
            );
            
            return TrigonometricInterval.from(
                    getPsiForCosTypeTheta(coefficients, bounds.getLowerBound()), 
                    getPsiForCosTypeTheta(coefficients, bounds.getUpperBound())
            );
        } else if (jointInfo instanceof TangentJointInfo tJ) {
            double tLo, tHi, tMin, tMax;
            double[] psiLo, psiHi;
            
            TanCoefficients coefficients = new TanCoefficients(
                    tJ.f1 * a.get(tJ.r1, tJ.c1), tJ.f1 * b.get(tJ.r1, tJ.c1), tJ.f1 * c.get(tJ.r1, tJ.c1),
                    tJ.f2 * a.get(tJ.r2, tJ.c2), tJ.f2 * b.get(tJ.r2, tJ.c2), tJ.f2 * c.get(tJ.r2, tJ.c2)
            );
            
            double[] extremes = getGlobalExtremesForCyclicFunction( coefficients );
            
            tLo = bounds.getLowerBound();
            psiLo = getPsiForTanTypeTheta(coefficients, tLo);
            tHi = bounds.getUpperBound();
            psiHi = getPsiForTanTypeTheta(coefficients, tHi);
            
            // Function is monotonic
            if (extremes.length == 0) { 
                double t1 = getTanTypeThetaForPsi(coefficients, PI);
                double t2 = getTanTypeThetaForPsi(coefficients, -PI);
                
                if (t1 < t2) { tMin = t1; tMax = t2; }
                else { tMin = t2; tMax = t1; }
            }
            // Singular arm angle
            else if (extremes.length == 1) {
                // Determine sign of limits difference
                double t1 = getTanTypeThetaForPsi(coefficients, extremes[0]-0.0001);
                double t2 = getTanTypeThetaForPsi(coefficients, extremes[0]+0.0001);
                
                if (t1 < t2) { // Graph 3.d in the paper
                    tMin = getTanTypeThetaForPsi(coefficients, -PI);
                    tMax = getTanTypeThetaForPsi(coefficients, PI);
                } else { // Graph 3.c in the paper
                    tMin = t2;
                    tMax = t1;
                }
            }
            // Function is cyclic
            else {
                double t1 = getTanTypeThetaForPsi(coefficients, extremes[0]);
                double t2 = getTanTypeThetaForPsi(coefficients, extremes[1]);

                if (t1 < t2) { tMin = t1; tMax = t2; }
                else { tMin = t2; tMax = t1; }
            }

            // 5 CASES :
            Interval domain = ContinuousInterval.from(-PI, PI);
            Interval regionLo = ContinuousInterval.from(psiLo[0], psiLo[0]);
            Interval regionHi = ContinuousInterval.from(psiHi[0], psiHi[0]);;
            // 1. No feasible regions of the arm angle exist.
            if (tMin > tHi || tMax < tLo) { return ContinuousInterval.EMPTY; }
            // 2. Solve equation t(psi) = tLo
            if (tMin < tLo && tMax <= tHi) { return regionLo; }
            // 3. Solve equation t(psi) = tHi
            if (tLo <= tMin && tMin <= tHi  && tMax > tHi) { return regionHi; }
            // 4. Exclude  2 regions: Solve equation t(psi) = tLo and t(psi) = tHi
            if (tMin < tLo && tMax > tHi) { return domain.minus(regionLo).minus(regionHi); }
            // 5. The entire domain is feasible.
            if (tLo <= tMin && tMin <= tHi && tLo <= tMax && tMax <= tHi) { return domain; }
        }
        throw new IllegalStateException("Joint should be either of type Cosine or Tangent");
    }

    /** @return An interval representing all feasible arm angles given the bounds of the joints */
    private Interval getFeasibleAngles(List<JointBounds> bounds) {
        // Theta 4 is independent of the arm angle
        Interval i0 = getFeasibleArmAngle(JOINT_ANALYTIC_INFO_MAP.get(0), bounds.get(0), aS, bS, cS);
        Interval i1 = getFeasibleArmAngle(JOINT_ANALYTIC_INFO_MAP.get(1), bounds.get(1), aS, bS, cS);
        Interval i2 = getFeasibleArmAngle(JOINT_ANALYTIC_INFO_MAP.get(2), bounds.get(2), aS, bS, cS);

        Interval i4 = getFeasibleArmAngle(JOINT_ANALYTIC_INFO_MAP.get(4), bounds.get(4), aW, bW, cW);
        Interval i5 = getFeasibleArmAngle(JOINT_ANALYTIC_INFO_MAP.get(5), bounds.get(5), aW, bW, cW);
        Interval i6 = getFeasibleArmAngle(JOINT_ANALYTIC_INFO_MAP.get(6), bounds.get(6), aW, bW, cW);
        return Interval.and(i0, i1, i2, i4, i5, i6).and(ContinuousInterval.from(-PI, PI));
    }


}
