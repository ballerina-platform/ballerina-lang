package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

/**
 * Array type model.
 */
public class ArrayType extends Type {
    @Expose
    public Type memberType;

    public ArrayType(Type memberType) {
        this.typeName = "array";
        this.memberType = memberType;
    }
}
