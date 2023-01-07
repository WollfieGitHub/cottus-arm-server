package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.joints.bounds.IntervalJointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.services.ArmLoaderService;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.utils.maths.intervals.trigonometric.TrigonometricInterval;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static java.lang.Math.*;

@ApplicationScoped
public class SimpleArmManipulator implements ArmManipulatorService {

    private static final DHTable DH_TABLE_1 = new DHTable(
    //                       |  J1 |    J2 |   J3  |    J4 |  J5   |    J6 |  J7   |   TIP  |
    /*D_i */    new double[] {   211,       0,218.584,      0,213.589,      0,173.659,     0},
    /*A_i */    new double[] {     0,       0,      0,      0,      0,      0,      0,     0},
    /*THETA_i */new double[] {     0,       0,      0,      0,      0,      0,      0,     0},
    /*ALPHA_i */new double[] {-PI/2.0, PI/2.0,-PI/2.0, PI/2.0,-PI/2.0, PI/2.0,      0,     0},
                new boolean[]{  false,  false,  false,  false,  false,  false,  false,  true},
                new String[] {"Shoulder_0", "Shoulder_1", "Shoulder_2", "Elbow", "Wrist_0", "Wrist_1", "Wrist_2"}
    );

    private static final JointBounds[] JOINT_BOUNDS_1 = new JointBounds[] {
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.EMPTY, // End effector
    };

    private CottusArm cottusArm;
    public CottusArm getArmState() { return this.cottusArm; }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INITIALIZATION                                 ||
// ||                                                                                      ||
// \\======================================================================================//

    @Inject ArmLoaderService armLoaderService;

    @PostConstruct
    void initArm() {
        this.cottusArm = armLoaderService.buildNewArmFrom(DH_TABLE_1, JOINT_BOUNDS_1);
    }
    
    @Override
    public void moveGiven(AngleSpecification specification) throws AngleOutOfBoundsException {
        if (!specification.isValidGiven(this.cottusArm)) { throw new AngleOutOfBoundsException(); }

        this.cottusArm.setAngles(specification.getAnglesFor(this.cottusArm));
    }

    @Override
    public void setAngle(
            int jointIndex, double angleRad
    ) throws AngleOutOfBoundsException { this.cottusArm.setAngle(jointIndex, angleRad); }

    @Override
    public double getAngle(int jointIndex) { return this.cottusArm.getAngle(jointIndex); }
}
