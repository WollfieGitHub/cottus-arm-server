package fr.wollfie.cottus.models.arm;

import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.InverseKinematicModule;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ArmManualController implements ArmManualControllerService {

    @Inject ArmManipulatorService armManipulatorService;
    
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
        
        Vector angleSolutions = InverseKinematicModule.inverseSolve(armManipulatorService.getArmState(), position, rotation);
        // If a solution is found, the following code executes, otherwise it does not
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < angleSolutions.dim; i++) { angles.add(angleSolutions.get(i)); }
        angles.add(effectorAngle);
        try {
            // Update the arm with the angles
            armManipulatorService.moveGiven(new AngleSpecification(angles));
            // It may be possible that the IK gives out of bound angles
        } catch (AngleOutOfBoundsException e) { throw new NoSolutionException(); }
    }

    @Override
    public void setAngle(int jointIndex, double angleRad) throws AngleOutOfBoundsException {
        if (!this.active) { return; }
        
        armManipulatorService.setAngle(jointIndex, angleRad);
    }

}
