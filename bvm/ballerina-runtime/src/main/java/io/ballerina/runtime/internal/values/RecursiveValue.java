package io.ballerina.runtime.internal.values;

import io.ballerina.runtime.api.types.semtype.Definition;

/**
 * Every value that can contain a recursive reference should implement this interface.
 */
interface RecursiveValue<E extends Definition> {

    E getReadonlyShapeDefinition();

    void setReadonlyShapeDefinition(E definition);

    void resetReadonlyShapeDefinition();
}
