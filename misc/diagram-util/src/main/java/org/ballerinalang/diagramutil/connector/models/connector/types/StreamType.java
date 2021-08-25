package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

/**
 * Stream type model.
 */
public class StreamType extends Type {
    @Expose
    public Type leftTypeParam;
    @Expose
    public Type rightTypeParam;

    public StreamType(Type leftTypeParam, Type rightTypeParam) {
        this.typeName = "stream";
        this.leftTypeParam = leftTypeParam;
        this.rightTypeParam = rightTypeParam;
    }
}
