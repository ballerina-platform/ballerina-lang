package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.ExecutionPlan;

public class FunctionDefinition extends AbstractDefinition {

    private String language;
    private String body;
    private String functionID;
    private Attribute.Type returnType;

    public Attribute.Type getReturnType() {
        return returnType;
    }

    public String getLanguage() {
        return language;
    }

    public String getBody() {
        return body;
    }

    public String getFunctionID() {
        return functionID;
    }

    public FunctionDefinition language(String language) {
        this.language = language;
        return this;
    }

    public FunctionDefinition body(String body) {
        this.body = body;
        return this;
    }

    public FunctionDefinition functionID(String functionID) {
        this.functionID = functionID;
        return this;
    }

    public FunctionDefinition type(Attribute.Type type) {
        this.returnType = type;
        return this;
    }
}
