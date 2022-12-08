package fr.wollfie.cottus.models.arm.positioning.articulations;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public class ChildSimulatedArticulation extends ArticulationImpl {

    ChildSimulatedArticulation(
            @JsonProperty("name") String name,
            @JsonProperty("parent") ArticulationImpl parent,
            @JsonProperty("axis") Axis3D axis,
            @JsonProperty("length") double lengthMm
    ) {
        super(name, parent, axis, lengthMm);
    }

    @Override
    public void update() {
        transform.setLocalRotation(Rotation.from(axis.getUnitVector().scaledBy(angleRad)));
        transform.setLocalPosition(Axis3D.X.getUnitVector().scaledBy(parent.getLength()));
    }
}
