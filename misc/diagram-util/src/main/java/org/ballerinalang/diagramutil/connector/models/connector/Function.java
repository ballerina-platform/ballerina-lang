package org.ballerinalang.diagramutil.connector.models.connector;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * Function model.
 */
public class Function {
    @Expose
    public String name;
    @Expose
    public List<Type> parameters;
    @Expose
    public Type returnType;
    @Expose
    public boolean isRemote;
    @Expose
    public String documentation;
    @Expose
    public Map<String, String> displayAnnotation;

    public Function(String name, List<Type> parameters, Type returnType, Map<String, String> displayAnnotation,
                    boolean isRemote, String documentation) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.displayAnnotation = displayAnnotation;
        this.isRemote = isRemote;
        this.documentation = documentation;
    }
}
