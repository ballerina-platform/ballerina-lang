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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
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
    
    private static final String RENAME_COMMAND = "Rename variable";

    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.BOOLEAN_LITERAL, SyntaxKind.NUMERIC_LITERAL, SyntaxKind.STRING_LITERAL,
                SyntaxKind.BINARY_EXPRESSION, SyntaxKind.BRACED_EXPRESSION, SyntaxKind.XML_TEMPLATE_EXPRESSION,
                SyntaxKind.FUNCTION_CALL, SyntaxKind.QUALIFIED_NAME_REFERENCE, SyntaxKind.INDEXED_EXPRESSION,
                SyntaxKind.FIELD_ACCESS, SyntaxKind.METHOD_CALL, SyntaxKind.CHECK_EXPRESSION, SyntaxKind.LET_EXPRESSION,
                SyntaxKind.MAPPING_CONSTRUCTOR, SyntaxKind.TYPEOF_EXPRESSION, SyntaxKind.UNARY_EXPRESSION,
                SyntaxKind.TYPE_TEST_EXPRESSION, SyntaxKind.TRAP_EXPRESSION, SyntaxKind.LIST_CONSTRUCTOR, 
                SyntaxKind.TYPE_CAST_EXPRESSION, SyntaxKind.TABLE_CONSTRUCTOR, SyntaxKind.IMPLICIT_NEW_EXPRESSION, 
                SyntaxKind.EXPLICIT_NEW_EXPRESSION, SyntaxKind.ERROR_CONSTRUCTOR, SyntaxKind.QUERY_EXPRESSION);
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        Node node = positionDetails.matchedCodeActionNode();
        Node parentNode = node.parent();
        // Avoid providing the code action for the following since it is syntactically incorrect.
        // 1. a mapping constructor used in a table constructor  
        // 2. a function call used in a local variable declaration
        // 3. a function/ method call used in an expression statement
        // 4. a constant declaration
        // 5. the variable reference of an assignment node
        // 6. the qualified name reference of a function call expression
        // 7. a record field with default value
        // 8. a function call expression used in a start action
        return context.currentSyntaxTree().isPresent() && context.currentSemanticModel().isPresent() &&
                !(node.kind() == SyntaxKind.MAPPING_CONSTRUCTOR && parentNode.kind() == SyntaxKind.TABLE_CONSTRUCTOR)
                && !(node.kind() == SyntaxKind.FUNCTION_CALL && parentNode.kind() == SyntaxKind.LOCAL_VAR_DECL) 
                && !((node.kind() == SyntaxKind.FUNCTION_CALL || node.kind() == SyntaxKind.METHOD_CALL) 
                && parentNode.kind() == SyntaxKind.CALL_STATEMENT) && parentNode.kind() != SyntaxKind.CONST_DECLARATION
                && !(parentNode.kind() == SyntaxKind.ASSIGNMENT_STATEMENT 
                && ((AssignmentStatementNode) parentNode).varRef().equals(node))
                && !(node.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE 
                && parentNode.kind() == SyntaxKind.FUNCTION_CALL)
                && parentNode.kind() != SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE
                && parentNode.kind() != SyntaxKind.ENUM_MEMBER
                && !(node.kind() == SyntaxKind.FUNCTION_CALL && parentNode.kind() == SyntaxKind.START_ACTION)
                && CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        
        Node node = posDetails.matchedCodeActionNode();
        if (isNotExtractable(node, context)) {
            return Collections.emptyList();
        }
        
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

        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_VARIABLE,
                List.of(varDeclEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract);
        addRenamePopup(context, codeAction, varDeclStr.length(), node.textRange().startOffset());
        
        return Collections.singletonList(codeAction);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Node getStatementNode(Node node) {
        Node statementNode = node;
        while (statementNode != null && !(statementNode instanceof StatementNode)
                && !(statementNode instanceof ModuleMemberDeclarationNode) 
                && !(statementNode instanceof ObjectFieldNode)) {
            statementNode = statementNode.parent();
        }
        return statementNode;
    }

    private String getLocalVarName(CodeActionContext context) {
        Set<String> allNames = context.visibleSymbols(context.range().getEnd()).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        
        return NameUtil.generateTypeName(VARIABLE_NAME_PREFIX, allNames);
    }

    private void addRenamePopup(CodeActionContext context, CodeAction codeAction,
                                int varDeclStrLength, int nodeStartOffset) {
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            return;
        }

        int startPos = varDeclStrLength + nodeStartOffset;
        LSClientCapabilities lsClientCapabilities = context.languageServercontext().get(LSClientCapabilities.class);
        if (lsClientCapabilities.getInitializationOptions().isRefactorRenameSupported()) {
            codeAction.setCommand(new Command(RENAME_COMMAND, "ballerina.action.rename",
                    List.of(context.fileUri(), startPos)));
        }
    }

    // If the variables within the selected range have been initialized in the closest statement node (and outside
    // the highlighted range), the expression is not extractable.
    private boolean isNotExtractable(Node matchedNode, CodeActionContext context) {
        List<Symbol> symbolsWithinRange = getVisibleSymbols(context, 
                PositionUtil.toPosition(matchedNode.lineRange().endLine())).stream()
                .filter(symbol -> (symbol.kind() == SymbolKind.VARIABLE || symbol.kind() == SymbolKind.PARAMETER) 
                        && context.currentSemanticModel().get().references(symbol).stream()
                        .anyMatch(location -> PositionUtil.isRangeWithinRange(PositionUtil
                                        .getRangeFromLineRange(location.lineRange()),
                                PositionUtil.toRange(matchedNode.lineRange()))))
                .filter(symbol -> symbol.getLocation().isPresent() && PositionUtil
                        .isRangeWithinRange(PositionUtil.getRangeFromLineRange(symbol.getLocation().get().lineRange()), 
                                PositionUtil.toRange(getStatementNode(matchedNode).lineRange())))
                .collect(Collectors.toList());
        
        if (symbolsWithinRange.size() == 0) {
            return false;
        }

        return symbolsWithinRange.stream().noneMatch(symbol -> symbol.getLocation().isPresent() && PositionUtil
                .isRangeWithinRange(PositionUtil.getRangeFromLineRange(symbol.getLocation().get().lineRange()), 
                        PositionUtil.toRange(matchedNode.lineRange())));
    }

    /**
     * This method is used because of the inconsistency in the context.visibleSymbols() and
     * semanticModel().visibleSymbols() in LS. This method can be replaced after fixing #37234
     */
    @Deprecated(forRemoval = true)
    private List<Symbol> getVisibleSymbols(CodeActionContext context, Position position) {
        return context.currentSemanticModel().get()
                .visibleSymbols(context.currentDocument().get(), PositionUtil.getLinePosition(position));
    }
}
