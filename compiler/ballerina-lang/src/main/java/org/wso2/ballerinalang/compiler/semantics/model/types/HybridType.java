package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.SemType;

/**
 * Represents SemType and BType components of a type.
 */
public class HybridType {
    private final SemType semTypeComponent; // null means component is empty
    private final BType bTypeComponent; // null means component is empty. More than one BType will have BUnionType

    public HybridType(SemType semTypeComponent, BType bTypeComponent) {
        this.semTypeComponent = semTypeComponent;
        this.bTypeComponent = bTypeComponent;
    }

    public SemType getSemTypeComponent() {
        return semTypeComponent;
    }

    public BType getBTypeComponent() {
        return bTypeComponent;
    }
}
