package fr.wollfie.cottus.models.arm.positioning.articulations;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.models.arm.positioning.Transform;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.common.constraint.Nullable;

import java.util.function.Consumer;

public class JointImpl implements Joint {
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       FIELDS                                         ||
// ||                                                                                      ||
// \\======================================================================================//
    
//=========   ====  == =
//      TRANSFORM PROPERTY
//=========   ====  == =
    
    @JsonIgnore protected final Transform transform;
    @Override @JsonGetter("transform") 
    public Transform getTransform() { return this.transform; }

//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    @Nullable protected final Joint parent;
    
    @Override @JsonGetter("parent")
    public Joint getParent() { return parent; }
    
//=========   ====  == =
//      ARTICULATION PROPERTIES
//=========   ====  == =
    
    @Override @JsonGetter("angleRad")
    public double getAngleRad() { 
        // This works given that the articulation only rotates around one axis
        return this.transform.getLocalRotation().getEulerAngles().norm();
    }

    @Override @JsonSetter("angleRad")
    public void setAngleRad(double angleRad) { 
        this.transform.setLocalRotation(Rotation.from(Axis3D.Z.getUnitVector().scaledBy(angleRad)));
    }
    
    @JsonGetter("length") public double getLength() { 
        if (parent == null) { return Vector3D.Zero().distanceTo(this.transform.getLocalPosition()); }
        return this.parent.getTransform().getGlobalPosition().distanceTo(this.transform.getGlobalPosition());
    }
    
    @JsonProperty("name") private final String name;
    @JsonGetter("name") public String getName() { return name; }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       CONSTRUCTOR                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    public JointImpl(
            @JsonProperty("name") String name,
            Joint parent,
            Consumer<Transform> transformInitFunction
    ) {
        this.parent = parent;
        this.name = name;
        this.transform = parent == null
                ? Transform.createFromRoot()
                : Transform.createFrom(parent.getTransform());
        transformInitFunction.accept(this.transform);
    }
    
}
