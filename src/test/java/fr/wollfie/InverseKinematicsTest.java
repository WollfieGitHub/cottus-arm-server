package fr.wollfie;

import fr.wollfie.cottus.dto.ArmSpecification;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class InverseKinematicsTest {
    
    @Inject ArmManualControllerService manualControllerService;
    @Inject ArmManipulatorService manipulatorService;
    
    @Test
    void inverseKinematicsIsFastEnough() throws NoSolutionException, AngleOutOfBoundsException {
        Vector3D goalPos = Vector3D.of(400, 0, 500);

        ArmSpecification specification = new AngleSpecification( 0, 0, 0, 0, 0, 0, 0 );
        manipulatorService.moveGiven(specification);
        
        long last = System.currentTimeMillis();
        
        manualControllerService.moveEndEffectorWith(
                goalPos, Rotation.Identity, Math.PI/2.0
        );
        long current = System.currentTimeMillis();
        
        List<Joint> joints = manipulatorService.getArmState().joints();
        Joint joint = joints.get(joints.size()-1);
        
        double error = joint.getTransform().getOrigin().subtract(goalPos).norm();
        assertThat(error, lessThan(1.0));
        assertTrue(current-last < 50);
    }
}
