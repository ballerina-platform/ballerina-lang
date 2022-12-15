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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.ExtractToFunctionStatementAnalyzer;
import org.ballerinalang.langserver.command.visitors.IsolatedBlockResolver;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action for extracting a code segment to a function.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractToFunctionCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Extract to function";

    private static final String EXTRACTED_PREFIX = "extracted";

    private static final Set<SyntaxKind> unSupportedModuleLevelSyntaxKinds = Set.of(SyntaxKind.CONST_DECLARATION,
            SyntaxKind.MODULE_XML_NAMESPACE_DECLARATION, SyntaxKind.ENUM_DECLARATION, SyntaxKind.TYPE_DEFINITION);

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()
                && (!isExpressionNode(positionDetails.matchedCodeActionNode())
                || isExpressionExtractable(positionDetails))
                && CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode());
    }

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        List<SyntaxKind> supportedSyntaxKinds = new ArrayList<>();
        supportedSyntaxKinds.addAll(getSupportedStatementSyntaxKindsList());
        supportedSyntaxKinds.addAll(getSupportedExpressionSyntaxKindsList());
        return supportedSyntaxKinds;
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        if (isExpressionNode(posDetails.matchedCodeActionNode())) {
            return getCodeActionsForExpressions(context, posDetails);
        }

        return getCodeActionsForStatements(context, posDetails);
    }

    /**
     * Get the code action when the syntax kind of the matched code action node is a statement or a list of statements.
     */
    private List<CodeAction> getCodeActionsForStatements(CodeActionContext context,
                                                         RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();
        Node enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);

        if (!isWithinFunctionDefinitionNode(matchedCodeActionNode)) {
            return Collections.emptyList();
        }

        SemanticModel semanticModel = context.currentSemanticModel().get();

        ExtractToFunctionStatementAnalyzer statementAnalyzer
                = new ExtractToFunctionStatementAnalyzer(context.range(), semanticModel);
        statementAnalyzer.analyze(matchedCodeActionNode);

        /*
         * Here we decide whether the content of the selected range is syntactically correct to be extracted to a
         * function.
         * */
        if (!statementAnalyzer.isExtractable()) {
            return Collections.emptyList();
        }

        Pair<Boolean, Optional<VariableSymbol>> isExtractableAndUpdatingVarPair
                = isExtractableAndUpdatingVar(statementAnalyzer, enclosingNode, context);

        /*
         * Here we decide whether the selected range is extractable by the usages of symbols, empty scenario implies
         * multiple variable symbols updated and referred after range
         */
        if (isExtractableAndUpdatingVarPair.getLeft().equals(Boolean.FALSE)) {
            return Collections.emptyList();
        }

        // Empty scenario implies that extracted function should not return any value
        Optional<VariableSymbol> updatedVar = isExtractableAndUpdatingVarPair.getRight();
        Optional<String> possibleTypeOfUpdatedVar = Optional.empty();

        String returnTypeDescriptor = "";
        if (updatedVar.isPresent()) {
            Optional<String> posType =
                    CodeActionUtil.getPossibleType(updatedVar.get().typeDescriptor(), new ArrayList<>(), context);
            if (posType.isEmpty()) {
                return Collections.emptyList();
            }
            possibleTypeOfUpdatedVar = posType;
            returnTypeDescriptor = String.format("returns %s", possibleTypeOfUpdatedVar.get());
        }

        Optional<List<Symbol>> argsSymbolsForExtractFunction =
                getVarAndParamSymbolsWithinRangeForStmts(context, enclosingNode);

        if (argsSymbolsForExtractFunction.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<ArgListsHolder> argLists = getArgLists(context, argsSymbolsForExtractFunction.get());
        if (argLists.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> argsForExtractFunction = argLists.get().extractFunctionArgs;
        List<String> argsForReplaceFunctionCall = argLists.get().replaceFunctionCallArgs;

        String functionName = getFunctionName(context, matchedCodeActionNode);
        String returnStatement = "";
        if (updatedVar.isPresent() && updatedVar.get().getName().isPresent()) {
            returnStatement = String.format("return %s;", updatedVar.get().getName().get());
        }

        List<Node> selectedNodes = statementAnalyzer.getSelectedNodes();
        String funcBody = selectedNodes.stream().map(Node::toSourceCode).collect(Collectors.joining(""));
        Range extractFunctionInsertRange;

        boolean newLineAtEnd = addNewLineAtEnd(enclosingNode);
        extractFunctionInsertRange = PositionUtil.toRange(enclosingNode.lineRange().endLine());

        // Check if the function call is invoked from an isolated context.
        IsolatedBlockResolver isolatedBlockResolver = new IsolatedBlockResolver();
        Boolean isIsolated = isolatedBlockResolver.findIsolatedBlock(matchedCodeActionNode);

        String extractFunction = FunctionGenerator.generateFunction(functionName, argsForExtractFunction,
                returnTypeDescriptor, returnStatement, newLineAtEnd, isIsolated, funcBody);

        try {
            // Adding a line separator which is removed by the Formatter
            extractFunction = CommonUtil.LINE_SEPARATOR + Formatter.format(extractFunction).stripTrailing();
        } catch (FormatterException e) {
            return Collections.emptyList();
        }
        String replaceFunctionCall = getReplaceFunctionCall(argsForReplaceFunctionCall, functionName, false);

        if (updatedVar.isPresent() && updatedVar.get().getName().isPresent()) {
            String varName = updatedVar.get().getName().get();
            replaceFunctionCall =
                    String.format("%s %s = %s", possibleTypeOfUpdatedVar.get(), varName, replaceFunctionCall);
        }

        Position replaceFuncCallStartPos = PositionUtil.toPosition(selectedNodes.get(0).lineRange().startLine());
        Position replaceFuncCallEndPos = PositionUtil
                .toPosition(selectedNodes.get(selectedNodes.size() - 1).lineRange().endLine());
        Range replaceFuncCallInsertRange = new Range(replaceFuncCallStartPos, replaceFuncCallEndPos);

        TextEdit extractFunctionEdit = new TextEdit(extractFunctionInsertRange, extractFunction);
        TextEdit replaceFunctionCallEdit = new TextEdit(replaceFuncCallInsertRange, replaceFunctionCall);

        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_FUNCTION,
                List.of(extractFunctionEdit, replaceFunctionCallEdit), context.fileUri(),
                CodeActionKind.RefactorExtract);

        return List.of(codeAction);
    }

    /**
     * Finds whether the selected range is extractable. If extractable, finds whether a value should be returned or not
     * from the extracted function. If yes, return the updated variable symbol. The two scenarios, if the nodes in the
     * range is extractable:
     * <p>
     * 1. extracted();                      // a value is not returned
     * 2. int varName = extracted();        // a value is returned
     *
     * @return A Pair of elements consisting the extract-ability and optional updating variable symbol
     */
    private Pair<Boolean, Optional<VariableSymbol>> isExtractableAndUpdatingVar(
            ExtractToFunctionStatementAnalyzer analyzer, Node enclosingNode, CodeActionContext context) {
        SemanticModel semanticModel = context.currentSemanticModel().get();
        // Updated symbols can be either localVar or moduleVar
        List<Symbol> updatedSymbols = analyzer.getUpdatedSymbols();
        List<Symbol> declaredVariableSymbols = analyzer.getDeclaredVariableSymbols();

        HashSet<Symbol> updatedOrDeclaredLocVarSymbolsInRange = new HashSet<>(updatedSymbols);
        updatedOrDeclaredLocVarSymbolsInRange.addAll(declaredVariableSymbols);

        List<Symbol> updatedModVarSymbols = updatedSymbols.stream()
                .filter(symbol -> symbol.getLocation().isPresent() && !PositionUtil
                        .isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.lineRange()))
                .collect(Collectors.toList());

        List<Symbol> updatedButNotDeclaredLocVarSymbolsInRange = updatedSymbols.stream()
                .filter(symbol -> symbol.getLocation().isPresent() && !PositionUtil.isRangeWithinRange
                        (PositionUtil.toRange(symbol.getLocation().get().lineRange()), context.range()))
                .filter(symbol -> !updatedModVarSymbols.contains(symbol))
                .collect(Collectors.toList());

        // LocalVarSymbols which are declared or updated inside the selected range and also referred after the range
        List<VariableSymbol> localVarSymbols = updatedOrDeclaredLocVarSymbolsInRange.stream()
                .filter(symbol -> !updatedModVarSymbols.contains(symbol))
                .filter(symbol -> semanticModel.references(symbol).stream()
                        .anyMatch(location -> PositionUtil.isRangeWithinRange
                                (PositionUtil.toRange(location.lineRange()),
                                        getRangeAfterHighlightedRange(context, enclosingNode))))
                .map(symbol -> (VariableSymbol) symbol)
                .collect(Collectors.toList());

        /*
         * Following checks are done when deciding whether the selected range R is extractable to a function.
         *
         * 1. All updated local variables inside R have been declared inside R.
         * 2. The number of local variables in (1.) is less or equal to 1.
         *
         * */
        boolean isRangeExtractable = updatedButNotDeclaredLocVarSymbolsInRange.size() == 0
                && localVarSymbols.size() <= 1;

        // Here we decide whether the selected range is extractable by the usages of symbols
        if (!isRangeExtractable) {
            return Pair.of(Boolean.FALSE, Optional.empty());
        }

        Optional<VariableSymbol> updatedVar = Optional.empty();
        if (localVarSymbols.size() == 1) {
            updatedVar = localVarSymbols.stream().findFirst();
        }
        return Pair.of(Boolean.TRUE, updatedVar);
    }

    private Range getRangeAfterHighlightedRange(CodeActionContext context, Node enclosingNode) {
        Position endPosOfEnclosingNode = PositionUtil.toPosition(enclosingNode.lineRange().endLine());
        Position endPosOfStatements
                = new Position(endPosOfEnclosingNode.getLine(), endPosOfEnclosingNode.getCharacter() - 1);
        return new Range(context.range().getEnd(), endPosOfStatements);
    }

    /**
     * This method finds the visible variable and parameter symbols which are referred inside the selected range for
     * getCodeActionsForStatements scenario.
     */
    private Optional<List<Symbol>> getVarAndParamSymbolsWithinRangeForStmts(CodeActionContext context, 
                                                                            Node enclosingNode) {
        List<Symbol> argsSymbolsForExtractFunction = new ArrayList<>();
        List<Symbol> visibleSymbols = getVisibleSymbols(context, context.range().getStart());

        // Local variable symbols which have been declared before the selected range but within the enclosing node
        List<Symbol> localVarSymbols = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE && symbol.getLocation().isPresent())
                // this check is done to filter the local variables defined inside the function
                .filter(symbol -> PositionUtil
                        .isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.lineRange()))
                .collect(Collectors.toList());

        // Declare a HashSet to collect local var symbols and param symbols and add param symbols
        HashSet<Symbol> paramAndLocVarSymbolsVisibleToRange = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.PARAMETER).collect(Collectors.toCollection(HashSet::new));

        // Add localVarSymbols to the above HashSet
        paramAndLocVarSymbolsVisibleToRange.addAll(localVarSymbols);

        paramAndLocVarSymbolsVisibleToRange.forEach(symbol -> {
            // Check whether there is at least one symbol location that is being referred inside the selected range
            Optional<Location> anyLocationReferred = context.currentSemanticModel().get().references(symbol).stream()
                    .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil
                            .getRangeFromLineRange(location.lineRange()), context.range()))
                    .findAny();
            if (anyLocationReferred.isPresent()) {
                argsSymbolsForExtractFunction.add(symbol);
            }
        });

        if (argsSymbolsForExtractFunction.stream().anyMatch(symbol -> symbol.getLocation().isEmpty())) {
            return Optional.empty();
        }

        return Optional.of(argsSymbolsForExtractFunction.stream()
                // getLocation() is present for each symbol
                .sorted(Comparator.comparingInt(symbol -> symbol.getLocation().get().textRange().startOffset()))
                .collect(Collectors.toList()));
    }

    /**
     * Get the code action when the syntax kind of the matched code action node is an expression.
     */
    private List<CodeAction> getCodeActionsForExpressions(CodeActionContext context,
                                                          RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();

        Node enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);
        Range functionInsertRange;

        boolean newLineAtEnd = addNewLineAtEnd(enclosingNode);
        functionInsertRange = PositionUtil.toRange(enclosingNode.lineRange().endLine());

        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(matchedCodeActionNode);

        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Collections.emptyList();
        }

        Optional<List<Symbol>> varAndParamSymbolsWithinRange =
                getVarAndParamSymbolsWithinRangeForExprs(matchedCodeActionNode.lineRange(), context);

        if (varAndParamSymbolsWithinRange.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<ArgListsHolder> argLists = getArgLists(context, varAndParamSymbolsWithinRange.get());

        if (argLists.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> argsForExtractFunction = argLists.get().extractFunctionArgs;
        List<String> argsForReplaceFunctionCall = argLists.get().replaceFunctionCallArgs;

        String functionName = getFunctionName(context, matchedCodeActionNode);
        String function = getFunction(context, matchedCodeActionNode, newLineAtEnd, typeSymbol.get(), functionName,
                argsForExtractFunction);

        String replaceFunctionCall = getReplaceFunctionCall(argsForReplaceFunctionCall, functionName, true);

        TextEdit extractFunctionEdit = new TextEdit(functionInsertRange, function);
        TextEdit replaceEdit = new TextEdit(PositionUtil.toRange(matchedCodeActionNode.lineRange()),
                replaceFunctionCall);
        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_FUNCTION,
                List.of(extractFunctionEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract);

        return List.of(codeAction);
    }

    private String getFunctionName(CodeActionContext context, NonTerminalNode matchedNode) {
        Set<String> visibleSymbolNames = getVisibleSymbols(context,
                PositionUtil.toPosition(matchedNode.lineRange().endLine())).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return NameUtil.generateTypeName(EXTRACTED_PREFIX, visibleSymbolNames);
    }

    /**
     * This method is used because of the inconsistency in the context.visibleSymbols() and
     * semanticModel().visibleSymbols() in LS. //TODO replace after fixing #37234
     */
    private List<Symbol> getVisibleSymbols(CodeActionContext context, Position position) {

        return context.currentSemanticModel().get()
                .visibleSymbols(context.currentDocument().get(), PositionUtil.getLinePosition(position));
    }

    private String getReplaceFunctionCall(List<String> varSymbolNames, String functionName, boolean isExpr) {
        String funcCall = functionName + CommonKeys.OPEN_PARENTHESES_KEY + String.join(", ", varSymbolNames)
                + CommonKeys.CLOSE_PARENTHESES_KEY;

        return isExpr ? funcCall : funcCall + CommonKeys.SEMI_COLON_SYMBOL_KEY;
    }

    private String getFunction(CodeActionContext context, NonTerminalNode matchedNode, boolean newLineEnd, 
                               TypeSymbol typeSymbol, String functionName, List<String> args) {
        String returnsClause = 
                String.format("returns %s", FunctionGenerator.getReturnTypeAsString(context, typeSymbol.signature()));
        String returnStatement;

        if (matchedNode.kind() == SyntaxKind.BRACED_EXPRESSION) {
            returnStatement = String.format("return %s;",
                    ((BracedExpressionNode) matchedNode).expression().toString().strip());
        } else {
            returnStatement = String.format("return %s;", matchedNode.toString().strip());
        }

        // Check if the function call is invoked from an isolated context.
        IsolatedBlockResolver isolatedBlockResolver = new IsolatedBlockResolver();
        Boolean isIsolated = isolatedBlockResolver.findIsolatedBlock(matchedNode);

        return FunctionGenerator.generateFunction(functionName, args, returnsClause, returnStatement, newLineEnd,
                isIsolated, "");
    }

    private Optional<List<Symbol>> getVarAndParamSymbolsWithinRangeForExprs(LineRange matchedLineRange,
                                                                            CodeActionContext context) {
        List<Symbol> varAndParamSymbols =
                getVisibleSymbols(context, PositionUtil.toPosition(matchedLineRange.endLine())).stream()
                        .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE || symbol.kind() == SymbolKind.PARAMETER)
                        .filter(symbol -> context.currentSemanticModel().get().references(symbol).stream()
                                .anyMatch(location -> PositionUtil.isRangeWithinRange(PositionUtil
                                                .getRangeFromLineRange(location.lineRange()),
                                        PositionUtil.toRange(matchedLineRange))))
                        .collect(Collectors.toList());

        if (varAndParamSymbols.stream().anyMatch(symbol -> symbol.getLocation().isEmpty())) {
            return Optional.empty();
        }

        return Optional.of(varAndParamSymbols.stream()
                // getLocation() is present for each symbol
                .filter(symbol -> !PositionUtil.isWithinLineRange(symbol.getLocation().get().lineRange(),
                        matchedLineRange))
                .sorted(Comparator.comparingInt(symbol -> symbol.getLocation().get().textRange().startOffset()))
                .collect(Collectors.toList()));
    }

    private Optional<ArgListsHolder> getArgLists(CodeActionContext context, List<Symbol> symbolsList) {
        List<String> argsForExtractFunction = new ArrayList<>();
        List<String> argsForReplaceFunctionCall = new ArrayList<>();

        for (Symbol symbol : symbolsList) {
            if (symbol.getName().isEmpty()) {
                return Optional.empty();
            }
            argsForReplaceFunctionCall.add(symbol.getName().get());

            Optional<String> possibleType = Optional.empty();
            if (symbol.kind() == SymbolKind.VARIABLE) {
                possibleType = CodeActionUtil
                        .getPossibleType(((VariableSymbol) symbol).typeDescriptor(), new ArrayList<>(), context);

            } else if (symbol.kind() == SymbolKind.PARAMETER) {
                possibleType = CodeActionUtil
                        .getPossibleType(((ParameterSymbol) symbol).typeDescriptor(), new ArrayList<>(), context);
            }
            if (possibleType.isEmpty()) {
                return Optional.empty();
            }
            argsForExtractFunction.add(String.format("%s %s", possibleType.get(), symbol.getName().get()));
        }

        ArgListsHolder argListsHolder = new ArgListsHolder();
        argListsHolder.extractFunctionArgs = argsForExtractFunction;
        argListsHolder.replaceFunctionCallArgs = argsForReplaceFunctionCall;

        return Optional.of(argListsHolder);
    }

    private Node findEnclosingModulePartNode(NonTerminalNode node) {
        Node reference = node;

        while (reference.parent() != null && reference.parent().kind() != SyntaxKind.MODULE_PART) {
            reference = reference.parent();
        }
        return reference;
    }

    private boolean isWithinFunctionDefinitionNode(NonTerminalNode node) {
        NonTerminalNode reference = node;

        while (reference != null && reference.parent() != null) {
            SyntaxKind parentKind = reference.parent().kind();
            if (parentKind == SyntaxKind.FUNCTION_DEFINITION || parentKind == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION
                    || parentKind == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                return true;
            }
            reference = reference.parent();
        }

        return false;
    }

    private boolean addNewLineAtEnd(Node enclosingNode) {
        for (Node node : enclosingNode.parent().children()) {
            if (node.lineRange().startLine().line() == enclosingNode.lineRange().endLine().line() + 1) {
                return true;
            }
        }

        return false;
    }

    /*
     * TODO remove with the fix of #37454
     * */
    private static boolean isExpressionNode(NonTerminalNode node) {
        return (node.kind().compareTo(SyntaxKind.BINARY_EXPRESSION) >= 0
                && node.kind().compareTo(SyntaxKind.TYPE_DESC) < 0);
    }

    private boolean isExpressionExtractable(RangeBasedPositionDetails positionDetails) {
        NonTerminalNode node = positionDetails.matchedCodeActionNode();
        Node enclosingModulePartNode = findEnclosingModulePartNode(node);
        SyntaxKind nodeKind = node.kind();
        SyntaxKind parentKind = node.parent().kind();
        /*
         * Avoid providing the code action for the following since it is syntactically incorrect.
         * 1. inside module level declarations or definitions // todo include module-client-decl after 2201.3.0
         * 2. a mapping constructor used in a table constructor
         * 3. a field name of string literal in a specific field,    ex: var val = {"name": 1};
         * 4. the qualified name reference of a function call expression
         * 5. a field access expression,
         *   5.1. with self keyword inside its expression
         *   5.2. as varRef() in assignment statement
         *   5.3. as lhsExpression() in compound assignment statement
         **/
        return !unSupportedModuleLevelSyntaxKinds.contains(enclosingModulePartNode.kind())
                && (nodeKind != SyntaxKind.MAPPING_CONSTRUCTOR || parentKind != SyntaxKind.TABLE_CONSTRUCTOR)
                && (nodeKind != SyntaxKind.STRING_LITERAL || parentKind != SyntaxKind.SPECIFIC_FIELD)
                && (nodeKind != SyntaxKind.QUALIFIED_NAME_REFERENCE || parentKind != SyntaxKind.FUNCTION_CALL)
                && (nodeKind != SyntaxKind.FIELD_ACCESS
                || (!((FieldAccessExpressionNode) node).expression().toSourceCode().strip().equals(SymbolUtil.SELF_KW)
                && (parentKind != SyntaxKind.ASSIGNMENT_STATEMENT
                || !((AssignmentStatementNode) node.parent()).varRef().equals(node))
                && (parentKind != SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT
                || !((CompoundAssignmentStatementNode) node.parent()).lhsExpression().equals(node))));
    }

    public static List<SyntaxKind> getSupportedExpressionSyntaxKindsList() {
        return List.of(
                SyntaxKind.BINARY_EXPRESSION,
                SyntaxKind.BRACED_EXPRESSION,
                SyntaxKind.QUALIFIED_NAME_REFERENCE,
                SyntaxKind.INDEXED_EXPRESSION,
                SyntaxKind.FIELD_ACCESS,
                SyntaxKind.METHOD_CALL,
                SyntaxKind.MAPPING_CONSTRUCTOR,
                SyntaxKind.TYPEOF_EXPRESSION,
                SyntaxKind.UNARY_EXPRESSION,
                SyntaxKind.TYPE_TEST_EXPRESSION,
                SyntaxKind.SIMPLE_NAME_REFERENCE,
                SyntaxKind.LIST_CONSTRUCTOR,
                SyntaxKind.TYPE_CAST_EXPRESSION,
                SyntaxKind.TABLE_CONSTRUCTOR,
                SyntaxKind.LET_EXPRESSION,
                SyntaxKind.IMPLICIT_NEW_EXPRESSION,
                SyntaxKind.EXPLICIT_NEW_EXPRESSION,
                SyntaxKind.STRING_LITERAL,
                SyntaxKind.NUMERIC_LITERAL,
                SyntaxKind.BOOLEAN_LITERAL,
                SyntaxKind.ERROR_CONSTRUCTOR
        );
    }

    public static List<SyntaxKind> getSupportedStatementSyntaxKindsList() {
        return List.of(
                SyntaxKind.LIST, // not a statement
                SyntaxKind.LOCAL_VAR_DECL,
                SyntaxKind.ASSIGNMENT_STATEMENT,
                SyntaxKind.IF_ELSE_STATEMENT,
                SyntaxKind.WHILE_STATEMENT,
                SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT,
                SyntaxKind.LOCK_STATEMENT,
                SyntaxKind.FOREACH_STATEMENT,
                SyntaxKind.MATCH_STATEMENT,
                SyntaxKind.DO_STATEMENT
        );
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class ArgListsHolder {
        private List<String> extractFunctionArgs;
        private List<String> replaceFunctionCallArgs;
    }
}
