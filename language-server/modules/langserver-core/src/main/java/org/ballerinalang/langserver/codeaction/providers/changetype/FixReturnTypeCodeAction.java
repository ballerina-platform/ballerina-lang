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
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
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
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE2066", "BCE2068");

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code())) {
            return Collections.emptyList();
        }

        //Suggest the code action only if the immediate parent of the matched node is a return statement 
        // and the return statement corresponds to the enclosing function's signature. 
        NonTerminalNode parentNode = positionDetails.matchedNode().parent();
        if (parentNode != null && parentNode.kind() != SyntaxKind.RETURN_STATEMENT) {
            return Collections.emptyList();
        }

        Optional<TypeSymbol> foundType;
        if ("BCE2068".equals(diagnostic.diagnosticInfo().code())) {
            foundType = positionDetails.diagnosticProperty(CodeActionUtil
                    .getDiagPropertyFilterFunction(DiagBasedPositionDetails
                            .DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX));
        } else {
            foundType = positionDetails.diagnosticProperty(
                    DiagBasedPositionDetails.DIAG_PROP_INCOMPATIBLE_TYPES_FOUND_SYMBOL_INDEX);
        }
        if (foundType.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<FunctionDefinitionNode> funcDef = CodeActionUtil.getEnclosedFunction(positionDetails.matchedNode());
        if (funcDef.isEmpty() || RuntimeConstants.MAIN_FUNCTION_NAME.equals(funcDef.get().functionName().text())) {
            return Collections.emptyList();
        }

        // Where to insert the edit: Depends on if a return statement already available or not
        Position start;
        Position end;
        if (funcDef.get().functionSignature().returnTypeDesc().isEmpty()) {
            // eg. function test() {...}
            Position funcBodyStart = CommonUtil.toPosition(funcDef.get().functionSignature().lineRange().endLine());
            start = funcBodyStart;
            end = funcBodyStart;
        } else {
            // eg. function test() returns () {...}
            ReturnTypeDescriptorNode returnTypeDesc = funcDef.get().functionSignature().returnTypeDesc().get();
            LinePosition retStart = returnTypeDesc.type().lineRange().startLine();
            LinePosition retEnd = returnTypeDesc.type().lineRange().endLine();
            start = new Position(retStart.line(), retStart.offset());
            end = new Position(retEnd.line(), retEnd.offset());
        }

        List<CodeAction> codeActions = new ArrayList<>();
        List<TextEdit> importEdits = new ArrayList<>();
        // Get all possible return types including ambiguous scenarios
        List<String> types = CodeActionUtil.getPossibleTypes(foundType.get(), importEdits, context);

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
            codeActions.add(createQuickFixCodeAction(commandTitle, edits, context.fileUri()));
        });

        return codeActions;
    }

    @Override
    public String getName() {
        return NAME;
    }
    
}
