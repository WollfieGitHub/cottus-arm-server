package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.EvolutionaryIK;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArmManualController implements ArmManualControllerService {

    @Inject ArmManipulatorService armManipulatorService;
    
    private final KinematicsModule kinematicsModule = new KinematicsModule();
    
    private boolean active = false;
    @Override public void setActive(boolean active) { this.active = active; }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       UPDATE LOOP                                    ||
// ||                                                                                      ||
// \\======================================================================================//

    @Override
    public void update() {
        if (!this.active) { return; }
        
    }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       ARM MOVEMENT CONTROL                           ||
// ||                                                                                      ||
// \\======================================================================================//
    


    @Override
    public void moveEndEffectorTo(Vector3D position) throws NoSolutionException {
        if (!this.active) { return; }
        
    }

    @Override
    public void moveElbowEffectorTo(Vector3D position) throws NoSolutionException {
        if (!this.active) { return; }
        
    }

    @Override
    public void moveArmWith(Vector3D elbowPosition, Vector3D endPosition, boolean elbowUp) throws NoSolutionException {
        if (!this.active) { return; }
        
    }

    @Override
    public void moveEndEffectorWith(Vector3D position, Rotation rotation, double effectorAngle) throws NoSolutionException {
        if (!this.active) { return; }

        List<Double> angleSolutions = kinematicsModule.inverseSolve(armManipulatorService.getArmState(), position, rotation);
        // If a solution is found, the following code executes, otherwise it does not
        List<Double> angles = new ArrayList<>(angleSolutions);
        angles.set(angles.size()-1, effectorAngle);
        ArmSpecification specification = new AngleSpecification(angles);
        try {
            // Update the arm with the angles
            
            armManipulatorService.moveGiven(specification);
            // It may be possible that the IK gives out of bound angles
        } catch (AngleOutOfBoundsException e) { throw new NoSolutionException(specification); }
    }

    @Override
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        if (!this.active) { Log.warn("Cannot change angle while animation is playing !"); return; }
        
        armManipulatorService.setAngle(jointIndex, angleRad);
    }

    @Override
    public void moveEndEffectorBy(float amountMm, Axis3D axis) throws NoSolutionException {
        CottusArm arm = this.armManipulatorService.getArmState();
        this.moveEndEffectorWith(
                arm.getEndEffector().getTransform().getOrigin().add(axis.scaledBy(amountMm)),
                Rotation.Identity,
                0
        );
    }

}
