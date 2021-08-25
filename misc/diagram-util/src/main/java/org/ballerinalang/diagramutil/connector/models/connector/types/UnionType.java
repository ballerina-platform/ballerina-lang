package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Union type model.
 */
public class UnionType extends Type {
    @Expose
    public List<Type> members;

    public UnionType() {
        this.typeName = "union";
        this.members = new ArrayList<>();
    }
}
