package org.ballerinalang.langserver.extensions.ballerina.diagram.completion;

import java.util.List;

public class SnippetParameter {
    private String name;
    private ParameterKind kind;
    private List<String> parameters;

    public SnippetParameter() {
    }

    public SnippetParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterKind getKind() {
        return kind;
    }

    public void setKind(ParameterKind kind) {
        this.kind = kind;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public enum ParameterKind {
        List,
        Input
    }
}
