package fr.wollfie.cottus.models.animation.preview;

import com.fasterxml.jackson.annotation.JsonGetter;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.animation.AnimationPreview;
import fr.wollfie.cottus.dto.animation.AnimationPreviewPoint;
import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.services.AnimationSamplerService;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AnimationSampler implements AnimationSamplerService {
    
    @Inject ArmManipulatorService armManipulatorService;
    
    /**
     * Creates a set of sampled points from the animation, representing the 
     * end effector's position and direction
     * @param animation The animation to sample
     * @param nbPoints The number of points to create
     * @return The sampled animation to preview
     */
    public AnimationPreview sample(ArmAnimation animation, int nbPoints) throws NoSolutionException {
        double duration = animation.getDurationSecs();
        CottusArm arm = armManipulatorService.getArmState();
        
        List<AnimationPreviewPoint> samples = new ArrayList<>();

        double timestamp;
        double dt = duration / nbPoints;
        for (int i = 0; i < nbPoints; i++) {
            timestamp = i * dt;
            ArmSpecification specification = animation.evaluateAt(timestamp);
            
            Vector posRot = KinematicsModule.forward(arm.dhTable(), Vector.fromList(specification.getAnglesFor(arm)));
            Vector3D position = Vector3D.of(posRot.get(0), posRot.get(1), posRot.get(2));
            Vector3D direction = Vector3D.of(posRot.get(3), posRot.get(4), posRot.get(5));
            
            samples.add(new AnimationPreviewPointImpl(
                    position, direction, timestamp
            ));
        }
        
        return new AnimationPreviewImpl(samples, duration);
    }
}
