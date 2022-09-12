/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.componentmodel.servicemodel.components.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Visitor class for ServiceDeclaration nodes.
 */
public class ServiceDeclarationNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;

    public ServiceDeclarationNodeVisitor(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    private final List<Service> services = new ArrayList<>();

    public List<Service> getServices() {
        return services;
    }


    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        StringBuilder serviceNameBuilder = new StringBuilder();
        String serviceId = "";
        NodeList<Node> serviceNameNodes = serviceDeclarationNode.absoluteResourcePath();
        for (Node serviceNameNode : serviceNameNodes) {
            serviceNameBuilder.append(serviceNameNode.toString());
        }

        String serviceName = serviceNameBuilder.toString().startsWith("/") ?
                serviceNameBuilder.substring(1) : serviceNameBuilder.toString();

        Optional<MetadataNode> metadataNode = serviceDeclarationNode.metadata();
        if (metadataNode.isPresent()) {
            NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
            serviceId = VisitorUtil.getId(annotationNodes);
        }

        ResourceAccessorDefinitionNodeVisitor resourceAccessorDefinitionNodeVisitor =
                new ResourceAccessorDefinitionNodeVisitor(serviceId, semanticModel);
        serviceDeclarationNode.accept(resourceAccessorDefinitionNodeVisitor);
        services.add(new Service(serviceName.trim(), serviceId, resourceAccessorDefinitionNodeVisitor.getResources()));
    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {

    }

    @Override
    public void visit(EnumDeclarationNode enumDeclarationNode) {

    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

    }

    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {

    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {

    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {

    }
}
