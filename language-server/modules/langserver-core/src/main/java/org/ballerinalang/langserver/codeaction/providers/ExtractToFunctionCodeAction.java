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
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
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
                && CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode())
                && (!isExpressionNode(positionDetails.matchedCodeActionNode())
                || isExpressionExtractable(context, positionDetails));
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
     * get the code action when the syntax kind of the matched code action node is a statement or a List of statements.
     */
    private List<CodeAction> getCodeActionsForStatements(CodeActionContext context,
                                                         RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();
        Optional<Node> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);

        if (enclosingNode.isEmpty() || !isWithinFunctionDefinitionNode(matchedCodeActionNode)) {
            return Collections.emptyList();
        }

        SemanticModel semanticModel = context.currentSemanticModel().get();

        ExtractToFunctionStatementAnalyzer statementAnalyzer
                = new ExtractToFunctionStatementAnalyzer(context.range(), semanticModel);
        statementAnalyzer.analyze(matchedCodeActionNode);

        /*
        * Here we decide whether the content of the selected range is syntactically correct
        * to be extracted to a function
        * */
        if (!statementAnalyzer.isExtractable()) {
            return Collections.emptyList();
        }

        Pair<Boolean, Optional<VariableSymbol>> isExtractableAndUpdatingVarPair
                = isExtractableAndUpdatingVar(statementAnalyzer, enclosingNode.get(), context);

        /*
        * Here we decide whether the selected range is extractable by the usages of symbols, empty scenario implies
        * multiple variable symbols updated and referred after range
         */
        if (isExtractableAndUpdatingVarPair.getLeft().equals(Boolean.FALSE)) {
            return Collections.emptyList();
        }

        // Empty scenario implies that extracted function should not return any value
        Optional<VariableSymbol> updatingVar = isExtractableAndUpdatingVarPair.getRight();
        Optional<String> possibleTypeOfUpdatingVar = Optional.empty();

        String returnTypeDescriptor = "";
        if (updatingVar.isPresent()) {
            Optional<String> posType =
                    CodeActionUtil.getPossibleType(updatingVar.get().typeDescriptor(), new ArrayList<>(), context);
            if (posType.isEmpty()) {
                return Collections.emptyList();
            }
            possibleTypeOfUpdatingVar = posType;
            returnTypeDescriptor = String.format("returns %s", possibleTypeOfUpdatingVar.get());
        }

        if (matchedCodeActionNode.kind() == SyntaxKind.RETURN_STATEMENT
                && ((ReturnStatementNode) matchedCodeActionNode).expression().isPresent()) {
            Optional<TypeSymbol> typeSymbol =
                    semanticModel.typeOf(((ReturnStatementNode) matchedCodeActionNode).expression().get());
            if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR
                    || typeSymbol.get().typeKind() == TypeDescKind.NONE) {
                return Collections.emptyList();
            }
             returnTypeDescriptor = String.format("returns %s", typeSymbol.get().typeKind().getName());
        }

        List<Symbol> argsSymbolsForExtractFunction =
                getVarAndParamSymbolsWithinRangeForStmts(context, enclosingNode.get());

        Optional<ArgListsHolder> argLists = getArgLists(context, argsSymbolsForExtractFunction);
        if (argLists.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> argsForExtractFunction = argLists.get().extractFunctionArgs;
        List<String> argsForReplaceFunctionCall = argLists.get().replaceFunctionCallArgs;

        String functionName = getFunctionName(context, matchedCodeActionNode);
        String returnStatement = "";
        if (updatingVar.isPresent() && updatingVar.get().getName().isPresent()) {
            returnStatement = String.format("return %s;", updatingVar.get().getName().get());
        }

        List<Node> selectedNodes = statementAnalyzer.getSelectedNodes();
        String funcBody = selectedNodes.stream().map(Node::toSourceCode).collect(Collectors.joining(""));
        Range extractFunctionInsertRange;

        boolean newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
        extractFunctionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());

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

        if (updatingVar.isPresent() && updatingVar.get().getName().isPresent()) {
            String varName = updatingVar.get().getName().get();
            replaceFunctionCall =
                    String.format("%s %s = %s", possibleTypeOfUpdatingVar.get(), varName, replaceFunctionCall);
        } else if (matchedCodeActionNode.kind() == SyntaxKind.RETURN_STATEMENT) {
            replaceFunctionCall = String.format("return %s", replaceFunctionCall);
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
     * Finds whether the selected range is extractable and if yes, whether a value should be returned or if yes,
     * the updating variable symbol. The two scenarios, if the nodes in the range is extractable:
     *
     * 1. extracted();                      // a value is not returned
     * 2. int varName = extracted();        // a value is returned
     *
     * @return A Pair of elements consisting the extract-ability and optional updating variable symbol
     */
    private Pair<Boolean, Optional<VariableSymbol>> isExtractableAndUpdatingVar(
            ExtractToFunctionStatementAnalyzer analyzer, Node enclosingNode, CodeActionContext context) {
        SemanticModel semanticModel = context.currentSemanticModel().get();
        // Updating symbols can be either localVar or moduleVar
        List<Symbol> updatedSymbols = analyzer.getUpdatedSymbols();
        List<Symbol> declaredVariableSymbols = analyzer.getDeclaredVariableSymbols();

        HashSet<Symbol> updatingOrDeclaredLocVarSymbolsInRange = new HashSet<>(updatedSymbols);
        updatingOrDeclaredLocVarSymbolsInRange.addAll(declaredVariableSymbols);

        List<Symbol> updatingModVarSymbols = updatedSymbols.stream()
                .filter(symbol -> symbol.getLocation().isPresent() && !PositionUtil
                        .isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.lineRange()))
                .collect(Collectors.toList());

        List<Symbol> updatingButNotDeclaredLocVarSymbolsInRange = updatedSymbols.stream()
                .filter(symbol -> symbol.getLocation().isPresent() && !PositionUtil.isRangeWithinRange
                        (PositionUtil.toRange(symbol.getLocation().get().lineRange()), context.range()))
                .filter(symbol -> !updatingModVarSymbols.contains(symbol))
                .collect(Collectors.toList());

        // localVarSymbols which are declared or updated inside the selected range and also referred after the range
        List<VariableSymbol> localVarSymbols = updatingOrDeclaredLocVarSymbolsInRange.stream()
                        .filter(symbol -> !updatingModVarSymbols.contains(symbol))
                        .filter(symbol -> semanticModel.references(symbol).stream()
                                .anyMatch(location -> PositionUtil.isRangeWithinRange
                                        (PositionUtil.toRange(location.lineRange()),
                                                getRangeAfterHighlightedRange(context, enclosingNode))))
                        .map(symbol -> (VariableSymbol) symbol)
                        .collect(Collectors.toList());

        /*
         * Following checks are done when deciding the selected range R is extractable to a function.
         *
         * 1. All updating local variables inside R have been declared inside R.
         * 2. The number of local variables in (1.) is less or equal to 1.
         *
         * */
        boolean isRangeExtractable = updatingButNotDeclaredLocVarSymbolsInRange.size() == 0
                && localVarSymbols.size() <= 1;

        // here we decide whether the selected range is extractable by the usages of symbols
        if (!isRangeExtractable) {
            return Pair.of(Boolean.FALSE, Optional.empty());
        }

        Optional<VariableSymbol> updatingVar = Optional.empty();
        if (localVarSymbols.size() == 1) {
            updatingVar = localVarSymbols.stream().findFirst();
        }
        return Pair.of(Boolean.TRUE, updatingVar);
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
    private List<Symbol> getVarAndParamSymbolsWithinRangeForStmts(CodeActionContext context, Node enclosingNode) {
        List<Symbol> argsSymbolsForExtractFunction = new ArrayList<>();
        List<Symbol> visibleSymbols = getVisibleSymbols(context, context.range().getStart());

        // Local variable symbols which has been declared before the selected range but within the enclosing node
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
            // check whether there is at least one symbol location that is being referred inside the selected range
            Optional<Location> anyLocationReferred = context.currentSemanticModel().get().references(symbol).stream()
                    .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil
                            .getRangeFromLineRange(location.lineRange()), context.range()))
                    .findAny();
            if (anyLocationReferred.isPresent()) {
                argsSymbolsForExtractFunction.add(symbol);
            }
        });

        return argsSymbolsForExtractFunction.stream()
                .sorted(Comparator.comparingInt(symbol -> symbol.getLocation().get().textRange().startOffset()))
                .collect(Collectors.toList());
    }

    /**
     * get the code action when the syntax kind of the matched code action node is an expression.
     */
    private List<CodeAction> getCodeActionsForExpressions(CodeActionContext context,
                                                          RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();

        // TODO improve the following newLineAtEnd logic with #37600
        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Optional<Node> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);
        Range functionInsertRange;

        if (enclosingNode.isPresent()) {
            newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
            functionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());
        } else {
            functionInsertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }

        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(matchedCodeActionNode);

        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Collections.emptyList();
        }

        List<Symbol> varAndParamSymbolsWithinRange =
                getVarAndParamSymbolsWithinRangeForExprs(matchedCodeActionNode.lineRange(), context);
        Optional<ArgListsHolder> argLists = getArgLists(context, varAndParamSymbolsWithinRange);

        if (argLists.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> argsForExtractFunction = argLists.get().extractFunctionArgs;
        List<String> argsForReplaceFunctionCall = argLists.get().replaceFunctionCallArgs;

        String functionName = getFunctionName(context, matchedCodeActionNode);
        String function = getFunction(matchedCodeActionNode, newLineAtEnd, typeSymbol.get(), functionName,
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

    private String getFunction(NonTerminalNode matchedNode, boolean newLineAtEnd, TypeSymbol typeSymbol,
                               String functionName, List<String> args) {
        String returnsClause = String.format("returns %s", typeSymbol.signature());
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

        return FunctionGenerator.generateFunction(functionName, args, returnsClause, returnStatement, newLineAtEnd,
                isIsolated, "");
    }

    private List<Symbol> getVarAndParamSymbolsWithinRangeForExprs(LineRange matchedLineRange,
                                                                  CodeActionContext context) {
        return getVisibleSymbols(context, PositionUtil.toPosition(matchedLineRange.endLine())).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE || symbol.kind() == SymbolKind.PARAMETER)
                .filter(symbol -> context.currentSemanticModel().get().references(symbol).stream()
                        .anyMatch(location -> PositionUtil.isRangeWithinRange(PositionUtil
                                        .getRangeFromLineRange(location.lineRange()),
                                PositionUtil.toRange(matchedLineRange))))
                // for every variable and parameter symbol, the symbol.getLocation() is not empty
                .filter(symbol -> !PositionUtil.isWithinLineRange(symbol.getLocation().get().lineRange(),
                        matchedLineRange))
                .sorted(Comparator.comparingInt(symbol -> symbol.getLocation().get().textRange().startOffset()))
                .collect(Collectors.toList());
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

    private Optional<Node> findEnclosingModulePartNode(NonTerminalNode node) {
        Node reference = node;

        while (reference != null && reference.parent() != null) {
            if (reference.parent().kind() == SyntaxKind.MODULE_PART) {
                return Optional.of(reference);
            }
            reference = reference.parent();
        }

        return Optional.empty();
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
    * // TODO remove with the fix of #37454
    * */
    private static boolean isExpressionNode(NonTerminalNode node) {
        return (node.kind().compareTo(SyntaxKind.BINARY_EXPRESSION) >= 0
                    && node.kind().compareTo(SyntaxKind.TYPE_DESC) < 0);
    }

    private boolean isExpressionExtractable(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        NonTerminalNode matchedCodeActionNode = positionDetails.matchedCodeActionNode();
        Optional<Node> enclosingModulePartNode = findEnclosingModulePartNode(matchedCodeActionNode);

        if (enclosingModulePartNode.isEmpty()
                || unSupportedModuleLevelSyntaxKinds.contains(enclosingModulePartNode.get().kind())) {
            return false;
        }

        if (context.currentSyntaxTree().isEmpty() || matchedCodeActionNode.kind() == SyntaxKind.MAPPING_CONSTRUCTOR
                && matchedCodeActionNode.parent().kind() == SyntaxKind.TABLE_CONSTRUCTOR) {
            return false;
        }

        if (matchedCodeActionNode.kind() == SyntaxKind.STRING_LITERAL
                && matchedCodeActionNode.parent().kind() == SyntaxKind.SPECIFIC_FIELD) {
            return false;
        }

        if (matchedCodeActionNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE
                && matchedCodeActionNode.parent().kind() == SyntaxKind.FUNCTION_CALL) {
            return false;
        }

        return matchedCodeActionNode.kind() != SyntaxKind.FIELD_ACCESS
                || (!((FieldAccessExpressionNode) matchedCodeActionNode).expression().toSourceCode().strip()
                .equals(SymbolUtil.SELF_KW)
                && matchedCodeActionNode.parent().kind() != SyntaxKind.ASSIGNMENT_STATEMENT
                && matchedCodeActionNode.parent().kind() != SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT);
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
                SyntaxKind.RETURN_STATEMENT,
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
