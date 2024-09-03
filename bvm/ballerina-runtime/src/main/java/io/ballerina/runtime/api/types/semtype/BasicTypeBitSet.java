package io.ballerina.runtime.api.types.semtype;

public abstract sealed class BasicTypeBitSet permits SemType {

    private int all;

    protected BasicTypeBitSet(int all) {
        this.all = all;
    }

    protected void setAll(int all) {
        this.all = all;
    }

    public final int all() {
        assert all != -1 : "SemType created by no arg constructor must be initialized with setAll";
        return all;
    }
}
