package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

import java.util.List;

/**
 * Enum model.
 */
public class EnumType extends Type {
    @Expose
    public List<Type> members;

    public EnumType(List<Type> members) {
        this.typeName = "enum";
        this.members = members;
    }
}
