package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Intersection type model.
 */
public class IntersectionType extends Type {

    @Expose
    public List<Type> members;

    public IntersectionType() {
        this.typeName = "intersection";
        this.members = new ArrayList<>();
    }
}
