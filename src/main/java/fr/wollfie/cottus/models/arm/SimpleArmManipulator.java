package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.models.arm.positioning.joints.bounds.IntervalJointBounds;
import fr.wollfie.cottus.models.arm.positioning.joints.bounds.SimpleJointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.services.ArmLoaderService;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.utils.maths.intervals.Interval;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static java.lang.Math.PI;

@ApplicationScoped
public class SimpleArmManipulator implements ArmManipulatorService {
    
    private static final DHTable DH_TABLE = new DHTable(
            //           |   J1 |    J2 |    J3_0|   J3  |    J4 |  J5_0 |  J5   |    J6 |  J7_0 |  J7   |
            new double[] {    50,       0,      0,  -30.5,      0,      0,  -30.5,      0,      0,      0,},
            new double[] {     0,   215.6,      0,      0,  213.3,      0,      0,  213.3,      0,      0,},
            new double[] {     0, -PI/2.0, PI/2.0,+PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,},
            new double[] {PI/2.0,  PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0,},
            new boolean[]{ false,   false,   true,  false,  false,   true,  false,  false,   true,  false,}
    );

    private static final JointBounds[] JOINT_BOUNDS = new JointBounds[] {
            IntervalJointBounds.ANY,
            SimpleJointBounds.fromDeg(180-90, 180+45),
            IntervalJointBounds.EMPTY,
            IntervalJointBounds.ANY,
            SimpleJointBounds.fromDeg(-90, 45),
            IntervalJointBounds.EMPTY,
            IntervalJointBounds.ANY,
            SimpleJointBounds.fromDeg(-90, 45),
            IntervalJointBounds.EMPTY,
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
        this.cottusArm = armLoaderService.buildNewArmFrom(DH_TABLE, JOINT_BOUNDS);
    }
    
    @Override
    public void moveGiven(ArmSpecification specification) throws AngleOutOfBoundsException {
        if (!specification.isValidGiven(
                this.cottusArm.joints().stream()
                        .filter(joint -> !joint.isVirtual())
                        .map(Joint::getBounds).toList()
        )) { throw new AngleOutOfBoundsException(); }
        
        this.cottusArm.setAngles(specification.getAngles());
    }

    @Override
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        this.cottusArm.setAngle(jointIndex, angleRad);
    }
}
