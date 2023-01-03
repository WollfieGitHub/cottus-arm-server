package fr.wollfie;

import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static java.lang.Math.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class InverseKinematicsTest {
    
    @Inject ArmManualControllerService manualControllerService;
    @Inject ArmManipulatorService manipulatorService;
    
    @Test
    void inverseKinematicsIsFastEnough() throws NoSolutionException, AngleOutOfBoundsException {
        Vector3D goalPos = Vector3D.of(20,0, 753.2-40);

        ArmSpecification specification = new AngleSpecification( 0, -PI, 0, -PI, 0, -PI, 0 );
        manipulatorService.moveGiven(specification);
        
        long last = System.currentTimeMillis();
        
        manualControllerService.moveEndEffectorWith(
                goalPos, 
                Rotation.from(Vector3D.of(0, 1.0, 0)), 
                PI/2.0
        );
        long current = System.currentTimeMillis();
        
        Joint joint = manipulatorService.getArmState().getEndEffector();
        
        double error = joint.getTransform().getOrigin().minus(goalPos).norm();
        assertThat(error, lessThan(1.0));
        assertTrue(current-last < 50);
    }
}
