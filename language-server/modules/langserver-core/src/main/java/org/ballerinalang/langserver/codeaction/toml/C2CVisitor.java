/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for validation related to code to cloud.
 *
 * @since 2.0.0
 */
public class C2CVisitor extends NodeVisitor {

    private final List<ListenerInfo> listeners = new ArrayList<>();
    private final List<ServiceInfo> services = new ArrayList<>();

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
            listeners.add(listenerInfo);
        }
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        ListenerInfo listenerInfo;
        String servicePath = toAbsoluteServicePath(serviceDeclarationNode.absoluteResourcePath());
        ExpressionNode expressionNode = serviceDeclarationNode.expressions().get(0);
        if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) expressionNode;
            String listenerName = referenceNode.name().text();
            listenerInfo = this.getListener(listenerName);
        } else {
            ExplicitNewExpressionNode refNode = (ExplicitNewExpressionNode) expressionNode;
            int port = Integer.parseInt(refNode.parenthesizedArgList().arguments().get(0).toString());
            listenerInfo = new ListenerInfo(servicePath, port);
            this.listeners.add(listenerInfo);
        }
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
        services.add(serviceInfo);
    }

    private String toAbsoluteServicePath(NodeList<Node> servicePathNodes) {
        StringBuilder absoluteServicePath = new StringBuilder();
        for (Node serviceNode : servicePathNodes) {
            absoluteServicePath.append(serviceNode.toString());
        }
        return absoluteServicePath.toString();
    }

    private ListenerInfo getListener(String name) {
        for (ListenerInfo info : this.listeners) {
            if (info.getName().equals(name)) {
                return info;
            }
        }
        return new ListenerInfo(name, 0);
    }

    public List<ListenerInfo> getListeners() {
        return listeners;
    }

    public List<ServiceInfo> getServices() {
        return services;
    }
}
