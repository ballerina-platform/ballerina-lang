package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

/**
 * Inclusion type model.
 */
public class InclusionType extends Type {
    @Expose
    public Type inclusionType;

    public InclusionType(Type inclusionType) {
        this.typeName = "inclusion";
        this.inclusionType = inclusionType;
    }
}
