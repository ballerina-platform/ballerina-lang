/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.componentmodel.ComponentModel;
import io.ballerina.componentmodel.Utils;
import io.ballerina.componentmodel.servicemodel.components.Service;
import io.ballerina.componentmodel.servicemodel.components.ServiceAnnotation;
import io.ballerina.projects.Package;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.componentmodel.ComponentModelingConstants.FORWARD_SLASH;
import static io.ballerina.componentmodel.ComponentModelingConstants.LISTENER;

/**
 * Visitor class for ServiceDeclaration nodes.
 *
 * @since 2201.2.2
 */
public class ServiceDeclarationNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final ComponentModel.PackageId packageId;
    private final Package currentPackage;
    private final List<Service> services = new ArrayList<>();

    public ServiceDeclarationNodeVisitor(SemanticModel semanticModel, Package currentPackage,
                                         ComponentModel.PackageId packageId) {

        this.packageId = packageId;
        this.semanticModel = semanticModel;
        this.currentPackage = currentPackage;
    }

    public List<Service> getServices() {
        return services;
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {

        StringBuilder serviceNameBuilder = new StringBuilder();
        ServiceAnnotation serviceAnnotation = new ServiceAnnotation("", "");
        NodeList<Node> serviceNameNodes = serviceDeclarationNode.absoluteResourcePath();
        for (Node serviceNameNode : serviceNameNodes) {
            serviceNameBuilder.append(serviceNameNode.toString().replace("\"", ""));
        }

        Optional<MetadataNode> metadataNode = serviceDeclarationNode.metadata();
        if (metadataNode.isPresent()) {
            NodeList<AnnotationNode> annotationNodes = metadataNode.get().annotations();
            serviceAnnotation = Utils.getServiceAnnotation(annotationNodes);
        }

        String serviceName = serviceNameBuilder.toString().startsWith(FORWARD_SLASH) ?
                serviceNameBuilder.substring(1) : serviceNameBuilder.toString();

        ServiceMemberFunctionNodeVisitor serviceMemberFunctionNodeVisitor =
                new ServiceMemberFunctionNodeVisitor(serviceAnnotation.getId(),
                        semanticModel, currentPackage, packageId);
        serviceDeclarationNode.accept(serviceMemberFunctionNodeVisitor);
        services.add(new Service(serviceName.trim(), serviceAnnotation.getId(), getServiceType(serviceDeclarationNode),
                serviceMemberFunctionNodeVisitor.getResources(),
                serviceMemberFunctionNodeVisitor.remoteFunctions, serviceAnnotation));
    }

    private String getServiceType(ServiceDeclarationNode serviceDeclarationNode) {

        String serviceType = null;
        SeparatedNodeList<ExpressionNode> expressionNodes = serviceDeclarationNode.expressions();
        for (ExpressionNode expressionNode : expressionNodes) {
            if (expressionNode instanceof ExplicitNewExpressionNode) {
                ExplicitNewExpressionNode explicitNewExpressionNode = (ExplicitNewExpressionNode) expressionNode;
                //todo: Implement using semantic model - returns null
                TypeDescriptorNode typeDescriptorNode = explicitNewExpressionNode.typeDescriptor();
                if (typeDescriptorNode instanceof QualifiedNameReferenceNode) {
                    QualifiedNameReferenceNode listenerNode = (QualifiedNameReferenceNode) typeDescriptorNode;
                    Optional<Symbol> listenerSymbol = semanticModel.symbol(listenerNode);
                    if (listenerSymbol.isPresent() && (listenerSymbol.get() instanceof TypeReferenceTypeSymbol)) {
                        serviceType = ((TypeReferenceTypeSymbol)
                                listenerSymbol.get()).signature().replace(LISTENER, "");
                    } else {
                        serviceType = listenerNode.modulePrefix().text().trim();
                    }
                }
            } else if (expressionNode instanceof SimpleNameReferenceNode) { // support when use listener from a var
                Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(expressionNode);
                if (typeSymbol.isPresent() && typeSymbol.get().typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                    serviceType = typeSymbol.get().signature().replace(LISTENER, "");
                }
            }
        }
        return serviceType;
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
