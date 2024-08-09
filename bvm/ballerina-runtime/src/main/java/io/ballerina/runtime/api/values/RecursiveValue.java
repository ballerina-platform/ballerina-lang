package io.ballerina.runtime.api.values;

import io.ballerina.runtime.api.types.semtype.Definition;

import java.util.Optional;

interface RecursiveValue {
    Optional<Definition> getReadonlyShapeDefinition();

    void setReadonlyShapeDefinition(Definition definition);
    void resetReadonlyShapeDefinition();
}
