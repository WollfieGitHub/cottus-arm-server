package fr.wollfie.cottus.utils.maths;

import fr.wollfie.cottus.utils.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

public class Vector {
    
    public final int dim;
    private final double[] values;

    public Vector(double... values) {
        this.values = values;
        this.dim = values.length;
    }
    
    private Vector apply(DoubleBinaryOperator operator, Vector that) {
        Preconditions.checkArgument(this.dim == that.dim);
        double[] values = new double[this.dim];
        for (int i = 0; i < this.dim; i++) {
            values[i] = operator.applyAsDouble(this.values[i], that.values[i]);
        }
        return new Vector(values);
    }
    
    public static Vector unit(int index, int dim) {
        Preconditions.checkArgument(index < dim);
        double[] values = new double[dim];
        values[index] = 1;
        return new Vector(values);
    }
    
    public Vector subtract(Vector that) {
        return apply((a,b) -> a-b, that);
    }

    public Vector add(Vector that) {
        return apply(Double::sum, that);
    }
    
    public Vector scaled(double scalar) {
        return new Vector(Arrays.stream(this.values).map(d -> d*scalar).toArray());
    }
    
    public double normSquared() {
        return Arrays.stream(apply((a,b) -> a*b, this).values)
                .reduce(0.0, Double::sum);
    }
    
    public double norm() { return Math.sqrt(this.normSquared()); }

    public double get(int i) { return this.values[i]; }

    public Vector3D to3D() {
        return Vector3D.of(get(0), get(1), get(2));
    }
}
