package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.cottus_arm.DrivenCottusArm;
import fr.wollfie.cottus.models.arm.cottus_arm.SimulatedCottusArm;
import fr.wollfie.cottus.models.arm.positioning.joints.bounds.IntervalJointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.utils.maths.intervals.trigonometric.TrigonometricInterval;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;

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
                new String[] {"Shoulder_0", "Shoulder_1", "Shoulder_2", "Elbow", "Wrist_0", "Wrist_1", "Wrist_2", "EndEffector"}
    );

    private static final List <JointBounds> JOINT_BOUNDS_1 = List.of(
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.EMPTY // End effector
    );

    private CottusArm cottusArm;
    @Override public CottusArm getArmState() { return this.cottusArm; }

    private CottusArm drivenCottusArm;
    @Override public CottusArm getDrivenArmState() { return this.drivenCottusArm; }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INITIALIZATION                                 ||
// ||                                                                                      ||
// \\======================================================================================//


    @PostConstruct
    void initArm() {
        this.cottusArm = new SimulatedCottusArm(DH_TABLE_1, JOINT_BOUNDS_1);
        this.drivenCottusArm = new DrivenCottusArm(this.cottusArm);
    }
    
    @Override
    public void moveGiven(AngleSpecification specification) throws AngleOutOfBoundsException {
        if (!specification.isValidGiven(this.cottusArm)) { throw new AngleOutOfBoundsException(); }

        this.cottusArm.setAngles(specification.getAnglesFor(this.cottusArm));
    }

    @Override
    public void moveGiven(ArmSpecification specification) throws AngleOutOfBoundsException, NoSolutionException {
        if (!specification.isValidGiven(this.cottusArm)) { throw new AngleOutOfBoundsException(); }

        this.cottusArm.setAngles(specification.getAnglesFor(this.cottusArm));
    }

    @Override
    public void moveDrivenGiven(AngleSpecification specification) {
        try {
            this.drivenCottusArm.setAngles(specification.getAnglesFor(this.drivenCottusArm));
        } catch (AngleOutOfBoundsException e) { throw new RuntimeException(e); }
    }

    @Override
    public void setAngle(
            int jointIndex, double angleRad
    ) throws AngleOutOfBoundsException { this.cottusArm.setAngle(jointIndex, angleRad); }

    @Override
    public double getAngle(int jointIndex) { return this.cottusArm.getAngle(jointIndex); }

    @Override
    public void setReady() {
        this.cottusArm.setReady();
    }
}
