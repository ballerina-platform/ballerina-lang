package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.HashMap;
import java.util.Map;

/**
 * Visitor for validation related to code to cloud.
 *
 * @since 2.0.0
 */
public class C2CVisitor extends NodeVisitor {

    Map<String, ListenerInfo> listeners = new HashMap<>();
    Map<String, ServiceInfo> services = new HashMap<>();

    @Override
    public void visit(ListenerDeclarationNode listenerDeclarationNode) {
        String listenerName = listenerDeclarationNode.variableName().text();
        Node initializer = listenerDeclarationNode.initializer();
        if (initializer.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            ImplicitNewExpressionNode initializerNode = (ImplicitNewExpressionNode) initializer;
            ParenthesizedArgList parenthesizedArgList = initializerNode.parenthesizedArgList().get();
            FunctionArgumentNode functionArgumentNode = parenthesizedArgList.arguments().get(0);
            ExpressionNode expression = ((PositionalArgumentNode) functionArgumentNode).expression();
            int port = Integer.parseInt(((BasicLiteralNode) expression).literalToken().text());
            ListenerInfo listenerInfo = new ListenerInfo(listenerName, port);
            listeners.put(listenerName, listenerInfo);
        }
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        ListenerInfo listenerInfo;
        ExpressionNode expressionNode = serviceDeclarationNode.expressions().get(0);
        if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) expressionNode;
            String listenerName = referenceNode.name().text();
            listenerInfo = listeners.get(listenerName);
            if (listenerInfo == null) {
                //TODO if listner is not found in the file. search in other packages?
                return;
            }

        } else {
            //TODO Inline
            listenerInfo = null;
        }
        String servicePath = toAbsoluteServicePath(serviceDeclarationNode.absoluteResourcePath());
        ServiceInfo serviceInfo = new ServiceInfo(listenerInfo, serviceDeclarationNode,
                servicePath);
        NodeList<Node> function = serviceDeclarationNode.members();
        for (Node node : function) {
            if (node.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
                String httpMethod = functionDefinitionNode.functionName().text();
                String resourcePath = toAbsoluteServicePath(functionDefinitionNode.relativeResourcePath());
                serviceInfo.addResource(new ResourceInfo(functionDefinitionNode, httpMethod, resourcePath));
            }
        }
        services.put(servicePath, serviceInfo);
    }

    private String toAbsoluteServicePath(NodeList<Node> servicePathNodes) {
        StringBuilder absoluteServicePath = new StringBuilder();
        for (Node serviceNode : servicePathNodes) {
            absoluteServicePath.append(serviceNode.toString());
        }
        return absoluteServicePath.toString();
    }

    public Map<String, ListenerInfo> getListeners() {
        return listeners;
    }

    public Map<String, ServiceInfo> getServices() {
        return services;
    }
}
