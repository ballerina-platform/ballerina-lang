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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                SyntaxKind.STRING_LITERAL, SyntaxKind.BINARY_EXPRESSION);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        Node node = positionDetails.matchedCodeActionNode();
        return CodeActionNodeValidator.validate(context.nodeAtRange()) && context.currentSyntaxTree().isPresent()
                && context.currentSemanticModel().isPresent() && node.parent().kind() != SyntaxKind.CONST_DECLARATION
                && node.parent().kind() != SyntaxKind.INVALID_EXPRESSION_STATEMENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                                    RangeBasedPositionDetails posDetails) {
        
        Node node = posDetails.matchedCodeActionNode();
        ExtractToConstantCodeAction.BasicLiteralNodeValidator nodeValidator = new ExtractToConstantCodeAction.BasicLiteralNodeValidator();
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
        
        Node rootNode = context.currentSyntaxTree().get().rootNode();
        ModulePartNode modulePartNode = (ModulePartNode) rootNode;
        Position position = PositionUtil.toPosition(modulePartNode.lineRange().startLine());
        NodeList<ImportDeclarationNode> importsList = modulePartNode.imports();
        if (!importsList.isEmpty()) {
            ImportDeclarationNode lastImport = importsList.get(importsList.size() - 1);
            position = new Position(lastImport.lineRange().endLine().line() + 2, 0);
        }
        
        String constDeclStr = String.format("const %s %s = %s;%n%n", typeSymbol.get().signature(), constName, value);
        TextEdit constDeclEdit = new TextEdit(new Range(position, position), constDeclStr);
        TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                PositionUtil.toPosition(replaceRange.endLine())),  constName);

        return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_CONSTANT, 
                List.of(constDeclEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract));
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Node visitor to ensure the highlighted range does not include nodes other than BasicLiteralNode.
     *
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
        protected void visitSyntaxNode(Node node) {
            invalidNode = true;
        }

        public Boolean getInvalidNode() {
            return invalidNode;
        }
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
}
