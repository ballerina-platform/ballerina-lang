package io.ballerina.componentmodel.servicemodel.components;

import java.util.List;

/**
 * Represents a Ballerina remote function.
 */
public class RemoteFunction {

    private final String name;
    private final List<FunctionParameter> parameters;
    private final List<String> returns;

    private final List<Interaction> interactions;

    public RemoteFunction(String name, List<FunctionParameter> parameters, List<String> returns,
                          List<Interaction> interactions) {

        this.name = name;
        this.parameters = parameters;
        this.returns = returns;
        this.interactions = interactions;
    }

    public String getName() {

        return name;
    }

    public List<FunctionParameter> getParameters() {

        return parameters;
    }

    public List<String> getReturns() {

        return returns;
    }

    public List<Interaction> getInteractions() {

        return interactions;
    }
}
