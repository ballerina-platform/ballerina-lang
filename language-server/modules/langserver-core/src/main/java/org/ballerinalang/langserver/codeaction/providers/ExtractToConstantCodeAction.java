/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Code Action for extracting to a constant.
 *
 * @since 2201.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractToConstantCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Extract To Constant";
    private static final String CONSTANT_NAME_PREFIX = "CONST";

    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.BOOLEAN_LITERAL, SyntaxKind.NUMERIC_LITERAL,
                SyntaxKind.STRING_LITERAL, SyntaxKind.BINARY_EXPRESSION, SyntaxKind.UNARY_EXPRESSION);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        Node node = positionDetails.matchedCodeActionNode();
        SyntaxKind parentKind = node.parent().kind();
        return  context.currentSyntaxTree().isPresent() && context.currentSemanticModel().isPresent() 
                && parentKind != SyntaxKind.CONST_DECLARATION 
                && parentKind != SyntaxKind.INVALID_EXPRESSION_STATEMENT
                && parentKind != SyntaxKind.CLIENT_DECLARATION 
                && parentKind != SyntaxKind.MODULE_CLIENT_DECLARATION
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                                    RangeBasedPositionDetails posDetails) {

        // Check if the selection is a range or a position
        if (isRange(context.range())) {
            // Selection is a range
            Node node = posDetails.matchedCodeActionNode();
            BasicLiteralNodeValidator nodeValidator = new BasicLiteralNodeValidator();
            node.accept(nodeValidator);
            if (nodeValidator.getInvalidNode()) {
                return Collections.emptyList();
            }

            String constName = getLocalVarName(context);
            String value = node.toSourceCode().strip();
            LineRange replaceRange = node.lineRange();
            Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(node);
            if (typeSymbol.isEmpty()) {
                return Collections.emptyList();
            }

            Position constDeclPosition = getPosition(context);
            String constDeclStr = String.format("const %s %s = %s;%n", typeSymbol.get().signature(), constName, value);
            TextEdit constDeclEdit = new TextEdit(new Range(constDeclPosition, constDeclPosition), constDeclStr);
            TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                    PositionUtil.toPosition(replaceRange.endLine())), constName);

            return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_CONSTANT,
                    List.of(constDeclEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract));
        }
        // Selection is a position
        // TODO Check for number of extractable nodes
        Node nodeAtCursor = posDetails.matchedCodeActionNode();
        List<Node> nodeList = new ArrayList<>();
        Node node = nodeAtCursor;

        BasicLiteralNodeValidator nodeValidator = new BasicLiteralNodeValidator();
        node.accept(nodeValidator);
        while (!(node instanceof StatementNode) && !(node instanceof ModuleMemberDeclarationNode)
                && !(node instanceof ObjectFieldNode) && !nodeValidator.getInvalidNode()) {
            nodeList.add(node);
            node = node.parent();
            node.accept(nodeValidator);
        }
        
        String constName = getLocalVarName(context);
        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(nodeAtCursor);
        if (typeSymbol.isEmpty()) {
            return Collections.emptyList();
        }

        Position constDeclPosition = getPosition(context);
        HashMap<String,List<TextEdit>> textEditMap = new HashMap<>();
        nodeList.forEach(extractableNode -> {
            String value = extractableNode.toSourceCode().strip();
            LineRange replaceRange = extractableNode.lineRange();
            String constDeclStr = String.format("const %s %s = %s;%n", typeSymbol.get().signature(), constName, value);
            TextEdit constDeclEdit =
                    new TextEdit(new Range(constDeclPosition, constDeclPosition), constDeclStr);
            TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                    PositionUtil.toPosition(replaceRange.endLine())), constName);
            textEditMap.put(value, List.of(constDeclEdit, replaceEdit));
        });
        
        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_CONSTANT,
                new Command(NAME, "ballerina.action.extract", 
                        List.of("extractConstant", context.fileUri() ,textEditMap)), CodeActionKind.RefactorExtract);
        // if capabilities aren't set, return list of text edits
        LSClientCapabilities lsClientCapabilities = context.languageServercontext().get(LSClientCapabilities.class);
//        if (lsClientCapabilities.getInitializationOptions().isRefactorExtractSupported()) {
//        codeAction.setCommand();
//        }
        return Collections.singletonList(codeAction);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static boolean isRange(Range range) {
        return !range.getStart().equals(range.getEnd());
    }

    private String getLocalVarName(CodeActionContext context) {
        Position pos = context.range().getEnd();
        Set<String> allNames = context.visibleSymbols(new Position(pos.getLine(), pos.getCharacter())).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return NameUtil.generateTypeName(CONSTANT_NAME_PREFIX, allNames);
    }

    private Position getPosition(CodeActionContext context) {
        ModulePartNode modulePartNode = context.currentSyntaxTree().get().rootNode();
        NodeList<ImportDeclarationNode> importsList = modulePartNode.imports();
        
        if (importsList.isEmpty()) {
            return PositionUtil.toPosition(modulePartNode.lineRange().startLine());
        }
        ImportDeclarationNode lastImport = importsList.get(importsList.size() - 1);
        return new Position(lastImport.lineRange().endLine().line() + 2, 0);
    }

    /**
     * Node visitor to ensure the highlighted range does not include nodes other than BasicLiteralNode.
     */
    static class BasicLiteralNodeValidator extends NodeVisitor {

        private boolean invalidNode = false;

        @Override
        public void visit(BinaryExpressionNode node) {
            node.lhsExpr().accept(this);
            node.rhsExpr().accept(this);
        }

        @Override
        public void visit(BasicLiteralNode node) {
        }

        @Override
        public void visit(UnaryExpressionNode node) {
        }

        @Override
        protected void visitSyntaxNode(Node node) {
            invalidNode = true;
        }

        public Boolean getInvalidNode() {
            return invalidNode;
        }
    }
}
