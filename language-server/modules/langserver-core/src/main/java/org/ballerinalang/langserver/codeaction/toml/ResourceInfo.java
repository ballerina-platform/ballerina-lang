package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;

/**
 * Represents Resource of a service in a ballerina document.
 *
 * @since 2.0.0
 */
public class ResourceInfo {
    private FunctionDefinitionNode node;
    private String httpMethod;
    private String path;

    public ResourceInfo(FunctionDefinitionNode node, String httpMethod, String path) {
        this.node = node;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    public FunctionDefinitionNode getNode() {
        return node;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" +
                ", httpMethod='" + httpMethod + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
