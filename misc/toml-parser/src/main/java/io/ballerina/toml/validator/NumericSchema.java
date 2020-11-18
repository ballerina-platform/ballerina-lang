package io.ballerina.toml.validator;

import java.util.Optional;

public class NumericSchema extends Schema {
    private Double minimum;
    private Double maximum;

    public NumericSchema(TypeEnum type) {
        super(type);
    }

    public NumericSchema(TypeEnum type, Double minimum, Double maximum) {
        super(type);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Optional<Double> getMinimum() {
        return Optional.ofNullable(minimum);
    }

    public Optional<Double> getMaximum() {
        return Optional.ofNullable(maximum);
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }
}
