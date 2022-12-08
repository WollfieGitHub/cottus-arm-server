package fr.wollfie.cottus.models.arm.positioning.articulations;

import fr.wollfie.cottus.dto.Articulation;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public class RootSimulatedArticulation extends ArticulationImpl {
    
    RootSimulatedArticulation(String name, Axis3D axis, double lengthMm) {
        super(name, null, axis, lengthMm);
    }

    @Override
    public void update() {
        transform.setLocalRotation(Rotation.from(axis.getUnitVector().scaledBy(angleRad)));
        transform.setLocalPosition(Vector3D.Zero());
    }

    @Override
    public Articulation getParent() {
        return null;
    }
}
