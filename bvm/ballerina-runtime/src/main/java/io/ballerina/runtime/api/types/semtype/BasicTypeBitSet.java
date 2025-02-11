package io.ballerina.runtime.api.types.semtype;

public sealed class BasicTypeBitSet permits SemType {

    private int all;

    public BasicTypeBitSet(int all) {
        this.all = all;
    }

    protected void setAll(int all) {
        this.all = all;
    }

    public final int all() {
        assert all != -1 : "SemType created by no arg constructor must be initialized with setAll";
        return all;
    }

    public BasicTypeBitSet union(BasicTypeBitSet basicTypeBitSet) {
        return new BasicTypeBitSet(all() | basicTypeBitSet.all());
    }

    public BasicTypeBitSet intersection(BasicTypeBitSet basicTypeBitSet) {
        return new BasicTypeBitSet(all() & basicTypeBitSet.all());
    }
}
