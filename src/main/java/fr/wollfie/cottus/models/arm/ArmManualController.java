package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ArmManualController implements ArmManualControllerService {

    @Inject ArmManipulatorService armManipulatorService;
    @Inject KinematicsModule kinematicsModule;
    
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
    public void moveTo(EndEffectorSpecification endEffectorSpecification) {
        if (!this.active) { return; }

        // TODO RETHINK THIS UGLY PIECE OF CODE-
        KinematicsModule.inverseSolve(armManipulatorService.getArmState(), endEffectorSpecification)
            .exceptionally(e -> {
                if (e instanceof NoSolutionException) { Log.error("No solution found !"); }
                else { throw new RuntimeException(e); }
                return null;
            }).thenAccept(angles -> {
                if (angles == null) { return; }
                // If a solution is found, the following code executes, otherwise it does not
                AngleSpecification specification = new AngleSpecification(angles);
                // Update the arm with the angles
                try {
                    armManipulatorService.moveGiven(specification);
                // Shouldn't happen since the IKSolver should clamp the angles to give only valid angles
                // or throw a NoSolutionException otherwise
                } catch (AngleOutOfBoundsException e) { throw new IllegalStateException(e); }
            });
    }

    @Override
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        if (!this.active) { Log.warn("Cannot change angle while animation is playing !"); return; }
        
        armManipulatorService.setAngle(jointIndex, angleRad);
    }

    @Override
    public double getAngle(int jointIndex) { return armManipulatorService.getAngle(jointIndex); }
}
