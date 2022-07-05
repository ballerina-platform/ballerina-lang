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

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Code Action for incompatible return types.
 *
 * @since 1.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class FixReturnTypeCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Fix Return Type";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068", "BCE3032");

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return false;
        }

        //Suggest the code action only if the immediate parent of the matched node is a return statement 
        // and the return statement corresponds to the enclosing function's signature. 
        NonTerminalNode parentNode = positionDetails.matchedNode().parent();
        if (parentNode != null && parentNode.kind() != SyntaxKind.RETURN_STATEMENT && 
                positionDetails.matchedNode().kind() != SyntaxKind.CHECK_EXPRESSION) {
            return false;
        }

        return CodeActionNodeValidator.validate(context.nodeAtCursor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
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

        Optional<FunctionDefinitionNode> funcDef = CodeActionUtil.getEnclosedFunction(positionDetails.matchedNode());
        if (funcDef.isEmpty() || RuntimeConstants.MAIN_FUNCTION_NAME.equals(funcDef.get().functionName().text())) {
            return Collections.emptyList();
        }

        List<TextEdit> importEdits = new ArrayList<>();
        List<String> types = new ArrayList<>();
        boolean returnTypeDescPresent = funcDef.get().functionSignature().returnTypeDesc().isPresent();

        if (checkExprDiagnostic) {
            // Add error return type for check expression
            if (returnTypeDescPresent) {
                types.add(funcDef.get().functionSignature().returnTypeDesc().get().type().toString().trim().concat("|")
                        .concat("error"));
            } else {
                types.add("error?");
            }
        } else {
            // Get all possible return types including ambiguous scenarios
            types = CodeActionUtil.getPossibleTypes(foundType.get(), importEdits, context);
        }

        // Where to insert the edit: Depends on if a return statement already available or not
        Position start;
        Position end;
        if (returnTypeDescPresent) {
            // eg. function test() returns () {...}
            ReturnTypeDescriptorNode returnTypeDesc = funcDef.get().functionSignature().returnTypeDesc().get();
            LinePosition retStart = returnTypeDesc.type().lineRange().startLine();
            LinePosition retEnd = returnTypeDesc.type().lineRange().endLine();
            start = new Position(retStart.line(), retStart.offset());
            end = new Position(retEnd.line(), retEnd.offset());
        } else {
            // eg. function test() {...}
            Position funcBodyStart = PositionUtil.toPosition(funcDef.get().functionSignature().lineRange().endLine());
            start = funcBodyStart;
            end = funcBodyStart;
        }

        List<CodeAction> codeActions = new ArrayList<>();
        types.forEach(type -> {
            List<TextEdit> edits = new ArrayList<>();

            String editText;
            // Process function node
            if (funcDef.get().functionSignature().returnTypeDesc().isEmpty()) {
                editText = " returns " + type;
            } else {
                editText = type;
            }
            edits.add(new TextEdit(new Range(start, end), editText));
            edits.addAll(importEdits);

            // Add code action
            String commandTitle = String.format(CommandConstants.CHANGE_RETURN_TYPE_TITLE, type);
            codeActions.add(createCodeAction(commandTitle, edits, context.fileUri(), CodeActionKind.QuickFix));
        });

        return codeActions;
    }

    @Override
    public String getName() {
        return NAME;
    }
    
}
