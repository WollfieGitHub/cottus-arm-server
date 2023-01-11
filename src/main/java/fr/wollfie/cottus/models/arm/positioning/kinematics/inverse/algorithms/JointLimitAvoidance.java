package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.utils.maths.MathUtils;
import fr.wollfie.cottus.utils.maths.intervals.ConvexInterval;
import fr.wollfie.cottus.utils.maths.intervals.Interval;
import fr.wollfie.cottus.utils.maths.intervals.UnionOfIntervals;
import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

/**
 * @implNote Also from <a href="https://ieeexplore.ieee.org/document/4631505">M. Shimizu, H. Kakuya, W. -K.
 * Yoon, K. Kitagaki and K. Kosuge, "Analytical Inverse Kinematic Computation for 7-DOF Redundant Manipulators
 * With Joint Limits and Its Application to Redundancy Resolution," in IEEE Transactions on Robotics, vol. 24, 
 * no. 5, pp. 1131-1142, Oct. 2008, doi: 10.1109/TRO.2008.2003266.</a> like {@link Analytical7DOFsIK}
 */
public class JointLimitAvoidance {
    
    // Since the shoulder supports a heavier load, it should
    // have priority on joint limit avoidance
    private static final double SHOULDER_LIMITS_AVOIDANCE_WEIGHT = 2;
    private static final double WRIST_LIMITS_AVOIDANCE_WEIGHT = 0.2;

    private final FeasibleArmAngles feasibleArmAngles;
    private final double aSt, bSt, cSt;
    private final double aWt, bWt, cWt;
    
    private final double feasiblePsiOpt;
    /** @return Optimal feasible psi (arm angle) that satisfies the joints' bounds */
    public double getFeasiblePsiOpt() { return feasiblePsiOpt; }

    public JointLimitAvoidance(
            FeasibleArmAngles feasibleArmAngles,
            SimpleMatrix aS, SimpleMatrix bS, SimpleMatrix cS, SimpleMatrix R03_D,
            SimpleMatrix aW, SimpleMatrix bW, SimpleMatrix cW, SimpleMatrix R47_D
    ) {
        this.feasibleArmAngles = feasibleArmAngles;

        aSt = aS.mult( R03_D.transpose() ).trace();
        bSt = bS.mult( R03_D.transpose() ).trace();
        cSt = cS.mult( R03_D.transpose() ).trace();

        aWt = aW.mult( R47_D.transpose() ).trace();
        bWt = bW.mult( R47_D.transpose() ).trace();
        cWt = cW.mult( R47_D.transpose() ).trace();
        
        double a = (SHOULDER_LIMITS_AVOIDANCE_WEIGHT * aSt + WRIST_LIMITS_AVOIDANCE_WEIGHT * aWt)
                / (SHOULDER_LIMITS_AVOIDANCE_WEIGHT + WRIST_LIMITS_AVOIDANCE_WEIGHT);
        double b = (SHOULDER_LIMITS_AVOIDANCE_WEIGHT * bSt + WRIST_LIMITS_AVOIDANCE_WEIGHT * bWt)
                / (SHOULDER_LIMITS_AVOIDANCE_WEIGHT + WRIST_LIMITS_AVOIDANCE_WEIGHT);

        double distAB = sqrt(a * a + b * b);
        double psi1 = MathUtils.normalizeAngle(2 * atan2(-b - distAB, a));
        double psi2 = MathUtils.normalizeAngle(2 * atan2(-b + distAB, a));
        
        double psi1Score = optFunction(psi1);
        double psi2Score = optFunction(psi2);
        
        double psiOpt = psi1Score > psi2Score ? psi1 : psi2;
        
        if (this.feasibleArmAngles.isFeasible(psiOpt)) { this.feasiblePsiOpt = psiOpt; }
        else { this.feasiblePsiOpt = getBestFeasiblePsi(psiOpt); }
        
        Log.infof("Psi = %5.3f, score = %5.3f", feasiblePsiOpt, optFunction(this.feasiblePsiOpt));
    }

    /** @return The best psi according to the opt function  */
    private double getBestFeasiblePsi(double psiOpt) {
        UnionOfIntervals feasibleAngles = this.feasibleArmAngles.getFeasibleArmAngles();
        List<ConvexInterval> intervals = feasibleAngles.getIntervals();
        if (intervals.isEmpty()) { return Double.NaN; }
        
        ConvexInterval lowerInterval = null;
        // First the value may be at the full left of all intervals
        ConvexInterval upperInterval = intervals.get(0);
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i < intervals.size(); i++) {
            ConvexInterval interval = intervals.get(i);

            double diff = abs(psiOpt - interval.upperBound);
            
            if (interval.upperBound < psiOpt && diff < minDiff) {
                minDiff = diff;
                lowerInterval = interval;
                upperInterval = (i < intervals.size()-1 ? intervals.get(i+1) : null);
            }
        }
        
        if (lowerInterval == null && upperInterval == null) { return Double.NaN; }
        if (lowerInterval == null) { return upperInterval.lowerBound; }
        if (upperInterval == null) { return lowerInterval.upperBound; }
        
        double loDiff = minDiff;
        double hiDiff = abs(upperInterval.lowerBound - psiOpt);
        
        return loDiff < hiDiff ? lowerInterval.upperBound : upperInterval.lowerBound;
    }

    private double optFunction(double psi) {
        Function<Double, Double> fS = x -> aSt * sin(x) + bSt * cos(x) + cSt;
        Function<Double, Double> fW = x -> aWt * sin(x) + bWt * cos(x) + cWt;
        
        return (SHOULDER_LIMITS_AVOIDANCE_WEIGHT * fS.apply(psi) + WRIST_LIMITS_AVOIDANCE_WEIGHT * fW.apply(psi))
                / ( SHOULDER_LIMITS_AVOIDANCE_WEIGHT + WRIST_LIMITS_AVOIDANCE_WEIGHT);
    }
}
