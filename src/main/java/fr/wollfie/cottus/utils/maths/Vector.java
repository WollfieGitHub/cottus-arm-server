package fr.wollfie.cottus.utils.maths;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.utils.Preconditions;
import org.ejml.simple.SimpleMatrix;

import javax.swing.plaf.TableHeaderUI;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Vector {
    
    /** The dimension of the vector */
    @JsonIgnore public final int dim;
    /** The coordinates in the vector */
    @JsonIgnore private final double[] values;

    /** Default constructor for the vector */
    public Vector(double... values) {
        this.values = values;
        this.dim = values.length;
    }

    /** @return A Vector with only zero coordinates of the specified dimension */
    public static Vector Zero(int dim) {
        double[] values = new double[dim];
        return new Vector(values);
    }

    /** @return A Vector with only zero coordinates except for one at the specified index */
    public static Vector unit(int index, int dim) {
        Preconditions.checkArgument(index < dim);
        double[] values = new double[dim];
        values[index] = 1;
        return new Vector(values);
    }

    /**
     * Creates a vector from a list of real values
     * @param values The real values
     * @return The vector corresponding to the values
     */
    public static Vector fromList(List<Double> values) {
        return new Vector(values.stream().mapToDouble(Double::doubleValue).toArray());
    }

    /**
     * Apply a coordinate-wise operation between {@code this} vector and {@code that}
     * to create a new vector
     * @param operator The operation to use between each coordinates
     * @param that The other vector for the right side of the operation
     * @return A new vector with its coordinates created from the operator
     */
    public Vector apply(DoubleBinaryOperator operator, Vector that) {
        Preconditions.checkArgument(this.dim == that.dim);
        double[] values = new double[this.dim];
        for (int i = 0; i < this.dim; i++) {
            values[i] = operator.applyAsDouble(this.values[i], that.values[i]);
        }
        return new Vector(values);
    }

    /**
     * Apply an operation on all coordinates of {@code this} Vector and 
     * return a new vector
     * @param operator The operation to apply to each coordinate
     * @return A new vector
     */
    public Vector apply(DoubleUnaryOperator operator) {
        double[] values = new double[this.dim];
        for (int i = 0; i < this.dim; i++) {
            values[i] = operator.applyAsDouble(this.values[i]);
        }
        return new Vector(values);
    }

    /**
     * Return the result of {@code this} vector minus {@code that} vector
     * @param that The vector to subtract
     * @return The result of the operation
     */
    public Vector minus(Vector that) {
        return apply((a,b) -> a-b, that);
    }

    /**
     * Return the result of {@code this} vector plus {@code that} vector
     * @param that The vector to add
     * @return The result of the operation
     */
    public Vector plus(Vector that) {
        return apply(Double::sum, that);
    }

    /**
     * Scale each coordinates by the specified scalar
     * @param scalar The scalar to scale each coordinates
     * @return The new vector with its coordinates scaled
     */
    public Vector scaled(double scalar) {
        double[] values = new double[dim];
        for (int i = 0; i < dim; i++) { values[i] = this.values[i]*scalar; }
        return new Vector(values);
    }
    
    /** @return The norm of {@code this} vector, squared (Often used to save a {@code sqrt()} operation */
    public double normSquared() {
        double sum = 0.0;
        double[] values = apply((a,b) -> a*b, this).values;
        for (double value : values) { sum += value; }
        return sum;
    }
    
    /** @return The norm of {@code this} Vector */
    public double norm() { return Math.sqrt(this.normSquared()); }

    /**
     * The coordinate at index i of this vector
     * @param i The index of the coordinate to return
     * @return The coordinate/value at index i
     */
    public double get(int i) { return this.values[i]; }

    /** @return A {@link Vector3D} composed of the first 3 coordinates of this vector */
    public Vector3D extract3D() {
        return Vector3D.of(get(0), get(1), get(2));
    }

    /** @return All values in the vector */
    public double[] getValues() { return Arrays.copyOf(values, values.length); }

    /** @return The normalized version of this vector */
    public Vector normalized() {
        if (this.isZero()) { return Vector.Zero(this.dim); }
        
        double norm = this.norm();
        return apply(v -> v/norm);
    }

    /** @return True if the vector is Zero, false otherwise */
    @JsonIgnore
    public boolean isZero() {
        for (double value : values) {
            if (!MathUtils.isZero(value)) { return false; }
        }
        return true;
    }

    /** @return True if the vector has any coordinate that is {@link Double#NaN}, false otherwise */
    @JsonIgnore
    public boolean isNan() {
        for (double value : values) {
            if (Double.isNaN(value)) { return true; }
        }
        return false;
    }

    /**
     * Clamp the values of the vector that exceed min (max) to min (max)
     * @param min The minimum value
     * @param max The maximum value
     * @return A vector with all its values between min and max
     */
    public Vector clamped(double min, double max) {
        double[] values = this.getValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) { values[i] = min; }
            else if (values[i] > max) { values[i] = max; }
        }
        return new Vector(values);
    }

    @Override
    public String toString() {
        return "Vector{" + Arrays.toString(values) + '}';
    }
    
    /** @return Return the vector as a {@link SimpleMatrix} object with 1 column and {@link Vector#dim} rows*/
    public SimpleMatrix toMatrix() {
        double[][] values = new double[dim][1];
        for (int i = 0; i < dim; i++) {
            values[i][0] = this.values[i];
        }
        return new SimpleMatrix(values);
    }

    /** @return This vector with its values as a list */
    public List<Double> toList() {
        return DoubleStream.of(this.values).boxed().collect(Collectors.toList());
    }
}
