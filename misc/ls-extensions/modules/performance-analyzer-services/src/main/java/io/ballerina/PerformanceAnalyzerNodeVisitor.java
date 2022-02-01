/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import io.ballerina.component.ActionInvocationNode;
import io.ballerina.component.EndPointNode;
import io.ballerina.component.ForStatementNode;
import io.ballerina.component.IfStatementNode;
import io.ballerina.component.Node;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.utils.ParserUtil;
import org.eclipse.lsp4j.Range;

import java.util.HashMap;
import java.util.Optional;

/**
 * Visitor to discover the program structure.
 *
 * @since 2.0.0
 */
public class PerformanceAnalyzerNodeVisitor extends NodeVisitor {

    private static final long DEFAULT_LOOP_SIZE = 2;
    static final String ENDPOINTS_KEY = "endpoints";
    static final String ACTION_INVOCATION_KEY = "actionInvocations";

    private final HashMap<LineRange, Object> variableMap;
    private final HashMap<LineRange, String> referenceMap;
    private final HashMap<String, EndPointNode> endPointDeclarationMap;

    private final Node startNode;
    private final SemanticModel model;
    private final String file;
    private final Range range;
    private Node currentNode;
    private Document document;
    private boolean withinRange = false;
    private int uuid;

    public PerformanceAnalyzerNodeVisitor(SemanticModel model, String file, Range range) {

        this.variableMap = new HashMap<>();
        this.referenceMap = new HashMap<>();
        this.endPointDeclarationMap = new HashMap<>();
        this.startNode = new Node();
        this.currentNode = this.startNode;
        this.model = model;
        this.file = file;
        this.range = range;
    }

    public void setDocument(Document document) {

        this.document = document;
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {

        modulePartNode.members().forEach(moduleMemberDeclarationNode -> moduleMemberDeclarationNode.accept(this));
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {

        Optional<Symbol> symbol = model.symbol(moduleVariableDeclarationNode);
        if (symbol.isEmpty() || moduleVariableDeclarationNode.initializer().isEmpty()) {
            return;
        }

        boolean clientObject = isClientObject(symbol.get());
        if (clientObject) {
            resolveEndPoint(symbol.get(), moduleVariableDeclarationNode.initializer().get());
        }

        LineRange lineRange = moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().
                location().lineRange();

        if (clientObject || isRecordObject(symbol.get())) {
            Optional<ExpressionNode> expressionNode = moduleVariableDeclarationNode.initializer();

            if (expressionNode.isPresent() && expressionNode.get().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
                this.registerVariableRef(lineRange, moduleVariableDeclarationNode);
            }
        } else if ((symbol.get().kind() == SymbolKind.VARIABLE) &&
                (moduleVariableDeclarationNode.initializer().get().kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION)) {

            this.registerVariableRef(lineRange, moduleVariableDeclarationNode);
        }

        if (moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().kind() ==
                SyntaxKind.CAPTURE_BINDING_PATTERN) {

            SyntaxKind initializerKind = moduleVariableDeclarationNode.initializer().get().kind();
            if (initializerKind == SyntaxKind.NUMERIC_LITERAL || initializerKind == SyntaxKind.STRING_LITERAL) {
                this.registerVariableRef(lineRange, moduleVariableDeclarationNode);
            }
        }
        moduleVariableDeclarationNode.typedBindingPattern().accept(this);
        moduleVariableDeclarationNode.initializer().ifPresent(initializer -> initializer.accept(this));
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

        LineRange lineRange = functionDefinitionNode.lineRange();
        withinRange = functionDefinitionNode.syntaxTree().filePath().equals(file)
                && range.getStart().getLine() == lineRange.startLine().line()
                && range.getStart().getCharacter() == lineRange.startLine().offset()
                && range.getEnd().getLine() == lineRange.endLine().line()
                && range.getEnd().getCharacter() == lineRange.endLine().offset();

        functionDefinitionNode.functionBody().accept(this);
        withinRange = false;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {

        Optional<Symbol> symbol = model.symbol(variableDeclarationNode);
        if (variableDeclarationNode.initializer().isEmpty() || symbol.isEmpty()) {
            return;
        }

        boolean clientObject = isClientObject(symbol.get());
        if (clientObject) {
            resolveEndPoint(symbol.get(), variableDeclarationNode.initializer().get());
        }

        LineRange lineRange = variableDeclarationNode.typedBindingPattern().bindingPattern().
                location().lineRange();

        if (clientObject || isRecordObject(symbol.get())) {
            Optional<ExpressionNode> expressionNode = variableDeclarationNode.initializer();

            if (expressionNode.isPresent() && expressionNode.get().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
                this.registerVariableRef(lineRange, variableDeclarationNode);
            }
        } else if ((symbol.get().kind() == SymbolKind.VARIABLE) &&
                (variableDeclarationNode.initializer().get().kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION)) {
            this.registerVariableRef(lineRange,
                    variableDeclarationNode);
        }

        if (variableDeclarationNode.typedBindingPattern().bindingPattern().kind() ==
                SyntaxKind.CAPTURE_BINDING_PATTERN) {

            SyntaxKind initializerKind = variableDeclarationNode.initializer().get().kind();
            if (initializerKind == SyntaxKind.NUMERIC_LITERAL || initializerKind == SyntaxKind.STRING_LITERAL) {
                this.registerVariableRef(lineRange, variableDeclarationNode);
            }
        }

        variableDeclarationNode.initializer().ifPresent(initializer -> initializer.accept(this));
    }

    @Override
    public void visit(IfElseStatementNode ifElseStatementNode) {

        IfStatementNode ifStatementNode = new IfStatementNode();
        Node currentParentNode = this.currentNode;

        Node ifBodyNode = new Node();
        this.currentNode = ifBodyNode;

        ifElseStatementNode.ifBody().accept(this);

        ifStatementNode.setIfBody(ifBodyNode.getNextNode());

        Node elseBodyNode = new Node();
        this.currentNode = elseBodyNode;

        ifElseStatementNode.elseBody().ifPresent(elseBody -> {
                    elseBody.accept(this);
                    ifStatementNode.setElseBody(elseBodyNode.getNextNode());
                }
        );
        this.currentNode = currentParentNode;
        this.setChildNode(ifStatementNode);
    }

    @Override
    public void visit(ForEachStatementNode forEachStatementNode) {

        io.ballerina.compiler.syntax.tree.Node node = forEachStatementNode.actionOrExpressionNode();
        long iterationsCount = DEFAULT_LOOP_SIZE;

        if (node.kind() == SyntaxKind.BRACED_EXPRESSION) {
            BracedExpressionNode bracedExpressionNode = (BracedExpressionNode) node;
            node = bracedExpressionNode.expression();

            if (node.kind() == SyntaxKind.BINARY_EXPRESSION) {
                BinaryExpressionNode binaryExpressionNode = (BinaryExpressionNode) node;
                io.ballerina.compiler.syntax.tree.Node rhsExpr = binaryExpressionNode.rhsExpr();
                io.ballerina.compiler.syntax.tree.Node lhsExpr = binaryExpressionNode.lhsExpr();
                if (rhsExpr.kind() == SyntaxKind.NUMERIC_LITERAL && lhsExpr.kind() == SyntaxKind.NUMERIC_LITERAL) {
                    long rhsValue = Long.parseLong(rhsExpr.toSourceCode().trim());
                    long lhsValue = Long.parseLong(lhsExpr.toSourceCode().trim());
                    iterationsCount = rhsValue - lhsValue + 1;
                }
            }
        }

        ForStatementNode forStatementNode = new ForStatementNode(iterationsCount);
        Node currentParentNode = this.currentNode;

        Node forBodyNode = new Node();
        this.currentNode = forBodyNode;

        forEachStatementNode.blockStatement().accept(this);
        forStatementNode.setForBody(forBodyNode.getNextNode());

        this.currentNode = currentParentNode;

        if (forEachStatementNode.blockStatement() != null) {
            this.setChildNode(forStatementNode);
        }
    }

    @Override
    public void visit(WhileStatementNode whileStatementNode) {

        ForStatementNode forStatementNode = new ForStatementNode(DEFAULT_LOOP_SIZE);
        Node currentParentNode = this.currentNode;

        Node forBodyNode = new Node();
        this.currentNode = forBodyNode;

        whileStatementNode.whileBody().accept(this);
        forStatementNode.setForBody(forBodyNode.getNextNode());

        this.currentNode = currentParentNode;
        if (forStatementNode.getForBody() != null) {
            this.setChildNode(forStatementNode);
        }
    }

    @Override
    public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {

        resolveActionInvocation(remoteMethodCallActionNode);
        remoteMethodCallActionNode.expression().accept(this);
    }

    @Override
    public void visit(AssignmentStatementNode assignmentStatementNode) {

        if (assignmentStatementNode.varRef().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE &&
                assignmentStatementNode.expression().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {

            Optional<Symbol> referenceSymbol = model.symbol(assignmentStatementNode.varRef());
            Optional<Symbol> expressionSymbol = model.symbol(assignmentStatementNode.expression());

            if (referenceSymbol.isEmpty() || expressionSymbol.isEmpty()) {
                return;
            }

        }

        assignmentStatementNode.varRef().accept(this);
        assignmentStatementNode.expression().accept(this);
    }

    private void setChildNode(Node node) {

        this.currentNode.setNextNode(node);
        this.currentNode = node;
    }

    private void registerVariableRef(LineRange key, Object value) {

        this.variableMap.put(key, value);
    }

    private void resolveActionInvocation(RemoteMethodCallActionNode remoteMethodCallActionNode) {

        String actionName = remoteMethodCallActionNode.methodName().toString();
        String actionPath = null;
        if (remoteMethodCallActionNode.arguments().size() > 0) {
            SeparatedNodeList<FunctionArgumentNode> arguments = remoteMethodCallActionNode.arguments();
            FunctionArgumentNode functionArgumentNode = arguments.get(0);
            if (functionArgumentNode.kind() == SyntaxKind.POSITIONAL_ARG) {
                SyntaxKind parameterKind = ((PositionalArgumentNode) functionArgumentNode).expression().kind();
                if (parameterKind == SyntaxKind.STRING_LITERAL) {
                    actionPath = remoteMethodCallActionNode.arguments().get(0).toString();
                    if (actionPath.startsWith("\"") && actionPath.endsWith("\"")) {
                        actionPath = actionPath.substring(1, actionPath.length() - 1);
                    }
                } else if (parameterKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    Optional<Symbol> expressionSymbol = model.symbol(remoteMethodCallActionNode.expression());
                    if (expressionSymbol.isPresent()) {
                        Optional<Location> location = expressionSymbol.get().getLocation();
                        if (location.isEmpty()) {
                            return;
                        }
                        LineRange lineRange = location.get().lineRange();

                        VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode)
                                variableMap.get(lineRange);
                        if (variableDeclarationNode != null) {
                            if (variableDeclarationNode.initializer().isPresent()) {
                                ExpressionNode token = variableDeclarationNode.initializer().get();
                                if (token.kind() == SyntaxKind.STRING_LITERAL) {
                                    actionPath = ((BasicLiteralNode) token).literalToken().text();
                                    actionPath = actionPath.substring(1, actionPath.length() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }

        LineRange actionPos = (remoteMethodCallActionNode).lineRange();

        LineRange actionExpRange = remoteMethodCallActionNode.expression().lineRange();
        LinePosition actionStartPos = actionExpRange.startLine();
        LinePosition actionEndPos = actionExpRange.endLine();
        LinePosition actionExpPos =
                LinePosition.from(actionStartPos.line(), (actionStartPos.offset() + actionEndPos.offset()) / 2);

        Optional<Symbol> symbol = model.symbol(document, actionExpPos);
        if (symbol.isPresent()) {
            Optional<Location> location = symbol.get().getLocation();
            if (location.isEmpty()) {
                return;
            }
            LineRange lineRange = location.get().lineRange();

            if (withinRange) {
                String pos = actionPos.filePath() + "/" + actionPos;
                ActionInvocationNode actionNode = new ActionInvocationNode(getUUID(lineRange),
                        actionName, actionPath, pos);
                this.currentNode.setNextNode(actionNode);
                this.setChildNode(actionNode);
            }
        }
    }

    private void resolveEndPoint(Symbol symbol, ExpressionNode expressionNode) {

        String url = null;

        if (expressionNode.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            ImplicitNewExpressionNode node = (ImplicitNewExpressionNode) expressionNode;
            if (node.parenthesizedArgList().isPresent() && !node.parenthesizedArgList().get().arguments().isEmpty()) {
                SeparatedNodeList<FunctionArgumentNode> arguments = node.parenthesizedArgList().get().arguments();
                url = findBaseUrl(arguments);
            }
        } else if (expressionNode.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            ExplicitNewExpressionNode node = (ExplicitNewExpressionNode) expressionNode;
            if (!node.parenthesizedArgList().arguments().isEmpty()) {
                UrlFinder urlFinder = new UrlFinder();
                node.accept(urlFinder);
                url = extractURL(urlFinder.getToken());
            }
        } else if (expressionNode.kind() == SyntaxKind.CHECK_EXPRESSION) {
            ExpressionNode expNode = ((CheckExpressionNode) expressionNode).expression();
            resolveEndPoint(symbol, expNode);
            return;
        } else {
            return;
        }

        TypeSymbol typeSymbol = getTypeDescriptor(((VariableSymbol) symbol).typeDescriptor());

        String signature = typeSymbol.signature();
        Optional<ModuleSymbol> typeSymbolModule = typeSymbol.getModule();
        if (typeSymbolModule.isEmpty() || symbol.getLocation().isEmpty()) {
            return;
        }

        LineRange lineRange = symbol.getLocation().get().lineRange();
        EndPointNode endpoint = new EndPointNode(typeSymbolModule.get().id().orgName() +
                "/" + typeSymbolModule.get().id().moduleName(),
                signature.substring(signature.lastIndexOf(":") + 1), url, lineRange);
        this.endPointDeclarationMap.put(getUUID(lineRange), endpoint);
    }

    private String getUUID(LineRange lineRange) {

        if (referenceMap.containsKey(lineRange)) {
            return referenceMap.get(lineRange);
        }

        String uuid = String.valueOf(this.uuid++);
        referenceMap.put(lineRange, uuid);
        return uuid;
    }

    private String findBaseUrl(SeparatedNodeList<FunctionArgumentNode> arguments) {
        // Assuming that the first argument is the base URL.
        for (FunctionArgumentNode argumentNode : arguments) {
            UrlFinder urlFinder = new UrlFinder();
            argumentNode.accept(urlFinder);

            String url = urlFinder.getToken();
            if (url.isEmpty()) {
                continue;
            }
            if (argumentNode.kind() == SyntaxKind.POSITIONAL_ARG &&
                    ((PositionalArgumentNode) argumentNode).expression().kind() == SyntaxKind.STRING_LITERAL) {
                argumentNode.accept(this);
                return extractURL(url);
            } else if (argumentNode.kind() == SyntaxKind.NAMED_ARG &&
                    ((NamedArgumentNode) argumentNode).expression().kind() == SyntaxKind.STRING_LITERAL) {
                argumentNode.accept(this);
                return extractURL(url);
            }
        }
        return "";
    }

    private String extractURL(String arg) {

        if (arg.startsWith("\"") && arg.endsWith("\"")) {
            return arg.substring(1, arg.length() - 1);
        } else {
            return arg;
        }
    }

    private boolean isClientObject(Symbol symbol) {

        if (symbol.kind() != SymbolKind.VARIABLE) {
            return false;
        }

        TypeSymbol typeSymbol = getTypeDescriptor(((VariableSymbol) symbol).typeDescriptor());

        return typeSymbol instanceof Qualifiable && ((Qualifiable) typeSymbol).qualifiers().contains(Qualifier.CLIENT);
    }

    private boolean isRecordObject(Symbol symbol) {

        if (symbol.kind() != SymbolKind.VARIABLE) {
            return false;
        }

        TypeSymbol typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
        TypeDescKind kind = typeSymbol.typeKind();

        if (kind == null) {
            return false;
        }

        return getTypeKind(kind, typeSymbol) == TypeDescKind.RECORD;
    }

    private TypeDescKind getTypeKind(TypeDescKind kind, TypeSymbol typeSymbol) {

        if (kind == TypeDescKind.TYPE_REFERENCE) {
            return ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor().typeKind();
        }
        return typeSymbol.typeKind();
    }

    private TypeSymbol getTypeDescriptor(TypeSymbol typeSymbol) {

        TypeDescKind kind = typeSymbol.typeKind();

        if (kind == null) {
            return typeSymbol;
        }

        if (kind == TypeDescKind.TYPE_REFERENCE) {
            return ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
        }
        return typeSymbol;
    }

    public HashMap<String, Object> getActionInvocations() {

        ParserUtil.getReducedTree(this.startNode);

        HashMap<String, Object> invocationInfo = new HashMap<>();
        invocationInfo.put(ENDPOINTS_KEY, this.endPointDeclarationMap);
        invocationInfo.put(ACTION_INVOCATION_KEY, this.startNode);
        return invocationInfo;
    }
}
