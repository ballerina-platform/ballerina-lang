/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.ReturnStatementFinder;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code Action for incompatible return types.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class FixReturnTypeCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Fix Return Type";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068", "BCE3032");

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return false;
        }

        // Suggest code action if the node is aligned with a check keyword.
        NonTerminalNode matchedNode = positionDetails.matchedNode();
        if (matchedNode.kind() == SyntaxKind.CHECK_ACTION || matchedNode.kind() == SyntaxKind.CHECK_EXPRESSION) {
            return CodeActionNodeValidator.validate(context.nodeAtRange());
        }

        // Suggest code action if the node is an expression inside a return statement.
        NonTerminalNode parentNode = matchedNode.parent();
        if (parentNode == null) {
            return false;
        }
        if (parentNode.kind() == SyntaxKind.RETURN_KEYWORD) {
            return CodeActionNodeValidator.validate(context.nodeAtRange());
        }

        // Suggest code action if the node is an expression nested into a return statement.
        NonTerminalNode grandParentNode = parentNode.parent();
        if (grandParentNode == null) {
            return false;
        }
        if (grandParentNode.kind() == SyntaxKind.RETURN_STATEMENT) {
            return CodeActionNodeValidator.validate(context.nodeAtRange());
        }

        // Suggest code action if the node is an expression inside a collect clause.
        if (matchedNode.kind() == SyntaxKind.COLLECT_CLAUSE) {
            NonTerminalNode ancestorNode = grandParentNode.parent();
            if (ancestorNode == null || ancestorNode.kind() != SyntaxKind.RETURN_STATEMENT) {
                return false;
            }
        }

        return CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {

        Optional<TypeSymbol> foundType = Optional.empty();
        if ("BCE2068".equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(CodeActionUtil
                    .getDiagPropertyFilterFunction(DiagBasedPositionDetails
                            .DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX));
        } else if ("BCE2066".equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        }
        boolean checkExprDiagnostic = "BCE3032".equals(diagnostic.diagnosticInfo().code());
        if (foundType.isEmpty() && !checkExprDiagnostic) {
            return Collections.emptyList();
        }
        if (context.currentSemanticModel().isEmpty() || context.currentDocument().isEmpty()) {
            return Collections.emptyList();
        }

        SemanticModel semanticModel = context.currentSemanticModel().get();
        FunctionSignatureNode functionSignatureNode;
        Optional<FunctionDefinitionNode> funcDef = Optional.empty();
        Optional<ExplicitAnonymousFunctionExpressionNode> anonFunc = CodeActionUtil
                .getEnclosingAnonFuncExpr(positionDetails.matchedNode());
        boolean isMainFunction = false;
        Optional<TypeSymbol> expectedReturnType = Optional.empty();
        if (anonFunc.isEmpty()) {
            funcDef = CodeActionUtil.getEnclosedFunction(positionDetails.matchedNode());
            if (funcDef.isEmpty()) {
                return Collections.emptyList();
            }
            // Not going to provide code action to change return type of the main() function
            isMainFunction = RuntimeConstants.MAIN_FUNCTION_NAME.equals(funcDef.get().functionName().text());
            functionSignatureNode = funcDef.get().functionSignature();
        } else {
            functionSignatureNode = anonFunc.get().functionSignature();
            Optional<TypeSymbol> annoExpectedTypeSymbol = semanticModel.expectedType(context.currentDocument().get(),
                    anonFunc.get().lineRange().startLine());
            if (annoExpectedTypeSymbol.isEmpty() || annoExpectedTypeSymbol.get().typeKind() != TypeDescKind.FUNCTION) {
                return Collections.emptyList();
            }
            expectedReturnType = ((FunctionTypeSymbol) annoExpectedTypeSymbol.get()).returnTypeDescriptor();
        }

        StartEndPositionDetails startEndPositionDetails = extractStartAndEndPosOfReturnNode(functionSignatureNode);
        if (checkExprDiagnostic) {
            if (expectedReturnType.isPresent() && !semanticModel.types().ERROR.subtypeOf(expectedReturnType.get())) {
                return Collections.emptyList();
            }
            // Add error return type for check expression
            String returnTypeDesc = functionSignatureNode.returnTypeDesc().isEmpty() ? "error?" :
                    functionSignatureNode.returnTypeDesc().get().type().toString().trim()
                            .concat("|").concat("error");
            return List.of(getReturnTypeChangeCodeAction(context, Set.of(returnTypeDesc), startEndPositionDetails,
                    functionSignatureNode, new ArrayList<>()));

        }
        if (isMainFunction) {
            return Collections.emptyList();
        }

        List<TextEdit> importEdits = new ArrayList<>();
        List<Set<String>> types = new ArrayList<>();
        List<CodeAction> codeActions = new ArrayList<>();
        List<List<String>> combinedTypes = new ArrayList<>();
        ReturnStatementFinder returnStatementFinder = new ReturnStatementFinder();
        if (funcDef.isPresent()) {
            returnStatementFinder.visit(funcDef.get());
        } else {
            returnStatementFinder.visit(anonFunc.get());
        }

        List<ReturnStatementNode> nodeList = returnStatementFinder.getNodeList();

        for (ReturnStatementNode returnStatementNode : nodeList) {
            if (returnStatementNode.expression().isEmpty() || context.currentSemanticModel().isEmpty()) {
                return Collections.emptyList();
            }
            ExpressionNode expression = returnStatementNode.expression().get();
            Optional<TypeSymbol> typeSymbol = semanticModel.typeOf(expression);
            if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.COMPILATION_ERROR) {
                return Collections.emptyList();
            }
            if (expectedReturnType.isPresent() && !typeSymbol.get().subtypeOf(expectedReturnType.get())) {
               continue;
            }

            if (typeSymbol.get().typeKind() == TypeDescKind.FUNCTION) {
                combinedTypes.add(Collections.singletonList("(" + CodeActionUtil.getPossibleTypes(typeSymbol.get(),
                        importEdits, context).get(0) + ")"));
            } else {
                combinedTypes.add(CodeActionUtil.getPossibleTypes(typeSymbol.get(), importEdits, context));
            }
        }

        CheckExprNodeFinder checkExprNodeFinder = new CheckExprNodeFinder();
        if (funcDef.isPresent()) {
            funcDef.get().accept(checkExprNodeFinder);
        } else {
            anonFunc.get().accept(checkExprNodeFinder);
        }
        if (checkExprNodeFinder.containCheckExprNode()) {
            if (expectedReturnType.isEmpty() || semanticModel.types().ERROR.subtypeOf(expectedReturnType.get())) {
                combinedTypes.add(Collections.singletonList("error"));
            }
        }

        types = getPossibleCombinations(combinedTypes, types);

        types.forEach(type -> codeActions.add(getReturnTypeChangeCodeAction(context, type, startEndPositionDetails,
                functionSignatureNode, importEdits)));

        return codeActions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static CodeAction getReturnTypeChangeCodeAction(CodeActionContext context, Set<String> typeSet,
                                                            StartEndPositionDetails positionDetails,
                                                            FunctionSignatureNode functionSignatureNode,
                                                            List<TextEdit> importEdits) {
        List<TextEdit> edits = new ArrayList<>();

        String editText;
        // Process function node
        String newType = String.join("|", typeSet);
        if (functionSignatureNode.returnTypeDesc().isEmpty()) {
            editText = " returns " + newType;
        } else {
            editText = newType;
        }
        edits.add(new TextEdit(new Range(positionDetails.start, positionDetails.end), editText));
        edits.addAll(importEdits);

        // Add code action
        String commandTitle = String.format(CommandConstants.CHANGE_RETURN_TYPE_TITLE, newType);
        return CodeActionUtil.createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.QuickFix);
    }

    private static StartEndPositionDetails extractStartAndEndPosOfReturnNode(FunctionSignatureNode signatureNode) {
        if (signatureNode.returnTypeDesc().isPresent()) {
            // eg. function test() returns () {...}
            ReturnTypeDescriptorNode returnTypeDesc = signatureNode.returnTypeDesc().get();
            LinePosition retStart = returnTypeDesc.type().lineRange().startLine();
            LinePosition retEnd = returnTypeDesc.type().lineRange().endLine();
            return new StartEndPositionDetails(new Position(retStart.line(), retStart.offset()),
                    new Position(retEnd.line(), retEnd.offset()));
        }
        // eg. function test() {...}
        Position funcBodyStart = PositionUtil.toPosition(signatureNode.lineRange().endLine());
        return new StartEndPositionDetails(funcBodyStart, funcBodyStart);
    }

    private static List<Set<String>> getPossibleCombinations(List<List<String>> combinedTypes,
                                                            List<Set<String>> typeList) {
        for (List<String> possibleTypes : combinedTypes) {
            // Add the items of the first combinedTypes to the typeList
            if (typeList.isEmpty()) {
                for (String type : possibleTypes) {
                    typeList.add(Set.of(type));
                }
                continue;
            }
            List<Set<String>> updatedTypes = new ArrayList<>();
            // Add each item in the next combinedTypes to the items listed in the typeList
            for (String type : possibleTypes) {
                for (Set<String> strings : typeList) {
                    Set<String> combination = new HashSet<>(strings);
                    combination.add(type);
                    updatedTypes.add(combination);
                }
            }
            if (updatedTypes.isEmpty()) {
                continue;
            }
            typeList = updatedTypes;
        }
        return typeList;
    }

    private record StartEndPositionDetails(Position start, Position end) {
    }

    /**
     * A visitor to find check expression exist inside a Node.
     */
    static class CheckExprNodeFinder extends NodeVisitor {

        private CheckExpressionNode checkExpressionNode = null;

        @Override
        public void visit(FunctionDefinitionNode functionDefinitionNode) {
            functionDefinitionNode.functionBody().accept(this);
        }

        @Override
        public void visit(CheckExpressionNode checkExpressionNode) {
            this.checkExpressionNode = checkExpressionNode;
        }

        boolean containCheckExprNode() {
            return this.checkExpressionNode != null;
        }
    }
}
