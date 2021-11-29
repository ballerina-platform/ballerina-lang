/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.INVALID_CALL_WITH_MUTABLE_ARGS_IN_MATCH_GUARD;

/**
 * Code action to convert an expression e to e.cloneReadOnly().
 * 
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ConvertToReadonlyCloneCodeAction extends AbstractCodeActionProvider {
    private static final String NAME = "Convert to Readonly Clone";
    private static final String CLONE_READONLY_PREFIX = "cloneReadOnly";

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!diagnostic.diagnosticInfo().code().equals(INVALID_CALL_WITH_MUTABLE_ARGS_IN_MATCH_GUARD.diagnosticId())) {
            return Collections.emptyList();
        }

        NonTerminalNode currentNode = positionDetails.matchedNode();
        Optional<TypeSymbol> typeDescriptor = context.currentSemanticModel().get().typeOf(currentNode);
        if (typeDescriptor.isEmpty() || !isCloneReadonlyAvailable(typeDescriptor.get())) {
            return Collections.emptyList();
        }
        Range range = CommonUtil.toRange(diagnostic.location().lineRange());
        String newText = getNewText(currentNode);
        String uri = context.filePath().toUri().toString();

        TextEdit textEdit = new TextEdit();
        textEdit.setRange(range);
        textEdit.setNewText(newText);
        List<TextEdit> textEdits = Collections.singletonList(textEdit);

        return Collections.singletonList(createQuickFixCodeAction(CommandConstants.CONVERT_TO_READONLY_CLONE,
                textEdits, uri));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    private boolean isCloneReadonlyAvailable(TypeSymbol typeSymbol) {
        return typeSymbol.langLibMethods().stream()
                .anyMatch(fSymbol -> fSymbol.getName().orElse("")
                        .equals(CLONE_READONLY_PREFIX));
    }

    private String getNewText(NonTerminalNode currentNode) {
        String prefix;
        switch (currentNode.kind()) {
            case LET_EXPRESSION:
            case CONDITIONAL_EXPRESSION:
            case CHECK_EXPRESSION:
            case XML_STEP_EXPRESSION:
                prefix = "(" + currentNode.toSourceCode().trim() + ")";
                break;
            default:
                prefix = currentNode.toSourceCode().trim();
                break;
        }

        return prefix + "." + CLONE_READONLY_PREFIX + "()";
    }
}
