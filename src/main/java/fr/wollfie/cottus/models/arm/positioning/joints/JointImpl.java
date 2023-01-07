package fr.wollfie.cottus.models.arm.positioning.joints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.dto.JointTransform;
import fr.wollfie.cottus.exception.AngleOutOfBoundsException;
import fr.wollfie.cottus.dto.Joint;
import io.quarkus.logging.Log;
import io.smallrye.common.constraint.Nullable;

public class JointImpl implements Joint {
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       FIELDS                                         ||
// ||                                                                                      ||
// \\======================================================================================//
    
//=========   ====  == =
//      TRANSFORM PROPERTY
//=========   ====  == =
    
    @JsonIgnore protected final JointTransform transform;
    @Override public JointTransform getTransform() { return this.transform; }

//=========   ====  == =
//      PARENT PROPERTY
//=========   ====  == =
    
    @Nullable protected final Joint parent;
    @Override public Joint getParent() { return parent; }
    
//=========   ====  == =
//      VIRTUAL PROPERTY
//=========   ====  == =
    
    protected final boolean virtual;
    @Override public boolean isVirtual() { return virtual; }

    //=========   ====  == =
//      ARTICULATION PROPERTIES
//=========   ====  == =

    private final JointBounds bounds;
    @Override public JointBounds getBounds() { return bounds; }

    @Override public double getAngleRad() { return this.transform.getAngle(); } 
    
    @Override
    public void setAngleRad(double angleRad) throws AngleOutOfBoundsException {
        if (bounds.isOutOfBounds(angleRad)) { throw new AngleOutOfBoundsException(); }
        
        this.transform.setAngle(angleRad);
    }
    
    private final String name;
    @Override public String getName() { return name; }

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       CONSTRUCTOR                                    ||
// ||                                                                                      ||
// \\======================================================================================//
    
    public JointImpl(
            @JsonProperty("name") String name,
            Joint parent, JointBounds bounds,
            JointTransform transform,
            boolean virtual
    ) {
        this.bounds = bounds;
        this.parent = parent;
        this.name = name;
        this.virtual = virtual;
        this.transform = transform;
    }

}
