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

package io.ballerina.projectdesign.servicemodel.nodevisitors;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ComputedResourceAccessSegmentNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projectdesign.servicemodel.components.Interaction;
import io.ballerina.projectdesign.servicemodel.components.ResourceId;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.tools.diagnostics.Location;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.projectdesign.ProjectDesignConstants.TYPE_MAP;

/**
 * Visitor class for RemoteMethodCallAction nodes.
 *
 * @since 2201.2.2
 */
public class ActionNodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final Package currentPackage;
    private final List<Interaction> interactionList = new LinkedList<>();

    public ActionNodeVisitor(SemanticModel semanticModel, Package currentPackage) {

        this.semanticModel = semanticModel;
        this.currentPackage = currentPackage;
    }

    public List<Interaction> getInteractionList() {
        return interactionList;
    }

    @Override
    public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {

        String clientName = String.valueOf(clientResourceAccessActionNode.expression()).trim();
        String resourceMethod = String.valueOf(clientResourceAccessActionNode.methodName().get().name().text());
        String resourcePath = getResourcePath(clientResourceAccessActionNode.resourceAccessPath());

        StatementNodeVisitor statementVisitor = new StatementNodeVisitor(clientName, semanticModel);
        NonTerminalNode parent = clientResourceAccessActionNode.parent().parent();

        // todo: get the connector type using semantic model
        //todo : implement using semantic model. Need to wait till bug fix
        while (statementVisitor.getServiceId() == null || statementVisitor.getServiceId().isEmpty()) { //isEmpty
            parent = parent.parent();
            if (parent != null) {
                parent.accept(statementVisitor);
            } else {
                break;
            }
        }

        Interaction resourceId = new Interaction(
                new ResourceId(statementVisitor.getServiceId(), resourceMethod, resourcePath),
                statementVisitor.getConnectorType());
        interactionList.add(resourceId);

    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {

        String clientName = String.valueOf(remoteMethodCallActionNode.expression()).trim();
        if (remoteMethodCallActionNode.expression() instanceof FieldAccessExpressionNode) {
            NameReferenceNode fieldName = ((FieldAccessExpressionNode)
                    remoteMethodCallActionNode.expression()).fieldName();
            if (fieldName instanceof SimpleNameReferenceNode) {
                clientName = ((SimpleNameReferenceNode) fieldName).name().text();
            }
            // todo : Other combinations
        } else if (remoteMethodCallActionNode.expression() instanceof SimpleNameReferenceNode) {
            clientName = ((SimpleNameReferenceNode) remoteMethodCallActionNode.expression()).name().text();
        }
        // todo : Other combinations
        String resourceMethod = remoteMethodCallActionNode.methodName().name().text();
        NonTerminalNode parent = remoteMethodCallActionNode.parent().parent();
        StatementNodeVisitor statementVisitor = new StatementNodeVisitor(clientName, semanticModel);

        //todo : implement using semantic model. Need to wait till bug fix
        // semanticModel.symbol(remoteMethodCallActionNode.expression()); -> returns null

        while (statementVisitor.getServiceId() == null || statementVisitor.getServiceId().isEmpty()) {
            parent = parent.parent();
            if (parent != null) {
                parent.accept(statementVisitor);
            } else {
                break;
            }
        }
        Interaction interaction = new Interaction(new ResourceId(statementVisitor.getServiceId(),
                resourceMethod, null), statementVisitor.getConnectorType());
        interactionList.add(interaction);

    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {

        if (functionCallExpressionNode.functionName() instanceof SimpleNameReferenceNode) {
            String methodName = ((SimpleNameReferenceNode) functionCallExpressionNode.functionName()).name().text();
            Optional<Symbol> symbol = semanticModel.symbol(functionCallExpressionNode.functionName());
            symbol.ifPresent(value -> findInteractions(methodName, value));
            if (!functionCallExpressionNode.arguments().isEmpty()) {
                functionCallExpressionNode.arguments().forEach(arg -> {
                    arg.accept(this);
                });
            }
        }
        // todo : Other combinations
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {

        if (methodCallExpressionNode.methodName() instanceof SimpleNameReferenceNode) {
            String methodName = ((SimpleNameReferenceNode) methodCallExpressionNode.methodName()).name().text();
            Optional<Symbol> symbol = semanticModel.symbol(methodCallExpressionNode.methodName());
            symbol.ifPresent(value -> findInteractions(methodName, value));
            if (!methodCallExpressionNode.arguments().isEmpty()) {
                methodCallExpressionNode.arguments().forEach(arg -> {
                    arg.accept(this);
                });
            }
        }
        // todo : Other combinations
    }

    private void findInteractions(String methodName, Symbol methodSymbol) {

        Optional<Location> location = methodSymbol.getLocation();
        Optional<ModuleSymbol> optionalModuleSymbol = methodSymbol.getModule();
        if (optionalModuleSymbol.isPresent()) {
            ModuleID moduleID = optionalModuleSymbol.get().id();
            currentPackage.modules().forEach(module -> {
                if (Objects.equals(moduleID.moduleName(), module.moduleName().toString())) {
                    Collection<DocumentId> documentIds = module.documentIds();
                    for (DocumentId documentId : documentIds) {
                        SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
                        // todo : Improve the logic
                        NonTerminalNode node = ((ModulePartNode) syntaxTree.rootNode())
                                .findNode(location.get().textRange());
                        if (!node.isMissing()) {
                            if (node instanceof FunctionDefinitionNode) {
                                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
                                String referencedFunctionName = functionDefinitionNode.functionName().text();
                                if (methodName.equals(referencedFunctionName)) {
                                    ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(semanticModel,
                                            currentPackage);
                                    functionDefinitionNode.accept(actionNodeVisitor);
                                    interactionList.addAll(actionNodeVisitor.getInteractionList());
                                }
                            } else if (node instanceof MethodDeclarationNode) {
                                MethodDeclarationNode methodDeclarationNode = (MethodDeclarationNode) node;
                                String referencedFunctionName = methodDeclarationNode.methodName().text();
                                if (methodName.equals(referencedFunctionName)) {
                                    ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(semanticModel,
                                            currentPackage);
                                    methodDeclarationNode.accept(actionNodeVisitor);
                                    interactionList.addAll(actionNodeVisitor.getInteractionList());
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private String getResourcePath(SeparatedNodeList<Node> accessPathNodes) {

        StringBuilder resourcePathBuilder = new StringBuilder();
        for (Node accessPathNode : accessPathNodes) {
            if (accessPathNode.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                resourcePathBuilder.append(((IdentifierToken) accessPathNode).text());
            } else if (accessPathNode.kind() == SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT) {
                ComputedResourceAccessSegmentNode accessSegmentNode =
                        (ComputedResourceAccessSegmentNode) accessPathNode;
                ExpressionNode expressionNode = accessSegmentNode.expression();
                if (expressionNode.kind() == SyntaxKind.STRING_LITERAL) {
                    resourcePathBuilder.append(String.format("/[%s]", TYPE_MAP.get(SyntaxKind.STRING_LITERAL)));
                } else if (expressionNode.kind().equals(SyntaxKind.NUMERIC_LITERAL)) {
                    SyntaxKind numericKind = ((BasicLiteralNode) expressionNode).literalToken().kind();
                    if (numericKind.equals(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)) {
                        resourcePathBuilder.append(String.format("/[%s]", TYPE_MAP.get(
                                SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)));
                    } else if (numericKind.equals(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN)) {
                        resourcePathBuilder.append(String.format("/[%s]", SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN));
                    } else {
                        resourcePathBuilder.append(String.format("/[%s]", SyntaxKind.NUMERIC_LITERAL));
                    }
                } else if (expressionNode.kind().equals(SyntaxKind.BOOLEAN_LITERAL)) {
                    resourcePathBuilder.append(String.format("/[%s]", SyntaxKind.BOOLEAN_LITERAL));
                } else if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                        expressionNode.kind() == SyntaxKind.FIELD_ACCESS) {
                    String varType = semanticModel.typeOf(expressionNode).get().signature();
                    resourcePathBuilder.append("/").append("[").append(varType.trim()).append("]");
                }
            }
        }
        return resourcePathBuilder.toString();
    }
}
