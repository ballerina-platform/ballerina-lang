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
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.ExtractToFunctionAnalyzer;
import org.ballerinalang.langserver.command.visitors.IsolatedBlockResolver;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
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
        return Stream
                .concat(getSupportedStatementSyntaxKindsList().stream(), getSupportedExpressionSyntaxKindsList()
                        .stream()).collect(Collectors.toList());
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        if (getSupportedStatementSyntaxKindsList().contains(posDetails.matchedCodeActionNode().kind())) {
            return getCodeActionsForStatements(context, posDetails);
        }
        return getCodeActionsForExpressions(context);
    }

    private List<CodeAction> getCodeActionsForStatements(CodeActionContext context,
                                                         RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();
        Optional<Node> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);
        Optional<NonTerminalNode> enclosingFuncDefNode = findEnclosingFunctionDefinitionNode(matchedCodeActionNode);
        if (context.currentSemanticModel().isEmpty() || enclosingNode.isEmpty() || enclosingFuncDefNode.isEmpty()) {
            return Collections.emptyList();
        }

        SemanticModel semanticModel = context.currentSemanticModel().get();

        Position endPosOfEnclosingNode = PositionUtil.toPosition(enclosingNode.get().lineRange().endLine());
        Position endPosOfStatements
                = new Position(endPosOfEnclosingNode.getLine(), endPosOfEnclosingNode.getCharacter() - 1);
        Range rangeAfterHighlightedRange = new Range(context.range().getEnd(), endPosOfStatements);

        List<Symbol> bindingSymbolsBeforeEndOfRange = getVisibleSymbols(context, context.range().getEnd()).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.CONSTANT
                        || symbol.kind() == SymbolKind.PARAMETER || symbol.kind() == SymbolKind.VARIABLE)
                .collect(Collectors.toList());

        List<Symbol> localVarSymbolsDeclarationsBeforeStartOfRange
                = getVisibleSymbols(context, context.range().getStart()).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE && symbol.getLocation().isPresent())
                .filter(symbol -> PositionUtil
                        .isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.get().lineRange()))
                .collect(Collectors.toList());

        List<Symbol> parameterSymbols = bindingSymbolsBeforeEndOfRange.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.PARAMETER)
                .collect(Collectors.toList());

        List<Symbol> visibleLocalAndParamSymbolsToRange = Stream
                .concat(localVarSymbolsDeclarationsBeforeStartOfRange.stream(), parameterSymbols.stream())
                .collect(Collectors.toList());

        // local var symbols which are referred in the selected range and parameter symbols if referred in the
        // selected range
        List<Symbol> argsSymbolsForExtractFunction = new ArrayList<>();
        visibleLocalAndParamSymbolsToRange.forEach(symbol -> {
            Optional<Location> anyLocationReferred = semanticModel.references(symbol).stream()
                    .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil
                            .getRangeFromLineRange(location.lineRange()), context.range()))
                    .findAny();
            if (anyLocationReferred.isPresent()) {
                argsSymbolsForExtractFunction.add(symbol);
            }
        });

        ExtractToFunctionAnalyzer extractToFunctionAnalyzer
                = new ExtractToFunctionAnalyzer(context.range(), semanticModel);
        extractToFunctionAnalyzer.analyze(matchedCodeActionNode);

        if (!extractToFunctionAnalyzer.isExtractable()) {
            return Collections.emptyList();
        }

        // these assigned symbols can be either localVar or moduleVar
        List<Symbol> assignmentStatementSymbolsInRange = extractToFunctionAnalyzer.getAssignmentStatementSymbols();
        List<Symbol> localVarDeclarationSymbolsInRange = extractToFunctionAnalyzer.getVarDeclarationSymbols();
        List<Node> selectedNodes = extractToFunctionAnalyzer.getSelectedNodes();

        Set<Symbol> assignmentOrLocalVarDeclSymbolsInRange =
                Stream.of(assignmentStatementSymbolsInRange, localVarDeclarationSymbolsInRange)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

        List<Symbol> assStmtModuleVarSymbolsInRange = assignmentStatementSymbolsInRange.stream()
                .filter(symbol -> symbol.getLocation().isPresent() && !PositionUtil
                        .isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.get().lineRange()))
                .collect(Collectors.toList());

        List<Symbol> assStmtLocalVarSymbolsInRangeAndNotDeclaredWithinRange = assignmentStatementSymbolsInRange.stream()
                .filter(symbol -> symbol.getLocation().isPresent() && !PositionUtil.isRangeWithinRange
                        (PositionUtil.toRange(symbol.getLocation().get().lineRange()), context.range()))
                .filter(symbol -> !assStmtModuleVarSymbolsInRange.contains(symbol))
                .collect(Collectors.toList());

        List<VariableSymbol> localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange =
                assignmentOrLocalVarDeclSymbolsInRange.stream()
                        .filter(symbol -> !assStmtModuleVarSymbolsInRange.contains(symbol))
                        .filter(symbol -> semanticModel.references(symbol).stream()
                                .anyMatch(location -> PositionUtil.isRangeWithinRange
                                        (PositionUtil.toRange(location.lineRange()), rangeAfterHighlightedRange)))
                        .map(symbol -> (VariableSymbol) symbol)
                        .collect(Collectors.toList());

        List<VariableSymbol> moduleVarSymbolsAssignedInRangeAndReferredAfterRange =
                assStmtModuleVarSymbolsInRange.stream()
                        .filter(symbol -> semanticModel.references(symbol).stream()
                                .anyMatch(location -> PositionUtil.isRangeWithinRange
                                        (PositionUtil.toRange(location.lineRange()), rangeAfterHighlightedRange)))
                        .map(symbol -> (VariableSymbol) symbol)
                        .collect(Collectors.toList());

        List<VariableSymbol> varSymbolsAssignedInRangeAndReferredAfterRange =
                Stream.concat(localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.stream(),
                        moduleVarSymbolsAssignedInRangeAndReferredAfterRange.stream()).collect(Collectors.toList());

        boolean isRangeExtractable = assStmtLocalVarSymbolsInRangeAndNotDeclaredWithinRange.size() == 0
                && varSymbolsAssignedInRangeAndReferredAfterRange.size() <= 1;

        if (!isRangeExtractable) {
            return Collections.emptyList();
        }

        Optional<VariableSymbol> updatingVar = Optional.empty();
        Optional<String> possibleTypeOfUpdatingVar = Optional.empty();
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.size() == 1) {
            updatingVar = Optional.of(localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.iterator().next());
        }

        if (updatingVar.isPresent()) {
            Optional<String> posType =
                    CodeActionUtil.getPossibleType(updatingVar.get().typeDescriptor(), new ArrayList<>(), context);
            if (posType.isEmpty()) {
                return Collections.emptyList();
            }
            possibleTypeOfUpdatingVar = posType;
        }

        String returnTypeDescriptor = "";
        if (updatingVar.isPresent()) {
            returnTypeDescriptor = String.format("returns %s", possibleTypeOfUpdatingVar.get());
        } else if (matchedCodeActionNode.kind() == SyntaxKind.RETURN_STATEMENT
                && ((ReturnStatementNode) matchedCodeActionNode).expression().isPresent()) {
            Optional<TypeSymbol> typeSymbol =
                    semanticModel.typeOf(((ReturnStatementNode) matchedCodeActionNode).expression().get());
            if (typeSymbol.isPresent() && (typeSymbol.get().typeKind() != TypeDescKind.COMPILATION_ERROR
                    || typeSymbol.get().typeKind() != TypeDescKind.NONE)) {
                returnTypeDescriptor = String.format("returns %s", typeSymbol.get().typeKind().getName());
            } else {
                return Collections.emptyList();
            }
        }

        List<String> argsForExtractFunction = new ArrayList<>();
        List<String> argsForReplaceFunctionCall = new ArrayList<>();

        for (Symbol symbol : argsSymbolsForExtractFunction) {
            if (symbol.getName().isEmpty()) {
                return Collections.emptyList();
            }
            argsForReplaceFunctionCall.add(symbol.getName().get());

            if (symbol.kind() == SymbolKind.VARIABLE && symbol.getName().isPresent()) {
                Optional<String> possibleType = CodeActionUtil
                        .getPossibleType(((VariableSymbol) symbol).typeDescriptor(), new ArrayList<>(), context);
                if (possibleType.isEmpty()) {
                    return Collections.emptyList();
                }
                argsForExtractFunction.add(String.format("%s %s", possibleType.get(), symbol.getName().get()));
            } else if (symbol.kind() == SymbolKind.PARAMETER) {
                Optional<String> possibleType = CodeActionUtil
                        .getPossibleType(((ParameterSymbol) symbol).typeDescriptor(), new ArrayList<>(), context);
                if (possibleType.isEmpty()) {
                    return Collections.emptyList();
                }
                argsForExtractFunction.add(String.format("%s %s", possibleType.get(), symbol.getName().get()));
            }
        }

        String functionName = getFunctionName(context);
        String returnStatement = "";
        if (updatingVar.isPresent() && updatingVar.get().getName().isPresent()) {
            returnStatement = String.format("return %s;", updatingVar.get().getName().get());
        }
        String funcBody = getFunctionBodyForStatements(selectedNodes);

        Range extractFunctionInsertRange;

        boolean newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
        extractFunctionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());

        // Check if the function call is invoked from an isolated context.
        IsolatedBlockResolver isolatedBlockResolver = new IsolatedBlockResolver();
        Boolean isIsolated = isolatedBlockResolver.findIsolatedBlock(matchedCodeActionNode);

        String extractFunction = generateFunction(functionName, argsForExtractFunction, returnTypeDescriptor,
                returnStatement, newLineAtEnd, isIsolated, funcBody);
        String replaceFunctionCall = getReplaceFunctionCall(argsForReplaceFunctionCall, functionName, true);

        if (updatingVar.isPresent() && updatingVar.get().getName().isPresent()) {
            //todo try to use this once. this is already used in "return <>" and "returns <>"
            String varName = updatingVar.get().getName().get();
            replaceFunctionCall =
                    String.format("%s %s = %s", possibleTypeOfUpdatingVar.get(), varName, replaceFunctionCall);
        } else if (matchedCodeActionNode.kind() == SyntaxKind.RETURN_STATEMENT) {
            replaceFunctionCall = String.format("return %s", replaceFunctionCall);
        }

        //todo change with newLineAtEnd
        Position replaceFuncCallStartPos = PositionUtil.toPosition(selectedNodes.get(0).lineRange().startLine());
        Position replaceFuncCallEndPos = PositionUtil
                .toPosition(selectedNodes.get(selectedNodes.size() - 1).lineRange().endLine());
        Range replaceFunctCallInsertRange = new Range(replaceFuncCallStartPos, replaceFuncCallEndPos);

        TextEdit extractFunctionEdit = new TextEdit(extractFunctionInsertRange, extractFunction); // at the end
        TextEdit replaceFunctionCallEdit = new TextEdit(replaceFunctCallInsertRange, replaceFunctionCall);

        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.EXTRACT_TO_FUNCTION,
                List.of(extractFunctionEdit, replaceFunctionCallEdit), context.fileUri(),
                CodeActionKind.RefactorExtract);

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

        if (firstLineOffset <= paddingOffset) {
            return selectedNodeStrings.stream()
                    .map(s -> " ".repeat(paddingOffset - firstLineOffset) + s)
                    .collect(Collectors.joining(""));
        } else {
            return selectedNodeStrings.stream()
                    .map(s -> s.substring(firstLineOffset - paddingOffset))
                    .collect(Collectors.joining(""));
        }
    }

    private List<CodeAction> getCodeActionsForExpressions(CodeActionContext context) {
        Node nodeAtRange = context.nodeAtRange();

        if (context.currentSyntaxTree().isEmpty() || context.currentSemanticModel().isEmpty() ||
                (nodeAtRange.kind() == SyntaxKind.MAPPING_CONSTRUCTOR
                        && nodeAtRange.parent() != null
                        && nodeAtRange.parent().kind() == SyntaxKind.TABLE_CONSTRUCTOR)) {
            return Collections.emptyList();
        }

        if (nodeAtRange.parent() != null) {
            if (nodeAtRange.parent().kind() == SyntaxKind.FIELD_ACCESS
                    && ((FieldAccessExpressionNode) nodeAtRange.parent()).fieldName().equals(nodeAtRange)) {
                return Collections.emptyList();
            } else if (nodeAtRange.parent().kind() == SyntaxKind.METHOD_CALL
                    && ((MethodCallExpressionNode) nodeAtRange.parent()).methodName().equals(nodeAtRange)) {
                return Collections.emptyList();
            }
        }

        if (nodeAtRange.kind() == SyntaxKind.FIELD_ACCESS) {
            if (((FieldAccessExpressionNode) nodeAtRange).expression().toSourceCode().strip().equals(SymbolUtil.SELF_KW)
                    || nodeAtRange.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT
                    || nodeAtRange.parent().kind() == SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT) {
                return Collections.emptyList();
            }
        }

        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        Optional<Node> enclosingNode = findEnclosingModulePartNode(nodeAtRange);
        Range functionInsertRange;

        if (enclosingNode.isPresent()) {
            newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
            functionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());
        } else {
            functionInsertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }

        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel().get().typeOf(nodeAtRange);

        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
            return Collections.emptyList();
        }

        String functionName = getFunctionName(context);
        String function = getFunction(nodeAtRange, newLineAtEnd, typeSymbol.get(), functionName, "", context);

        String replaceFunctionCall = getReplaceFunctionCall(context, functionName);

        TextEdit extractFunctionEdit = new TextEdit(functionInsertRange, function);
        TextEdit replaceEdit = new TextEdit(PositionUtil.toRange(nodeAtRange.lineRange()),
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
        List<Symbol> varNamesWithinTheRange = getVarSymbolsWithinRangeForExpressions(context);
        List<String> args = context.nodeAtRange().kind() != SyntaxKind.LET_EXPRESSION ? varNamesWithinTheRange.stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList()) : Collections.emptyList();
        StringBuilder fnBuilder = new StringBuilder();
        fnBuilder.append(functionName)
                .append(CommonKeys.OPEN_PARENTHESES_KEY)
                .append(String.join(", ", args))
                .append(CommonKeys.CLOSE_PARENTHESES_KEY);
        return fnBuilder.toString();
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

    private String getFunction(Node nodeAtRange, boolean newLineAtEnd, TypeSymbol typeSymbol,
                               String functionName, String funcBody, CodeActionContext context) {
        List<String> args = new ArrayList<>();
        List<Symbol> varAndParamSymbolsWithinRange = getVarSymbolsWithinRangeForExpressions(context);

        if (nodeAtRange.kind() != SyntaxKind.LET_EXPRESSION) {
            varAndParamSymbolsWithinRange.forEach(symbol -> {
                if (symbol.kind() == SymbolKind.VARIABLE) {
                    VariableSymbol variableSymbol = (VariableSymbol) symbol;
                    TypeSymbol rawType = CommonUtil.getRawType(variableSymbol.typeDescriptor());
                    args.add(rawType.signature() + " " + symbol.getName().get());
                } else if (symbol.kind() == SymbolKind.PARAMETER) {
                    Optional<String> possibleType = CodeActionUtil
                            .getPossibleType(((ParameterSymbol) symbol).typeDescriptor(), new ArrayList<>(), context);
                    args.add(possibleType.get() + " " + symbol.getName().get());
                }
            });
        }

        String returnsClause = String.format("returns %s", typeSymbol.signature());
        String returnStatement;

        if (nodeAtRange.kind() == SyntaxKind.BRACED_EXPRESSION) {
            returnStatement = String.format("return %s;",
                    ((BracedExpressionNode) nodeAtRange).expression().toString().strip());
        } else {
            returnStatement = String.format("return %s;", nodeAtRange.toString().strip());
        }
        return generateFunction(functionName, args, returnsClause, returnStatement, newLineAtEnd, false, funcBody);
    }

    private List<Symbol> getVarSymbolsWithinRangeForExpressions(CodeActionContext context) {
        return getVisibleSymbols(context, PositionUtil.toPosition(context.nodeAtRange().lineRange().endLine())).stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE || symbol.kind() == SymbolKind.PARAMETER)
                .filter(symbol -> context.currentSemanticModel().get().references(symbol).stream()
                        .anyMatch(location -> PositionUtil.isRangeWithinRange(PositionUtil
                                        .getRangeFromLineRange(location.lineRange()),
                                PositionUtil.toRange(context.nodeAtRange().lineRange()))))
                .collect(Collectors.toList());
    }

    private Optional<Node> findEnclosingModulePartNode(Node node) {
        Node reference = node;
        while (reference != null && reference.parent() != null) {
            if (reference.parent().kind() == SyntaxKind.MODULE_PART) {
                return Optional.of(reference);
            }
            reference = reference.parent();
        }
        return Optional.empty();
    }

    private Optional<NonTerminalNode> findEnclosingFunctionDefinitionNode(NonTerminalNode node) {
        NonTerminalNode reference = node;
        while (reference != null && reference.parent() != null) {
            SyntaxKind parentKind = reference.parent().kind();
            if (parentKind == SyntaxKind.FUNCTION_DEFINITION || parentKind == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION
                    || parentKind == SyntaxKind.OBJECT_METHOD_DEFINITION) {
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

    public static List<SyntaxKind> getSupportedExpressionSyntaxKindsList() {
        return List.of(
                SyntaxKind.BINARY_EXPRESSION,
                SyntaxKind.BRACED_EXPRESSION,
//                SyntaxKind.FUNCTION_CALL, // do not provide
                SyntaxKind.QUALIFIED_NAME_REFERENCE,
                SyntaxKind.INDEXED_EXPRESSION,
                SyntaxKind.FIELD_ACCESS,
                SyntaxKind.METHOD_CALL,
//                SyntaxKind.CHECK_EXPRESSION, // cannot provide
                SyntaxKind.MAPPING_CONSTRUCTOR, // what is the return type descriptor if providing
                SyntaxKind.TYPEOF_EXPRESSION,
                SyntaxKind.UNARY_EXPRESSION,
                SyntaxKind.TYPE_TEST_EXPRESSION,
                SyntaxKind.SIMPLE_NAME_REFERENCE,
//                SyntaxKind.TRAP_EXPRESSION, // cannot provide
                SyntaxKind.LIST_CONSTRUCTOR,
                SyntaxKind.TYPE_CAST_EXPRESSION,
                SyntaxKind.TABLE_CONSTRUCTOR,
                SyntaxKind.LET_EXPRESSION,
//                SyntaxKind.XML_TEMPLATE_EXPRESSION,
//                SyntaxKind.RAW_TEMPLATE_EXPRESSION,
//                SyntaxKind.STRING_TEMPLATE_EXPRESSION,
                SyntaxKind.IMPLICIT_NEW_EXPRESSION,
                SyntaxKind.EXPLICIT_NEW_EXPRESSION,
//                SyntaxKind.PARENTHESIZED_ARG_LIST, // cannot provide
//                SyntaxKind.EXPLICIT_ANONYMOUS_FUNCTION_EXPRESSION,
//                SyntaxKind.IMPLICIT_ANONYMOUS_FUNCTION_EXPRESSION,
//                SyntaxKind.QUERY_EXPRESSION,
//                SyntaxKind.ANNOT_ACCESS,
//                SyntaxKind.OPTIONAL_FIELD_ACCESS,
//                SyntaxKind.CONDITIONAL_EXPRESSION,
//                SyntaxKind.TRANSACTIONAL_EXPRESSION,
                SyntaxKind.OBJECT_CONSTRUCTOR,
//                SyntaxKind.XML_FILTER_EXPRESSION, // do not provide
//                SyntaxKind.XML_STEP_EXPRESSION, // do not provide
//                SyntaxKind.XML_NAME_PATTERN_CHAIN, // do not provide
//                SyntaxKind.XML_ATOMIC_NAME_PATTERN, // do not provide
                SyntaxKind.STRING_LITERAL,
                SyntaxKind.NUMERIC_LITERAL,
                SyntaxKind.BOOLEAN_LITERAL,
//                SyntaxKind.NIL_LITERAL, // do not provide
//                SyntaxKind.NULL_LITERAL, // do not provide
//                SyntaxKind.BYTE_ARRAY_LITERAL,
//                SyntaxKind.ASTERISK_LITERAL,
//                SyntaxKind.REQUIRED_EXPRESSION,
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
}
