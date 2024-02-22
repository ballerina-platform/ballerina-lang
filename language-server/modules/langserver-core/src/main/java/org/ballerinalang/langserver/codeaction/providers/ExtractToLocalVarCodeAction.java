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
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.ExpressionNodeValidator;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
        SyntaxKind nodeKind = node.kind();
        SyntaxKind parentKind = parentNode.kind();
        // Avoid providing the code action for the following since it is syntactically incorrect.
        // 1. a mapping constructor used in a table constructor  
        // 2. a function call used in a local variable declaration
        // 3. a function/ method call used in an expression statement
        // 4. a constant declaration
        // 5. the variable reference of an assignment node
        // 6. the qualified name reference of a function call expression
        // 7. a record field with default value
        // 8. a function call expression used in a start action
        // 9. a client declaration or a module client declaration
        return context.currentSyntaxTree().isPresent() && context.currentSemanticModel().isPresent()
                && !(nodeKind == SyntaxKind.MAPPING_CONSTRUCTOR && parentKind == SyntaxKind.TABLE_CONSTRUCTOR)
                && !(nodeKind == SyntaxKind.FUNCTION_CALL && parentKind == SyntaxKind.LOCAL_VAR_DECL)
                && !((nodeKind == SyntaxKind.FUNCTION_CALL || nodeKind == SyntaxKind.METHOD_CALL)
                && parentKind == SyntaxKind.CALL_STATEMENT)
                && parentKind != SyntaxKind.CONST_DECLARATION
                && !(parentKind == SyntaxKind.ASSIGNMENT_STATEMENT
                && ((AssignmentStatementNode) parentNode).varRef().equals(node))
                && !(nodeKind == SyntaxKind.QUALIFIED_NAME_REFERENCE && parentKind == SyntaxKind.FUNCTION_CALL)
                && parentKind != SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE
                && parentKind != SyntaxKind.ENUM_MEMBER
                && !(nodeKind == SyntaxKind.FUNCTION_CALL && parentKind == SyntaxKind.START_ACTION)
                && parentKind != SyntaxKind.MATCH_CLAUSE
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

        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(node);
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Collections.emptyList();
        }

        Node statementNode = getStatementNode(node);
        if (statementNode == null || statementNode.kind() == SyntaxKind.OBJECT_FIELD) {
            return Collections.emptyList();
        }

        // Check if the selection is a range or a position, and whether quick picks are supported by the client
        LSClientCapabilities lsClientCapabilities = context.languageServercontext().get(LSClientCapabilities.class);
        if (CodeActionUtil.isRangeSelection(context.range())
                || !lsClientCapabilities.getInitializationOptions().isQuickPickSupported()) {

            List<TextEdit> textEdits = getTextEdits(context, typeSymbol.get(), node, statementNode);

            CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_VARIABLE,
                    textEdits, context.fileUri(), CodeActionKind.RefactorExtract);
            CodeActionUtil.addRenamePopup(context, codeAction, CommandConstants.RENAME_COMMAND_TITLE_FOR_VARIABLE,
                    getRenamePosition(textEdits.get(1).getRange()));
            return Collections.singletonList(codeAction);
        }

        ExpressionNodeValidator nodeValidator = new ExpressionNodeValidator();
        node.accept(nodeValidator);
        if (nodeValidator.isInvalidNode()) {
            return Collections.emptyList();
        }

        // Selection is a position
        List<Node> nodeList = getPossibleExpressionNodes(node);
        LinkedHashMap<String, List<TextEdit>> textEditMap = new LinkedHashMap<>();
        LinkedHashMap<String, Position> renamePositionMap = new LinkedHashMap<>();

        // List out the expression node list that we are going to provide quick pick support
        // Find the arg list for each of the expression nodes
        nodeList.forEach(extractableNode -> {
            Optional<TypeSymbol> tSymbol = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.typeOf(extractableNode));
            if (tSymbol.isEmpty() || tSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
                return;
            }
            String key = extractableNode.toSourceCode().strip();
            textEditMap.put(key,
                    getTextEdits(context, tSymbol.get(), extractableNode, statementNode));
            renamePositionMap.put(key, getRenamePosition(PositionUtil.toRange(extractableNode.lineRange())));
        });

        if (lsClientCapabilities.getInitializationOptions().isPositionalRefactorRenameSupported()) {
            return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_VARIABLE,
                    new Command(NAME, CommandConstants.EXTRACT_COMMAND,
                            List.of(NAME, context.filePath().toString(), textEditMap, renamePositionMap)),
                    CodeActionKind.RefactorExtract));
        }

        return Collections.singletonList(CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_VARIABLE,
                new Command(NAME, CommandConstants.EXTRACT_COMMAND,
                        List.of(NAME, context.filePath().toString(), textEditMap)), CodeActionKind.RefactorExtract));
    }

    private List<TextEdit> getTextEdits(CodeActionContext context, TypeSymbol typeSymbol, Node matchedNode,
                                        Node statementNode) {
        String varName = getLocalVarName(context);
        String value = matchedNode.toSourceCode().strip();
        LineRange replaceRange = matchedNode.lineRange();

        String paddingStr = StringUtils.repeat(" ", statementNode.lineRange().startLine().offset());
        String typeDescriptor = FunctionGenerator.getReturnTypeAsString(context, typeSymbol.signature());
        String varDeclStr = String.format("%s %s = %s;%n%s", typeDescriptor, varName, value, paddingStr);
        Position varDeclPos = new Position(statementNode.lineRange().startLine().line(),
                statementNode.lineRange().startLine().offset());
        TextEdit varDeclEdit = new TextEdit(new Range(varDeclPos, varDeclPos), varDeclStr);
        TextEdit replaceEdit = new TextEdit(new Range(PositionUtil.toPosition(replaceRange.startLine()),
                PositionUtil.toPosition(replaceRange.endLine())), varName);

        return List.of(varDeclEdit, replaceEdit);
    }

    private Position getRenamePosition(Range range) {
        return new Position(range.getEnd().getLine() + 1, range.getStart().getCharacter());
    }

    private static List<Node> getPossibleExpressionNodes(Node node) {
        ExpressionNodeValidator nodeValidator = new ExpressionNodeValidator();
        // Identify the sub-expressions to be extracted
        List<Node> nodeList = new ArrayList<>();
        while (node != null && !isStatementOrModuleDeclaration(node) && node.kind() != SyntaxKind.OBJECT_FIELD
                && !nodeValidator.isInvalidNode()) {
            nodeList.add(node);
            node = node.parent();
            node.accept(nodeValidator);
        }
        return nodeList;
    }

    private static boolean isStatementOrModuleDeclaration(Node node) {
        return node instanceof StatementNode || node instanceof ModuleMemberDeclarationNode;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Node getStatementNode(Node node) {
        Node statementNode = node;
        while (statementNode != null && !(statementNode instanceof StatementNode)
                && !(statementNode instanceof ModuleMemberDeclarationNode)
                && statementNode.kind() != SyntaxKind.OBJECT_FIELD) {
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

    // If the variables within the selected range have been initialized in the closest statement node (and outside
    // the highlighted range), the expression is not extractable.
    private boolean isNotExtractable(Node matchedNode, CodeActionContext context) {
        List<Symbol> symbolsWithinRange = getVisibleSymbols(context,
                PositionUtil.toPosition(matchedNode.lineRange().endLine())).stream()
                .filter(symbol -> (symbol.kind() == SymbolKind.VARIABLE || symbol.kind() == SymbolKind.PARAMETER)
                        && context.currentSemanticModel().get().references(symbol).stream()
                        .anyMatch(location ->
                                PositionUtil.isWithinLineRange(location.lineRange(), matchedNode.lineRange())))
                .filter(symbol -> symbol.getLocation().isPresent() && PositionUtil.isWithinLineRange(
                        symbol.getLocation().get().lineRange(), getStatementNode(matchedNode).lineRange()))
                .collect(Collectors.toList());

        if (symbolsWithinRange.size() == 0) {
            return false;
        }

        return symbolsWithinRange.stream().noneMatch(symbol -> symbol.getLocation().isPresent() && PositionUtil
                .isWithinLineRange(symbol.getLocation().get().lineRange(), matchedNode.lineRange()));
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
