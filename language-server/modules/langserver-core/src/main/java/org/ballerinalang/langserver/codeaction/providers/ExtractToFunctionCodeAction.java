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
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionAssignmentFinder;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Code Action for extracting code segments to a function.
 *
 * @since 2201.2.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractToFunctionCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Extract to method";

    private static final String EXTRACTED_PREFIX = "extracted";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        List<SyntaxKind> syntaxKinds = new ArrayList<>(CodeActionUtil.getExpressionSyntaxKindsList());
        syntaxKinds.add(SyntaxKind.LIST);
        //todo check other cases and also extractable for local var decl
        syntaxKinds.add(SyntaxKind.BLOCK_STATEMENT);
        syntaxKinds.add(SyntaxKind.IF_ELSE_STATEMENT);
//        syntaxKinds.add(SyntaxKind.LOCAL_VAR_DECL);
//        syntaxKinds.add(SyntaxKind.ASSIGNMENT_STATEMENT);
//        syntaxKinds.add(SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT);
        return syntaxKinds;
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        SyntaxKind kind = posDetails.matchedCodeActionNode().kind();
        if (kind == SyntaxKind.LIST || kind == SyntaxKind.BLOCK_STATEMENT || kind == SyntaxKind.IF_ELSE_STATEMENT) {
            return getCodeActionsForStatements(context, posDetails);
        }
        return getCodeActionsForExpressions(context, posDetails);
    }

    private List<CodeAction> getCodeActionsForStatements(CodeActionContext context,
                                                         RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();
        Optional<NonTerminalNode> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode); // todo this should support nested if else blocks as well
        if (context.currentSemanticModel().isEmpty() || enclosingNode.isEmpty() || enclosingNode.get().kind() != SyntaxKind.FUNCTION_DEFINITION) { // todo remove this to allow global level extract to funcs
            return Collections.emptyList();
        }

        SemanticModel semanticModel = context.currentSemanticModel().get();

        NodeList<StatementNode> statementNodes = ((FunctionBodyBlockNode) ((FunctionDefinitionNode) enclosingNode.get()).functionBody()).statements();
        Position endPosOfLastStatementNode = PositionUtil.toPosition(statementNodes.get(statementNodes.size() - 1).lineRange().endLine());
        Range rangeAfterHighlightedRange = new Range(context.range().getEnd(), endPosOfLastStatementNode); // todo check whether startPos correct here

        List<Symbol> bindingSymbolsBeforeEndOfRange = getVisibleSymbols(context, context.range().getEnd()).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.CONSTANT
                        || symbol.kind() == SymbolKind.PARAMETER || symbol.kind() == SymbolKind.VARIABLE)
                .collect(Collectors.toList());

        List<Symbol> localVarSymbolsDeclarationsBeforeStartOfRange = getVisibleSymbols(context, context.range().getStart()).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE)
                .filter(symbol -> PositionUtil.isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.get().lineRange()))
                .collect(Collectors.toList());

        List<Symbol> parameterSymbols = bindingSymbolsBeforeEndOfRange.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.PARAMETER)
                .collect(Collectors.toList());

        List<Symbol> visibleLocalAndParamSymbolsToRange = Stream.concat(localVarSymbolsDeclarationsBeforeStartOfRange.stream(), parameterSymbols.stream()).collect(Collectors.toList());

        // local var symbols which are referred in the selected range and parameter symbols if referred in the selected range
        List<Symbol> argsSymbolsForExtractFunction = new ArrayList<>();
        visibleLocalAndParamSymbolsToRange.forEach(symbol -> {
            Optional<Location> anyLocationReffered = semanticModel.references(symbol).stream()
                    .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil.getRangeFromLineRange(location.lineRange()), context.range()))
                    .findAny();
            if (anyLocationReffered.isPresent()) {
                argsSymbolsForExtractFunction.add(symbol);
            }
        });

        CodeActionAssignmentFinder codeActionAssignmentFinder = new CodeActionAssignmentFinder(context.range(), semanticModel);
        codeActionAssignmentFinder.assignmentFinder(matchedCodeActionNode);

        // these assigned symbols can be either localVar or moduleVar
        List<Symbol> assignmentStatementSymbolsInRangeNEW = codeActionAssignmentFinder.getAssignmentStatementSymbols();
        List<Symbol> localVarDeclarationSymbolsInRangeNEW = codeActionAssignmentFinder.getVarDeclarationSymbols();
        List<Node> selectedNodes = codeActionAssignmentFinder.getSelectedNodes();

        Set<Symbol> assignmentOrLocalVarDeclSymbolsInRangeNEW = Stream.of(assignmentStatementSymbolsInRangeNEW, localVarDeclarationSymbolsInRangeNEW).flatMap(Collection::stream).collect(Collectors.toSet());

        List<Symbol> assStmtModuleVarSymbolsListNEW = assignmentStatementSymbolsInRangeNEW.stream()
                .filter(symbol -> !PositionUtil.isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.get().lineRange()))
                .collect(Collectors.toList());

        List<Symbol> assStmtLocalVarSymbolsInRangeAndNotDeclaredWithinRangeListNEW = assignmentStatementSymbolsInRangeNEW.stream()
                .filter(symbol -> !PositionUtil.isRangeWithinRange(PositionUtil.toRange(symbol.getLocation().get().lineRange()), context.range()))
                .filter(symbol -> !assStmtModuleVarSymbolsListNEW.contains(symbol))
                .collect(Collectors.toList());

        List<VariableSymbol> localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW = assignmentOrLocalVarDeclSymbolsInRangeNEW.stream()
                .filter(symbol -> !assStmtModuleVarSymbolsListNEW.contains(symbol))
                .filter(symbol -> !semanticModel.references(symbol).stream()
                        .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil.toRange(location.lineRange()), rangeAfterHighlightedRange)).collect(Collectors.toList()).isEmpty())
                .map(symbol -> (VariableSymbol) symbol)
                .collect(Collectors.toList());

        List<VariableSymbol> moduleVarSymbolsAssignedInRangeAndReferredAfterRangeNEW = assStmtModuleVarSymbolsListNEW.stream()
                .filter(symbol -> !semanticModel.references(symbol).stream()
                        .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil.toRange(location.lineRange()), rangeAfterHighlightedRange)).collect(Collectors.toList()).isEmpty())
                .map(symbol -> (VariableSymbol) symbol)
                .collect(Collectors.toList());

        List<VariableSymbol> varSymbolsAssignedInRangeAndReferredAfterRangeNEW = Stream.concat(localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.stream(), moduleVarSymbolsAssignedInRangeAndReferredAfterRangeNEW.stream()).collect(Collectors.toList());

        boolean isRangeExtractableNEW = assStmtLocalVarSymbolsInRangeAndNotDeclaredWithinRangeListNEW.size() == 0
                && varSymbolsAssignedInRangeAndReferredAfterRangeNEW.size() <= 1;

        if (!isRangeExtractableNEW) {
            return Collections.emptyList();
        }

        String returnTypeDescriptor = "";
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.size() == 1) {
            TypeDescKind returnTypeDescKind = localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.get(0).typeDescriptor().typeKind();
            returnTypeDescriptor = String.format("returns %s", returnTypeDescKind.getName());
        }

        List<String> argsForExtractFunction = new ArrayList<>();
        List<String> argsForReplaceFunctionCall = new ArrayList<>();

        argsSymbolsForExtractFunction.forEach(symbol -> {
            if (symbol.kind() == SymbolKind.VARIABLE && symbol.getName().isPresent()) {
                argsForExtractFunction.add(String.format("%s %s", ((VariableSymbol) symbol).typeDescriptor().typeKind().getName(), symbol.getName().get()));
            } else if (symbol.kind() == SymbolKind.PARAMETER) {
                argsForExtractFunction.add(String.format("%s %s", ((ParameterSymbol) symbol).typeDescriptor().typeKind().getName(), symbol.getName().get()));
            }
        });

        argsSymbolsForExtractFunction.forEach(symbol -> argsForReplaceFunctionCall.add(symbol.getName().get()));

        String functionName = getFunctionName(context);
        // todo get a better logic for following
        String returnStatement = "";
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.size() == 1) {
            returnStatement = String.format("return %s;", localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.get(0).getName().get());
        }
        // todo try to do this with leadingMinutiae()
        String funcBody = getFunctionBodyForStatements(selectedNodes);

        // todo start get start to end to a function
        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Range extractFunctionInsertRange;

        if (enclosingNode.isPresent()) { // todo keeping this to support module level extract to funcs in future
            newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
            extractFunctionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());
        } else {
            extractFunctionInsertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }
        // todo end

        String extractFunction = generateFunction(functionName, argsForExtractFunction, returnTypeDescriptor, returnStatement, newLineAtEnd, false, funcBody);
        String replaceFunctionCall = getReplaceFunctionCall(argsForReplaceFunctionCall, functionName, true);
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.size() == 1) {
            //todo try to use this once. this is already used in "return <>" and "returns <>"
            TypeDescKind returnTypeDescKind = localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.get(0).typeDescriptor().typeKind();
            String varName = localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRangeNEW.get(0).getName().get();
            replaceFunctionCall = String.format("%s %s = %s", returnTypeDescKind.getName(), varName, replaceFunctionCall);
        }

        //todo change with newLineAtEnd
        Position replaceFunctionCallStartPosition = PositionUtil.toPosition(selectedNodes.get(0).lineRange().startLine());
        Position replaceFunctionCallEndPosition = PositionUtil.toPosition(selectedNodes.get(selectedNodes.size() - 1).lineRange().endLine());
        Range replaceFunctionCallInsertRange = new Range(replaceFunctionCallStartPosition, replaceFunctionCallEndPosition);

        TextEdit extractFunctionEdit = new TextEdit(extractFunctionInsertRange, extractFunction); // at the end
        TextEdit replaceFunctionCallEdit = new TextEdit(replaceFunctionCallInsertRange, replaceFunctionCall);

        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_FUNCTION,
                List.of(extractFunctionEdit, replaceFunctionCallEdit), context.fileUri(), CodeActionKind.RefactorExtract);

        return List.of(codeAction);
    }

    private String getFunctionBodyForStatements(List<Node> selectedNodes) {
        List<String> selectedNodeStrings = selectedNodes.stream().map(Node::toSourceCode).collect(Collectors.toList());
        int paddingOffset = 4;
        String firstLine = selectedNodeStrings.get(0); //assumes selectedNodes is not empty
        int firstLineOffset = firstLine.length() - firstLine.stripLeading().length();
        String firstLinePadding = " ".repeat(paddingOffset);
        String firstLineWithOffset = firstLinePadding + firstLine.stripLeading();

        if (selectedNodeStrings.size() == 1) {
            return firstLineWithOffset;
        }

        boolean anyLinesWithLeftwardOffsets = selectedNodeStrings.stream()
                .map(s -> s.length() - s.stripLeading().length())
                .anyMatch(lineOffset -> lineOffset < firstLineOffset);

        if (anyLinesWithLeftwardOffsets) {
            return firstLineWithOffset + String.join("", selectedNodeStrings.subList(1, selectedNodes.size()));
        }

        if(firstLineOffset <= paddingOffset) {
            return selectedNodeStrings.stream()
                    .map(s -> " ".repeat(paddingOffset - firstLineOffset) + s)
                    .collect(Collectors.joining(""));
        } else {
            return selectedNodeStrings.stream()
                    .map(s -> s.substring(firstLineOffset - paddingOffset))
                    .collect(Collectors.joining(""));
        }
    }

    private Optional<NonTerminalNode> findEnclosingStatementBlockNode(NonTerminalNode node) {
        NonTerminalNode reference = node;
        List<SyntaxKind> statementBlockNodesKinds = List.of(SyntaxKind.BLOCK_STATEMENT, SyntaxKind.IF_ELSE_STATEMENT,
                SyntaxKind.DO_STATEMENT, SyntaxKind.MATCH_STATEMENT, SyntaxKind.FOREACH_STATEMENT,
                SyntaxKind.WHILE_STATEMENT, SyntaxKind.LOCK_STATEMENT);

        while (reference != null) {
            if (statementBlockNodesKinds.contains(reference.kind())) {
                return Optional.of(reference);
            }
            reference = reference.parent();
        }
        return Optional.empty();
    }

    private List<CodeAction> getCodeActionsForExpressions(CodeActionContext context,
                                                          RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();

        Path filePath = context.filePath();
        if (context.currentSyntaxTree().isEmpty()
                || context.workspace().semanticModel(filePath).isEmpty()) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Optional<NonTerminalNode> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);
        Range functionInsertRange;

        if (enclosingNode.isPresent()) {
            newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
            functionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());
        } else {
            functionInsertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }

        TypeSymbol typeSymbol = posDetails.matchedTopLevelTypeSymbol();// todo typeof(matchedNode?)
        if (typeSymbol == null || typeSymbol.kind() != SymbolKind.TYPE) {// null in func() {2}
            return Collections.emptyList();
        }

        TypeDescKind typeDescKind = typeSymbol.typeKind();

        String functionName = getFunctionName(context);
        String function = getFunction(matchedCodeActionNode, newLineAtEnd, typeDescKind, functionName, "", context);

        String replaceFunctionCall = getReplaceFunctionCall(context, functionName);

        TextEdit extractFunctionEdit = new TextEdit(functionInsertRange, function);
        TextEdit replaceEdit = new TextEdit(PositionUtil.toRange(matchedCodeActionNode.lineRange()),
                replaceFunctionCall);
        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_FUNCTION,
                List.of(extractFunctionEdit, replaceEdit), context.fileUri(), CodeActionKind.RefactorExtract);
        return List.of(codeAction);
    }

    private String getFunctionName(CodeActionContext context) {
        Set<String> visibleSymbolNames = context.visibleSymbols(context.range().getEnd()).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        return NameUtil.generateTypeName(EXTRACTED_PREFIX, visibleSymbolNames);
    }

    private List<Symbol> getVisibleSymbols(CodeActionContext context, Position position) {
        return context.currentSemanticModel().get()
                .visibleSymbols(context.currentDocument().get(), PositionUtil.getLinePosition(position));
    }

    private String getReplaceFunctionCall(CodeActionContext context, String functionName) {
        List<String> args = new ArrayList<>();
        List<Symbol> varNamesWithinTheRange = getVarNamesWithinTheRange(context);
        List<String> collect = varNamesWithinTheRange.stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        collect.forEach(s -> {
            args.add(s);
        });
        StringBuilder fnBuilder = new StringBuilder();
        fnBuilder.append(functionName)
                .append(CommonKeys.OPEN_PARENTHESES_KEY)
                .append(String.join(", ", args))
                .append(CommonKeys.CLOSE_PARENTHESES_KEY);
        String replaceFunctionName = fnBuilder.toString();
        return replaceFunctionName;
    }

    private String getReplaceFunctionCall(List<String> varSymbolNames, String functionName, boolean isFuncEnd) {
        StringBuilder fnBuilder = new StringBuilder();
        fnBuilder.append(functionName)
                .append(CommonKeys.OPEN_PARENTHESES_KEY)
                .append(String.join(", ", varSymbolNames))
                .append(CommonKeys.CLOSE_PARENTHESES_KEY);

        if (isFuncEnd) {
            fnBuilder.append(CommonKeys.SEMI_COLON_SYMBOL_KEY);
        }
        return fnBuilder.toString();
    }

    private String getFunction(NonTerminalNode matchedCodeActionNode, boolean newLineAtEnd, TypeDescKind typeDescKind,
                               String functionName, String funcBody, CodeActionContext context) {
        List<String> args = new ArrayList<>();
        List<Symbol> varNamesWithinTheRange = getVarNamesWithinTheRange(context);
        varNamesWithinTheRange.forEach(symbol -> {
            if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol;
                TypeSymbol rawType = CommonUtil.getRawType(variableSymbol.typeDescriptor());
                // todo try the usage of getParameterTypeAsString in FunctionGenerator.java
                args.add(rawType.signature() + " " + symbol.getName().get());
            }
        });
        String returnsClause = String.format("returns %s", typeDescKind.getName());
        String returnStatement = String.format("return %s;", matchedCodeActionNode.toString().strip());
        String function = generateFunction(functionName, args, returnsClause,
                returnStatement, newLineAtEnd, false, funcBody);
        return function;
    }

    private boolean isMatchedNodeHighlighted(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        Range highlightedRange = context.range();
        NonTerminalNode matchedNode = positionDetails.matchedCodeActionNode();
        Range matchedNodeRange = PositionUtil.toRange(matchedNode.lineRange());
        return highlightedRange.getStart() != highlightedRange.getEnd()
                && highlightedRange.equals(matchedNodeRange);
    }

    private List<Symbol> getVarNamesWithinTheRange(CodeActionContext context) {
        List<Symbol> collect = context.visibleSymbols(context.range().getEnd()).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE || symbol.kind() == SymbolKind.PARAMETER)
                .filter(symbol -> context.currentSemanticModel().get().references(symbol).stream()
                        .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil
                                .getRangeFromLineRange(location.lineRange()), context.range())).findAny().isPresent())
//                .filter(symbol -> PositionUtil.isWithinRange(PositionUtil.toPosition(symbol.getLocation().get().lineRange()), context.range()))
                //        .filter(symbol -> PositionUtil.is)
                //        .map(symbol -> symbol.getLocation().get().lineRange())
                .collect(Collectors.toList());
        return collect;
    }

    private Optional<NonTerminalNode> findEnclosingModulePartNode(NonTerminalNode node) {
        NonTerminalNode reference = node;
        while (reference != null && reference.parent() != null) {
            if (reference.parent().kind() == SyntaxKind.MODULE_PART) {
                return Optional.of(reference);
            }
            reference = reference.parent();
        }
        return Optional.empty();
    }

    private boolean addNewLineAtEnd(Node enclosingNode) {
        for (Node node : enclosingNode.parent().children()) {
            if (node.lineRange().startLine().line() == enclosingNode.lineRange().endLine().line() + 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static String generateFunction(String functionName, List<String> args, String returnsClause,
                                          String returnStmt, boolean newLineAtEnd, boolean isolated, String funcBody) {
        // padding
        int padding = 4;
        String paddingStr = StringUtils.repeat(" ", padding);

        // return statement
        String bodyReturnStmt = "";
        if (!returnStmt.isEmpty()) {
            bodyReturnStmt = paddingStr + returnStmt + CommonUtil.LINE_SEPARATOR;
        }

        StringBuilder fnBuilder = new StringBuilder();
        if (!functionName.isEmpty()) {
            fnBuilder.append(CommonUtil.LINE_SEPARATOR).append(CommonUtil.LINE_SEPARATOR);
        }

        if (isolated) {
            fnBuilder.append("isolated ");
        }

        fnBuilder.append("function").append(" ").append(functionName)
                .append(CommonKeys.OPEN_PARENTHESES_KEY)
                .append(String.join(", ", args))
                .append(CommonKeys.CLOSE_PARENTHESES_KEY);

        if (!returnsClause.isEmpty()) {
            fnBuilder.append(" ").append(returnsClause);
        }

        fnBuilder.append(" ").append(CommonKeys.OPEN_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR)
                .append(funcBody)
                .append(bodyReturnStmt)
                .append(CommonKeys.CLOSE_BRACE_KEY);

        if (!functionName.isEmpty() && newLineAtEnd) {
            fnBuilder.append(CommonUtil.LINE_SEPARATOR);
        }

        return fnBuilder.toString();
    }
}
