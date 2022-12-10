package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

public class IKSolution {

    public final double value;
    public final IKTag tag;

    private IKSolution(double value, IKTag tag) {
        this.value = value;
        this.tag = tag;
    }

    public static IKSolution of(double value) { return new IKSolution(value, null); }
    public static IKSolution ofTagged(double value, IKTag tag) { return new IKSolution(value, tag); }
    
}
