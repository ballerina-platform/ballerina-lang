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
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
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
import java.util.Collections;
import java.util.HashSet;
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
//        syntaxKinds.add(SyntaxKind.LOCAL_VAR_DECL);
//        syntaxKinds.add(SyntaxKind.ASSIGNMENT_STATEMENT);
//        syntaxKinds.add(SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT);
        return syntaxKinds;
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        if (posDetails.matchedCodeActionNode().kind() == SyntaxKind.LIST) {
            return getCodeActionsForStatements(context, posDetails);
        }
        return getCodeActionsForExpressions(context, posDetails);
    }

    private List<CodeAction> getCodeActionsForStatements(CodeActionContext context,
                                                         RangeBasedPositionDetails posDetails) {
        NonTerminalNode matchedCodeActionNode = posDetails.matchedCodeActionNode();
        Optional<NonTerminalNode> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode); // todo this should support nested if else blocks as well
        if (context.currentSemanticModel().isEmpty() || enclosingNode.isEmpty() || enclosingNode.get().kind() != SyntaxKind.FUNCTION_DEFINITION) {
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

        List<Symbol> varSymbolDeclarationsBeforeEndOfRange = bindingSymbolsBeforeEndOfRange.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.VARIABLE)
                .collect(Collectors.toList());

        List<Symbol> localVarSymbolsDeclarationsBeforeEndOfRange = new ArrayList<>();

        varSymbolDeclarationsBeforeEndOfRange.forEach(
                symbol -> {
                    //todo should we check the range with module level or any other method to eliminate module level vars
                    if (PositionUtil.isWithinLineRange(symbol.getLocation().get().lineRange(), enclosingNode.get().lineRange())) {
                        localVarSymbolsDeclarationsBeforeEndOfRange.add(symbol);
                    }
                }
        );

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

        // here "InRange" refers to context.range()
        // these are somewhat similar to localVarDeclarationsInRange
        List<Symbol> varSymbolDeclarationsInRange = bindingSymbolsBeforeEndOfRange.stream() // constants and parameters are not initialized inside highlighted range hence name var
                .filter(symbol -> symbol.getLocation().isPresent())
                .filter(symbol -> PositionUtil.isWithinRange(PositionUtil.toPosition(symbol.getLocation().get().lineRange().endLine()), context.range()))
                .collect(Collectors.toList());

        List<Symbol> varSymbolsDeclaredInRangeAndReferredAfter = new ArrayList<>();
        varSymbolDeclarationsInRange.forEach(symbol -> {
            semanticModel.references(symbol).forEach(location -> {
                if (PositionUtil.isRangeWithinRange(PositionUtil.toRange(location.lineRange()), rangeAfterHighlightedRange)) {
                    varSymbolsDeclaredInRangeAndReferredAfter.add(symbol);
                }
            });
        });

        List<Node> assignmentNodeVarRefsInRange = new ArrayList<>();
        List<Node> comAssignmentNodeLhsExprsInRange = new ArrayList<>();
        List<Node> localVarDeclarationsInRange = new ArrayList<>();
        List<Node> selectedNodes = new ArrayList<>();

        matchedCodeActionNode.children().forEach(node -> {
            if (PositionUtil.isWithinRange(PositionUtil.toPosition(node.lineRange().endLine()), context.range())) {
                selectedNodes.add(node);
                if (node.kind() == SyntaxKind.ASSIGNMENT_STATEMENT
                        && !assignmentNodeVarRefsInRange.contains(((AssignmentStatementNode) node).varRef())) {
                    assignmentNodeVarRefsInRange.add(((AssignmentStatementNode) node).varRef());
                } else if (node.kind() == SyntaxKind.COMPOUND_ASSIGNMENT_STATEMENT
                        && !comAssignmentNodeLhsExprsInRange.contains(((CompoundAssignmentStatementNode) node).lhsExpression())) {
                    comAssignmentNodeLhsExprsInRange.add(((CompoundAssignmentStatementNode) node).lhsExpression());
                } else if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) { //todo add initizialzer.ispresent()
                    localVarDeclarationsInRange.add(((VariableDeclarationNode) node).typedBindingPattern().bindingPattern());
                }
            }
        });

        List<Node> assignmentOrLocalVarNodesInRange = Stream.concat(assignmentNodeVarRefsInRange.stream(), localVarDeclarationsInRange.stream()).collect(Collectors.toList());
        // these are declared in range, and these are queried from nodes not symbols. and then converted to symbols
        Set<Symbol> localVarsDeclaredOrAssignedInRange = assignmentOrLocalVarNodesInRange.stream().map(node -> semanticModel.symbol(node).get()).filter(symbol -> PositionUtil.isRangeWithinRange(PositionUtil.toRange(symbol.getLocation().get().lineRange()), context.range())).collect(Collectors.toSet());
        // these are assigned in range but declared before range (checked for location not within range)
        List<Symbol> assignmentsInRangeAndDeclaredBeforeRange = assignmentNodeVarRefsInRange.stream().map(node -> semanticModel.symbol(node).get()).filter(symbol -> !PositionUtil.isRangeWithinRange(PositionUtil.toRange(symbol.getLocation().get().lineRange()), context.range())).collect(Collectors.toList());

        Set<String> uniqueAssNodeVarRefsInRange = new HashSet<>();
        assignmentNodeVarRefsInRange.forEach(node -> uniqueAssNodeVarRefsInRange.add(node.toString().strip()));

        Set<String> uniqueAssOrLocalVarNodesInRange = new HashSet<>();
        assignmentOrLocalVarNodesInRange.forEach(node -> uniqueAssOrLocalVarNodesInRange.add(node.toString().strip()));

        List<Node> varNodesReferredAfterRangeAndUpdatedInRange = new ArrayList<>(); // SimpleNameReferenceNode with name without striping
        List<Symbol> varSymbolsReferredAfterRangeAndUpdatedInRange = new ArrayList<>();

        List<VariableSymbol> localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange = localVarsDeclaredOrAssignedInRange.stream()
                .filter(symbol -> !semanticModel.references(symbol).stream()
                        .filter(location -> PositionUtil.isRangeWithinRange(PositionUtil.toRange(location.lineRange()), rangeAfterHighlightedRange)).collect(Collectors.toList()).isEmpty())
                .map(symbol -> (VariableSymbol) symbol)
                .collect(Collectors.toList());

        //todo add compound statement nodes as well
        assignmentNodeVarRefsInRange.forEach(
                node -> varSymbolDeclarationsBeforeEndOfRange.forEach(
                        symbol -> semanticModel.references(symbol).forEach(
                                location -> {
                                    Range locationRange = PositionUtil.toRange(location.lineRange());
                                    if (PositionUtil.isRangeWithinRange(locationRange, rangeAfterHighlightedRange)
                                            && symbol.getName().isPresent()
                                            && node.toString().strip().equals(symbol.getName().get())) {
                                        varNodesReferredAfterRangeAndUpdatedInRange.add(node);
                                        varSymbolsReferredAfterRangeAndUpdatedInRange.add(symbol);
                                    }
                                })));

        Set<String> uniqueVarNodesReferredAfterRangeAndUpdatedInRange = new HashSet<>();
        varNodesReferredAfterRangeAndUpdatedInRange.forEach(node -> uniqueVarNodesReferredAfterRangeAndUpdatedInRange.add(node.toString().strip()));

        boolean isRangeExtractable =
                // this is to check local vars declared before the range updated in range
                // todo if we need to allow these, we need to find the declaration and put it inside the extract()
                // todo Q do we need to change the below check? no! example
//                function testFunction(int aa) {
//                    int age = 1;
//                    int population = 1000;
//
//                    int v = 9;
//                    v = v + 1;
//                    age = population + aa;
//                    age = age + 1 + v;
//
//                    doNothing(age);
//                }
                assignmentsInRangeAndDeclaredBeforeRange.size() == 0
                // this check is to check references of updates after range. //todo improve to return more than one variable. Q how to return when returning different types? Record?
//                && uniqueVarNodesReferredAfterRangeAndUpdatedInRange.size() <= 1
//                        // declared (above is updated)
//                && varSymbolsDeclaredInRangeAndReferredAfter.size() <= 1;


                        // here we consider both local var or assigned together inside range and referred after range
        && localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.size() <= 1;

        if (!isRangeExtractable) {
            return Collections.emptyList();
        }

        String returnTypeDescriptor = "";
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.size() == 1) {
            TypeDescKind returnTypeDescKind = localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.get(0).typeDescriptor().typeKind();
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
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.size() == 1) {
            returnStatement = String.format("return %s;", localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.get(0).getName().get());
        }
        // todo change name
        List<String> selectedNodeStrings = selectedNodes.stream().map(Node::toString).collect(Collectors.toList());
        String funcBody = String.join("", selectedNodeStrings);

        // todo start get start to end to a function
        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        LineRange rootLineRange = syntaxTree.rootNode().lineRange();
        int endLine = rootLineRange.endLine().line() + 1;
        int endCol = 0;
        // If the file ends with a new line
        boolean newLineAtEnd = rootLineRange.endLine().offset() == 0;

        // todo this enclosing node should be used when original enclosing node used at start differ, example if else block
        // if not you can disregard this
//        Optional<NonTerminalNode> enclosingNode = findEnclosingModulePartNode(matchedCodeActionNode);
        Range extractFunctionInsertRange;

        if (enclosingNode.isPresent()) {
            newLineAtEnd = addNewLineAtEnd(enclosingNode.get());
            extractFunctionInsertRange = PositionUtil.toRange(enclosingNode.get().lineRange().endLine());
        } else {
            extractFunctionInsertRange = new Range(new Position(endLine, endCol), new Position(endLine, endCol));
        }
        // todo end

        // todo improve newLine at end, here i have set it to true
        String extractFunction = generateFunction(functionName, argsForExtractFunction, returnTypeDescriptor, returnStatement, newLineAtEnd, false, funcBody);
        String replaceFunctionCall = getReplaceFunctionCall(argsForReplaceFunctionCall, functionName, true);
        if (localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.size() == 1) {
            //todo try to use this once. this is already used in "return <>" and "returns <>"
            TypeDescKind returnTypeDescKind = localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.get(0).typeDescriptor().typeKind();
            String varName = localVarSymbolsDeclaredOrAssignedInRangeAndReferredAfterRange.get(0).getName().get();
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
