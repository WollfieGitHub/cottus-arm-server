package fr.wollfie.cottus.models.animation.preview;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.AnimationPreviewPoint;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.cottus_arm.DrivenCottusArm;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.services.AnimationSamplerService;
import fr.wollfie.cottus.services.ArmCommunicationService;
import fr.wollfie.cottus.services.ArmStateService;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AnimationSampler implements AnimationSamplerService {
    
    @Inject
    ArmStateService armStateService;
    @Inject ArmCommunicationService armCommunicationService;
    
    /**
     * Creates a set of sampled points from the animation, representing the 
     * end effector's position and direction
     * @param animation The animation to sample
     * @param nbPointsPerSec The number of points to create
     * @return The sampled animation to preview
     */
    public AnimationPreview sample(ArmAnimation animation, int nbPointsPerSec) throws NoSolutionException {
        double duration = animation.getDurationSecs();
        DrivenCottusArm arm = new DrivenCottusArm(armStateService.getArmState());
        
        List<AnimationPreviewPoint> samples = new ArrayList<>();

        double timestamp;
        int nbPoints = (int) (animation.getDurationSecs() * nbPointsPerSec);
        double dt = animation.getDurationSecs() / nbPoints;
        
        for (int i = 0; i < nbPoints; i++) {
            try {
                timestamp = i * dt;
                ArmSpecification specification = animation.evaluateAt(timestamp);

                List<Double> angles = specification.getAnglesFor(arm);

                Vector posRot = KinematicsModule.forward( arm.dhTable(), Vector.fromList(angles), true );
                Vector3D position = Vector3D.of(posRot.get(0), posRot.get(1), posRot.get(2));
                Vector3D direction = Vector3D.of(posRot.get(3), posRot.get(4), posRot.get(5));

                samples.add(new AnimationPreviewPointImpl(
                        position, direction, timestamp
                ));
                
                arm.setAngles(angles);
            } catch (NoSolutionException | AngleOutOfBoundsException e) { /* Silenced */ }
        }
        
        return new AnimationPreviewImpl(samples, duration);
    }

    /**
     * <p>
     *     Calculate a lower bound for the time the arm will take to go through all the sampled points
     *     of the specified animation, given the configured speed for the stepper motors.
     * </p>
     * <p>
     *     The bigger the amount of sample points in the animation, the more accurate the lower 
     *     bound will be to reality
     * </p>
     * @param animation The animation
     * @param nbPointsPerSec Number of sample points
     * @return The lower bound for the time in seconds the arm will take to go through
     * all the animation's points.
     */
    public double getMinTimeSec(ArmAnimation animation, int nbPointsPerSec) throws NoSolutionException {
        // TODO MAYBE TAKE INTO ACCOUNT ACCELERATION TOO
        double motorsRadPerSec = armCommunicationService.getMotorSpeed();
        CottusArm arm = armStateService.getArmState();

        int nbPoints = (int) (animation.getDurationSecs() * nbPointsPerSec);
        double dt = animation.getDurationSecs() / nbPoints;
        double sumSec = 0;
        
        Vector aCurr = Vector.fromList( animation.evaluateAt( 0 ).getAnglesFor(arm) );
        Vector aNext, aDiff;
        double aDiffMax, tDiffMax;
        for (int i = 1; i < nbPoints-1; i++) {

            try {
                aNext = Vector.fromList( animation.evaluateAt( (i+1) * dt ).getAnglesFor(arm) );
                aDiff = aNext.minus(aCurr);

                aDiffMax = aDiff.toList().stream().max(Double::compareTo).orElse(Double.MAX_VALUE);
                tDiffMax = aDiffMax / motorsRadPerSec;
                sumSec += tDiffMax;

                aCurr = aNext;
            } catch (NoSolutionException e) { /* Silenced */ }

        }
        return sumSec;
    }
}
