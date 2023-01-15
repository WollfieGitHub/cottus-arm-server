package fr.wollfie.cottus.models.arm.positioning.workspace;

import fr.wollfie.cottus.dto.WorkspaceSample;
import fr.wollfie.cottus.models.arm.cottus_arm.DrivenCottusArm;
import fr.wollfie.cottus.services.ArmStateService;
import fr.wollfie.cottus.services.WorkspaceSampler;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.intervals.ConvexInterval;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class WorkspaceSamplerImpl implements WorkspaceSampler {

    @Inject ArmStateService armStateService;

    @Override
    public WorkspaceSample computeFromJointSpace(int... nbPoints) {
        throw new NotImplementedYet();
    }

    @Override
    public WorkspaceSample computeFrom3DSpace(ConvexInterval xAxis, int nbPointsX, ConvexInterval yAxis, int nbPointsY, ConvexInterval zAxis, int nbPointsZ) {
        throw new NotImplementedYet();
    }
}
