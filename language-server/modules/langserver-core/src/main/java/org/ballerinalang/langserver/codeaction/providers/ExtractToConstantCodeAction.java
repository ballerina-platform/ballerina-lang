/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.ConstantVisitor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

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
    private static final String CONSTANT_NAME_PREFIX = "CONSTANT";

    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.BOOLEAN_LITERAL, SyntaxKind.NUMERIC_LITERAL,
                SyntaxKind.NUMERIC_LITERAL, SyntaxKind.BINARY_EXPRESSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                                    RangeBasedPositionDetails posDetails) {
        
        if (context.currentSyntaxTree().isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }
        Node node = CommonUtil.findNode(context.range(), context.currentSyntaxTree().get());
        if (node.parent().kind() == SyntaxKind.CONST_DECLARATION ||
                node.parent().kind() == SyntaxKind.INVALID_EXPRESSION_STATEMENT ||
                (node.kind() != SyntaxKind.NUMERIC_LITERAL && node.kind() != SyntaxKind.STRING_LITERAL && 
                        node.kind() != SyntaxKind.BOOLEAN_LITERAL && node.kind() != SyntaxKind.BINARY_EXPRESSION)) {
            return Collections.emptyList();
        }
        
        // If the node is a BinaryExpressionNode, check whether it contains only BasicLiteralNodes
        if (node.kind() == SyntaxKind.BINARY_EXPRESSION) {
            ConstantVisitor constantVisitor = new ConstantVisitor();
            node.accept(constantVisitor);
            if (constantVisitor.getInvalidNode()) {
                return Collections.emptyList();
            }
        }
        
        Set<String> visibleSymbolNames = context.visibleSymbols(context.cursorPosition()).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        String constName = NameUtil.generateTypeName(CONSTANT_NAME_PREFIX, visibleSymbolNames);

        String value = "";
        LineRange replaceRange = null;
        Optional<TypeSymbol> typeSymbol = Optional.empty();
        switch(node.kind()) {
            case NUMERIC_LITERAL:
            case STRING_LITERAL:
            case BOOLEAN_LITERAL:
                BasicLiteralNode basicLiteralNode = (BasicLiteralNode) node;
                value = basicLiteralNode.toSourceCode().strip();
                replaceRange = basicLiteralNode.lineRange();
                typeSymbol = context.currentSemanticModel().get().typeOf(basicLiteralNode);
                break;
            case BINARY_EXPRESSION:
                BinaryExpressionNode binaryExpressionNode = (BinaryExpressionNode) node;
                value = binaryExpressionNode.toSourceCode().strip();
                replaceRange = binaryExpressionNode.lineRange();
                typeSymbol = context.currentSemanticModel().get().typeOf(binaryExpressionNode);
                break;
            default:
        }

        if (typeSymbol.isEmpty()) {
            return Collections.emptyList();
        }
        
        Node modPartNode = node;
        while (modPartNode.parent().kind() != SyntaxKind.MODULE_PART) {
            modPartNode = modPartNode.parent();
        }
        
        String constDeclStr = String.format("const %s %s = %s;%n%n", typeSymbol.get().signature(), constName, value);
        TextEdit constDeclEdit = new TextEdit(new Range(PositionUtil.toPosition(modPartNode.lineRange().startLine()),
                PositionUtil.toPosition(modPartNode.lineRange().startLine())), constDeclStr);
        TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                PositionUtil.toPosition(replaceRange.endLine())),  constName);

        return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_CONSTANT, 
                List.of(constDeclEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
