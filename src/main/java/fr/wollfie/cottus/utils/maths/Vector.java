package fr.wollfie.cottus.utils.maths;

import fr.wollfie.cottus.utils.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

public class Vector {
    
    public final int dim;
    private final double[] values;

    public Vector(double... values) {
        this.values = values;
        this.dim = values.length;
    }
    
    public static Vector Zero(int dim) {
        double[] values = new double[dim];
        return new Vector(values);
    }
    
    public Vector apply(DoubleBinaryOperator operator, Vector that) {
        Preconditions.checkArgument(this.dim == that.dim);
        double[] values = new double[this.dim];
        for (int i = 0; i < this.dim; i++) {
            values[i] = operator.applyAsDouble(this.values[i], that.values[i]);
        }
        return new Vector(values);
    }
    
    public Vector apply(DoubleUnaryOperator operator) {
        double[] values = new double[this.dim];
        for (int i = 0; i < this.dim; i++) {
            values[i] = operator.applyAsDouble(this.values[i]);
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
        double[] values = new double[dim];
        for (int i = 0; i < dim; i++) { values[i] = this.values[i]*scalar; }
        return new Vector(values);
    }
    
    public double normSquared() {
        double sum = 0.0;
        double[] values = apply((a,b) -> a*b, this).values;
        for (double value : values) { sum += value; }
        return sum;
    }
    
    public double norm() { return Math.sqrt(this.normSquared()); }

    public double get(int i) { return this.values[i]; }

    public Vector3D to3D() {
        return Vector3D.of(get(0), get(1), get(2));
    }

    public double[] getValues() { return values; }

    @Override
    public String toString() {
        return "Vector{" + Arrays.toString(values) + '}';
    }
}
