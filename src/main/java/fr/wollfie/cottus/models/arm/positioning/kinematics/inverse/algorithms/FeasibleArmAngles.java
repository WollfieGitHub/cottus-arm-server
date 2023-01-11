package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.utils.maths.intervals.ConvexInterval;
import fr.wollfie.cottus.utils.maths.intervals.Interval;
import fr.wollfie.cottus.utils.maths.intervals.UnionOfIntervals;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.groups.MultiItemCombine3;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static fr.wollfie.cottus.utils.maths.MathUtils.*;
import static fr.wollfie.cottus.utils.maths.MathUtils.maxWithNan;
import static java.lang.Math.*;
import static java.lang.Math.PI;

/**
 * Used in {@link JointLimitAvoidance} to determine which of the arm angles is feasible
 */
public class FeasibleArmAngles {

    private final UnionOfIntervals feasibleArmAngles;
    /** The interval of feasible arm angles */
    public UnionOfIntervals getFeasibleArmAngles() { return feasibleArmAngles; }
    
    /** @return True if the arm angle {@param psi} is feasible given the joint bounds, False otherwise */
    public boolean isFeasible(double psi) {
        return this.feasibleArmAngles.contains(psi);
    }

    /** Computes an interval representing all feasible arm angles given the bounds of the joints */
    public FeasibleArmAngles(
            Map<Integer, Analytical7DOFsIK.JointAnalyticInfo> jointInfos, Map<Integer, JointBounds> bounds,
            SimpleMatrix aS, SimpleMatrix bS, SimpleMatrix cS,
            SimpleMatrix aW, SimpleMatrix bW, SimpleMatrix cW
    ) {
        // Theta 4 is independent of the arm angle
        UnionOfIntervals i0 = getFeasibleArmAngle(jointInfos.get(0), bounds.get(0), aS, bS, cS);
        UnionOfIntervals i1 = getFeasibleArmAngle(jointInfos.get(1), bounds.get(1), aS, bS, cS);
        UnionOfIntervals i2 = getFeasibleArmAngle(jointInfos.get(2), bounds.get(2), aS, bS, cS);

        UnionOfIntervals i4 = getFeasibleArmAngle(jointInfos.get(4), bounds.get(4), aW, bW, cW);
        UnionOfIntervals i5 = getFeasibleArmAngle(jointInfos.get(5), bounds.get(5), aW, bW, cW);
        UnionOfIntervals i6 = getFeasibleArmAngle(jointInfos.get(6), bounds.get(6), aW, bW, cW);
        
        this.feasibleArmAngles = UnionOfIntervals.inter(i0, i1, i2, i4, i5, i6).inter(ConvexInterval.from(-PI, PI));
    }

//=========   ====  == =
//      COMPUTE ARM ANGLE DEPENDING ON JOINT TYPE
//=========   ====  == =

    /** @return The result of the equation c = a*sinx + b*cosx, solved for x */
    private double solveBase(double a, double b, double c) {
        if (isZero(b+c)) { return normalizeAngle(-PI); }
        else {
            return normalizeAngle(2 * atan2(a - sqrt(a*a + b*b - c*c), b+c));
        }
    }

    /** @return psi = f-1(theta_i) */
    private double getPsiForCosTypeTheta(
            CosCoefficients t, double theta
    ) {
        // Singular case
        if (!isZero(sin(theta))) {
            // We have "cos(theta) = a*sin(psi) + b*sin(psi) + c" => Derive "psi = f-1(theta)"
            double k = cos(theta)-t.c;
            return this.solveBase(t.a, t.b, k);
        }

        double aAndBSquared = t.a*t.a + t.b*t.b;

        if (isZero(aAndBSquared - (t.c-1)*(t.c-1))) {
            return normalizeAngle(2 * atan2(t.a, (t.b-(t.c-1))));

        } else if(isZero(aAndBSquared - (t.c+1)*(t.c+1))) {
            return normalizeAngle(2 * atan2(t.a, (t.b-(t.c+1))));

        } else { throw new IllegalStateException("Something went wrong here..."); }
    }

    /** @return psi = f-1(theta_i). Two solutions : result[0] < result[1] */
    private double[] getPsiForTanTypeTheta(TanCoefficients t, double theta) {

        // We only need to solve for the cos, but it must respect the sin
        double k, psi;
        if (t.a1*t.a1 + t.b1*t.b1 - (sin(theta)-t.c1)*(sin(theta)-t.c1) >= 0) {
            k = sin(theta)-t.c1;
            psi = this.solveBase(t.a1, t.b1, k);
        } else if (t.a2*t.a2 + t.b2*t.b2 - (cos(theta)-t.c2)*(cos(theta)-t.c2) >= 0) {
            k = cos(theta)-t.c2;
            psi = this.solveBase(t.a2, t.b2, k);
            // No solution, singularity
        } else { return new double[0]; }

        double sinT = (t.a1 * sin(psi) + t.b1 * cos(psi) + t.c1);
        double costT = (t.a2 * sin(psi) + t.b2 * cos(psi) + t.c2);

        if (isZero(abs(tan(theta) - sinT/costT))) {
            // Two solutions
            return new double[] { min(psi, PI-psi), max(psi, PI-psi) };
            // No solution : Theta is a singularity
        } else {
            throw new IllegalStateException(String.format("tanT = %5.3f, sol = %5.3f, sin = %5.3f, cos = %5.3f", tan(theta), sinT/costT, sinT, costT));
        }
    }

    /** @return Theta_i = f(psi) */
    private double getCosTypeThetaForPsi(CosCoefficients t, double psi) {
        return acos(t.a * sin(psi) + t.b * cos(psi) + t.c);
    }

    /** @return Theta_i = f(psi) */
    private double getTanTypeThetaForPsi(TanCoefficients t, double psi) {
        return atan2(
                t.a1 * sin(psi) + t.b1 * cos(psi) + t.c1,
                t.a2 * sin(psi) + t.b2 * cos(psi) + t.c2
        );
    }

    /** Compute phiMin and phiMax */
    private double[] getGlobalExtremesForCosTypeTheta(CosCoefficients t) {
        double aAndBSquared = t.a*t.a + t.b*t.b;

        if (isZero(aAndBSquared - (t.c-1)*(t.c-1))) { return new double[]{ 
                normalizeAngle(-PI),
                normalizeAngle(2 * atan2(t.a, (t.b-(t.c-1))))
        }; }
        else if(isZero(aAndBSquared - (t.c+1)*(t.c+1))) { return new double[]{ 
                normalizeAngle(2 * atan2(t.a, (t.b-(t.c+1)))),
                normalizeAngle(PI)
        }; }
        else {
            double psi1 = normalizeAngle(2 * atan2(-t.b - sqrt(aAndBSquared), t.a));
            double psi2 = normalizeAngle(2 * atan2(-t.b + sqrt(aAndBSquared), t.a));

            return new double[] { min(psi1, psi2), max(psi1, psi2) };
        }
    }

    /** Compute phiMin and phiMax */
    private double[] getGlobalExtremesForTanTypeTheta(TanCoefficients t) {
        double at = t.b2*t.c1 - t.b1*t.c2;
        double bt = t.a1*t.c2 - t.a2*t.c1;
        double ct = t.a1*t.b2 - t.a2*t.b1;

        double delta = at*at + bt*bt - ct*ct;
        if (delta < 0) { return new double[0]; }
        else if (isZero(delta)) { return new double[] { 2*atan2(at, bt-ct) }; }
        else { return new double[] {
                normalizeAngle(2 * atan2(at - sqrt(delta), bt-ct)),
                normalizeAngle(2 * atan2(at + sqrt(delta), bt-ct)),
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
    private UnionOfIntervals getFeasibleArmAngle(
            Analytical7DOFsIK.JointAnalyticInfo jointInfo, JointBounds bounds,
            SimpleMatrix a, SimpleMatrix b, SimpleMatrix c
    ) {
        if (jointInfo instanceof Analytical7DOFsIK.CosineJointInfo cJ) {
            return getFeasibleArmAngleForCosTypeTheta(bounds, a, b, c, cJ);

        } else if (jointInfo instanceof Analytical7DOFsIK.TangentJointInfo tJ) {
            return getFeasibleArmAngleForTanTypeTheta(bounds, a, b, c, tJ);
        }
        throw new IllegalStateException("Joint should be either of type Cosine or Tangent");
    }

    @NotNull
    private ConvexInterval getFeasibleArmAngleForCosTypeTheta(JointBounds bounds, SimpleMatrix a, SimpleMatrix b, SimpleMatrix c, Analytical7DOFsIK.CosineJointInfo cJ) {

        CosCoefficients coefficients = new CosCoefficients(
                a.get(cJ.r(), cJ.c()), b.get(cJ.r(), cJ.c()), c.get(cJ.r(), cJ.c())
        );

        double[] extremesPsi = getGlobalExtremesForCosTypeTheta(coefficients);
        double psiMin = extremesPsi[0];
        double psiMax = extremesPsi[1];

        double[] extremesTheta = new double[] {
                getCosTypeThetaForPsi(coefficients, psiMin),
                getCosTypeThetaForPsi(coefficients, psiMax)
        };

        double tMin = min(extremesTheta[0], extremesTheta[0]);
        double tMax = max(extremesTheta[0], extremesTheta[0]);

        double psi1 = Double.NaN, psi2 = Double.NaN;

        if (bounds.getLowerBound() > tMin) { psi1 = getPsiForCosTypeTheta(coefficients, bounds.getUpperBound()); }
        if (bounds.getUpperBound() < tMax) { psi2 = getPsiForCosTypeTheta(coefficients, bounds.getUpperBound()); }

        double psiLo = minWithNan(minWithNan(psi1, psi2), psiMin);
        double psiHi = maxWithNan(maxWithNan(psi1, psi2), psiMax);

        return new ConvexInterval(psiLo, psiHi);

    }

    private UnionOfIntervals getFeasibleArmAngleForTanTypeTheta(JointBounds bounds, SimpleMatrix a, SimpleMatrix b, SimpleMatrix c, Analytical7DOFsIK.TangentJointInfo tJ) {
        double tLo, tHi, tMin, tMax;
        double[] psiLo, psiHi;

        TanCoefficients coefficients = new TanCoefficients(
                a.get(tJ.r1(), tJ.c1()), b.get(tJ.r1(), tJ.c1()), c.get(tJ.r1(), tJ.c1()),
                a.get(tJ.r2(), tJ.c2()), b.get(tJ.r2(), tJ.c2()), c.get(tJ.r2(), tJ.c2())
        );

        double[] extremes = getGlobalExtremesForTanTypeTheta( coefficients );

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
        ConvexInterval domain = ConvexInterval.from(-PI, PI);
        // 1. No feasible regions of the arm angle exist.
        if (tMin > tHi || tMax < tLo) {
            return ConvexInterval.EMPTY;
        }

        // 2. Solve equation t(psi) = tLo
        if (tMin < tLo && tMax <= tHi) {
            return ConvexInterval.from(psiLo[0], psiLo[1]);
        }

        // 3. Solve equation t(psi) = tHi
        if (tLo <= tMin && tMin <= tHi  && tMax > tHi) {
            return ConvexInterval.from(psiHi[0], psiHi[1]);
        }
        // 4. Exclude  2 regions: Solve equation t(psi) = tLo and t(psi) = tHi
        if (tMin < tLo && tMax > tHi) {
            return domain
                    .minus(ConvexInterval.from(psiLo[0], psiLo[1]))
                    .minus(ConvexInterval.from(psiHi[0], psiHi[1]));
        }
        // 5. The entire domain is feasible.
        if (tLo <= tMin && tMin <= tHi && tLo <= tMax && tMax <= tHi) {
            return domain;
        }
        Log.infof("%5.3f, %5.3f, %5.3f, %5.3f", tMin, tLo, tHi, tMax);
        throw new IllegalStateException("Something went wrong...");
    }
}
