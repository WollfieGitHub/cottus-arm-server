package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.models.arm.positioning.joints.bounds.IntervalJointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.services.ArmLoaderService;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.utils.maths.intervals.trigonometric.TrigonometricInterval;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static java.lang.Math.PI;

@ApplicationScoped
public class SimpleArmManipulator implements ArmManipulatorService {
    
    private static final DHTable DH_TABLE_0 = new DHTable(
    //                        |   J1 |    J2 |    J3_0|   J3  |    J4 |  J5_0 |  J5   |    J6 |  J7_0 |  J7   |
    /*A_i */     new double[] {    50,       0,      0,  -30.5,      0,      0,  -30.5,      0,      0,      0,},
    /*D_i */     new double[] {     0,   215.6,      0,      0,  213.3,      0,      0,  213.3,      0,      0,},
    /*THETA_i */ new double[] {     0, -PI/2.0, PI/2.0,+PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,},
    /*ALPHA_i */ new double[] {PI/2.0,  PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0,},
    /*VIRTUAL */ new boolean[]{ false,   false,   true,  false,  false,   true,  false,  false,   true,  false,}
    );

    private static final JointBounds[] JOINT_BOUNDS_0 = new JointBounds[] {
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(180-90, 180+45)),
            IntervalJointBounds.EMPTY,
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(180-90, 180+45)),
            IntervalJointBounds.EMPTY,
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(180-90, 180+45)),
            IntervalJointBounds.EMPTY,
            IntervalJointBounds.ANY,
    };

    private static final DHTable DH_TABLE_1 = new DHTable(
    //                       |  J1 |    J2 |   J3  |    J4 |  J5   |    J6 |  J7   |
    /*A_i */    new double[] {    50,       0,  215.6,      0,  213.3,      0,      0,},
    /*D_i */    new double[] {     0,       0,      0,      0,      0,      0,   30.5,},
    /*THETA_i */new double[] {     0,       0,-PI/2.0,      0,      0,      0,      0,},
    /*ALPHA_i */new double[] {-PI/2.0, PI/2.0,-PI/2.0, PI/2.0,-PI/2.0, PI/2.0,      0,},
                new boolean[]{  false,  false,  false,  false,  false,  false,  false,}
    );

    private static final JointBounds[] JOINT_BOUNDS_1 = new JointBounds[] {
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
            IntervalJointBounds.of(TrigonometricInterval.withDeg(-90, +45)),
            IntervalJointBounds.ANY,
    };

    private CottusArm cottusArm;
    public CottusArm getArmState() { return this.cottusArm; }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INITIALIZATION                                 ||
// ||                                                                                      ||
// \\======================================================================================//

    @Inject
    ArmLoaderService armLoaderService;

    @PostConstruct
    void initArm() {
        this.cottusArm = armLoaderService.buildNewArmFrom(DH_TABLE_1, JOINT_BOUNDS_1);
    }
    
    @Override
    public void moveGiven(ArmSpecification specification) throws AngleOutOfBoundsException {
        if (!specification.isValidGiven(this.cottusArm)) { throw new AngleOutOfBoundsException(); }
        
        this.cottusArm.setAngles(specification.getAngles());
    }

    @Override
    public void setAngle(
            int jointIndex, double angleRad
    ) throws AngleOutOfBoundsException { this.cottusArm.setAngle(jointIndex, angleRad); }

    @Override
    public double getAngle(int jointIndex) { return this.cottusArm.getAngle(jointIndex); }
}
