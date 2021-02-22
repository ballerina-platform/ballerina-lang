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
package org.ballerinalang.langserver.codeaction.providers.createvar;

import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;

/**
 * Code Action for type guard variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ErrorHandleInsideCodeAction extends CreateVariableCodeAction {
    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 997;
    }

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
            return Collections.emptyList();
        }

        Symbol matchedSymbol = positionDetails.matchedSymbol();
        TypeSymbol typeDescriptor = positionDetails.matchedExprType();
        String uri = context.fileUri();
        if (typeDescriptor == null || typeDescriptor.typeKind() != TypeDescKind.UNION) {
            return Collections.emptyList();
        }
        UnionTypeSymbol unionType = (UnionTypeSymbol) typeDescriptor;
        boolean isRemoteInvocation = matchedSymbol instanceof Qualifiable &&
                ((Qualifiable) matchedSymbol).qualifiers().contains(Qualifier.REMOTE);
        if (isRemoteInvocation) {
            return Collections.emptyList();
        }

        Range range = CommonUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, context);

        // Add type guard code action
        String commandTitle = CommandConstants.CREATE_VAR_TYPE_GUARD_TITLE;
        List<TextEdit> edits = CodeActionUtil.getTypeGuardCodeActionEdits(createVarTextEdits.name, range, unionType,
                                                                          context);
        if (edits.isEmpty()) {
            return Collections.emptyList();
        }

        edits.add(createVarTextEdits.edits.get(0));
        edits.addAll(createVarTextEdits.imports);
        return Collections.singletonList(AbstractCodeActionProvider.createQuickFixCodeAction(commandTitle, edits, uri));
    }
}
