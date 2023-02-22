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

package io.ballerina.architecturemodelgenerator.generators.service.nodevisitors;

import io.ballerina.architecturemodelgenerator.diagnostics.ComponentModelingDiagnostics;
import io.ballerina.architecturemodelgenerator.diagnostics.DiagnosticMessage;
import io.ballerina.architecturemodelgenerator.model.service.Interaction;
import io.ballerina.architecturemodelgenerator.model.service.ResourceId;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Annotatable;
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
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.tools.diagnostics.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.FORWARD_SLASH;
import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.TYPE_MAP;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getClientModuleName;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getElementLocation;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getServiceAnnotation;

/**
 * Visitor class for RemoteMethodCallAction nodes.
 *
 * @since 2201.2.2
 */
public class ActionNodeVisitor extends NodeVisitor {
    private final PackageCompilation packageCompilation;
    private final SemanticModel semanticModel;
    private final Package currentPackage;
    private final List<Interaction> interactionList = new LinkedList<>();
    private final String filePath;
    private final Set<NameReferenceNode> visitedFunctionNames = new HashSet<>();
    private final String modulePrefix;

    public ActionNodeVisitor(PackageCompilation packageCompilation, SemanticModel semanticModel,
                             Package currentPackage, String filePath) {
        this(packageCompilation, semanticModel, currentPackage, filePath, new HashSet<>(), null);
    }

    public ActionNodeVisitor(PackageCompilation packageCompilation, SemanticModel semanticModel,
                             Package currentPackage, String filePath, Set<NameReferenceNode> visitedFunctionNames,
                             String modulePrefix) {
        this.packageCompilation = packageCompilation;
        this.semanticModel = semanticModel;
        this.currentPackage = currentPackage;
        this.filePath = filePath;
        this.visitedFunctionNames.addAll(visitedFunctionNames);
        this.modulePrefix = modulePrefix;
    }

    public List<Interaction> getInteractionList() {
        return interactionList;
    }

    public Set<NameReferenceNode> getVisitedFunctionNames() {
        return visitedFunctionNames;
    }

    @Override
    public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {
        NameReferenceNode clientNode = null;

        String resourceMethod = null;
        String resourcePath = null;
        String serviceId = null;

        List<ComponentModelingDiagnostics> diagnostics = new ArrayList<>();
        try {
            if (clientResourceAccessActionNode.expression().kind().equals(SyntaxKind.FIELD_ACCESS)) {
                NameReferenceNode fieldName = ((FieldAccessExpressionNode)
                        clientResourceAccessActionNode.expression()).fieldName();
                if (fieldName.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                    clientNode = fieldName;
                }
                // todo : Other combinations
            } else if (clientResourceAccessActionNode.expression().kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                clientNode = (SimpleNameReferenceNode) clientResourceAccessActionNode.expression();
            } else if (clientResourceAccessActionNode.expression().kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                clientNode = (QualifiedNameReferenceNode) clientResourceAccessActionNode.expression();
            }
            resourceMethod = String.valueOf(clientResourceAccessActionNode.methodName().get().name().text());
            resourcePath = getResourcePath(clientResourceAccessActionNode.resourceAccessPath());

            Optional<Symbol> clientSymbol = semanticModel.symbol(clientNode);
            if (clientSymbol.isPresent()) {
                Annotatable annotatableSymbol = (Annotatable) clientSymbol.get();
                serviceId = getServiceAnnotation(annotatableSymbol, filePath).getId();
            }
        } catch (Exception e) {
            DiagnosticMessage message = DiagnosticMessage.failedToGenerateInteraction(e.getMessage());
            ComponentModelingDiagnostics diagnostic = new ComponentModelingDiagnostics(
                    message.getCode(), message.getDescription(), message.getSeverity(), null, null
            );
            diagnostics.add(diagnostic);
        }

        Interaction interaction = new Interaction(
                new ResourceId(serviceId, resourceMethod, resourcePath),
                getClientModuleName(clientNode, semanticModel), getElementLocation(filePath,
                clientResourceAccessActionNode.lineRange()), diagnostics);
        interactionList.add(interaction);
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
        NameReferenceNode clientNode = null;

        String resourceMethod = null;
        String serviceId = null;

        List<ComponentModelingDiagnostics> diagnostics = new ArrayList<>();
        try {
            if (remoteMethodCallActionNode.expression().kind().equals(SyntaxKind.FIELD_ACCESS)) {
                NameReferenceNode fieldName = ((FieldAccessExpressionNode)
                        remoteMethodCallActionNode.expression()).fieldName();
                if (fieldName.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                    clientNode = fieldName;
                }
                // todo : Other combinations
            } else if (remoteMethodCallActionNode.expression().kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                clientNode = (SimpleNameReferenceNode) remoteMethodCallActionNode.expression();
            } else if (remoteMethodCallActionNode.expression().kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                clientNode = (QualifiedNameReferenceNode) remoteMethodCallActionNode.expression();
            }

            if (clientNode != null) {
                resourceMethod = remoteMethodCallActionNode.methodName().name().text();

                Optional<Symbol> clientSymbol = semanticModel.symbol(clientNode);
                if (clientSymbol.isPresent()) {
                    Annotatable annotatableSymbol = (Annotatable) clientSymbol.get();
                    serviceId = getServiceAnnotation(annotatableSymbol, filePath).getId();
                }
            }
        } catch (Exception e) {
            DiagnosticMessage message = DiagnosticMessage.failedToGenerateInteraction(e.getMessage());
            ComponentModelingDiagnostics diagnostic = new ComponentModelingDiagnostics(
                    message.getCode(), message.getDescription(), message.getSeverity(), null, null
            );
            diagnostics.add(diagnostic);
        }

        if (clientNode != null) {
            Interaction interaction = new Interaction(new ResourceId(serviceId,
                    resourceMethod, null), getClientModuleName(clientNode, semanticModel),
                    getElementLocation(filePath, remoteMethodCallActionNode.lineRange()), diagnostics);
            interactionList.add(interaction);
        }
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        if ((functionCallExpressionNode.functionName() instanceof SimpleNameReferenceNode ||
                functionCallExpressionNode.functionName() instanceof QualifiedNameReferenceNode) &&
                isNodeAlreadyVisited(functionCallExpressionNode.functionName())) {

            visitedFunctionNames.add(functionCallExpressionNode.functionName());
            Optional<Symbol> symbol = semanticModel.symbol(functionCallExpressionNode.functionName());
            symbol.ifPresent(value -> findInteractions(functionCallExpressionNode.functionName(), value));
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
        if (methodCallExpressionNode.methodName() instanceof SimpleNameReferenceNode &&
                isNodeAlreadyVisited(methodCallExpressionNode.methodName())) {

            visitedFunctionNames.add(methodCallExpressionNode.methodName());
            Optional<Symbol> symbol = semanticModel.symbol(methodCallExpressionNode.methodName());
            symbol.ifPresent(value -> findInteractions(methodCallExpressionNode.methodName(), value));
            if (!methodCallExpressionNode.arguments().isEmpty()) {
                methodCallExpressionNode.arguments().forEach(arg -> {
                    arg.accept(this);
                });
            }
        }
        // todo : Other combinations
    }

    private void findInteractions(NameReferenceNode nameNode, Symbol methodSymbol) {

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
                            SemanticModel nextSemanticModel = packageCompilation.getSemanticModel(module.moduleId());
                            boolean isReferredNodeFromSameModule = isReferredNodeFromSameModule(nameNode, module);
                            String modulePrefix = isReferredNodeFromSameModule ?
                                    null : module.moduleName().moduleNamePart();
                            if (node instanceof FunctionDefinitionNode) {
                                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) node;
                                String referencedFunctionName = functionDefinitionNode.functionName().text();
                                if (isReferredFunction(nameNode, referencedFunctionName)) {
                                    ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(packageCompilation,
                                            nextSemanticModel, currentPackage, this.filePath, visitedFunctionNames,
                                            modulePrefix);
                                    functionDefinitionNode.accept(actionNodeVisitor);
                                    interactionList.addAll(actionNodeVisitor.getInteractionList());
                                    visitedFunctionNames.addAll(actionNodeVisitor.getVisitedFunctionNames());
                                }
                            } else if (node instanceof MethodDeclarationNode) {
                                MethodDeclarationNode methodDeclarationNode = (MethodDeclarationNode) node;
                                String referencedFunctionName = methodDeclarationNode.methodName().text();
                                if (isReferredFunction(nameNode, referencedFunctionName)) {
                                    ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(packageCompilation,
                                            nextSemanticModel, currentPackage, this.filePath, visitedFunctionNames,
                                            modulePrefix);
                                    methodDeclarationNode.accept(actionNodeVisitor);
                                    interactionList.addAll(actionNodeVisitor.getInteractionList());
                                    visitedFunctionNames.addAll(actionNodeVisitor.getVisitedFunctionNames());
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
            if (resourcePathBuilder.length() > 0) {
                resourcePathBuilder.append(FORWARD_SLASH);
            }
            if (accessPathNode.kind() == SyntaxKind.IDENTIFIER_TOKEN) {
                resourcePathBuilder.append(((IdentifierToken) accessPathNode).text());
            } else if (accessPathNode.kind() == SyntaxKind.COMPUTED_RESOURCE_ACCESS_SEGMENT) {
                ComputedResourceAccessSegmentNode accessSegmentNode =
                        (ComputedResourceAccessSegmentNode) accessPathNode;
                ExpressionNode expressionNode = accessSegmentNode.expression();
                if (expressionNode.kind() == SyntaxKind.STRING_LITERAL) {
                    resourcePathBuilder.append(String.format("[%s]", TYPE_MAP.get(SyntaxKind.STRING_LITERAL)));
                } else if (expressionNode.kind().equals(SyntaxKind.NUMERIC_LITERAL)) {
                    SyntaxKind numericKind = ((BasicLiteralNode) expressionNode).literalToken().kind();
                    if (numericKind.equals(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)) {
                        resourcePathBuilder.append(String.format("[%s]", TYPE_MAP.get(
                                SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN)));
                    } else if (numericKind.equals(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN)) {
                        resourcePathBuilder.append(String.format("[%s]", SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN));
                    } else {
                        resourcePathBuilder.append(String.format("[%s]", SyntaxKind.NUMERIC_LITERAL));
                    }
                } else if (expressionNode.kind().equals(SyntaxKind.BOOLEAN_LITERAL)) {
                    resourcePathBuilder.append(String.format("[%s]", SyntaxKind.BOOLEAN_LITERAL));
                } else if (expressionNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                        expressionNode.kind() == SyntaxKind.FIELD_ACCESS) {
                    String varType = semanticModel.typeOf(expressionNode).get().signature();
                    resourcePathBuilder.append("[").append(varType.trim()).append("]");
                }
            }
        }
        return resourcePathBuilder.toString();
    }

    private boolean isNodeAlreadyVisited(NameReferenceNode functionName) {
        if (functionName instanceof SimpleNameReferenceNode) {
            return visitedFunctionNames.stream().noneMatch(nameNode -> {
                if (nameNode instanceof SimpleNameReferenceNode) {
                    return ((SimpleNameReferenceNode) nameNode).name().text()
                            .equals(((SimpleNameReferenceNode) functionName).name().text());
                } else if (nameNode instanceof QualifiedNameReferenceNode && modulePrefix != null) {
                    return getQualifiedNameRefNodeFuncNameText((QualifiedNameReferenceNode) nameNode)
                            .equals(modulePrefix + ":" + ((SimpleNameReferenceNode) functionName).name().text());
                }
                return false;
            });
        } else if (functionName instanceof QualifiedNameReferenceNode) {
            return visitedFunctionNames.stream().noneMatch(nameNode -> {
                if (nameNode instanceof QualifiedNameReferenceNode) {
                    return getQualifiedNameRefNodeFuncNameText((QualifiedNameReferenceNode) nameNode)
                            .equals(getQualifiedNameRefNodeFuncNameText((QualifiedNameReferenceNode) functionName));
                }
                return false;
            });
        }
        return false;
    }

    private String getQualifiedNameRefNodeFuncNameText(QualifiedNameReferenceNode nameNode) {
        return nameNode.modulePrefix().text() + ((Token) nameNode.colon()).text() + nameNode.identifier().text();
    }

    private boolean isReferredFunction(NameReferenceNode nameNode, String referredFunctionName) {
        if (nameNode instanceof SimpleNameReferenceNode) {
            return ((SimpleNameReferenceNode) nameNode).name().text().equals(referredFunctionName);
        } else if (nameNode instanceof QualifiedNameReferenceNode) {
            return ((QualifiedNameReferenceNode) nameNode).identifier().text().equals(referredFunctionName);
        }
        return false;
    }

    private boolean isReferredNodeFromSameModule(Node currentNode, Module referredNodeModule) {
        String currentFilePath = currentNode.syntaxTree().filePath();
        Module currentNodeModule = null;

        List<Module> modules = new ArrayList<>();
        currentPackage.modules().forEach(modules::add);

        for (Module module : modules) {
            if (module.documentIds().stream().anyMatch(docId ->
                    module.document(docId).syntaxTree().filePath().equals(currentFilePath))) {
                currentNodeModule = module;
                break;
            }
        }

        return currentNodeModule != null &&
                currentNodeModule.moduleId().moduleName().equals(referredNodeModule.moduleId().moduleName());
    }
}
