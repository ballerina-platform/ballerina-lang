package org.ballerinalang.test.util;

import org.ballerinalang.core.model.types.BUnionType;

public class CyclicUnionData {
    public io.ballerina.runtime.api.types.UnionType runtimeUnionType;
    public BUnionType newUnionTypeGettingCreated;

    public CyclicUnionData(io.ballerina.runtime.api.types.UnionType runtimeUnionType,
                           BUnionType newUnionTypeGettingCreated) {
        this.runtimeUnionType = runtimeUnionType;
        this.newUnionTypeGettingCreated = newUnionTypeGettingCreated;
    }
}
