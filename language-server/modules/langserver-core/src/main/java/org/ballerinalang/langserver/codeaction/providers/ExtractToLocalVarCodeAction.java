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
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action for extracting an expression to a local variable.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractToLocalVarCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Extract To Local Variable";
    private static final String VARIABLE_NAME_PREFIX = "var";

    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.BOOLEAN_LITERAL, SyntaxKind.NUMERIC_LITERAL, SyntaxKind.STRING_LITERAL,
                SyntaxKind.BINARY_EXPRESSION, SyntaxKind.START_ACTION, SyntaxKind.BRACED_EXPRESSION,
                SyntaxKind.FUNCTION_CALL, SyntaxKind.QUALIFIED_NAME_REFERENCE, SyntaxKind.INDEXED_EXPRESSION,
                SyntaxKind.FIELD_ACCESS, SyntaxKind.METHOD_CALL, SyntaxKind.CHECK_EXPRESSION, 
                SyntaxKind.MAPPING_CONSTRUCTOR, SyntaxKind.TYPEOF_EXPRESSION, SyntaxKind.UNARY_EXPRESSION,
                SyntaxKind.TYPE_TEST_EXPRESSION, SyntaxKind.TRAP_EXPRESSION, SyntaxKind.LIST_CONSTRUCTOR, 
                SyntaxKind.TYPE_CAST_EXPRESSION, SyntaxKind.TABLE_CONSTRUCTOR, SyntaxKind.LET_EXPRESSION,
                SyntaxKind.IMPLICIT_NEW_EXPRESSION, SyntaxKind.EXPLICIT_NEW_EXPRESSION, 
                SyntaxKind.PARENTHESIZED_ARG_LIST, SyntaxKind.ERROR_CONSTRUCTOR, SyntaxKind.OBJECT_CONSTRUCTOR);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        Node node = positionDetails.matchedCodeActionNode();
        // Avoid providing the code action for a mapping constructor used in a table constructor and 
        // a function call used in a local variable declaration, since it produces syntax errors.
        return context.currentSyntaxTree().isPresent() && context.currentSemanticModel().isPresent()
                && CodeActionNodeValidator.validate(context.nodeAtRange()) && 
                !(node.kind() == SyntaxKind.MAPPING_CONSTRUCTOR && node.parent().kind() == SyntaxKind.TABLE_CONSTRUCTOR)
                && !(node.kind() == SyntaxKind.FUNCTION_CALL && node.parent().kind() == SyntaxKind.LOCAL_VAR_DECL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        
        Node node = posDetails.matchedCodeActionNode(); 
        String varName = getLocalVarName(context);
        String value = node.toSourceCode().strip();
        LineRange replaceRange = node.lineRange();
        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(node);
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Collections.emptyList();
        }

        Node statementNode = getStatementNode(node);
        if (statementNode == null) {
            return Collections.emptyList();
        }
        String paddingStr = StringUtils.repeat(" ", statementNode.lineRange().startLine().offset());
        String varDeclStr = String.format("%s %s = %s;%n%s", typeSymbol.get().signature(), varName, value, paddingStr);
        Position varDeclPos = new Position(statementNode.lineRange().startLine().line(), 
                statementNode.lineRange().startLine().offset());
        TextEdit varDeclEdit = new TextEdit(new Range(varDeclPos, varDeclPos), varDeclStr);
        TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                PositionUtil.toPosition(replaceRange.endLine())),  varName);

        return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_VARIABLE, 
                List.of(varDeclEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract));
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Node getStatementNode(Node node) {
        Node statementNode = node;
        while (statementNode != null && !(statementNode instanceof StatementNode)
                && !(statementNode instanceof ModuleMemberDeclarationNode)) {
            statementNode = statementNode.parent();
        }
        return statementNode;
    }

    private String getLocalVarName(CodeActionContext context) {
        Position pos = context.range().getEnd();
        Set<String> allNames = context.visibleSymbols(new Position(pos.getLine(), pos.getCharacter())).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        
        return NameUtil.generateTypeName(VARIABLE_NAME_PREFIX, allNames);
    }
}
