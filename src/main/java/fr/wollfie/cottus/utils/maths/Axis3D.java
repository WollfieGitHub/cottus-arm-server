package fr.wollfie.cottus.utils.maths;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Axis3D {
    
    X(Vector3D.of(1, 0, 0)),
    Y(Vector3D.of(0, 1, 0)),
    Z(Vector3D.of(0, 0, 1)),
    ;
    
    @JsonProperty("unitVector") private final Vector3D unitVector;
    @JsonGetter("unitVector") public Vector3D getUnitVector() { return this.unitVector; }
    
    Axis3D(Vector3D unitVector) { this.unitVector = unitVector; }
}
