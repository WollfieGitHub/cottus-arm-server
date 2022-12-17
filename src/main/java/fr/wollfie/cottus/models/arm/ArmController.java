package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.joints.SimpleJointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.InverseKinematicModule;
import fr.wollfie.cottus.services.ArmControllerService;
import fr.wollfie.cottus.services.ArmLoaderService;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;

@ApplicationScoped
public class ArmController implements ArmControllerService {

    private static final DHTable DH_TABLE = new DHTable(
            //           |   J1 |    J2 |    J3_0|   J3_1|    J4 |  J5_1 |  J5_2 |    J6 |  J7_1 |  J7_2 |
            new double[] {    50,       0,      0,  -30.5,      0,      0,  -30.5,      0,      0,      0,},
            new double[] {     0,   215.6,      0,      0,  213.3,      0,      0,  213.3,      0,      0,},
            new double[] {     0, -PI/2.0, PI/2.0,+PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,},
            new double[] {PI/2.0,  PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0, PI/2.0,-PI/2.0,-PI/2.0,},
            new boolean[]{ false,   false,   true,  false,  false,   true,  false,  false,   true,  false,}
    );
    
    private static final JointBounds[] JOINT_BOUNDS = new JointBounds[] {
            SimpleJointBounds.fromDeg(-180, 180),
            SimpleJointBounds.fromDeg(-90, 45),
            SimpleJointBounds.fromDeg(0, 0),
            SimpleJointBounds.fromDeg(-180, 180),
            SimpleJointBounds.fromDeg(-90, 45),
            SimpleJointBounds.fromDeg(0, 0),
            SimpleJointBounds.fromDeg(-180, 180),
            SimpleJointBounds.fromDeg(-90, 45),
            SimpleJointBounds.fromDeg(0, 0),
            SimpleJointBounds.fromDeg(-180, 180),
    };

    private CottusArm cottusArm;
    @Override public CottusArm getArmState() { return this.cottusArm; }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INITIALIZATION                                 ||
// ||                                                                                      ||
// \\======================================================================================//
    
    @Inject ArmLoaderService armLoaderService;

    @PostConstruct
    void initArm() {
        this.cottusArm = armLoaderService.buildNewArmFrom(DH_TABLE, JOINT_BOUNDS);
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       UPDATE LOOP                                    ||
// ||                                                                                      ||
// \\======================================================================================//

    @Override
    public void update() {
        
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       ARM MOVEMENT CONTROL                           ||
// ||                                                                                      ||
// \\======================================================================================//


    @Override
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        this.cottusArm.setAngle(jointIndex, angleRad);
    }

    @Override
    public void moveGiven(ArmSpecification specification) throws AngleOutOfBoundsException {
        this.cottusArm.setAngles(specification.getAngles());
    }

    @Override
    public void moveEndEffectorTo(Vector3D position) throws NoSolutionException {

    }

    @Override
    public void moveElbowEffectorTo(Vector3D position) throws NoSolutionException {

    }

    @Override
    public void moveArmWith(Vector3D elbowPosition, Vector3D endPosition, boolean elbowUp) throws NoSolutionException {

    }

    @Override
    public void moveEndEffectorWith(Vector3D position, Rotation rotation, double effectorAngle) throws NoSolutionException {
        List<Double> angleSolutions = InverseKinematicModule.inverseSolve(cottusArm, position, rotation);
        // If a solution is found, the following code executes, otherwise it does not
        List<Double> angles = new ArrayList<>(angleSolutions);
        angles.add(effectorAngle);
        try {
            // Update the arm with the angles
            cottusArm.setAngles(angles);
            // It may be possible that the IK gives out of bound angles
        } catch (AngleOutOfBoundsException e) { throw new NoSolutionException(); }
    }

}
