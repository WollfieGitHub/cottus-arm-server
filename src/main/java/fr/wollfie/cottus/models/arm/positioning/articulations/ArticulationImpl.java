package fr.wollfie.cottus.models.arm.positioning.articulations;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.wollfie.cottus.models.arm.positioning.Transform;
import fr.wollfie.cottus.dto.Articulation;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.common.constraint.Nullable;

public class ArticulationImpl implements Articulation {
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       FIELDS                                         ||
// ||                                                                                      ||
// \\======================================================================================//
    
//=========   ====  == =
//      TRANSFORM PROPERTY
//=========   ====  == =
    
    @JsonIgnore protected final Transform transform;
    @JsonGetter("transform") public Transform getTransform() { return this.transform; }

//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    @Nullable protected final Articulation parent;
    
    @Override @JsonGetter("parent")
    public Articulation getParent() { return parent; }
    
//=========   ====  == =
//      ARTICULATION PROPERTIES
//=========   ====  == =
    
    @JsonProperty("angleRad")
    protected double angleRad;
    
    @Override @JsonGetter("angleRad")
    public double getAngleRad() { return this.angleRad; }

    @Override @JsonSetter("angleRad")
    public void setAngleRad(double angleRad) { this.angleRad = angleRad; }
    
    @JsonProperty("axis")
    protected final Axis3D axis;
    @JsonGetter("axis") public Axis3D getAxis() { return axis; }
    
    @JsonProperty("length") private final double lengthMm;
    @JsonGetter("length") public double getLength() { return lengthMm; }
    
    @JsonProperty("name") private final String name;
    @JsonGetter("name") public String getName() { return name; }

//=========   ====  == =
//      OVERRIDE PROPERTIES 
//=========   ====  == =
    
    @Override
    public void update() {
        transform.setLocalRotation(Rotation.from(axis.getUnitVector().scaledBy(angleRad)));
        transform.setLocalPosition(Axis3D.X.getUnitVector().scaledBy(parent.getLength()));
    }
    
    @Override public Vector3D getGlobalPosition() { return transform.getGlobalPosition(); }
    @Override public Vector3D getLocalPosition() {
        return transform.getLocalPosition();
    }
    @Override public Rotation getGlobalRotation() {
        return transform.getGlobalRotation();
    }
    @Override public Rotation getLocalRotation() {
        return transform.getLocalRotation();
    }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       CONSTRUCTOR                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    ArticulationImpl(
            @JsonProperty("name") String name,
            ArticulationImpl parent,
            @JsonProperty("axis") Axis3D axis,
            @JsonProperty("length") double lengthMm
    ) {
        this.transform = parent == null 
                ? Transform.createFromRoot()
                : Transform.createFrom(parent.transform);
        this.parent = parent;
        this.axis = axis;
        this.lengthMm = lengthMm;
        this.name = name;
    }
    
}
