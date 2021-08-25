package org.ballerinalang.diagramutil.connector.models.connector;

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * Connector model.
 */
public class Connector {
    @Expose
    public String orgName;
    @Expose
    public String moduleName;
    @Expose
    public String packageName;
    @Expose
    public String version;
    @Expose
    public String name;
    @Expose
    public String documentation;
    @Expose
    public Map<String, String> displayAnnotation;
    @Expose
    public List<Function> functions;

    public Connector(String orgName, String moduleName, String packageName, String version, String name,
                     String documentation, Map<String, String> displayAnnotation, List<Function> functions) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.version = version;
        this.name = name;
        this.documentation = documentation;
        this.displayAnnotation = displayAnnotation;
        this.functions = functions;
    }
}
