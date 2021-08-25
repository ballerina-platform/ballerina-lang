package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

import java.util.List;

/**
 * Record type model.
 */
public class RecordType extends Type {
    @Expose
    public List<Type> fields;
    @Expose
    public boolean hasRestType;
    @Expose
    public Type restType;

    public RecordType(List<Type> fields, Type restType) {
        this.typeName = "record";
        this.fields = fields;
        this.restType = restType;
        if (restType != null) {
            this.hasRestType = true;
        }
    }
}
