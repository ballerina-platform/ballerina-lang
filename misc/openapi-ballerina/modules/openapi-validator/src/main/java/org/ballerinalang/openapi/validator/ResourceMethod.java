package org.ballerinalang.openapi.validator;

import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This model for handling the service details.
 */
public class ResourceMethod {
    private Diagnostic.DiagnosticPosition resourcePosition;
    private String method;
    private Diagnostic.DiagnosticPosition methodPosition;
    private Map<String, BLangSimpleVariable> parameters;
    private String body;

    ResourceMethod() {
        this.method = null;
        this.methodPosition = null;
        this.resourcePosition = null;
        this.parameters = new HashMap<>();
        this.body = null;
    }

    Diagnostic.DiagnosticPosition getMethodPosition() {
        return methodPosition;
    }

    void setMethodPosition(Diagnostic.DiagnosticPosition methodsPosition) {
        this.methodPosition = methodsPosition;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }

    public void setParameters(List<? extends SimpleVariableNode> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 1) {
                SimpleVariableNode simpleVariableNode = parameters.get(i);
                if (simpleVariableNode instanceof BLangSimpleVariable) {
                    BLangSimpleVariable variable = (BLangSimpleVariable) simpleVariableNode;
                    this.parameters.put(variable.getName().getValue(), variable);
                }
            }
        }
    }

    public List<ResourceParameter> getParamNames() {
        List<ResourceParameter> paramNames = new ArrayList<>();
        for (Map.Entry<String, BLangSimpleVariable> entry : this.parameters.entrySet()) {
            ResourceParameter resourceParameter = new ResourceParameter();
            resourceParameter.setName(entry.getKey());
            if (entry.getValue().type != null && entry.getValue().type.tsymbol != null) {
                resourceParameter.setType(entry.getValue().type.tsymbol.name.getValue());
            }
            resourceParameter.setParameter(entry.getValue());
            paramNames.add(resourceParameter);
        }
        return paramNames;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    void setResourcePosition(Diagnostic.DiagnosticPosition position) {
        this.resourcePosition = position;
    }

    public Diagnostic.DiagnosticPosition getResourcePosition() {
        return resourcePosition;
    }

}
