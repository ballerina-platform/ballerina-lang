package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

/**
 * Error type model.
 */
public class ErrorType extends Type {
    @Expose
    public boolean isErrorUnion;
    @Expose
    public Type errorUnion;
    @Expose
    public Type detailType;

    public ErrorType() {
        this.typeName = "error";
    }
}
