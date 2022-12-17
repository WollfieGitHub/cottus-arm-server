package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.models.animation.ArmAnimation;
import fr.wollfie.cottus.utils.maths.Vector3D;

import java.util.Objects;

public final class LineToAnimation implements ArmAnimation {
    
    private final Vector3D position;
    private final double timeSec;

    private boolean isPlaying;
    
    public LineToAnimation(
            @JsonProperty("position") Vector3D position,
            @JsonProperty("timeSec") double timeSec
    ) {
        this.position = position;
        this.timeSec = timeSec;
    }

    @Override
    public ArmSpecification evaluateAt(double secFromStart) { return null; }

    @Override
    public double getDurationSecs() { return timeSec; }

    @Override
    public double getSecondsElapsedFromStart() { return 0; }

    @Override
    public boolean isPlaying() { return false; }


}
