package fr.wollfie.cottus.models.arm.positioning.articulations;

import fr.wollfie.cottus.dto.Articulation;
import fr.wollfie.cottus.utils.maths.Axis3D;
import io.smallrye.common.constraint.Nullable;

import javax.inject.Singleton;

@Singleton
public class ArticulationFactory {

    /**
     * Builds a new articulation from the specified parameter, you either receive a {@link RootSimulatedArticulation}
     * if the parent is null or {@link ChildSimulatedArticulation} otherwise
     * @param name The name to give to the articulation, to uniquely identify it
     * @param parent A possible parent for the articulation
     * @param axis The axis along which the articulation rotates
     * @param lengthMm The length of the articulation in millimeters
     * @return The newly created articulation
     */
    public Articulation buildSimulated(String name, @Nullable ArticulationImpl parent, Axis3D axis, double lengthMm) { 
        if (parent == null) { return new RootSimulatedArticulation(name, axis, lengthMm); }
        else { return new ChildSimulatedArticulation(name, parent, axis, lengthMm); }
    }

    /**
     * Builds a new driven articulation from the specified parameters
     * @param dependency The articulation on which the driven articulation depends
     * @param parent The parent of the articulation if any
     * @param stepSize The step size for the driven articulation
     * @return The newly created articulation
     */
    public Articulation buildDriven(Articulation dependency, DrivenArticulation parent, double stepSize) {
        return new DrivenArticulation(dependency, parent, stepSize);
    }
}
